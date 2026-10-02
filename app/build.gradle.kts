plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.pharaohparty.app"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.pharaohparty.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"
    }
    buildFeatures { compose = true; buildConfig = true }
    buildTypes { release { isMinifyEnabled = false; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro") } }
    buildConfigField("String", "SUPABASE_URL", "\"https://fdvgpdfesvigtvmzjhnt.supabase.co\"")
    buildConfigField("String", "SUPABASE_KEY", "\"sb_publishable_gW5LE2T9Y6rfv63DMHnYew_64sa1cOL\"")
    buildConfigField("String", "AUTH_FUNCTION", "\"pharaoh-auth\"")
    buildConfigField("String", "LIVEKIT_TOKEN_FUNCTION", "\"livekit-token\"")
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.09.00"))
    implementation("androidx.activity:activity-compose:1.12.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.datastore:datastore-preferences:1.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("io.ktor:ktor-client-android:3.3.1")
    implementation("io.ktor:ktor-client-content-negotiation:3.3.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.1")
    implementation("io.livekit:livekit-android:2.29.0")
    implementation("io.github.jan-tennert.supabase:bom:3.5.0")
    implementation("io.github.jan-tennert.supabase:realtime-kt")
}