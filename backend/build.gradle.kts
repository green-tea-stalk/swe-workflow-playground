plugins {
    alias(libs.plugins.micronaut.application)
    alias(libs.plugins.shadow)
    alias(libs.plugins.spotless)
    alias(libs.plugins.micronaut.aot)
}

version = "0.1"
group = "com.example"

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor(libs.micronaut.data.processor)
    annotationProcessor(libs.micronaut.http.validation)
    annotationProcessor(libs.micronaut.serde.processor)
    annotationProcessor(libs.micronaut.validation.processor)

    implementation(libs.micronaut.data.jdbc)
    implementation(libs.micronaut.flyway)
    implementation(libs.micronaut.serde.jackson)
    implementation(libs.micronaut.sql.hikari)
    implementation(libs.micronaut.validation)
    implementation(libs.jakarta.validation.api)

    compileOnly(libs.micronaut.http.client)

    runtimeOnly(libs.logback.classic)
    runtimeOnly(libs.mysql.connector.j)
    runtimeOnly(libs.flyway.mysql)
    runtimeOnly(libs.yaml.snakeyaml)

    testImplementation(libs.micronaut.http.client)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation(libs.testcontainers.mysql)
    testImplementation(libs.testcontainers.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

application {
    mainClass = "com.example.bulletinboard.Application"
}

java {
    sourceCompatibility = JavaVersion.toVersion("25")
    targetCompatibility = JavaVersion.toVersion("25")
}

micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("com.example.*")
    }
    aot {
        optimizeServiceLoading = false
        convertYamlToJava = false
        precomputeOperations = true
        cacheEnvironment = true
        optimizeClassLoading = true
        deduceEnvironment = true
        optimizeNetty = true
        replaceLogbackXml = true
    }
}

tasks.withType<AbstractTestTask>().configureEach {
    failOnNoDiscoveredTests = false
}

spotless {
    java {
        target("src/**/*.java")
        palantirJavaFormat("2.71.0")
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
    }
}
