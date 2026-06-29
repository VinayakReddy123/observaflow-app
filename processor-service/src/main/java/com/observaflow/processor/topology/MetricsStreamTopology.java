package com.observaflow.processor.topology;

import java.time.Duration;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import com.observaflow.processor.model.ProcessedMetric;
import com.observaflow.processor.model.TDigestAggregate;
import com.observaflow.processor.model.TelemetryEvent;
import com.observaflow.processor.serde.JsonSerde;

@Configuration
@EnableKafkaStreams
public class MetricsStreamTopology {

    private static final String INPUT_TOPIC = "raw-telemetry";
    private static final String OUTPUT_TOPIC = "processed-metrics";

    @Bean
    public KStream<String, TelemetryEvent> buildTopology(StreamsBuilder builder) {

        JsonSerde<TelemetryEvent> telemetrySerde = new JsonSerde<>(TelemetryEvent.class);
        JsonSerde<TDigestAggregate> aggregateSerde = new JsonSerde<>(TDigestAggregate.class);
        JsonSerde<ProcessedMetric> metricSerde = new JsonSerde<>(ProcessedMetric.class);

        KStream<String, TelemetryEvent> rawStream = builder.stream(
                INPUT_TOPIC,
                Consumed.with(Serdes.String(), telemetrySerde)
        );

        rawStream
                // only process METRIC type events — ignore logs and traces
                .filter((key, event) -> event != null &&
                        event.getType() == TelemetryEvent.EventType.METRIC)

                // group by tenantId:serviceId — one aggregate per service per tenant
                .groupBy((key, event) -> event.getTenantId() + ":" + event.getServiceId(),
                        Grouped.with(Serdes.String(), telemetrySerde))

                // 1-minute tumbling window
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1)))

                // absorb each event into TDigestAggregate
                .aggregate(
                        TDigestAggregate::new,
                        (groupKey, event, aggregate) -> {
                            aggregate.setTenantId(event.getTenantId());
                            aggregate.setServiceId(event.getServiceId());
                            aggregate.add(extractValue(event));
                            return aggregate;
                        },
                        Materialized.with(Serdes.String(), aggregateSerde)
                )

                // convert KTable → KStream so we can write to topic
                .toStream()

                // build ProcessedMetric from window result
                .<String, ProcessedMetric>map((windowedKey, aggregate) -> {
                    ProcessedMetric metric = ProcessedMetric.builder()
                            .tenantId(aggregate.getTenantId())
                            .serviceId(aggregate.getServiceId())
                            .p50(aggregate.quantile(0.50))
                            .p95(aggregate.quantile(0.95))
                            .p99(aggregate.quantile(0.99))
                            .eventCount(aggregate.getCount())
                            .windowStart(windowedKey.window().start())
                            .windowEnd(windowedKey.window().end())
                            .build();
                    return KeyValue.pair(aggregate.getTenantId(), metric);
                })

                // write to processed-metrics topic
                .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), metricSerde));

        return rawStream;
    }

    // extract the numeric value from the event payload
    private double extractValue(TelemetryEvent event) {
        if (event.getPayload() == null) return 0.0;
        Object value = event.getPayload().get("value");
        if (value instanceof Number) return ((Number) value).doubleValue();
        return 0.0;
    }
}
