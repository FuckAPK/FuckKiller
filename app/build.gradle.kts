import java.util.Properties

plugins {
    id("com.android.application")
    id("kotlin-android")
}
android {
    namespace = "org.baiyu.fuckkiller"
    compileSdk = 34
    defaultConfig {
        applicationId = "org.baiyu.fuckkiller"
        minSdk = 26
        targetSdk = 34
        versionCode = 8
        versionName = "3.0"
        resourceConfigurations += "en"
        vectorDrawables.useSupportLibrary = true
    }
    signingConfigs {
        create("release") {
            val properties = Properties().apply {
                load(File("signing.properties").reader())
            }
            storeFile = File(properties.getProperty("storeFilePath"))
            storePassword = properties.getProperty("storePassword")
            keyPassword = properties.getProperty("keyPassword")
            keyAlias = properties.getProperty("keyAlias")
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    compileOnly("de.robv.android.xposed:api:82")
    implementation("androidx.preference:preference-ktx:1.2.1")
}