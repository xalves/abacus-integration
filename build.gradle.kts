plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.spring") version "2.0.20"
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
    jacoco
}

group = "abacus.integration"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.3.4"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.mockk:mockk:1.13.8") // MockK for Kotlin mocking
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0") // For testing HTTP
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport) // Generate report after tests run
}

// Configure JaCoCo
jacoco {
    toolVersion = "0.8.11" // Latest stable version
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // Tests are required to run before generating the report

    reports {
        xml.required.set(true)  // For CI/CD integration
        html.required.set(true) // Human-readable report
        csv.required.set(false) // Optional
    }
}

// Optional: Add coverage verification task
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.50".toBigDecimal() // 50% minimum coverage
            }
        }

        rule {
            element = "CLASS"
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.40".toBigDecimal() // 40% per class
            }
            excludes = listOf(
                "abacus.RouteLlmCliApplicationKt",
                "abacus.RouteLlmCliApplication"
            )
        }
    }
}

// Make 'check' task run coverage verification
tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }

    // For example:
    jvmToolchain(21)
}