//package buildsrc.convention
//
//import org.gradle.api.Project
//import org.gradle.api.component.SoftwareComponent
//import org.gradle.api.publish.PublishingExtension
//import org.gradle.api.publish.maven.MavenPublication
//import org.gradle.kotlin.dsl.configure
//import org.gradle.kotlin.dsl.create
//import org.gradle.kotlin.dsl.get
//
//fun Project.configurePublishing(versionStr: String) = {
//  configure<PublishingExtension> {
//    publications {
//      create<MavenPublication>("maven") {
//        groupId = group.toString()
//        artifactId = project.name
//        version = versionStr
////        afterEvaluate {
//        from(components["java"] as SoftwareComponent)
////        }
//      }
//    }
//  }
//  java {
//    withSourcesJar()
//  }
//}