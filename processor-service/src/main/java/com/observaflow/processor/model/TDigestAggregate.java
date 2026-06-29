package com.observaflow.processor.model;

import java.util.ArrayList;
import java.util.List;

import com.tdunning.math.stats.TDigest;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TDigestAggregate {

    private List<Double> values = new ArrayList<>();
    private long count = 0;
    private String tenantId;
    private String serviceId;

    public void add(double value) {
        values.add(value);
        count++;
    }

    public double quantile(double q) {
        if (values.isEmpty())
            return 0.0;
        TDigest digest = TDigest.createDigest(100);
        for (double value : values) {
            digest.add(value);
        }
        return digest.quantile(q);
    }
}
