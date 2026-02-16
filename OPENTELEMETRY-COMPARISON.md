# OpenTelemetry Integration: Spring Boot 3.x vs Spring Boot 4.x

## Overview

This document compares two approaches to adding OpenTelemetry observability (metrics, logging, distributed tracing) to the `json-placeholder` application, both targeting the **Grafana LGTM stack** (Loki, Grafana, Tempo, Mimir) via Docker Compose.

| | **Before (main branch)** | **After (sb4 branch)** |
|---|---|---|
| **Spring Boot** | 3.2.5 | 4.0.0 |
| **Gradle** | 8.13 | 8.14 (required by SB4) |
| **Approach** | Community Micrometer bridge | Native SB4 starter |

---

## Dependencies

### Before: Spring Boot 3.2.5 (`main` branch)

Requires multiple dependencies from different projects to wire OTel together:

```kotlin
// OpenTelemetry: Micrometer tracing bridge + OTLP exporters
implementation(platform("io.opentelemetry:opentelemetry-bom:1.35.0"))
implementation("io.micrometer:micrometer-tracing-bridge-otel")
implementation("io.opentelemetry:opentelemetry-exporter-otlp")

// Micrometer OTLP registry for metrics export
implementation("io.micrometer:micrometer-registry-otlp")

// AOP for @Observed annotations
implementation("org.springframework.boot:spring-boot-starter-aop")
```

**5 dependency lines** across 3 different groups (OpenTelemetry, Micrometer, Spring Boot).

### After: Spring Boot 4.0.0 (`sb4` branch)

A single native starter replaces most of the above:

```kotlin
// OpenTelemetry: Spring Boot 4 native starter
implementation("org.springframework.boot:spring-boot-starter-opentelemetry")

// Micrometer OTLP registry for metrics export
implementation("io.micrometer:micrometer-registry-otlp")

// AspectJ for @Observed annotations (renamed from spring-boot-starter-aop in SB4)
implementation("org.springframework.boot:spring-boot-starter-aspectj")
```

**3 dependency lines**, all version-managed by SB4's BOM. No external BOM needed.

The `spring-boot-starter-opentelemetry` transitively includes:
- OpenTelemetry API
- `micrometer-tracing-bridge-otel`
- OTLP exporters for traces

---

## Build System

### Before: Spring Boot 3.2.5

```kotlin
plugins {
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.7"
}
```

Uses the `io.spring.dependency-management` plugin for BOM management.

### After: Spring Boot 4.0.0

```kotlin
plugins {
    id("org.springframework.boot") version "4.0.0"
}

dependencies {
    implementation(platform(
        org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES
    ))
}
```

Drops the dependency management plugin in favor of Gradle's native `platform()` BOM support. Also requires:
- Gradle 8.14+ (SB4 requirement)
- `pluginManagement` block in `settings.gradle.kts` (for milestone repos if needed)

---

## Configuration Properties

### Before: Spring Boot 3.2.5

```properties
# Distributed tracing via Micrometer -> OTel bridge
management.tracing.sampling.probability=1.0
management.otlp.tracing.endpoint=http://localhost:4318/v1/traces

# Metrics export via OTLP
management.otlp.metrics.export.url=http://localhost:4318/v1/metrics
management.otlp.metrics.export.step=10s
```

