package com.biobox;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import com.biobox.core.BiomeGenerator;
import com.biobox.core.SquareGrid;
import com.biobox.core.SquareTileRenderer;
import com.biobox.input.InputController;
import com.biobox.input.KeyboardInputController;
import com.biobox.input.TouchInputController;
import com.biobox.platform.PlatformAdapter;
import com.biobox.platform.DesktopPlatformAdapter;
import com.biobox.platform.AndroidPlatformAdapter;
import com.biobox.ui.EditorUI;
import com.biobox.ui.DesktopEditorUI;
import com.biobox.ui.AndroidEditorUI;

/**
 * Classe principal do BioBox - RPG World Generator
 * Detecta automaticamente a plataforma e inicializa os componentes apropriados
 */
public class BioBoxGame extends ApplicationAdapter {
    // Componentes básicos
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    
    // Componentes do jogo
    private SquareGrid grid;
    private BiomeGenerator worldGenerator;
    private SquareTileRenderer tileRenderer;
    
    // Componentes modulares por plataforma
    private boolean isAndroid;
    private PlatformAdapter platformAdapter;
    private InputController inputController;
    private EditorUI editorUI;
    
    // Constantes
    private static final int GRID_WIDTH = 100;
    private static final int GRID_HEIGHT = 70;
    
    @Override
    public void create() {
        // Detectar plataforma
        isAndroid = Gdx.app.getType() == Application.ApplicationType.Android;
        
        // Log inicialização
        Gdx.app.log("BioBox", "Iniciando BioBox em plataforma: " + 
                   (isAndroid ? "Android" : "Desktop"));
        
        try {
            // Inicializar componentes básicos
            batch = new SpriteBatch();
            shapeRenderer = new ShapeRenderer();
            font = new BitmapFont();
            font.getData().setScale(isAndroid ? 1.5f : 1.0f); // Fonte maior no Android
            
            // Inicializar mundo
            grid = new SquareGrid(GRID_WIDTH, GRID_HEIGHT);
            worldGenerator = new BiomeGenerator();
            tileRenderer = new SquareTileRenderer(grid, shapeRenderer, batch);
            
            // Adaptar para a plataforma
            if (isAndroid) {
                platformAdapter = new AndroidPlatformAdapter();
                editorUI = new AndroidEditorUI(grid, shapeRenderer, batch, font, 
                                              tileRenderer, worldGenerator);
                inputController = new TouchInputController(editorUI.getCamera());
            } else {
                platformAdapter = new DesktopPlatformAdapter();
                editorUI = new DesktopEditorUI(grid, shapeRenderer, batch, font, 
                                              tileRenderer, worldGenerator);
                inputController = new KeyboardInputController(editorUI.getCamera());
            }
            
            // Configurar processador de entrada
            Gdx.input.setInputProcessor(inputController.getInputProcessor());
            
            // Gerar mundo inicial
            worldGenerator.generateWorld(grid, BiomeGenerator.WorldType.CLASSIC);
            tileRenderer.markDirty();
            
            // Informar sucesso
            platformAdapter.showMessage("BioBox inicializado com sucesso!");
            Gdx.app.log("BioBox", "Inicialização concluída");
            
        } catch (Exception e) {
            Gdx.app.error("BioBox", "Erro na inicialização", e);
            if (platformAdapter != null) {
                platformAdapter.showError("Erro na inicialização", e.getMessage());
            }
        }
    }
    
    @Override
    public void render() {
        try {
            // Limpar a tela
            Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            
            // Processar input
            inputController.update(Gdx.graphics.getDeltaTime());
            
            // Atualizar UI
            editorUI.update(Gdx.graphics.getDeltaTime(), inputController);
            
            // Renderizar
            editorUI.render();
            
        } catch (Exception e) {
            Gdx.app.error("BioBox", "Erro na renderização", e);
        }
    }
    
    @Override
    public void resize(int width, int height) {
        try {
            Gdx.app.log("BioBox", "Resize: " + width + "x" + height);
            editorUI.resize(width, height);
        } catch (Exception e) {
            Gdx.app.error("BioBox", "Erro no resize", e);
        }
    }
    
    @Override
    public void pause() {
        try {
            Gdx.app.log("BioBox", "Pause");
            // Salvar estado se necessário
        } catch (Exception e) {
            Gdx.app.error("BioBox", "Erro no pause", e);
        }
    }
    
    @Override
    public void resume() {
        try {
            Gdx.app.log("BioBox", "Resume");
            // Restaurar estado se necessário
            tileRenderer.markDirty(); // Garantir atualização da textura
        } catch (Exception e) {
            Gdx.app.error("BioBox", "Erro no resume", e);
        }
    }
    
    @Override
    public void dispose() {
        try {
            Gdx.app.log("BioBox", "Dispose");
            if (batch != null) batch.dispose();
            if (shapeRenderer != null) shapeRenderer.dispose();
            if (font != null) font.dispose();
            if (tileRenderer != null) tileRenderer.dispose();
            if (editorUI != null) editorUI.dispose();
        } catch (Exception e) {
            Gdx.app.error("BioBox", "Erro no dispose", e);
        }
    }
}
