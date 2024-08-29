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
    implementation("com.typesafe.akka:akka-stream-kafka_2.13:3.0.1")
    implementation("com.typesafe.akka:akka-stream_2.13:2.6.19")
    implementation("com.typesafe.akka:akka-actor-typed_2.13:2.6.19")
    implementation("com.typesafe.akka:akka-serialization-jackson_2.13:2.6.19")
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
