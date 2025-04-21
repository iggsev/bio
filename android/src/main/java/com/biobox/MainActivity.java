package com.biobox;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.biobox.ui.ResponsiveWorldEditorUI;

public class MainActivity extends AndroidApplication {
    private static final String TAG = "BioBoxApp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
            
            // Utilizar modo imersivo para melhor experiência
            config.useImmersiveMode = true;
            
            // Desabilitar sensores desnecessários para economizar bateria
            config.useAccelerometer = false;
            config.useCompass = false;
            
            // Configurar OpenGL
            config.r = 8;
            config.g = 8;
            config.b = 8;
            config.a = 8;
            config.depth = 16;
            
            // Iniciar com mensagem para debug
            Log.i(TAG, "Iniciando BioBox com UI Responsiva...");
            Toast.makeText(this, "Iniciando BioBox...", Toast.LENGTH_SHORT).show();
            
            // Inicializar o jogo com a versão responsiva
            initialize(new ResponsiveWorldEditorMain(), config);
            
            Log.i(TAG, "BioBox iniciado com sucesso!");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar BioBox", e);
            Toast.makeText(this, "Erro ao iniciar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}