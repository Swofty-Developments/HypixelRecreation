plugins {
    `java-library`
    id("com.vanniktech.maven.publish")
}

description = "The gameplay-world seam shared by Polyp and world systems (Archipelago): MechanicsWorld + tick scaling"
group = "io.github.term4"
version = rootProject.version
java.toolchain.languageVersion = JavaLanguageVersion.of(25)

mavenPublishing {
    coordinates(group.toString(), "polyp-world", version.toString())
    publishToMavenCentral()
    if (providers.gradleProperty("signingInMemoryKey").isPresent || providers.gradleProperty("signing.keyId").isPresent) {
        signAllPublications()
    }

    pom {
        name = "polyp-world"
        description = project.description
        url = "https://github.com/TennacleCore/Polyp"

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
            url = "https://github.com/TennacleCore/Polyp"
            connection = "scm:git:git://github.com/TennacleCore/Polyp.git"
            developerConnection = "scm:git:ssh://git@github.com/TennacleCore/Polyp.git"
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    val minestomVersion = "2026.08.16-26.2"
    compileOnly("net.minestom:minestom:$minestomVersion")
    compileOnly("org.slf4j:slf4j-api:2.0.18")
}
