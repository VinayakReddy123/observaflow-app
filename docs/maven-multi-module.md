# Maven Multi-Module — Simple Explanation

## The Problem Without It

You have 5 Spring Boot services. Each has its own pom.xml.
Each pom.xml independently declares versions:

```
ingest-service/pom.xml     → spring-boot 3.2.5, lombok 1.18.32
processor-service/pom.xml  → spring-boot 3.2.5, lombok 1.18.32
alert-service/pom.xml      → spring-boot 3.2.5, lombok 1.18.32
websocket-service/pom.xml  → spring-boot 3.2.5, lombok 1.18.32
api-gateway/pom.xml        → spring-boot 3.2.5, lombok 1.18.32
```

Spring Boot 3.2.6 releases. You update ingest-service. You forget alert-service.
Now two services run different Spring Boot versions in production. Bugs happen.

This is called VERSION DRIFT. Multi-module solves it.

---

## The Solution

One parent pom.xml owns ALL versions.
Child modules inherit from it and never declare versions themselves.

```
observaflow-app/
├── pom.xml                ← THE BOSS. All versions live here.
├── common/
│   └── pom.xml            ← says "my parent is the boss pom"
├── ingest-service/
│   └── pom.xml            ← says "my parent is the boss pom"
├── processor-service/
│   └── pom.xml
├── alert-service/
│   └── pom.xml
├── websocket-service/
│   └── pom.xml
└── api-gateway/
    └── pom.xml
```

Spring Boot 3.2.6 releases? Change ONE line in the parent.
All 5 services update. Zero drift possible.

---

## Three Key Sections in the Parent pom.xml

### 1. `<modules>` — tells Maven which folders are child modules
```xml
<modules>
    <module>common</module>
    <module>ingest-service</module>
    <module>processor-service</module>
    <module>alert-service</module>
    <module>websocket-service</module>
    <module>api-gateway</module>
</modules>
```

### 2. `<dependencyManagement>` — declares versions, adds NOTHING
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.32</version>   <!-- version declared here -->
        </dependency>
    </dependencies>
</dependencyManagement>
```
This does NOT add lombok to any service.
It just says "IF a service asks for lombok, give it version 1.18.32".

### 3. `<dependencies>` — adds to EVERY module automatically
```xml
<dependencies>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>   <!-- no version needed, parent manages it -->
    </dependency>
</dependencies>
```
Put only things every service needs here (e.g. lombok, test).

---

## Child pom.xml — What It Looks Like

```xml
<parent>
    <groupId>com.observaflow</groupId>
    <artifactId>observaflow-parent</artifactId>  <!-- point to boss pom -->
    <version>1.0.0-SNAPSHOT</version>
</parent>

<artifactId>ingest-service</artifactId>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
        <!-- NO version here — parent manages it -->
    </dependency>
</dependencies>
```

---

## Why the `common` Module Exists

ingest-service publishes MetricEventDTO to Kafka.
processor-service reads MetricEventDTO from Kafka.

Without common module: you define MetricEventDTO in BOTH services.
They drift apart. Kafka serialization breaks.

With common module: define MetricEventDTO once.
Both services depend on common. Always identical. No drift.

```
common/
└── src/main/java/com/observaflow/common/dto/
    └── MetricEventDTO.java   ← single source of truth
```

Both ingest-service and processor-service add this to their pom.xml:
```xml
<dependency>
    <groupId>com.observaflow</groupId>
    <artifactId>common</artifactId>
</dependency>
```

---

## One Build Command

Without multi-module:
```bash
cd ingest-service && mvn install
cd processor-service && mvn install
cd alert-service && mvn install
# ... repeat 5 times, manually, in the right order
```

With multi-module (from root folder):
```bash
mvn clean install -DskipTests
```
Maven figures out build order automatically.
common builds first (others depend on it), then all services.

---

## Summary

| Problem                          | Solution                              |
|----------------------------------|---------------------------------------|
| Version drift across services    | Parent pom owns all versions          |
| Duplicate shared classes         | common module — define once           |
| Building 5 projects separately   | One mvn install from root             |
| Can't navigate across services   | One IntelliJ project, full navigation |

---

---

# Day 2 — Concepts Learned & Mistakes Corrected

## Concept: The Correct Inheritance Chain

Wrong (what was built initially):
```
spring-boot-starter-parent
├── ingest-service      ← directly talks to Spring's parent
├── processor-service   ← directly talks to Spring's parent
└── ...
```

Correct:
```
spring-boot-starter-parent   ← Spring's parent (CEO)
        ↑
observaflow-parent           ← YOUR root pom.xml (Manager)
        ↑
ingest-service, processor-service, alert-service...  (Employees)
```

Why it matters: without YOUR parent in the middle, there is nobody to share
lombok/actuator/test across all services. Every service would declare them separately.

---

## Concept: `<relativePath>` — Why It Exists

```xml
<parent>
    <groupId>com.observaflow</groupId>
    <artifactId>observaflow-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>   ← this line
