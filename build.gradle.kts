buildscript {
    configurations.classpath {
        resolutionStrategy.force(
            "org.ow2.asm:asm:9.10.1",
            "org.ow2.asm:asm-analysis:9.10.1",
            "org.ow2.asm:asm-commons:9.10.1",
            "org.ow2.asm:asm-tree:9.10.1",
            "org.ow2.asm:asm-util:9.10.1",
        )
    }
}

plugins {
    base
    java
    id("io.freefair.lombok") version "9.5.0" apply false
    id("io.sentry.jvm.gradle") version "6.19.0" apply false
}

group = "net.swofty"
version = "1.0"

repositories {
    mavenCentral()
}

val libsCatalog: VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "io.freefair.lombok")

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.viaversion.com")
        maven("https://jitpack.io")
        maven("https://repo.lucko.me/")
        maven("https://repo.nexomc.com/releases")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    dependencies {
        testImplementation(libsCatalog.findLibrary("junit-jupiter").get())
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")

        implementation(libsCatalog.findLibrary("reflections").get())
        implementation(libsCatalog.findLibrary("json").get())
        implementation(libsCatalog.findLibrary("sentry-async-profiler").get())

        compileOnly(libsCatalog.findLibrary("lombok").get())

        implementation(platform(libsCatalog.findLibrary("jackson-bom").get()))
        implementation(libsCatalog.findLibrary("jackson-core").get())
        implementation(libsCatalog.findLibrary("jackson-databind").get())
        implementation(libsCatalog.findLibrary("jackson-annotations").get())
    }

    tasks.test {
        useJUnitPlatform()
    }
}
