import java.net.URI
import org.gradle.internal.os.OperatingSystem

plugins {
    application
    kotlin("jvm") version "1.6.10"
}

group = "com.octobeard"
version = "1.0-SNAPSHOT"
val lwjglVersion = "3.3.1"
val libp5xVersion = "0.353.0-beta-3"
val osNatives = "natives-macos"
//val osNatives = "natives-linux"
//val osNatives = "natives-linux-arm64"


repositories {
    mavenCentral()
    maven { url = URI("https://jitpack.io") }
}

dependencies {
    implementation(kotlin("stdlib"))
    // import processing4 beta 6 library via jitpack maven mirror
    // https://github.com/codelerity/libp5x-examples/
    implementation("org.praxislive.libp5x:processing-core:$libp5xVersion")
    implementation("org.praxislive.libp5x:processing-lwjgl:$libp5xVersion")
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    implementation(fileTree(mapOf("dir" to "libs/lwjgl-release-3.3.1", "include" to listOf("*.jar"))))

    implementation("org.lwjgl:lwjgl")
    implementation("org.lwjgl:lwjgl-assimp")
    implementation("org.lwjgl:lwjgl-egl")
    implementation("org.lwjgl:lwjgl-glfw")
    implementation("org.lwjgl:lwjgl-opengl")
    implementation("org.lwjgl:lwjgl-opengles")
    implementation("org.lwjgl:lwjgl-stb")
    runtimeOnly("org.lwjgl:lwjgl::$osNatives")
    runtimeOnly("org.lwjgl:lwjgl-assimp::$osNatives")
    runtimeOnly("org.lwjgl:lwjgl-glfw::$osNatives")
    runtimeOnly("org.lwjgl:lwjgl-opengl::$osNatives")
    runtimeOnly("org.lwjgl:lwjgl-opengles::$osNatives")
    runtimeOnly("org.lwjgl:lwjgl-stb::$osNatives")

    // import HYPE jar in libs directory 2.x
    implementation(fileTree(mapOf("dir" to "libs/hype", "include" to listOf("*.jar"))))
    // import minim library 2.2.2
    implementation(fileTree(mapOf("dir" to "libs/minim", "include" to listOf("*.jar"))))
}

application {
    mainClass.set("MainKt")
    applicationDefaultJvmArgs = listOf(
        "-Djava.awt.headless=true",
        "-XstartOnFirstThread",
        "-XX:+IgnoreUnrecognizedVMOptions"
    )
}