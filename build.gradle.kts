plugins {
    id("java")
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("checkstyle")
    id("org.owasp.dependencycheck") version "12.0.2"
}

group = "com.serezk4"
version = "1.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

/// versions ///
val jakartaValidationVersion = "3.1.0"
val flywayDatabasePostgresVersion = "11.3.1"
val r2dbcPoolVersion = "1.0.2.RELEASE"
val log4jVersion = "2.24.3"
val junitBomVersion = "5.10.0"
val mapstructVersion = "1.6.3"
val telegramBotsVersion = "8.2.0"
val hibernateVersion = "6.6.7.Final"
val apachePoiVersion = "5.4.0"

/// word ///
dependencies {
    implementation("org.apache.poi:poi-ooxml:$apachePoiVersion")
}

/// validation ///
dependencies {
    implementation("jakarta.validation:jakarta.validation-api:$jakartaValidationVersion")
    implementation("org.springframework.boot:spring-boot-starter-validation")
}

/// database ///
dependencies {
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.hibernate.orm:hibernate-core:${hibernateVersion}")
}

/// logging ///
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-log4j2")

    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion") {
        exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
    }

    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
}

configurations.all {
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    exclude(group = "ch.qos.logback", module = "logback-classic")
    exclude(group = "ch.qos.logback", module = "logback-core")
}

/// tests ///
dependencies {
    testImplementation(platform("org.junit:junit-bom:$junitBomVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

/// health ///
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-core")
    implementation("io.micrometer:micrometer-registry-prometheus")
}

/// useful things ///
dependencies {
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
}

/// telegram ///
dependencies {
    implementation("org.telegram:telegrambots-longpolling:$telegramBotsVersion")
    implementation("org.telegram:telegrambots-client:$telegramBotsVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.check {
    dependsOn("checkstyleMain")
    dependsOn("checkstyleTest")
}

dependencyCheck {
    failBuildOnCVSS = 7.0f
    suppressionFiles = listOf("dependency-check-suppressions.xml")
    analyzers.assemblyEnabled = false
    nvd {
        apiKey = System.getenv("NVD_API_KEY") ?: ""
    }
}
