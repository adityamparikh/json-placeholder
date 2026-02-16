package dev.aparikh.jsonplaceholder.config;

import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.resources.Resource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenTelemetryLogConfig {

    @Value("${otel.logs.endpoint:http://localhost:4318/v1/logs}")
    private String logsEndpoint;

    @Value("${spring.application.name:json-placeholder}")
    private String serviceName;

    private SdkLoggerProvider loggerProvider;

    @PostConstruct
    public void installLogAppender() {
        OtlpHttpLogRecordExporter logExporter = OtlpHttpLogRecordExporter.builder()
                .setEndpoint(logsEndpoint)
                .build();

        loggerProvider = SdkLoggerProvider.builder()
                .addLogRecordProcessor(BatchLogRecordProcessor.builder(logExporter).build())
                .setResource(Resource.builder()
                        .put("service.name", serviceName)
                        .build())
                .build();

        OpenTelemetrySdk sdk = OpenTelemetrySdk.builder()
                .setLoggerProvider(loggerProvider)
                .build();

        OpenTelemetryAppender.install(sdk);
    }

    @PreDestroy
    public void shutdown() {
        if (loggerProvider != null) {
            loggerProvider.close();
        }
    }
}
