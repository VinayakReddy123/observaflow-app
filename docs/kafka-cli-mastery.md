# Kafka CLI Mastery Guide — ObservaFlow

Everything you need to operate Kafka from the command line. All commands run
inside the Kafka container: `docker exec -it infra-kafka-1-1 bash`

---

## 1. TOPICS — Create, Inspect, Modify, Delete

### Create a topic
```bash
kafka-topics --create \
  --topic <topic-name> \
  --partitions 3 \
  --replication-factor 3 \
  --bootstrap-server localhost:9092
```

ObservaFlow topics:
```bash
kafka-topics --create --topic raw-telemetry --partitions 3 --replication-factor 3 --bootstrap-server localhost:9092
kafka-topics --create --topic processed-metrics --partitions 3 --replication-factor 3 --bootstrap-server localhost:9092
kafka-topics --create --topic alert-events --partitions 3 --replication-factor 3 --bootstrap-server localhost:9092
```

### List all topics
```bash
kafka-topics --list --bootstrap-server localhost:9092
```

### Describe a topic — see partitions, replicas, leaders
```bash
kafka-topics --describe --topic raw-telemetry --bootstrap-server localhost:9092
```

Output explained:
```
Topic: raw-telemetry  PartitionCount: 3  ReplicationFactor: 3
  Partition: 0  Leader: 1  Replicas: 1,2,3  Isr: 1,2,3
  Partition: 1  Leader: 2  Replicas: 2,3,1  Isr: 2,3,1
  Partition: 2  Leader: 3  Replicas: 3,1,2  Isr: 3,1,2
```
- **Leader** — the broker handling reads/writes for this partition
- **Replicas** — all brokers that have a copy
- **Isr (In-Sync Replicas)** — brokers fully caught up with the leader
  - If Isr < Replicas, a broker is lagging — this is a warning sign

### Increase partition count (can only increase, never decrease)
```bash
kafka-topics --alter \
  --topic raw-telemetry \
  --partitions 6 \
  --bootstrap-server localhost:9092
```

### Delete a topic
```bash
kafka-topics --delete --topic raw-telemetry --bootstrap-server localhost:9092
```

---

## 2. PRODUCER — Send Messages to a Topic

### Basic producer (interactive — type messages, press Enter to send)
```bash
kafka-console-producer \
  --topic raw-telemetry \
  --bootstrap-server localhost:9092
```
Type a message and press Enter. Ctrl+C to stop.

### Producer with key (key determines which partition the message goes to)
```bash
kafka-console-producer \
  --topic raw-telemetry \
  --bootstrap-server localhost:9092 \
  --property "key.separator=:" \
  --property "parse.key=true"
```
Then type: `service-1:{"cpu":85,"memory":70}`

### Send messages from a file
```bash
kafka-console-producer \
  --topic raw-telemetry \
  --bootstrap-server localhost:9092 < messages.txt
```

---

## 3. CONSUMER — Read Messages from a Topic

### Read new messages only (from now)
```bash
kafka-console-consumer \
  --topic raw-telemetry \
  --bootstrap-server localhost:9092
```

### Read ALL messages from the beginning
```bash
kafka-console-consumer \
  --topic raw-telemetry \
  --from-beginning \
  --bootstrap-server localhost:9092
```

### Read with keys displayed
```bash
kafka-console-consumer \
  --topic raw-telemetry \
  --from-beginning \
  --bootstrap-server localhost:9092 \
  --property print.key=true \
  --property key.separator=" | "
```

### Read only N messages then stop
```bash
kafka-console-consumer \
  --topic raw-telemetry \
  --from-beginning \
  --max-messages 10 \
  --bootstrap-server localhost:9092
```

### Read from a specific partition
```bash
kafka-console-consumer \
  --topic raw-telemetry \
  --partition 0 \
  --offset earliest \
  --bootstrap-server localhost:9092
```

### Consumer with a named group (important — tracks offset per group)
```bash
kafka-console-consumer \
  --topic raw-telemetry \
  --bootstrap-server localhost:9092 \
  --group my-test-group
```

---

## 4. CONSUMER GROUPS — Track Lag, Reset Offsets

### List all consumer groups
```bash
kafka-consumer-groups --list --bootstrap-server localhost:9092
```

### Describe a group — see lag per partition
```bash
kafka-consumer-groups \
  --describe \
  --group my-test-group \
  --bootstrap-server localhost:9092
```

Output:
```
GROUP          TOPIC          PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG
my-test-group  raw-telemetry  0          100             105             5
my-test-group  raw-telemetry  1          98              98              0
my-test-group  raw-telemetry  2          102             102             0
```
- **CURRENT-OFFSET** — last message the consumer processed
- **LOG-END-OFFSET** — last message written to the partition
- **LAG** — how far behind the consumer is (5 unprocessed messages on partition 0)
- In ObservaFlow, if processor-service lag grows, it means aggregation is falling behind

### Reset offsets to beginning (reprocess all messages)
```bash
kafka-consumer-groups \
  --reset-offsets \
  --group my-test-group \
  --topic raw-telemetry \
  --to-earliest \
  --bootstrap-server localhost:9092 \
  --execute
```

### Reset offsets to latest (skip all existing messages)
```bash
kafka-consumer-groups \
  --reset-offsets \
  --group my-test-group \
  --topic raw-telemetry \
  --to-latest \
  --bootstrap-server localhost:9092 \
  --execute
```

---

## 5. BROKER & CLUSTER HEALTH

