package com.biobox;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.biobox.ui.ResponsiveWorldEditorUI;

/**
 * Classe principal para a versão responsiva do editor de mundos RPG,
 * otimizada para dispositivos Android.
 */
public class ResponsiveWorldEditorMain extends ApplicationAdapter {
    // Componentes de renderização
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    
    // Componentes do jogo
    private SquareGrid grid;
    private BiomeGenerator worldGenerator;
    private SquareTileRenderer tileRenderer;
    
    // UI responsiva
    private ResponsiveWorldEditorUI editorUI;
    
    // Constantes
    private static final int GRID_WIDTH = 100;
    private static final int GRID_HEIGHT = 70;
    
    @Override
    public void create() {
        try {
            // Inicializar componentes de renderização
            batch = new SpriteBatch();
            shapeRenderer = new ShapeRenderer();
            
            // Criar fonte com tamanho adequado para touch
            font = new BitmapFont();
            font.getData().setScale(1.5f); // Fonte maior para dispositivos móveis
            
            // Inicializar componentes do mundo
            grid = new SquareGrid(GRID_WIDTH, GRID_HEIGHT);
            worldGenerator = new BiomeGenerator();
            tileRenderer = new SquareTileRenderer(grid, shapeRenderer, batch);
            
            // Criar UI responsiva
            editorUI = new ResponsiveWorldEditorUI(
                grid,
                shapeRenderer,
                batch,
                font,
                tileRenderer,
                worldGenerator
            );
            
            // Definir o processador de entrada
            Gdx.input.setInputProcessor(editorUI.getInputProcessor());
            
            // Gerar mundo inicial
            worldGenerator.generateWorld(grid, BiomeGenerator.WorldType.CLASSIC);
            tileRenderer.markDirty();
            
            Gdx.app.log("ResponsiveWorldEditor", "Inicialização concluída com sucesso");
        } catch (Exception e) {
            Gdx.app.error("ResponsiveWorldEditor", "Erro na inicialização", e);
        }
    }
    
    @Override
    public void render() {
        try {
            // Limpar a tela
            Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            
            // Atualizar a UI
            editorUI.update(Gdx.graphics.getDeltaTime(), null);
            
            // Renderizar
            editorUI.render();
        } catch (Exception e) {
            Gdx.app.error("ResponsiveWorldEditor", "Erro na renderização", e);
        }
    }
    
    @Override
    public void resize(int width, int height) {
        try {
            Gdx.app.log("ResponsiveWorldEditor", "Resize: " + width + "x" + height);
            editorUI.resize(width, height);
        } catch (Exception e) {
            Gdx.app.error("ResponsiveWorldEditor", "Erro no resize", e);
        }
    }
    
    @Override
    public void pause() {
        try {
            Gdx.app.log("ResponsiveWorldEditor", "Pause");
        } catch (Exception e) {
            Gdx.app.error("ResponsiveWorldEditor", "Erro no pause", e);
        }
    }
    
    @Override
    public void resume() {
        try {
            Gdx.app.log("ResponsiveWorldEditor", "Resume");
            tileRenderer.markDirty(); // Garantir atualização da textura
        } catch (Exception e) {
            Gdx.app.error("ResponsiveWorldEditor", "Erro no resume", e);
        }
    }
    
    @Override
    public void dispose() {
        try {
            Gdx.app.log("ResponsiveWorldEditor", "Dispose");
            if (batch != null) batch.dispose();
            if (shapeRenderer != null) shapeRenderer.dispose();
            if (font != null) font.dispose();
            if (tileRenderer != null) tileRenderer.dispose();
            if (editorUI != null) editorUI.dispose();
        } catch (Exception e) {
            Gdx.app.error("ResponsiveWorldEditor", "Erro no dispose", e);
        }
    }
}