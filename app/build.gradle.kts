plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
}

val appName = "PerfPulse"

android {
  namespace = "com.droid.perfpulse"
  compileSdk = 37
  defaultConfig {
    applicationId = "com.droid.perfpulse"
    minSdk = 26
    targetSdk = 37
    versionCode = 1
    versionName = "1.0"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_FILE")
      if (!keystorePath.isNullOrBlank() && file(keystorePath).exists()) {
        storeFile = file(keystorePath)
        storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "android"
        keyAlias = System.getenv("KEY_ALIAS") ?: "perfpulse"
        keyPassword = System.getenv("KEY_PASSWORD") ?: "android"
      } else {
        val debugKeystore = signingConfigs.getByName("debug").storeFile
        if (debugKeystore != null && debugKeystore.exists()) {
          initWith(signingConfigs.getByName("debug"))
        }
      }
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  buildFeatures {
    compose = true
    aidl = false
    buildConfig = false
    shaders = false
  }

  testOptions {
    unitTests {
      isIncludeAndroidResources = true
      isReturnDefaultValues = true
    }
  }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
}

base {
  archivesName.set(appName)
}

kotlin {
  jvmToolchain(21)
  compilerOptions {
    optIn.add("androidx.compose.material3.ExperimentalMaterial3ExpressiveApi")
    optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
  }
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  testImplementation(composeBom)

  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material3.navigation.suite)
  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Local tests: JUnit, coroutines, Robolectric + Compose UI tests on the JVM
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.androidx.test.core)
  testImplementation(libs.androidx.test.ext.junit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
}
