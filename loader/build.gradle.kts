import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.util.*

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
    implementation(project(":type.island"))
    implementation(project(":type.hub"))
    implementation(project(":type.thefarmingislands"))
    implementation(project(":type.spidersden"))
    implementation(project(":type.theend"))
    implementation(project(":type.crimsonisle"))
    implementation(project(":type.goldmine"))
    implementation(project(":type.deepcaverns"))
    implementation(project(":type.dwarvenmines"))
    implementation(project(":type.thepark"))
    implementation(project(":type.galatea"))
    implementation(project(":type.backwaterbayou"))
    implementation(project(":type.jerrysworkshop"))
    implementation(project(":type.dungeonhub"))
    implementation(project(":type.skyblockgeneric"))
    implementation(project(":type.garden"))

    implementation(project(":type.prototypelobby"))

    implementation(project(":type.bedwarslobby"))
    implementation(project(":type.bedwarsgame"))
    implementation(project(":type.bedwarsconfigurator"))

    implementation(project(":type.murdermysterylobby"))
    implementation(project(":type.murdermysterygame"))
    implementation(project(":type.murdermysteryconfigurator"))

    implementation(project(":type.skywarslobby"))
    implementation(project(":type.skywarsgame"))
    implementation(project(":type.skywarsconfigurator"))
    implementation(project(":type.ravengardgeneric"))
    implementation(project(":type.ravengardlobby"))
    implementation(project(":type.ravengarddungeon"))
    implementation(project(":type.mainlobby"))

    implementation(project(":type.replayviewer"))

    implementation(project(":type.generic"))
    implementation(project(":commons"))
    implementation(project(":proxy.api"))
    implementation(project(":spark"))
    implementation(project(":anticheat"))
    implementation(libs.slf4j.api)
    implementation(libs.tinylog.api)
    implementation(libs.tinylog.impl)
    implementation(libs.minestom) {
        exclude(group = "org.jboss.shrinkwrap.resolver", module = "shrinkwrap-resolver-depchain")
    }
    implementation(libs.caffeine)
    implementation(libs.adventure.text.minimessage)
    implementation(libs.polar)
    implementation(libs.snakeyaml)
}

application {
    mainClass.set("net.swofty.loader.Hypixel")
    applicationDefaultJvmArgs = listOf("-Duser.dir=${rootProject.projectDir}")
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("HypixelCore")
        archiveClassifier.set("")
        archiveVersion.set("")
    }
}

val testFlow: String by project
val players: String by project
val serverType: String? = project.findProperty("serverType") as String?

tasks.register<JavaExec>("runServer") {
    group = "application"
    description = "Runs the application with the specified server type"

    dependsOn("build")

    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    })

    val resolvedServerType = serverType ?: throw GradleException("Please provide a server type using -PserverType=<type>")
    val projectDir = rootProject.projectDir

    workingDir = projectDir

    mainClass.set("net.swofty.loader.Hypixel")
    classpath = sourceSets["main"].runtimeClasspath
    args(resolvedServerType)

    doLast {
        println("Application started with server type: $resolvedServerType")
    }
}

