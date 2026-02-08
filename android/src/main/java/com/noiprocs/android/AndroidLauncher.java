package com.noiprocs.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.noiprocs.LibGDXApp;

/** Launches the Android application. */
public class AndroidLauncher extends AndroidApplication {
    private static final String PLATFORM = "android";
    private static final String TYPE = "client";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true; // Recommended, but not required.

        // Get parameters from intent or use defaults
        String username = "player";
        String hostname = "192.168.50.49";
        int port = 8080;

        // Create Android-specific touch input controller
        TouchInputController inputController = new TouchInputController();

        initialize(
            new LibGDXApp(PLATFORM, username, TYPE, hostname, port, inputController),
            configuration);
    }
}
