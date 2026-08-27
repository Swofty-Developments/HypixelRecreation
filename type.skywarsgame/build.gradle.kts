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
    implementation(project(":type.game"))
    implementation(project(":type.generic"))
    implementation(project(":type.skywarslobby"))
    implementation(project(":commons"))
    implementation(project(":proxy.api"))
    implementation(project(":pvp"))
    implementation(libs.mongodb.bson)
    implementation(libs.adventure.text.minimessage)
    implementation(libs.polar)
    implementation(libs.fastutil)
    compileOnly(libs.minestom) {
        exclude(group = "org.jboss.shrinkwrap.resolver", module = "shrinkwrap-resolver-depchain")
    }
    implementation(libs.tinylog.api)
    implementation(libs.tinylog.impl)
    implementation(libs.gson)

    testImplementation(libs.minestom)
    testImplementation(platform("org.junit:junit-bom:6.1.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
