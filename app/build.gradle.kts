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
    packagingOptions {
        exclude("META-INF/LICENSE.md")
        exclude("META-INF/NOTICE.md")
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/*.kotlin_module")  // Nếu sử dụng Kotlin
        exclude("META-INF/ASL2.0")
        exclude("META-INF/*.version")
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

    implementation ("com.google.android.gms:play-services-fitness:21.2.0")
    implementation ("com.google.android.gms:play-services-auth:20.0.1")
    implementation ("androidx.room:room-runtime:2.6.1")
    annotationProcessor ("androidx.room:room-compiler:2.6.1")
    implementation ("com.google.android.gms:play-services-maps:19.2.0")
    implementation ("com.google.android.gms:play-services-location:21.3.0")
    implementation ("com.mikhaellopez:circularprogressbar:3.1.0")
    implementation ("com.sun.mail:android-mail:1.6.6")
    implementation ("com.sun.mail:android-activation:1.6.6")

    implementation ("com.google.api-client:google-api-client-android:1.23.0")
    implementation ("com.google.apis:google-api-services-gmail:v1-rev110-1.25.0")
    implementation ("com.google.oauth-client:google-oauth-client-jetty:1.23.0")
    implementation ("com.google.http-client:google-http-client-gson:1.41.0")

}