plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp") version "2.1.0-1.0.29"
    id("androidx.room")
    kotlin("plugin.serialization") version "2.2.0"
}

android {
    namespace = "ru.gfastg98.qr_scanner_compose"
    compileSdk = 35

    defaultConfig {
        applicationId = "ru.gfastg98.qr_scanner_compose"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    room {
        schemaDirectory("$projectDir/exports")
        generateKotlin = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Activity
    val activityVersion = "1.10.1"
    implementation("androidx.activity:activity-compose:$activityVersion")
    implementation("androidx.activity:activity-ktx:$activityVersion")

    // CameraX
    val cameraXVersion = "1.4.2"
    implementation("androidx.camera:camera-camera2:$cameraXVersion")
    implementation("androidx.camera:camera-core:$cameraXVersion")
    implementation("androidx.camera:camera-extensions:$cameraXVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraXVersion")
    implementation("androidx.camera:camera-view:$cameraXVersion")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2025.06.01"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // accompanist
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.27.0")

    // core
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.1")
    implementation("androidx.navigation:navigation-compose:2.9.0")

    // Gson
    implementation("com.google.code.gson:gson:2.11.0")

    // qr codes
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation("com.github.androidmads:QRGenerator:1.0.1")

    // room
    val roomVersion = "2.7.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation("androidx.room:room-paging:$roomVersion")

    // Koin for Android
    val koinVersion = "4.1.0"
    implementation("io.insert-koin:koin-android:$koinVersion")
    implementation("io.insert-koin:koin-androidx-compose:$koinVersion")
    implementation("io.insert-koin:koin-androidx-workmanager:$koinVersion")

    //serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")

    // BoofCV
    implementation("org.boofcv:boofcv-core:1.2.2")
    implementation("org.boofcv:boofcv-android:1.2.2")

    // Lottie
    implementation("com.airbnb.android:lottie:6.7.1")

    // Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.06.00"))
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