Tracing and metrics use `management.otlp.*` properties (Micrometer's namespace). No native log export via OTLP.

### After: Spring Boot 4.0.0

```properties
# Distributed tracing - native OTel property namespace
management.tracing.sampling.probability=1.0
management.opentelemetry.tracing.export.otlp.endpoint=http://localhost:4318/v1/traces

# Log export - new in SB4 (native OTel property namespace)
management.opentelemetry.logging.export.otlp.endpoint=http://localhost:4318/v1/logs

# Metrics export via OTLP
management.otlp.metrics.export.url=http://localhost:4318/v1/metrics
management.otlp.metrics.export.step=10s
```

Key differences:
- **Traces** use `management.opentelemetry.tracing.*` (new native namespace)
- **Logs** get a dedicated `management.opentelemetry.logging.*` endpoint (not available in SB3)
- **Metrics** still use `management.otlp.metrics.*` (Micrometer's OTLP registry)

---

## Service Layer (`@Observed`)

### Identical in both branches

```java
@Observed(name = "json-placeholder.get-all-posts", contextualName = "get-all-posts")
@Cacheable(value = "posts")
public List<Post> getAllPosts() { ... }
```

The `@Observed` annotation from `io.micrometer.observation.annotation` works the same way in both versions. It requires:
- `ObservedAspect` bean registered via `ObservationConfig`
- AOP/AspectJ starter (`spring-boot-starter-aop` in SB3, `spring-boot-starter-aspectj` in SB4)

---

## Infrastructure (Docker Compose)

### Identical in both branches

```yaml
services:
  lgtm:
    image: grafana/otel-lgtm:latest
    container_name: lgtm
    ports:
      - "3000:3000"   # Grafana UI
      - "4317:4317"   # OTLP gRPC receiver
      - "4318:4318"   # OTLP HTTP receiver
```

Both approaches use the same LGTM stack and OTLP HTTP endpoint (port 4318).

---

## Log Correlation

### Before: Spring Boot 3.2.5

Logs include trace context via MDC keys configured in `logback-spring.xml`:

```
2026-02-15 10:30:05.801 [http-nio-8080-exec-1] [traceId=abc123 spanId=def456] INFO  JsonPlaceholderService - Fetching all posts
```

Logs are **not** exported via OTLP — they only appear in stdout with trace IDs for manual correlation in Grafana.

### After: Spring Boot 4.0.0

Same console output with trace context, **plus** native OTLP log export:

```properties
management.opentelemetry.logging.export.otlp.endpoint=http://localhost:4318/v1/logs
```

Logs are sent directly to Loki via OTLP, enabling full correlation in Grafana without log scraping.

---

## Summary: What Changes and What Stays the Same

| Aspect | SB 3.2.5 (Before) | SB 4.0.0 (After) |
|---|---|---|
| **OTel Dependency** | 5 lines across 3 groups + external BOM | 1 native starter |
| **Dependency Mgmt** | `io.spring.dependency-management` plugin | Gradle native `platform()` |
| **Trace Config** | `management.otlp.tracing.*` | `management.opentelemetry.tracing.*` |
| **Log Export** | Not available natively | `management.opentelemetry.logging.*` |
| **Metrics Config** | `management.otlp.metrics.*` | `management.otlp.metrics.*` (unchanged) |
| **AOP Starter** | `spring-boot-starter-aop` | `spring-boot-starter-aspectj` |
| **@Observed Usage** | Identical | Identical |
| **Docker Compose** | Identical | Identical |
| **Gradle Version** | 8.13 | 8.14+ required |
| **GraalVM Native** | Requires extra config | First-class support |
| **Auto-instrumentation** | Manual bridge only | HTTP server/client, JDBC auto-instrumented |

### Migration Effort: Low

The `@Observed` annotations and `ObservationConfig` are the same across both versions. The main migration work is:
1. Swap dependencies (3 lines changed)
2. Update property namespaces (`management.otlp.tracing` -> `management.opentelemetry.tracing`)
3. Rename AOP starter (`-aop` -> `-aspectj`)
4. Upgrade Gradle to 8.14+

---

## How to Run

### Start the LGTM stack:
```bash
docker compose up -d
```

### Start the application:
```bash
./gradlew bootRun
```

### Access:
- **Application**: http://localhost:8080/api/posts
- **Grafana**: http://localhost:3000 (admin/admin)
  - **Tempo** (traces): Explore -> Tempo data source
  - **Loki** (logs): Explore -> Loki data source
  - **Mimir** (metrics): Explore -> Mimir data source

### Test an endpoint and see traces:
```bash
curl http://localhost:8080/api/posts/1
```

Then open Grafana -> Explore -> Tempo to see the distributed trace with spans for:
- HTTP server request (`GET /api/posts/1`)
- `@Observed` span: `get-post-by-id`
- HTTP client request to `jsonplaceholder.typicode.com`
