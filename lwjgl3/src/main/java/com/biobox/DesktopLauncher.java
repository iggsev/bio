package com.biobox;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Biobox Map Editor");
        config.setWindowedMode(1280, 720);
        config.setForegroundFPS(60);
        
        // Importante para evitar flickering
        config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 0);
        config.useVsync(true);
        
        // Opções de linha de comando
        boolean fullscreen = false;
        for (String arg : args) {
            if (arg.equals("--fullscreen")) {
                fullscreen = true;
            }
        }
        
        if (fullscreen) {
            config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
        }
        
        // Iniciar a aplicação
        new Lwjgl3Application(new MapEditorScreen(), config);
    }
}