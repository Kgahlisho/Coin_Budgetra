plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    //this is for the database
    kotlin("kapt")
}

android {
    namespace = "com.example.coin_budgetra"
    compileSdk = 35


    defaultConfig {
        applicationId = "com.example.coin_budgetra"
        minSdk = 24
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
    kotlinOptions {
        jvmTarget = "11"
    }
}
dependencies {
    // this Kotlin BOM — will prevent the duplicate kotlin-stdlib classes (fixes checkDebugDuplicateClasses)
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:2.0.21"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


//this is for the database
    implementation ("androidx.room:room-runtime:2.6.1")
    implementation ("androidx.room:room-ktx:2.6.1")

    kapt("androidx.room:room-compiler:2.6.1")

}
