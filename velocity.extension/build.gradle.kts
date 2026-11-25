import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    application
    `maven-publish`
    alias(libs.plugins.shadow)
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.4.1"
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.viaversion.com")

    maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }
    maven { url = uri("https://repo.codemc.io/repository/maven-snapshots/") }
}

dependencies {
    compileOnly(libs.velocity.api)
    annotationProcessor(libs.velocity.api)
    compileOnly(files("velocity-4.1.0-SNAPSHOT-16.jar"))

    implementation(libs.atlas.redis)
    implementation(libs.jedis)
    implementation(project(":commons"))
    implementation(project(":proxy.api"))
    implementation(libs.mongodb.bson)
    implementation(libs.mongodb.driver.sync)

    implementation(libs.viabackwards.common)
    implementation(libs.viarewind.common)
    implementation(libs.viaversion)

    implementation(libs.packetevents.velocity)

    implementation(platform(libs.netty.bom))
    implementation(libs.netty.buffer)
    implementation(libs.netty.codec)
    implementation(libs.netty.codec.http)
    implementation(libs.netty.transport)
    implementation(libs.netty.handler)

    implementation(libs.byte.buddy)
    implementation(libs.byte.buddy.agent)
    implementation(libs.tinylog.api)
    implementation(libs.tinylog.impl)
}

evaluationDependsOn(":commons")

application {
    mainClass.set("net.swofty.velocity.SkyBlockVelocity")
}

tasks.withType<ShadowJar> {
    manifest {
        attributes["Main-Class"] = "net.swofty.velocity.SkyBlockVelocity"
    }
    archiveBaseName.set("SkyBlockProxy")
    archiveClassifier.set("")
    archiveVersion.set("")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(tasks["shadowJar"])
        }
    }
}