</parent>
```

Maven needs to find observaflow-parent. It has two options:
- Look on disk using relativePath → finds it instantly at one folder up
- Search Maven Central (internet) → fails because your pom was never published

`../pom.xml` means: go one folder up from the current service, read pom.xml there.

Without this line → BUILD FAILURE:
```
Could not find artifact com.observaflow:observaflow-parent:1.0.0-SNAPSHOT
```

---

## Concept: webmvc vs webflux

| | spring-boot-starter-webmvc | spring-boot-starter-webflux |
|---|---|---|
| Model | Blocking — one thread per request | Non-blocking reactive |
| Scale | Limited by thread count | Handles thousands of concurrent requests |
| Use case | Simple CRUD apps | High throughput (ingest-service: 1000 req/sec) |
| Returns | Plain objects | Mono<T> or Flux<T> |

ObservaFlow uses webflux everywhere because ingest-service receives up to 1000 req/sec.
Blocking threads would not survive that load.

---

## Concept: spring-kafka vs org.apache.kafka

```
org.apache.kafka        ← raw Kafka client (low level, lots of boilerplate)
        ↑
org.springframework.kafka  ← Spring wrapper (clean API, auto-configuration)
```

Raw Apache Kafka — you write 10 lines to send one message.
Spring Kafka — you write 1 line: `kafkaTemplate.send("topic", key, value)`

Spring Kafka still uses Apache Kafka underneath. When you add spring-kafka to pom.xml,
it pulls in org.apache.kafka:kafka-clients automatically as a transitive dependency.

---

## Concept: Maven Never Needs Manual Dependency Install

You do NOT install jars manually. You declare in pom.xml, Maven downloads automatically.

```
pom.xml declaration → mvn install → Maven Central → ~/.m2/repository (local cache)
```

First build: slow (downloads everything).
Every build after: fast (reads from ~/.m2 on disk).

Same pattern across ecosystems:
- Java/Maven   → pom.xml → mvn install → ~/.m2/
- Node.js/npm  → package.json → npm install → node_modules/
- Python/pip   → requirements.txt → pip install → site-packages/

---

## Concept: Spring Initializr Is Just a pom.xml Generator

start.spring.io does NOT install anything. It just writes XML for you.

Selecting "Spring Reactive Web" on Initializr = it adds this to pom.xml:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

Once you know the artifact IDs, you don't need Initializr at all.
Use it only to look up correct artifact IDs when you don't know them.

---

## Concept: `<packaging>pom</packaging>`

Every Maven project produces an artifact. Default is a `.jar`.
When you write `<packaging>pom</packaging>` in the root, you tell Maven:
"This project produces nothing — it only exists to be a parent."

Without it, Maven would try to package the root as a jar with no source code → error.

---

## Mistakes Made & How They Were Fixed

### Mistake 1: Spring Boot version 4.0.6
```xml
<!-- WRONG -->
<version>4.0.6</version>
```
Spring Boot 4 does not exist. This was Spring Framework 4, not Spring Boot.
Maven would fail trying to download it from Central.

Fix: changed to `3.2.5` (current stable Spring Boot version).

---

### Mistake 2: Services pointing directly to spring-boot-starter-parent
```xml
<!-- WRONG — all 5 services had this -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.6</version>
</parent>
```

Fix: all services now point to observaflow-parent, which points to spring-boot-starter-parent.

---

### Mistake 3: webmvc instead of webflux
```xml
<!-- WRONG -->
<artifactId>spring-boot-starter-webmvc</artifactId>

<!-- CORRECT -->
<artifactId>spring-boot-starter-webflux</artifactId>
```

ObservaFlow is reactive. webmvc is blocking. Would have failed to handle high throughput.

---

### Mistake 4: Trailing slash in artifactId
```xml
<!-- WRONG -->
<artifactId>alert-service/</artifactId>
<artifactId>websocket-service/</artifactId>

<!-- CORRECT -->
<artifactId>alert-service</artifactId>
<artifactId>websocket-service</artifactId>
```

The slash would cause Maven to create broken jar filenames and path resolution errors.

---

### Mistake 5: Package names with underscores
```
com.observaflow.ingest_service   ← WRONG
com.observaflow.websocket_service ← WRONG

com.observaflow.ingest    ← CORRECT
com.observaflow.websocket ← CORRECT
```

Java package naming convention: lowercase letters and dots only. No underscores.
The physical folder names on disk must match the package declaration in each .java file.

---

### Mistake 6: Root pom.xml was empty
The root pom.xml file existed but had no content.
Maven cannot run without a valid root pom.

Fix: wrote the complete root pom with packaging=pom, modules list,
Spring Boot 3.2.5 parent, Spring Cloud BOM, and shared dependencies.

---

### Mistake 7: Wrong package in websocket test file
```java
// WRONG
package com.observaflow.websocket_service;

// CORRECT
package com.observaflow.websocket;
```

Package declaration in the .java file must match the physical folder path on disk.
