plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.dresscatalog"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.dresscatalog"
        minSdk = 28
        targetSdk = 36
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
    // твои стандартные зависимости из version catalog
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // 🔽🔽🔽 ДОБАВЛЕНО: Retrofit + Gson-конвертер
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // 🔽🔽🔽 ДОБАВЛЕНО: OkHttp + логгер запросов
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // (на будущее, если будешь делать список платьев в RecyclerView)
    // implementation("androidx.recyclerview:recyclerview:1.3.2")
}
