plugins {
  kotlin("jvm")
}

group = "com.mattbobambrose.mcp_utils.examples"
version = "unspecified"

//repositories {
//  mavenCentral()
//}

dependencies {
  testImplementation(kotlin("test"))
}

tasks.test {
  useJUnitPlatform()
}
//kotlin {
//  jvmToolchain(17)
//}