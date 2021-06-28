# android-simple-app

A very simple app that launches a web view to a URL. This is designed to be a "starter" app.

# Citrix MAM SDK

To take this simple app and make it managed by Citrix Endpoint Management via the MAM SDK follow these steps.
These steps are taken from https://developer.cloud.com/citrixworkspace/mobile-application-integration/android-native/docs/setting-up-the-library

## Add Properties to build.gradle

Add these properties to your root `build.gradle` file.

```groovy
ext {
    mamSdkLibraryMaven="https://raw.githubusercontent.com/citrix/citrix-mam-sdks/main/maven"
    mamSdkLibraryTools="https://github.com/citrix/citrix-mam-sdks/raw/main/tools/java"
    mamSdkVersion="21.7.0"
    appPackageName="com.terry.androidsimpleapp"
    
    keyStorePath="../mvpntest.keystore"
    keystorePassword="Hello123"
    keyAlias="key0"
    keyPassword="Hello123"
}
```

## Add the MAM SDK repository

Add these properties to your root `build.gradle` file.

```groovy
allprojects {
    repositories {
        maven { url "$rootProject.ext.mamSdkLibraryMaven" }
        google()
        mavenCentral()
    }
}
```

## Add the MAM SDK to your app's gradle file

Add the following to the **dependencies** section in `app/build.gradle` (not the root `build.gradle`):

```groovy
dependencies {
    implementation "com.citrix.android.sdk:mamsdk:${rootProject.ext.mamSdkVersion}"
}
```

## Enable Support for Desugar Bytecode Transformations 

Add the following to the **android -> compileOptions** section in `app/build.gradle` (not the root `build.gradle`):

```groovy
android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
```

## Add ApplicationId

Add the following to the **android -> defaultConfig** section in `app/build.gradle` (not the root `build.gradle`):

```groovy
android {
    defaultConfig {
        applicationId "com.terry.androidsimpleapp"
```

## Add Signing Configuration

Add the following to the **android -> signingConfigs** and **android -> buildTypes** sections in `app/build.gradle` (not the root `build.gradle`):

```groovy
android {
    signingConfigs {
        debug {
            storeFile file(rootProject.ext.keyStorePath)
            storePassword "$rootProject.ext.keystorePassword"
            keyAlias "$rootProject.ext.keyAlias"
            keyPassword "$rootProject.ext.keyPassword"
        }
        release {
            storeFile file(rootProject.ext.keyStorePath)
            storePassword "$rootProject.ext.keystorePassword"
            keyAlias "$rootProject.ext.keyAlias"
            keyPassword "$rootProject.ext.keyPassword"
        }
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
            signingConfig signingConfigs.release
        }
        debug {
            signingConfig signingConfigs.debug
        }
    }
}
```

## Configure Proguard

In your app's `app\build.gradle` file you might have references to Proguard, such as something like this:

```groovy
android {
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

If so then in your Proguard file, such as `proguard-rules.pro`, add the following:

```text
keep class com.citrix.** {*;}
keepattributes Exceptions
```

## (Optional) Add Download Plugin

Add the following to the **plugins** section in `app/build.gradle` (not the root `build.gradle`):

```groovy
plugins {
    id 'com.android.application'
    id "de.undercouch.download" version "4.1.1"
}
```

## Add GenerateMDX task

Add the following in `app/build.gradle` (not the root `build.gradle`):

```groovy
task downloadTools(type: Download, dependsOn: build) {
    src "${rootProject.ext.mamSdkLibraryTools}/managed-app-utility.jar"
    dest "$buildDir/tools/managed-app-utility.jar"
    overwrite false
}

task generateMdx(type: Exec, dependsOn: [downloadTools]) {
    commandLine 'java', '-jar', "$buildDir/tools/managed-app-utility.jar", 'wrap',
            '-in', "$buildDir/outputs/apk/release/${project.name}-release.apk",
            '-out', "$buildDir/outputs/apk/release/${project.name}.mdx",
            '-appType', 'sdkapp',
            '-storeUrl', "https://play.google.com/store/apps/details?id=${rootProject.ext.appPackageName}",
            '-keystore', "${rootProject.ext.keyStorePath}",
            '-storepass', "${rootProject.ext.keystorePassword}",
            '-keyalias', "${rootProject.ext.keyAlias}",
            '-keypass', "${rootProject.ext.keyPassword}"

}

build.finalizedBy generateMdx
```

## Check Your Build

Compile the project and make sure that you don't have any errors. Confirm that you see a MDX file
generated in the `app/build/outputs/apk/release` folder.

