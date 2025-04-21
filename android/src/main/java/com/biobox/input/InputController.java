package com.biobox.input;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;

/**
 * Interface para controladores de entrada
 * Define métodos para processamento de entrada do usuário
 */
public interface InputController {
    
    /**
     * Atualiza o estado do controlador
     * @param deltaTime tempo desde a última atualização
     */
    void update(float deltaTime);
    
    /**
     * Retorna o processador de entrada para LibGDX
     * @return processador de entrada
     */
    InputProcessor getInputProcessor();
    
    /**
     * Retorna a câmera controlada
     * @return câmera
     */
    OrthographicCamera getCamera();
    
    /**
     * Verifica se uma tecla específica está pressionada
     * @param keyCode código da tecla
     * @return true se a tecla estiver pressionada
     */
    boolean isKeyPressed(int keyCode);
    
    /**
     * Retorna a posição do cursor/toque em coordenadas de tela
     * @return posição x,y
     */
    Vector2 getCursorPosition();
    
    /**
     * Converte coordenadas de tela para coordenadas de mundo
     * @param screenX coordenada X na tela
     * @param screenY coordenada Y na tela
     * @return coordenadas de mundo
     */
    Vector2 screenToWorld(float screenX, float screenY);
    
    /**
     * Verifica se houve toque/clique
     * @return true se houve toque/clique
     */
    boolean isTouched();
    
    /**
     * Obtém o delta de scroll/zoom
     * @return valor do delta (positivo para zoom in, negativo para zoom out)
     */
    float getZoomDelta();
    
    /**
     * Redefine o estado do controlador
     */
    void reset();
}
