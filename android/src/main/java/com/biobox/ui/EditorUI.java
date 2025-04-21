package com.biobox.ui;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.biobox.input.InputController;

/**
 * Interface para UI do editor
 * Define métodos para renderização e interação com a interface do usuário
 */
public interface EditorUI {
    
    /**
     * Atualiza o estado da UI
     * @param deltaTime tempo desde a última atualização
     * @param inputController controlador de entrada
     */
    void update(float deltaTime, InputController inputController);
    
    /**
     * Renderiza a UI
     */
    void render();
    
    /**
     * Trata redimensionamento da janela/tela
     * @param width nova largura
     * @param height nova altura
     */
    void resize(int width, int height);
    
    /**
     * Libera recursos
     */
    void dispose();
    
    /**
     * Retorna a câmera do mapa
     * @return câmera
     */
    OrthographicCamera getCamera();
    
    /**
     * Alterna a visualização da grade
     */
    void toggleGrid();
    
    /**
     * Verifica se a grade está visível
     * @return true se a grade estiver visível
     */
    boolean isGridVisible();
    
    /**
     * Alterna entre os modos do editor
     * @param mode índice do modo (0: editor de terreno, 1: gerador de mundo)
     */
    void setEditorMode(int mode);
    
    /**
     * Obtém o modo atual do editor
     * @return índice do modo atual
     */
    int getEditorMode();
    
    /**
     * Gera um novo mundo
     * @param worldType tipo de mundo a ser gerado
     */
    void generateWorld(int worldType);
    
    /**
     * Verifica se o ponto está na área do mapa
     * @param x coordenada X na tela
     * @param y coordenada Y na tela
     * @return true se o ponto estiver na área do mapa
     */
    boolean isInMapArea(float x, float y);
    
    /**
     * Obtém a largura da tela de UI
     * @return largura da tela de UI
     */
    float getUIWidth();
    
    /**
     * Obtém a altura da tela de UI
     * @return altura da tela de UI
     */
    float getUIHeight();
}
