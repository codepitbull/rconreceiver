plugins {
    id("java")
    id("com.github.sgtsilvio.gradle.utf8") version "0.1.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("com.github.hierynomus.license") version "0.16.1"
    id("org.owasp.dependencycheck") version "7.4.4"
}


group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

dependencies {
    compileOnly("com.hivemq:hivemq-edge-adapter-sdk:${property("hivemq-edge-adapter-sdk.version")}")
    compileOnly("commons-io:commons-io:${property("commons-io.version")}")
    compileOnly("com.fasterxml.jackson.core:jackson-databind:${property("jackson.version")}")
    compileOnly("org.slf4j:slf4j-api:${property("slf4j.version")}")
    implementation(project(":rcon"))
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:${property("junit.jupiter.version")}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${property("junit.jupiter.version")}")
    testImplementation("org.junit.platform:junit-platform-launcher:${property("junit.jupiter.platform.version")}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${property("junit.jupiter.version")}")
    testImplementation("com.hivemq:hivemq-edge-adapter-sdk:${property("hivemq-edge-adapter-sdk.version")}")
    testImplementation("org.mockito:mockito-core:${property("mockito.version")}")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:${property("jackson.version")}")
}

tasks.test {
    useJUnitPlatform()
}

license {
    header = file("HEADER")
    mapping("java", "SLASHSTAR_STYLE")
}