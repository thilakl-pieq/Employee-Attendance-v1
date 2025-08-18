plugins {
    kotlin("jvm") version "2.1.21"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("service.ApplicationKt") // Change this if your main class path differs
}

repositories {
    mavenCentral()
}

dependencies {
    dependencies {
        // Core JDBI library - lightweight abstraction on top of JDBC
        implementation("org.jdbi:jdbi3-core:3.45.1")

        // JDBI SQL Object extension - allows defining DAO interfaces with @SqlQuery, @SqlUpdate annotations
        implementation("org.jdbi:jdbi3-sqlobject:3.45.1")

        // JDBI Kotlin extension - adds Kotlin-friendly APIs (e.g., extension functions, better null handling)
        implementation("org.jdbi:jdbi3-kotlin:3.45.1")

        // HikariCP - high-performance JDBC connection pool (commonly used with JDBI and Spring)
        implementation("com.zaxxer:HikariCP:5.1.0")

        // PostgreSQL JDBC driver - required to connect to a PostgreSQL database
        implementation("org.postgresql:postgresql:42.7.2")
    }

    // Jackson Kotlin module for JSON serialization
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0")

    // Jackson JavaTimeModule for Java 8 date/time types support
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.0")

    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    // Dropwizard core
    implementation("io.dropwizard:dropwizard-core:4.0.15")
    implementation("io.dropwizard:dropwizard-client:4.0.15")

    // Kotlin stdlib
    implementation(kotlin("stdlib"))

    // kotlinx.serialization core library (if you plan to use it fully)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.9.0")

    // Testing dependencies
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2") // To fix Gradle warning about test framework

    // Optional for mocking and assertion libraries
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("org.assertj:assertj-core:3.24.2")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21)) // Use your desired Java version (e.g., 17 or 21)
    }
}

tasks.named<JavaExec>("run") {
    // Default program arguments for Dropwizard
    args("server", "src/main/resources/config.yml")
}
