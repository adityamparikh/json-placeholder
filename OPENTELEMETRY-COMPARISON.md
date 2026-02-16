# OpenTelemetry Integration: Spring Boot 3.2.5 vs Spring Boot 4.0.0

## Overview

This document compares OpenTelemetry observability (metrics, tracing, logging) in the `json-placeholder` application across two branches, both targeting the **Grafana LGTM stack** (Loki, Grafana, Tempo, Mimir) via Docker Compose.

The `sb4` branch was migrated using the `spring-boot-4-migration` skill and follows the [foojay.io Spring Boot 4 OpenTelemetry guide](https://foojay.io/today/spring-boot-4-opentelemetry-explained/).

| | **main branch (SB 3.2.5)** | **sb4 branch (SB 4.0.0)** |
|---|---|---|
| **Spring Boot** | 3.2.5 | 4.0.0 |
| **Spring Framework** | 6.x | 7.x |
| **Gradle** | 8.13 | 8.14 (required by SB4) |
| **Approach** | Micrometer bridge + manual wiring | Native `spring-boot-starter-opentelemetry` |

---

## 1. Dependencies

### Spring Boot 3.2.5 (`main`)

Seven dependency lines across four groups, plus an external BOM:

```kotlin
// External BOM for OpenTelemetry version management
implementation(platform("io.opentelemetry:opentelemetry-bom:1.35.0"))

// Tracing: Micrometer bridge to OTel + OTLP exporter
implementation("io.micrometer:micrometer-tracing-bridge-otel")
implementation("io.opentelemetry:opentelemetry-exporter-otlp")

// Logging: Logback OTLP appender + OTel SDK (for manual wiring)
implementation("io.opentelemetry.instrumentation:opentelemetry-logback-appender-1.0:2.1.0-alpha")
implementation("io.opentelemetry:opentelemetry-sdk")

// Metrics: OTLP registry
implementation("io.micrometer:micrometer-registry-otlp")

// AOP for @Observed
implementation("org.springframework.boot:spring-boot-starter-aop")
```

Also requires a manual `OpenTelemetryLogConfig` Java class (~30 lines) to programmatically build `SdkLoggerProvider`, create an `OtlpHttpLogRecordExporter`, and install it into the logback appender.

### Spring Boot 4.0.0 (`sb4`)

Four dependency lines, all version-managed by SB4's BOM:

```kotlin
// One starter replaces 5 manual OTel/Micrometer deps
implementation("org.springframework.boot:spring-boot-starter-opentelemetry")

// Logback OTLP appender for log export
implementation("io.opentelemetry.instrumentation:opentelemetry-logback-appender-1.0:2.21.0-alpha")

// Metrics OTLP registry
implementation("io.micrometer:micrometer-registry-otlp")

// AspectJ for @Observed (renamed from spring-boot-starter-aop)
implementation("org.springframework.boot:spring-boot-starter-aspectj")
```

`spring-boot-starter-opentelemetry` transitively provides:
- OpenTelemetry API + SDK
- `micrometer-tracing-bridge-otel`
- OTLP exporters for traces and logs
- Auto-configuration for `OpenTelemetrySdk` bean

---

## 2. Build System

### Spring Boot 3.2.5

```kotlin
plugins {
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.7"
}
```

### Spring Boot 4.0.0

```kotlin
plugins {
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
}
```

Both use `io.spring.dependency-management` (it works fine with SB4). SB4 also requires Gradle 8.14+.

**Additional SB4 starter changes:**
- `spring-boot-starter-web` → `spring-boot-starter-webmvc` (old name deprecated)
- `spring-boot-starter-aop` → `spring-boot-starter-aspectj` (old name deprecated)
- Added `spring-boot-starter-restclient` (SB4 modular: `RestClient.Builder` auto-config is no longer bundled in webmvc)

---

## 3. Configuration Properties

### Spring Boot 3.2.5

```properties
management.tracing.sampling.probability=1.0
management.otlp.tracing.endpoint=http://localhost:4318/v1/traces
management.otlp.metrics.export.url=http://localhost:4318/v1/metrics
management.otlp.metrics.export.step=10s
# No log export property — requires OpenTelemetryLogConfig.java
```

### Spring Boot 4.0.0

```properties
management.tracing.sampling.probability=1.0
management.opentelemetry.tracing.export.endpoint=http://localhost:4318/v1/traces
management.opentelemetry.logging.export.otlp.endpoint=http://localhost:4318/v1/logs
management.metrics.export.otlp.url=http://localhost:4318/v1/metrics
management.metrics.export.otlp.step=10s
```

**Property migrations** (per the [SB4 migration guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)):

| SB 3.2.5 Property | SB 4.0.0 Property |
|---|---|
| `management.otlp.tracing.endpoint` | `management.opentelemetry.tracing.export.endpoint` |
| `management.otlp.tracing.timeout` | `management.opentelemetry.tracing.export.timeout` |
| `management.otlp.metrics.export.url` | `management.metrics.export.otlp.url` |
| *(no equivalent)* | `management.opentelemetry.logging.export.otlp.endpoint` |

---

## 4. Log Export (OTLP)

### Spring Boot 3.2.5 — Manual Wiring

Three pieces required:

1. **Dependencies**: `opentelemetry-logback-appender-1.0` + `opentelemetry-sdk`
2. **logback-spring.xml**: OTEL appender
3. **OpenTelemetryLogConfig.java** (~30 lines):
   ```java
   @Configuration
   public class OpenTelemetryLogConfig {
       @PostConstruct
       public void installLogAppender() {
           OtlpHttpLogRecordExporter exporter = OtlpHttpLogRecordExporter.builder()
                   .setEndpoint(logsEndpoint).build();
           SdkLoggerProvider provider = SdkLoggerProvider.builder()
                   .addLogRecordProcessor(BatchLogRecordProcessor.builder(exporter).build())
                   .setResource(Resource.builder().put("service.name", serviceName).build())
                   .build();
           OpenTelemetrySdk sdk = OpenTelemetrySdk.builder()
                   .setLoggerProvider(provider).build();
           OpenTelemetryAppender.install(sdk);
       }
   }
   ```

### Spring Boot 4.0.0 — Per [foojay.io Guide](https://foojay.io/today/spring-boot-4-opentelemetry-explained/)

SB4 auto-configures the `OpenTelemetrySdk` bean. You still need:

1. **Dependency**: `opentelemetry-logback-appender-1.0`
2. **logback-spring.xml**: OTEL appender (same as SB3)
3. **InstallOpenTelemetryAppender.java** (~10 lines, much simpler):
   ```java
   @Component
   public class InstallOpenTelemetryAppender implements InitializingBean {
       private final OpenTelemetry openTelemetry;
       public InstallOpenTelemetryAppender(OpenTelemetry openTelemetry) {
           this.openTelemetry = openTelemetry;
       }
       @Override
       public void afterPropertiesSet() {
           OpenTelemetryAppender.install(openTelemetry);
       }
   }
   ```

The key difference: SB4 provides the `OpenTelemetry` bean automatically. You just inject it and call `install()`. No manual `SdkLoggerProvider`, `BatchLogRecordProcessor`, or `OtlpHttpLogRecordExporter` creation needed.

---

## 5. Metrics (OTel Semantic Conventions)

### Spring Boot 3.2.5

JVM metrics use Micrometer's default naming conventions (e.g., `jvm_memory_used_bytes`).

### Spring Boot 4.0.0 — Per [foojay.io Guide](https://foojay.io/today/spring-boot-4-opentelemetry-explained/)

Added `OpenTelemetryConfiguration` to use OTel semantic convention metric names:

```java
@Configuration(proxyBeanMethods = false)
public class OpenTelemetryConfiguration {
    @Bean
    ProcessorMetrics processorMetrics() {
        return new ProcessorMetrics(List.of(),
            new OpenTelemetryJvmCpuMeterConventions(Tags.empty()));
    }
    @Bean
    JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics(List.of(),
            new OpenTelemetryJvmMemoryMeterConventions(Tags.empty()));
    }
    // ... JvmThreadMetrics, ClassLoaderMetrics similarly
}
```

This maps metric names to the [OTel semantic conventions](https://opentelemetry.io/docs/specs/semconv/system/jvm-metrics/) (e.g., `jvm.memory.used` instead of `jvm_memory_used_bytes`).

---

## 6. Trace ID in HTTP Responses

### Spring Boot 3.2.5

Not included.

### Spring Boot 4.0.0 — Per [foojay.io Guide](https://foojay.io/today/spring-boot-4-opentelemetry-explained/)

Added `TraceIdFilter` that exposes the current trace ID as an `X-Trace-Id` response header:

```java
@Component
public class TraceIdFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        if (response instanceof HttpServletResponse httpResponse) {
            String traceId = Span.current().getSpanContext().getTraceId();
            httpResponse.setHeader("X-Trace-Id", traceId);
        }
        chain.doFilter(request, response);
    }
}
```

This is useful for correlating client requests with backend traces in Grafana.

---

## 7. Service Layer (`@Observed`)

### Identical in both branches

```java
@Observed(name = "json-placeholder.get-all-posts", contextualName = "get-all-posts")
@Cacheable(value = "posts")
public List<Post> getAllPosts() { ... }
```

The `@Observed` annotation works the same way. Both require:
- `ObservedAspect` bean (via `ObservationConfig`)
- `management.observations.annotations.enabled=true`
- AOP starter: `spring-boot-starter-aop` (SB3) / `spring-boot-starter-aspectj` (SB4)

---

## 8. Log Correlation (Console)

### Identical in both branches

```xml
<pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [traceId=%X{traceId} spanId=%X{spanId}] %-5level %logger{36} - %msg%n</pattern>
```

> **Note**: Use camelCase MDC keys (`traceId`, `spanId`), not snake_case. The Micrometer tracing bridge populates camelCase keys.

---

## 9. Other SB4 Migration Changes

These are not OTel-specific but were required during the migration:

| Change | Reason |
|---|---|
| `GenericJackson2JsonRedisSerializer` → `RedisSerializer.json()` | Class removed in Spring Data Redis (SB4) |
| Empty sentinel master breaks tests | SB4 validates sentinel master ID is not blank |
| `spring-boot-starter-restclient` added | SB4 modular: `RestClient.Builder` not in webmvc starter |

---

## Summary

| Aspect | SB 3.2.5 (main) | SB 4.0.0 (sb4) |
|---|---|---|
| **OTel dependencies** | 7 lines + external BOM | 4 lines, SB4 BOM manages versions |
| **Log export setup** | 30-line manual `OpenTelemetryLogConfig` | 10-line `InstallOpenTelemetryAppender` |
| **Trace export property** | `management.otlp.tracing.endpoint` | `management.opentelemetry.tracing.export.endpoint` |
| **Log export property** | None (hardcoded in Java) | `management.opentelemetry.logging.export.otlp.endpoint` |
| **Metrics export property** | `management.otlp.metrics.export.url` | `management.metrics.export.otlp.url` |
| **OTel metric conventions** | Micrometer defaults | OTel semantic conventions via config beans |
| **Trace ID in responses** | Not available | `X-Trace-Id` header via `TraceIdFilter` |
| **AOP starter** | `spring-boot-starter-aop` | `spring-boot-starter-aspectj` |
| **Web starter** | `spring-boot-starter-web` | `spring-boot-starter-webmvc` + `spring-boot-starter-restclient` |
| **@Observed usage** | Identical | Identical |
| **Docker Compose** | Identical | Identical |
| **Gradle version** | 8.13 | 8.14+ required |
| **Auto-instrumentation** | Manual bridge only | HTTP server/client auto-instrumented |

### Migration Effort: Low–Medium

The `@Observed` annotations and `ObservationConfig` are identical. The main work:

1. Upgrade Gradle to 8.14+
2. Swap dependencies (7 manual OTel deps → 1 starter + logback appender)
3. Rename starters: `web` → `webmvc`, `aop` → `aspectj`, add `restclient`
4. Migrate OTLP properties to `management.opentelemetry.*` namespace
5. Replace `OpenTelemetryLogConfig` with `InstallOpenTelemetryAppender`
6. Add `OpenTelemetryConfiguration` for OTel metric naming conventions
7. Add `TraceIdFilter` for trace ID response headers
8. Fix `GenericJackson2JsonRedisSerializer` → `RedisSerializer.json()`

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
  - **Tempo** (traces): Explore → Tempo data source
  - **Loki** (logs): Explore → Loki data source
  - **Mimir** (metrics): Explore → Mimir data source

### Test an endpoint:
```bash
curl -v http://localhost:8080/api/posts/1
```

On the `sb4` branch, the response includes `X-Trace-Id` — use it to find the trace in Grafana Tempo.
