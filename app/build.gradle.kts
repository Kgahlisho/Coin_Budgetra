plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.coin_budgetra"
    compileSdk = 34
    //compileSdk = 34


    defaultConfig {
        applicationId = "com.example.coin_budgetra"
        minSdk = 24
        targetSdk = 34
//        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_17 //version17
        targetCompatibility = JavaVersion.VERSION_17 //version17
    }
    kotlinOptions {
        jvmTarget = "17"
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


    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)



}