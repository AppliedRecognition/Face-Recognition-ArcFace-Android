import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.dokka.gradle.DokkaTaskPartial

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.vanniktech.publish)
    signing
}

version = "1.1.2"

android {
    namespace = "com.appliedrec.verid3.facerecognition.arcface.cloud"
    compileSdk = 36

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
}

dependencies {
    api(project(":arcface-core"))
    api(libs.verid.common)
    implementation(libs.verid.serialization)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.kotlin.serialization)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    testImplementation(libs.junit)
    androidTestImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(libs.face.detection.retinaface)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}


mavenPublishing {
    coordinates("com.appliedrec", "face-recognition-arcface-cloud")
    pom {
        name.set("ArcFace face recognition for Ver-ID")
        description.set("Face recognition implementation for Ver-ID SDK using ArcFace model")
        url.set("https://github.com/AppliedRecognition/Face-Recognition-ArcFace-Android")
        licenses {
            license {
                name.set("Commercial")
                url.set("https://raw.githubusercontent.com/AppliedRecognition/Face-Recognition-ArcFace-Android/main/LICENCE.txt")
            }
        }
        developers {
            developer {
                id.set("appliedrec")
                name.set("Applied Recognition")
                email.set("support@appliedrecognition.com")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/AppliedRecognition/Face-Recognition-ArcFace-Android.git")
            developerConnection.set("scm:git:ssh://github.com/AppliedRecognition/Face-Recognition-ArcFace-Android.git")
            url.set("https://github.com/AppliedRecognition/Face-Recognition-ArcFace-Android")
        }
    }
    publishToMavenCentral(automaticRelease = true)
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

tasks.withType<DokkaTaskPartial>().configureEach {
    moduleName.set("Face recognition ArcFace")
    moduleVersion.set(project.version.toString())
}