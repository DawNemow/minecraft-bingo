java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.7.2"
}

dependencies {
    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }
}

group = "com.extremelyd1"
version = "1.11.0-fallen.6"
description = "MinecraftBingo"