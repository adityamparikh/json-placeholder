plugins {
    java
    jacoco
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "dev.aparikh"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Redis and Caching
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-cache")

    // JTE templating engine
    implementation("gg.jte:jte:3.1.9")
    implementation("gg.jte:jte-spring-boot-starter-3:3.1.9")

    // HTML to PDF conversion
    implementation("org.xhtmlrenderer:flying-saucer-pdf:9.3.1")
    implementation("com.openhtmltopdf:openhtmltopdf-core:1.0.10")
    implementation("com.openhtmltopdf:openhtmltopdf-pdfbox:1.0.10")

    // HTML to DOCX conversion
    implementation("org.docx4j:docx4j-core:11.4.9")
    implementation("org.docx4j:docx4j-JAXB-ReferenceImpl:11.4.9") // Use Reference Implementation instead of MOXy
    implementation("org.docx4j:docx4j-export-fo:11.4.9")
    implementation("org.jsoup:jsoup:1.17.2") // For HTML parsing

    // JAXB implementation
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.4")

    // HTML to RTF conversion
    implementation("com.tutego:jrtf:0.7")

    // Claude API Integration
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")

    // Testcontainers dependencies with explicit versions
    val testcontainersVersion = "1.19.7"
    testImplementation("org.testcontainers:junit-jupiter:${testcontainersVersion}")
    testImplementation("org.testcontainers:testcontainers:${testcontainersVersion}")

    // Jedis for Redis client
    testImplementation("redis.clients:jedis:5.1.0")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs = listOf("-Xlint:-unchecked", "-parameters")
}
