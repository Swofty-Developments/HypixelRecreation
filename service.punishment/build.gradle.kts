import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    application
    alias(libs.plugins.shadow)
}

group = "net.swofty"
version = "3.0"

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    maven("https://jitpack.io")
    mavenCentral()
}

dependencies {
    implementation(project(":service.generic"))
    implementation(project(":commons"))
    implementation(libs.caffeine)
    implementation(libs.tinylog.api)
    implementation(libs.tinylog.impl)
    implementation(libs.gson)
    implementation(libs.mongodb.bson)
    implementation(libs.mongodb.driver.sync)

    implementation(libs.jedis)
}

application {
    mainClass.set("net.swofty.service.punishment.PunishmentService")
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("ServicePunishment")
        archiveClassifier.set("")
        archiveVersion.set("")
    }
}
