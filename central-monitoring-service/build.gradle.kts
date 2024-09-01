plugins {
    kotlin("jvm")
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation("org.apache.kafka:kafka-clients:3.2.0")
    implementation("org.apache.kafka:kafka-streams:3.2.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")
    implementation("com.typesafe:config:1.4.2")
    implementation("org.slf4j:slf4j-api:2.0.7")

    // Reactor Kafka dependency
    implementation("io.projectreactor.kafka:reactor-kafka:1.3.11")

    // Reactor Core dependency
    implementation("io.projectreactor:reactor-core:3.5.7")
    
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    testImplementation("org.mockito:mockito-core:5.4.0")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")

}
application {
    mainClass.set("com.davithayrapetyan.weathermonitoring.CentralMonitoringServiceKt")
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "com.davithayrapetyan.weathermonitoring.CentralMonitoringServiceKt"
    }
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })
}

sourceSets {
    test {
        kotlin.srcDirs("src/test/kotlin")
        resources.srcDirs("src/test/resources")
    }
}

tasks.test {
    useJUnitPlatform()
}