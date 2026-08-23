import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    application
    id("com.gradleup.shadow") version "9.6.1"
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
    implementation(project(":type.game"))
    implementation(project(":commons"))
    implementation(libs.caffeine)
    implementation(libs.tinylog.api)
    implementation(libs.tinylog.impl)
    implementation(libs.gson)
    implementation(libs.mongodb.bson)
    implementation(libs.mongodb.driver.sync)

}

application {
    mainClass.set("net.swofty.service.replay.ReplayService")
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("ServiceReplay")
        archiveClassifier.set("")
        archiveVersion.set("")
    }
}
