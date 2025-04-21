package com.biobox;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class MainActivity extends AndroidApplication {
    private static final String TAG = "BioBoxApp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
            config.useImmersiveMode = true;
            config.useAccelerometer = false;
            config.useCompass = false;
            
            // Configure OpenGL
            config.r = 8;
            config.g = 8;
            config.b = 8;
            config.a = 8;
            config.depth = 16;
            
            // Iniciar com mensagem para debug
            Log.i(TAG, "Iniciando BioBox...");
            Toast.makeText(this, "Iniciando BioBox...", Toast.LENGTH_SHORT).show();
            
            // Use o WorldEditorMain principal em vez da versão de teste
            initialize(new WorldEditorMain(), config);
            
            Log.i(TAG, "BioBox iniciado com sucesso!");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar BioBox", e);
            Toast.makeText(this, "Erro ao iniciar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}