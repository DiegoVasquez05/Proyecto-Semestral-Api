plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Datos de la llave de firma del APK release. Se leen de variables de entorno para no
// guardar contraseñas en el repositorio (en GitHub Actions vienen de los "secrets").
val rutaLlaveDeFirma = providers.environmentVariable("RACKPRO_KEYSTORE_PATH").orNull
val contrasenaLlaveDeFirma = providers.environmentVariable("RACKPRO_KEYSTORE_PASSWORD").orNull
val aliasLlaveDeFirma = providers.environmentVariable("RACKPRO_KEY_ALIAS").orNull
val contrasenaAliasDeFirma = providers.environmentVariable("RACKPRO_KEY_PASSWORD").orNull

// Dirección de la API. Se puede cambiar en gradle.properties (rackpro.apiUrl).
val urlApi = providers.gradleProperty("rackpro.apiUrl").get()

android {
    namespace = "com.example.proyectov2"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.proyectov2"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "API_URL", "\"$urlApi\"")
    }

    signingConfigs {
        create("release") {
            if (rutaLlaveDeFirma != null) {
                storeFile = file(rutaLlaveDeFirma)
                storePassword = contrasenaLlaveDeFirma
                keyAlias = aliasLlaveDeFirma
                keyPassword = contrasenaAliasDeFirma
            }
        }
    }

    buildTypes {
        release {
            // Solo se firma si existe la llave (en GitHub Actions o en un equipo configurado).
            if (rutaLlaveDeFirma != null) {
                signingConfig = signingConfigs.getByName("release")
            }
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}