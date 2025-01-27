java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.14"
}

dependencies {
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }
}

group = "com.extremelyd1"
version = "1.11.0-fallen.7"
description = "MinecraftBingo"