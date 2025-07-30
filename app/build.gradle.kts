plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

import java.util.Properties

android {
    namespace = "now.link"
    compileSdk = 36

    defaultConfig {
        applicationId = "now.link.ulmaridae"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Optimize for size - disable multidex if not needed
        multiDexEnabled = false
    }

    // Configure ABI splits to build separate APKs for each architecture
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a")
            isUniversalApk = false  // Set to true if you also want a universal APK
        }
    }

    // Android resources configuration
    androidResources {
        // Keep only essential locales
        localeFilters += listOf("en", "zh-rCN")
    }

    // Enable R8 full mode
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.16"
    }

    // Lint configuration
    lint {
        abortOnError = false
        checkReleaseBuilds = false
        // Create a baseline for lint issues
        baseline = file("lint-baseline.xml")
    }

    // Compiler optimizations
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = "11"
        // Additional Kotlin compiler optimizations
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all"
        )
    }

    // Packaging options for R8
    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/*.kotlin_module"
            )
        }
    }

    signingConfigs {
        create("release") {
            // Load signing properties from file
            val signingPropsFile = file("../signing.properties")
            val signingProps = Properties()
            
            if (signingPropsFile.exists()) {
                signingProps.load(signingPropsFile.inputStream())
            }
            
            // Helper function to get property with fallbacks: env var -> properties file -> gradle property
            fun getSigningProperty(key: String): String? {
                return System.getenv(key) 
                    ?: signingProps.getProperty(key) 
                    ?: project.findProperty(key)?.toString()
            }
            
            val keystorePassword = getSigningProperty("KEYSTORE_PASSWORD")
            val keyAlias = getSigningProperty("KEY_ALIAS")
            val keyPassword = getSigningProperty("KEY_PASSWORD")
            
            // Only configure signing if we have the required properties
            if (!keystorePassword.isNullOrEmpty() &&
                !keyAlias.isNullOrEmpty() && !keyPassword.isNullOrEmpty()) {
                
                val keystoreFilePath = file("../keystore.jks")
                if (keystoreFilePath.exists() || System.getenv("KEYSTORE_FILE") != null) {
                    storeFile = keystoreFilePath
                    storePassword = keystorePassword
                    this.keyAlias = keyAlias
                    this.keyPassword = keyPassword
                }
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "r8-rules.pro"
            )
            // Additional R8 optimizations
            isDebuggable = false
            isJniDebuggable = false
            renderscriptOptimLevel = 3

            // Enable R8 full mode optimizations
            proguardFiles += file("consumer-rules.pro")
            
            // Use signing config if available
            if (signingConfigs.getByName("release").storeFile != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.activity)
    implementation(libs.androidx.compose.viewmodel)
    implementation(libs.androidx.compose.livedata)
    implementation(libs.androidx.compose.navigation)
    implementation(libs.accompanist.permissions)
    
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}