import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.3.20-Beta1"
    id("com.gradleup.shadow") version "8.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "net.integr"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        name = "spigotmc-repo"
    }
    maven("https://repo.extendedclip.com/releases/")
}

dependencies {
    implementation("org.spigotmc:spigot-api:1.21.11-R0.1-SNAPSHOT")

    implementation("me.clip:placeholderapi:2.12.2")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(kotlin("scripting-jvm"))
    implementation(kotlin("scripting-common"))
    implementation(kotlin("scripting-jvm-host"))
    implementation(kotlin("scripting-dependencies"))

    implementation(kotlin("compiler-embeddable"))

    implementation("org.apache.ivy:ivy:2.5.2")

    // The core engine
    implementation("tools.jackson.core:jackson-databind:3.0.4")
    implementation("tools.jackson.dataformat:jackson-dataformat-yaml:3.0.4")
    implementation("tools.jackson.module:jackson-module-kotlin:3.0.4")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.mockito:mockito-core:5.2.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.2.0")

}

tasks {
    runServer {
        minecraftVersion("1.21.11")
    }
}

tasks.test {
    useJUnitPlatform()
    environment("EXEC_CONTEXT", "test")
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.withType<ShadowJar> {
    mergeServiceFiles()
}
