plugins {
    java
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
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.viaversion.com")
}

dependencies {
    implementation(project(":commons"))
    implementation(project(":packer"))
    implementation(project(":proxy.api"))
    implementation(project(":type.generic"))
    implementation(libs.mongodb.bson)
    implementation(libs.mongodb.driver.sync)
    implementation(libs.tinylog.api)
    implementation(libs.tinylog.impl)
    implementation(libs.minestom) {
        exclude(group = "org.jboss.shrinkwrap.resolver", module = "shrinkwrap-resolver-depchain")
    }
    implementation(libs.polar)
    implementation(libs.fastutil)
    implementation(libs.snakeyaml)
    implementation(libs.kotlin.stdlib)
}

tasks.processResources {
    from(rootProject.file("configuration/skyblock/Minestom.fairysouls.yml"))
    from(rootProject.file("configuration/skyblock/Minestom.regions.yml"))
    from(rootProject.file("configuration/skyblock/Minestom.crystals.yml"))
}
