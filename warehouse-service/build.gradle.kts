plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(project(":common"))
    implementation("org.apache.kafka:kafka-clients:3.8.0")
}

application {
    mainClass.set("com.davithayrapetyan.weathermonitoring.WarehouseServiceKt")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "com.davithayrapetyan.weathermonitoring.WarehouseServiceKt"
    }
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })
}
