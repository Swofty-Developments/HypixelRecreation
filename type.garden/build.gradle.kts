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

dependencies {
    implementation(project(":type.skyblockgeneric"))
    implementation(project(":type.generic"))
    implementation(project(":commons"))
    implementation(project(":proxy.api"))
    compileOnly("net.minestom:minestom:2025.12.20c-1.21.11") {
        exclude(group = "org.jboss.shrinkwrap.resolver", module = "shrinkwrap-resolver-depchain")
    }
    implementation("org.tinylog:tinylog-api:2.7.0")
    implementation("org.tinylog:tinylog-impl:2.7.0")
    implementation("org.mongodb:bson:4.11.2")
    implementation("dev.hollowcube:polar:1.15.0")
    implementation("it.unimi.dsi:fastutil:8.5.18")
    implementation("dev.hollowcube:schem:2.0.1")
}
