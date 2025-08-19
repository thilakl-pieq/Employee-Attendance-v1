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
    // JDBI and DB
    implementation("org.jdbi:jdbi3-core:3.45.1")
    implementation("org.jdbi:jdbi3-sqlobject:3.45.1")
    implementation("org.jdbi:jdbi3-kotlin:3.45.1")
    implementation("org.postgresql:postgresql:42.7.2")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("io.dropwizard:dropwizard-jdbi3:4.0.15")

    // Dropwizard core
    implementation("io.dropwizard:dropwizard-core:4.0.15")
    implementation("io.dropwizard:dropwizard-client:4.0.15")

    // Jackson Kotlin module for JSON serialization
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0")

    // Jackson JavaTimeModule for Java 8 date/time types support
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.0")

    // Kotlin stdlib
    implementation(kotlin("stdlib"))

    // kotlinx.serialization core library (optional, if you need it)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.9.0")

    // Unit testing and mocking
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
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

    jvmArgs = listOf("-Duser.timezone=Asia/Kolkata")
}
