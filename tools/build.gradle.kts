plugins {
  // Apply the shared build logic from a convention plugin.
  // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
  id("buildsrc.convention.kotlin-jvm")

  // Apply the Application plugin to add support for building an executable JVM application.
  `java-library`
  `maven-publish`
  alias(libs.plugins.shadow)
  alias(libs.plugins.versions)
}

repositories {
  mavenCentral()
  maven { url = uri("https://jitpack.io") }
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

//application {
//    // Define the Fully Qualified Name for the application main class
//    // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
//    mainClass = "org.example.app.AppKt"
//}
