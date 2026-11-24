// Mistly Client for Forge 1.8.9: built with architectury-loom (Essential's fork), as the
// Forge1.8.9Template does, so it needs no ForgeGradle 2 nor Gradle 2. The code is Java 8.

plugins {
    idea
    java
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
}

val mcVersion: String by project
val modid: String by project

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

loom {
    runConfigs {
        "client" {
            if (org.apache.commons.lang3.SystemUtils.IS_OS_MAC_OSX) {
                vmArgs.remove("-XstartOnFirstThread")
            }
        }
        remove(getByName("server"))
    }
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
    }
}

sourceSets.main {
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
}

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")
}

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("mcversion", mcVersion)
    inputs.property("modid", modid)
    filesMatching("mcmod.info") {
        expand(inputs.properties)
    }
}

// misty-client-<version>+mc1.8.9.jar, named like the jars of the Fabric versions
tasks.jar {
    archiveClassifier.set("dev")
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
    // the Misty License in every jar, as in the Fabric versions
    from(file("../LICENSE.txt")) {
        rename { "LICENSE_misty-client.txt" }
    }
}

tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    archiveBaseName.set("misty-client")
    archiveVersion.set("${project.version}+mc$mcVersion")
    archiveClassifier.set("")
}