### List all brokers in the cluster
```bash
kafka-broker-api-versions --bootstrap-server localhost:9092
```

### Check topic configuration
```bash
kafka-configs \
  --describe \
  --topic raw-telemetry \
  --bootstrap-server localhost:9092
```

### Check broker configuration
```bash
kafka-configs \
  --describe \
  --broker 1 \
  --bootstrap-server localhost:9092
```

---

## 6. COMMON ERRORS & HOW TO DIAGNOSE THEM

---

### ERROR 1 — Kafka fails to start, ZooKeeper connection refused

**Symptom:**
```
WARN [Controller id=1] Error connecting to ... (kafka.controller)
ERROR Error while creating ephemeral at /brokers/ids/1 (ZooKeeper)
```

**Cause:** Kafka started before ZooKeeper was ready.

**How to identify:**
```bash
docker compose logs zookeeper
docker compose logs kafka-1
```

**Fix:**
```bash
docker compose down
docker compose up -d
```
The `depends_on` in your compose file handles ordering, but ZooKeeper needs ~10s to be truly ready. If it keeps failing, add a healthcheck or just restart.

---

### ERROR 2 — Under-replicated partitions (Isr < Replicas)

**Symptom:** One Kafka broker is down or lagging.

**How to identify:**
```bash
kafka-topics --describe --topic raw-telemetry --bootstrap-server localhost:9092
```
Look for Isr count less than Replicas count:
```
Isr: 1,2   # only 2 in sync instead of 3 — broker 3 is behind
```

**Fix:** Restart the lagging broker:
```bash
docker compose restart kafka-3
```

---

### ERROR 3 — `docker compose up` fails with "port already in use"

**Symptom:**
```
Error: Bind for 0.0.0.0:9092 failed: port is already allocated
```

**Cause:** A previous container is still running on that port.

**How to identify:**
```bash
docker ps
```

**Fix:**
```bash
docker compose down
docker compose up -d
```

---

### ERROR 4 — Kafka topic creation fails with "replication factor larger than available brokers"

**Symptom:**
```
ERROR org.apache.kafka.common.errors.InvalidReplicationFactorException:
Replication factor: 3 larger than available brokers: 1
```

**Cause:** Not all 3 Kafka brokers are running when you try to create the topic.

**How to identify:**
```bash
docker compose ps
```
Check that kafka-1, kafka-2, kafka-3 all show `running`.

**Fix:** Wait for all brokers to start, then recreate the topic.

---

### ERROR 5 — Data lost after `docker compose down`

**Symptom:** Topics exist but all messages are gone after restart.

**Cause:** This is expected behavior. `docker compose down` stops containers but keeps volumes. Messages persist.
BUT — `docker compose down -v` **deletes volumes** — all Kafka data, MongoDB data, Redis data is wiped.

**The difference:**
```bash
docker compose down        # stops containers, KEEPS volumes (data safe)
docker compose down -v     # stops containers, DELETES volumes (data gone)
```

**When to use `-v`:** Only when you want a completely fresh start — e.g., corrupt data, testing from scratch, or changing Kafka config that requires clean state.

**In ObservaFlow:** Use `docker compose down` normally. Use `docker compose down -v` only when you intentionally want to reset everything.

---

### ERROR 6 — Consumer not receiving messages

**Symptom:** Producer sends messages but consumer sees nothing.

**How to identify:**
```bash
# Check if messages exist in the topic
kafka-console-consumer \
  --topic raw-telemetry \
  --from-beginning \
  --max-messages 5 \
  --bootstrap-server localhost:9092

# Check consumer group lag
kafka-consumer-groups --describe --group <your-group> --bootstrap-server localhost:9092
```

**Common causes:**
- Consumer is reading from latest offset (not from-beginning) — no new messages arriving
- Consumer group offset is already at the end — reset it
- Wrong topic name in application config

---

### ERROR 7 — `docker compose down -v` and now topics are gone

This is what happened in your session. You ran `down -v` which deleted the Kafka volumes. Kafka stores topic metadata in ZooKeeper AND in its own log directories. When the volume is deleted, all topics and messages are wiped.

**Recovery:** Just recreate the topics:
```bash
docker exec -it infra-kafka-1-1 bash

kafka-topics --create --topic raw-telemetry --partitions 3 --replication-factor 3 --bootstrap-server localhost:9092
kafka-topics --create --topic processed-metrics --partitions 3 --replication-factor 3 --bootstrap-server localhost:9092
kafka-topics --create --topic alert-events --partitions 3 --replication-factor 3 --bootstrap-server localhost:9092
```

**Prevention:** Never run `down -v` unless you intend to wipe everything.

---

## 7. DAILY WORKFLOW CHEATSHEET

```bash
# Start the stack
docker compose up -d

# Check all containers running
docker compose ps

# Get into Kafka CLI
docker exec -it infra-kafka-1-1 bash

# Verify topics exist
kafka-topics --list --bootstrap-server localhost:9092

# Check topic health
kafka-topics --describe --topic raw-telemetry --bootstrap-server localhost:9092

# Test: send a message
kafka-console-producer --topic raw-telemetry --bootstrap-server localhost:9092

# Test: read messages
kafka-console-consumer --topic raw-telemetry --from-beginning --bootstrap-server localhost:9092

# Check consumer lag
kafka-consumer-groups --describe --group <group-name> --bootstrap-server localhost:9092

# Stop stack (keep data)
docker compose down

# Stop stack + wipe all data (careful!)
docker compose down -v
```
