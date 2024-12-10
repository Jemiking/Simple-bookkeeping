// 项目级别的 build.gradle.kts

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
}

buildscript {
    dependencies {
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.android.gradle.plugin)
    }
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}
