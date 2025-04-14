package com.biobox;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;

public class DesktopLauncher {
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Iniciando teste básico do LibGDX...");
        System.out.println("========================================");
        
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Teste Básico de Cor");
        config.setWindowedMode(800, 480);
        config.setForegroundFPS(60);
        
        new Lwjgl3Application(new ColorTest(), config);
    }
    
    // Jogo que apenas muda a cor de fundo quando barra de espaço é pressionada
    public static class ColorTest extends ApplicationAdapter {
        private int colorIndex = 0;
        private float[][] colors = {
            {1, 0, 0, 1},  // Vermelho
            {0, 1, 0, 1},  // Verde
            {0, 0, 1, 1},  // Azul
            {1, 1, 0, 1},  // Amarelo
            {0, 1, 1, 1},  // Ciano
            {1, 0, 1, 1}   // Magenta
        };
        
        @Override
        public void create() {
            System.out.println("Aplicação iniciada com sucesso!");
            System.out.println("Pressione ESPAÇO para mudar a cor");
        }
        
        @Override
        public void render() {
            // Verifica se a barra de espaço foi pressionada
            if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                colorIndex = (colorIndex + 1) % colors.length;
                System.out.println("Cor alterada para índice: " + colorIndex);
            }
            
            // Obtém a cor atual
            float[] currentColor = colors[colorIndex];
            
            // Limpa a tela com a cor atual
            Gdx.gl.glClearColor(currentColor[0], currentColor[1], currentColor[2], currentColor[3]);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        }
    }
}