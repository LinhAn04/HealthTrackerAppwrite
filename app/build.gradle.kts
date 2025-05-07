plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud"
    compileSdk = 35

    defaultConfig {
        applicationId = "hcmute.edu.vn.lehoanglinhan.healthtrackerappwritecloud"
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("io.appwrite:sdk-for-android:7.0.0")



    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.activity:activity:1.8.0")// Cho Activity Result API
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

}