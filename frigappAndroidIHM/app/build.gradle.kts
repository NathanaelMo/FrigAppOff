plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("org.openapi.generator") version "7.4.0"
}

android {
    namespace = "com.monnier.frigapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.monnier.frigapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

afterEvaluate {
    tasks.named("compileDebugKotlin") {
        dependsOn("openApiGenerate")
    }
    tasks.named("compileReleaseKotlin") {
        dependsOn("openApiGenerate")
    }
}

openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set("$rootDir/openapi_frigapp_v1.0.yaml")
    outputDir.set("${projectDir}/build/generated/openapi")
    apiPackage.set("com.generate.frigapp.api")
    modelPackage.set("com.generate.frigapp.model")
    invokerPackage.set("com.generate.frigapp.network")
    configOptions.set(mapOf(
        "library" to "jvm-retrofit2",
        "dateLibrary" to "java8",
        "useCoroutines" to "true",
        "serializationLibrary" to "moshi"
    ))
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("androidx.compose.material:material:1.6.0")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")

    // DataStore — persistance sécurisée du JWT
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Coroutines — nécessaire pour les appels suspend dans les repositories
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // CameraX — flux caméra pour le scan de code-barres
    implementation("androidx.camera:camera-core:1.4.1")
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")

    // ML Kit — détection de codes-barres EAN
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    // Coil — affichage des images produits (Open Food Facts)
    implementation("io.coil-kt:coil-compose:2.7.0")

}