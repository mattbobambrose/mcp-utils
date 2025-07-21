import buildsrc.convention.versionStr

plugins {
  // Apply the shared build logic from a convention plugin.
  // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
  id("buildsrc.convention.kotlin-jvm")
  `java-library`
  `maven-publish`
  alias(libs.plugins.shadow)
  alias(libs.plugins.versions)
}

dependencies {
  // Project "app" depends on project "utils". (Project paths are separated with ":", so ":utils" refers to the top-level "utils" project.)
//    implementation(project(":shell"))
  implementation(libs.mcp.kotlin)
//    implementation(libs.slf4j)
  implementation(libs.ktor.client.content.negotation)
  implementation(libs.ktor.serialization)
  implementation(libs.openai.java)
  implementation(libs.utils.json)
  testImplementation(libs.kotlin.test)
}

java {
  withSourcesJar()
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      groupId = group.toString()
      artifactId = project.name
      version = versionStr
      from(components["java"] as SoftwareComponent)
    }
  }
}
