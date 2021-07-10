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
    mamSdkVersion="21.6.0.+"
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

## Add a new TunnelHandler class

There are multiple ways to proceed, but we'll be following the model that the sample app source code
does. We create a `TunnelHandler` class to hold the `handleMessage` logic.

```java
package com.terry.androidsimpleapp;

import android.os.Message;
import com.citrix.mvpn.api.MvpnDefaultHandler;

public class TunnelHandler extends MvpnDefaultHandler {
    private final Callback callback;

    public interface Callback {
        void onTunnelStarted();
        void onError(boolean isSessionExpired);
    }

    public TunnelHandler(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        if (callback != null) {
            if (isNetworkTunnelRunning()) {
                callback.onTunnelStarted();
            } else {
                callback.onError(isSessionExpired());
            }
        }
    }
}
```

## Modify MainActivity

We're going to modify the `MainActivity` with some code to start the tunnel when the view gets created.
We start by creating a variable for the `TunnelHandler` that we created, but as a 
`MvpnDefaultHandler` type.

```java
    private MvpnDefaultHandler mvpnHandler;
```

We then modify `onCreate` and add the following:

```java
        if (mvpnHandler == null) {
            mvpnHandler = new TunnelHandler(this);
        }
        Log.i(TAG, "Before calling startTunnel()");
        try {
            MicroVPNSDK.startTunnel(this, new Messenger(mvpnHandler));
        } catch (Exception e) {
            Log.e(TAG, "Failed to start tunnel: " + e.getMessage());
        }
```

Now we need to implement an interface:

```java
public class MainActivity extends AppCompatActivity implements TunnelHandler.Callback {
```

And then fill in the missing method implementations:

```java
    @Override
    public void onTunnelStarted() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Started tunnel!", Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onError(boolean isSessionExpired) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Error with tunnel!", Toast.LENGTH_LONG).show();
        });
    }
```

## Modify WebView

In this example we use a WebView, and so we'll be modifying the code that creates the WebView.

```
    WebView webView = findViewById(R.id.idWebView);
    try {
        WebViewClient webviewClient = new WebViewClient();
        webView.setWebViewClient(webviewClient);
        webView = MicroVPNSDK.enableWebViewObjectForNetworkTunnel(this, webView, webviewClient);
        webView.loadUrl(url);
    } catch(NetworkTunnelNotStartedException nse) {
        Log.e(TAG, "TunnelNotStarted: " + nse.getMessage());
    } catch(MvpnException e) {
        Log.e(TAG, "Mvpn Error: " + e.getMessage());
    }
```

## Build and test on a device with Secure Hub

Compile your code and run it on an actual Android device that has been enrolled.

