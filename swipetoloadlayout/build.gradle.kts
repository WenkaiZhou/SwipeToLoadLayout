plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 14
        targetSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

}

dependencies {
    compileOnly("androidx.core:core:1.7.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.21")
}

// Release maven configuration
setProperty("POM_ARTIFACT_ID", "swipetoloadlayout")
setProperty("POM_NAME", "SwipeToLoadLayout")
setProperty("VERSION_CODE", "3")
setProperty("VERSION_NAME", "2.0.1")
setProperty("POM_DESCRIPTION", "SwipeToLoadLayout")

apply(plugin = "com.vanniktech.maven.publish")