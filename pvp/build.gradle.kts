plugins {
    `java-library`
    id("com.vanniktech.maven.publish") version "0.36.0"
}

description = "A library for Minestom 1.8 mechanics"
group = "io.github.term4"
version = "0.2.0"
java.toolchain.languageVersion = JavaLanguageVersion.of(25)

mavenPublishing {
    coordinates(group.toString(), "polyp", version.toString())
    publishToMavenCentral()
    // sign only when keys are configured (release env); keyless dev machines can still publishToMavenLocal
    if (providers.gradleProperty("signingInMemoryKey").isPresent || providers.gradleProperty("signing.keyId").isPresent) {
        signAllPublications()
    }

    pom {
        name = "polyp"
        description = project.description
        url = "https://github.com/Term4/Polyp"

        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/MIT"
                distribution = "repo"
            }
        }

        developers {
            developer {
                name = "Term4"
                id = "Term4"
                email = "gptkc2003@gmail.com"
                url = "https://github.com/Term4"
            }
        }

        scm {
            url = "https://github.com/Term4/Polyp"
            connection = "scm:git:git://github.com/Term4/Polyp.git"
            developerConnection = "scm:git:ssh://git@github.com/Term4/Polyp.git"
        }
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    compileOnly(project(":codegen"))
    annotationProcessor(project(":codegen"))
    val minestomVersion = "2026.07.12-26.2"
    val slf4jVersion = "2.0.18"
    val junitVersion = "6.0.3"

    compileOnly("net.minestom:minestom:$minestomVersion")
    // SLF4J facade only; a Minestom runtime always provides the api + a binding, so compileOnly (like Minestom). Never ship a binding.
    compileOnly("org.slf4j:slf4j-api:$slf4jVersion")

    // Unit testing
    testImplementation("net.minestom:minestom:$minestomVersion")
    // headless server harness (Env/@EnvTest) for entity-backed golden tests; test-only, never shipped
    testImplementation("net.minestom:testing:$minestomVersion")
    testImplementation("org.tinylog:tinylog-api:2.8.0-M1")
    testImplementation("org.tinylog:tinylog-impl:2.8.0-M1")
    testImplementation("org.tinylog:slf4j-tinylog:2.8.0-M1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junitVersion")
}

tasks.test {
    useJUnitPlatform()
    // synchronous player spawns + relaxed thread asserts (like Minestom's Env); must be set before ServerFlag loads
    systemProperty("minestom.inside-test", "true")
}