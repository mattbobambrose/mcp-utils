plugins {
  // Apply the shared build logic from a convention plugin.
  // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
  id("buildsrc.convention.kotlin-jvm")
  // Apply Kotlin Serialization plugin from `gradle/libs.versions.toml`.
//    alias(libs.plugins.kotlinPluginSerialization)
  `java-library`
  `maven-publish`
  alias(libs.plugins.shadow)
  alias(libs.plugins.versions)
}

val versionStr = "0.1.1-SNAPSHOT"

group = "com.github.mattbobambrose.mcp_utils"
version = versionStr

repositories {
  mavenCentral()
  maven { url = uri("https://jitpack.io") }
}

dependencies {
  // Apply the kotlinx bundle of dependencies from the version catalog (`gradle/libs.versions.toml`).
  implementation(libs.bundles.kotlinxEcosystem)
  implementation(libs.mcp.kotlin)
//    implementation(libs.slf4j)
  implementation(libs.ktor.client.content.negotation)
  implementation(libs.ktor.serialization)
  implementation(libs.openai.java)
  implementation(libs.utils.json)
  testImplementation(kotlin("test"))
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      groupId = group.toString()
      artifactId = project.name
      version = versionStr
      from(components["java"])
    }
  }
}

java {
  withSourcesJar()
}
