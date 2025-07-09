package buildsrc.convention

import gradle.kotlin.dsl.accessors._535c4c276e625605c4ded5a017fa2c29.java
import org.gradle.api.Project
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get

fun Project.configurePublishing(versionStr: String) = {
  configure<PublishingExtension> {
    publications {
      create<MavenPublication>("maven") {
        groupId = group.toString()
        artifactId = project.name
        version = versionStr
//        afterEvaluate {
          from(components["java"] as SoftwareComponent)
//        }
      }
//      create<MavenPublication>("release") {
//        groupId = "com.example"
//        artifactId = "mylibrary"
//        version = "1.0.0"
//        // 'afterEvaluate' is not needed in Kotlin DSL for most cases
//        from(components["release"])
//      }
    }
  }
  java {
    withSourcesJar()
  }
}