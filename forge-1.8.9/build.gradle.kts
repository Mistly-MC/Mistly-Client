// Mistly Client for Forge 1.8.9: built with architectury-loom (Essential's fork), as the
// Forge1.8.9Template does, so it needs no ForgeGradle 2 nor Gradle 2. The code is Java 8.
// It hooks into the game with mixins (src/main/resources/mixins.mistlyclient.json): Mixin is
// shipped inside the jar, so the players need nothing more.

plugins {
    idea
    java
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val mcVersion: String by project
val modid: String by project

// the version of the releases, the same as the Fabric versions' (mod.version of stonecutter.properties.toml)
version = file("../stonecutter.properties.toml").readLines()
    .first { it.trim().startsWith("mod.version") }
    .substringAfter("\"").substringBefore("\"")

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

loom {
    // the game started through Mixin's tweaker, as from the jar's manifest
    launchConfigs {
        "client" {
            arg("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker")
        }
    }
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
        mixinConfig("mixins.$modid.json")
    }
    @Suppress("UnstableApiUsage")
    mixin {
        defaultRefmapName.set("mixins.$modid.refmap.json")
    }
}

sourceSets.main {
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
}

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven/")
}

// what goes inside the jar: Mixin
val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")
    // Mixin 0.7, the one the mods of this version ship (Java 8, LaunchWrapper); the 0.8
    // processor writes the refmap (the game's obfuscated names of what the mixins target)
    shadowImpl("org.spongepowered:mixin:0.7.11-SNAPSHOT") {
        isTransitive = false
    }
    annotationProcessor("org.spongepowered:mixin:0.8.5-SNAPSHOT")
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

// Forge starts Mixin through its tweaker, and still loads the jar as a mod
tasks.withType(Jar::class) {
    manifest.attributes(
        mapOf(
            "FMLCorePluginContainsFMLMod" to "true",
            "ForceLoadAsMod" to "true",
            "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
            "MixinConfigs" to "mixins.$modid.json",
        )
    )
}

// misty-client-<version>+mc1.8.9.jar, named like the jars of the Fabric versions
tasks.jar {
    archiveClassifier.set("dev")
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
}

// the mod with Mixin inside, then remapped to the game's names
tasks.shadowJar {
    archiveClassifier.set("with-deps")
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
    configurations = listOf(shadowImpl)
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    // the Misty License in every jar, as in the Fabric versions
    from(file("../LICENSE.txt")) {
        rename { "LICENSE_misty-client.txt" }
    }
}

tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    input.set(tasks.shadowJar.flatMap { it.archiveFile })
    archiveBaseName.set("misty-client")
    archiveVersion.set("${project.version}+mc$mcVersion")
    archiveClassifier.set("")
}
