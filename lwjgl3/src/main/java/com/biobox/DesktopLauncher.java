package com.biobox;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("RPG World Generator");
        config.setWindowedMode(1280, 720);
        config.setForegroundFPS(60);
        config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 0);
        config.useVsync(true);
        
        // Create and run the application
        new Lwjgl3Application(new RPGWorldGenerator(), config);
    }
}