tasks.register("runWithTestFlow") {
    group = "application"
    description = "Runs multiple servers based on a test flow YAML configuration"

    dependsOn("build")

    doFirst {
        if (!project.hasProperty("testFlow")) {
            throw GradleException("Please provide a test flow using -PtestFlow=<flowname>")
        }
        if (!project.hasProperty("players")) {
            throw GradleException("Please provide players using -Pplayers=<player1,player2,player3>")
        }
    }

    doLast {
        val testFlowName = project.property("testFlow") as String
        val playersList = project.property("players") as String
        val testFlowFile = file("testflows/${testFlowName}.yml")

        if (!testFlowFile.exists()) {
            throw GradleException("Test flow file not found: ${testFlowFile.absolutePath}")
        }

        val lines = testFlowFile.readLines()
        val startServers = mutableListOf<String>()
        var handler = ""
        var inStartServers = false

        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("start_servers:") -> inStartServers = true
                trimmed.startsWith("handler:") -> {
                    handler = trimmed.substringAfter("handler:").trim()
                    inStartServers = false
                }

                inStartServers && trimmed.startsWith("- ") -> {
                    startServers.add(trimmed.substring(2).trim())
                }

                !trimmed.startsWith("#") && trimmed.isNotEmpty() && !trimmed.contains(":") -> {
                    if (inStartServers) inStartServers = false
                }
            }
        }

        // Calculate total expected servers
        val totalExpectedServers = startServers.sumOf { serverSpec ->
            val parts = serverSpec.split(":")
            if (parts.size > 1) parts[1].toInt() else 1
        }

        println("Starting test flow: $testFlowName")
        println("Handler: $handler")
        println("Players: $playersList")
        println("Total expected servers: $totalExpectedServers")
        println("Starting servers:")

        val serverProcesses = mutableListOf<Process>()
        var globalServerIndex = 0

        startServers.forEach { serverSpec ->
            val parts = serverSpec.split(":")
            val serverType = parts[0]
            val count = if (parts.size > 1) parts[1].toInt() else 1

            println("  - $count x $serverType servers")

            repeat(count) { index ->
                val serverName = "$serverType-${index + 1}"
                val processBuilder = ProcessBuilder()
                processBuilder.command(
                    "java",
                    "-Duser.dir=${rootProject.projectDir}",
                    "-cp", sourceSets["main"].runtimeClasspath.asPath,
                    "net.swofty.loader.Hypixel",
                    serverType,
                    "--test-flow", testFlowName,
                    "--test-flow-handler", handler,
                    "--test-flow-players", playersList,
                    "--test-flow-index", index.toString(),
                    "--test-flow-total", count.toString(),
                    "--test-flow-global-index", globalServerIndex.toString(),
                    "--test-flow-server-configs", startServers.joinToString(","),
                )
                processBuilder.directory(rootProject.projectDir)

                // Redirect output to show in console with server prefix
                processBuilder.redirectErrorStream(true)

                println("Starting $serverType server ${index + 1}/$count (global index: $globalServerIndex)...")
                val process = processBuilder.start()
                serverProcesses.add(process)

                // Create a daemon thread to handle output from this server
                Thread {
                    try {
                        process.inputStream.bufferedReader().use { reader ->
                            var line: String?
                            while (reader.readLine().also { line = it } != null) {
                                println("[$serverName] $line")
                            }
                        }
                    } catch (_: Exception) {
                        // Process was terminated, exit gracefully
                    }
                }.apply {
                    isDaemon = true  // This allows JVM to exit even if thread is still running
                    start()
                }

                globalServerIndex++

                // Small delay between server starts
                Thread.sleep(200)
            }
        }

        println("All servers started. Test flow: $testFlowName is now active.")
        println("Type 'exit' to stop all servers")

        // Handle shutdown more gracefully
        val shutdownHook = Thread {
            println("\nShutting down all test flow servers...")
            serverProcesses.forEach { process ->
                try {
                    process.destroyForcibly()
                    process.waitFor(5, TimeUnit.SECONDS)
                } catch (_: Exception) {
                    // Ignore errors during shutdown
                }
            }
            println("All servers stopped.")
        }
        Runtime.getRuntime().addShutdownHook(shutdownHook)

        // Create a thread to wait for user input
        val inputThread = Thread {
            val scanner = Scanner(System.`in`)
            while (true) {
                try {
                    val input = scanner.nextLine()
                    if (input.trim().lowercase() == "exit") {
                        println("Exit command received, shutting down servers...")
                        serverProcesses.forEach { process ->
                            try {
                                process.destroyForcibly()
                            } catch (_: Exception) {
                                // Ignore errors
                            }
                        }
                        break
                    } else {
                        println("Type 'exit' to stop all servers")
                    }
                } catch (_: Exception) {
                    // Input was interrupted, likely due to shut down
                    break
                }
            }
        }.apply {
            isDaemon = true
            start()
        }

        try {
            // Wait for all processes to complete
            serverProcesses.forEach { process ->
                process.waitFor()
            }
        } catch (_: InterruptedException) {
            println("\nReceived interrupt signal, shutting down...")
            // The shutdown hook will handle cleanup
        } finally {
            // Remove shutdown hook if we're exiting normally
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownHook)
            } catch (_: IllegalStateException) {
                // Shutdown already in progress, ignore
            }
        }
    }
}

tasks.named("runServer").configure {
    mustRunAfter("build")
}

tasks.named("runWithTestFlow").configure {
    mustRunAfter("build")
}
