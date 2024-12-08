// app/build.gradle.kts

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true",
                    "room.expandProjection" to "true"
                )
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    
    buildFeatures {
        viewBinding = true
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Material Design
    implementation("com.google.android.material:material:1.10.0")

    // Room
    implementation("androidx.room:room-runtime:${libs.versions.room.runtime.get()}")
    implementation("androidx.room:room-ktx:${libs.versions.room.runtime.get()}")
    kapt("androidx.room:room-compiler:${libs.versions.room.runtime.get()}")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:${libs.versions.navigation.compose.get()}")
    implementation("androidx.navigation:navigation-ui-ktx:${libs.versions.navigation.compose.get()}")

    // Hilt
    implementation("com.google.dagger:hilt-android:${libs.versions.hilt.get()}")
    kapt("com.google.dagger:hilt-android-compiler:${libs.versions.hilt.get()}")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${libs.versions.lifecycle.runtime.ktx.get()}")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${libs.versions.lifecycle.runtime.ktx.get()}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${libs.versions.lifecycle.runtime.ktx.get()}")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}
