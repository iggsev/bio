package com.biobox;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Aplicação principal para o gerador de mundos RPG com tiles quadrados
 */
public class RPGWorldGenerator extends ApplicationAdapter {
    private static final int GRID_WIDTH = 100;
    private static final int GRID_HEIGHT = 70;
    private static final int SCREEN_WIDTH = 1280;
    private static final int SCREEN_HEIGHT = 720;

    private SquareGrid grid;
    private BiomeGenerator worldGenerator;
    private SquareTileRenderer tileRenderer;
    private WorldEditor worldEditor;
    private BiomeGenerator.WorldType currentWorldType;

    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;
    private Viewport viewport;

    private boolean showHelp = true;
    private float lastGenerationTime = 0;

    private Vector3 lastMousePos = new Vector3();
    private boolean isDragging = false;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);

        camera = new OrthographicCamera();
        viewport = new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT, camera);
        camera.position.set(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2, 0);
        camera.update();

        grid = new SquareGrid(GRID_WIDTH, GRID_HEIGHT);
        worldGenerator = new BiomeGenerator();
        tileRenderer = new SquareTileRenderer(grid, shapeRenderer, batch);
        worldEditor = new WorldEditor(grid, shapeRenderer, batch, font, tileRenderer, worldGenerator);

        generateNewWorld();

        // Configurar InputMultiplexer
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(worldEditor.getStage());
        multiplexer.addProcessor(new WorldInputProcessor());
        Gdx.input.setInputProcessor(multiplexer);

        centerCamera();
    }

    private void generateNewWorld() {
        long startTime = System.currentTimeMillis();
        currentWorldType = worldGenerator.getRandomWorldType();
        worldGenerator.generateWorld(grid, currentWorldType);
        tileRenderer.markDirty();
        lastGenerationTime = (System.currentTimeMillis() - startTime) / 1000f;
    }

    private void generateWorld(BiomeGenerator.WorldType type) {
        long startTime = System.currentTimeMillis();
        currentWorldType = type;
        worldGenerator.generateWorld(grid, type);
        tileRenderer.markDirty();
        lastGenerationTime = (System.currentTimeMillis() - startTime) / 1000f;
    }

    private void centerCamera() {
        int gridWidthInPixels = GRID_WIDTH * SquareTileRenderer.TILE_SIZE;
        int gridHeightInPixels = GRID_HEIGHT * SquareTileRenderer.TILE_SIZE;
        camera.position.set(gridWidthInPixels / 2f, gridHeightInPixels / 2f, 0);
        camera.zoom = 2f;
        camera.update();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        handleInput();
        camera.update();

        tileRenderer.render(camera);
        worldEditor.render(camera);

        renderUI();
    }

    private void handleInput() {
        if (worldEditor.handleInput(camera)) {
            return; // Editor processou o input
        }
        // Input adicional é tratado pelo WorldInputProcessor via InputMultiplexer
    }

    private void renderUI() {
        batch.begin();
        String fpsText = "FPS: " + Gdx.graphics.getFramesPerSecond();
        font.draw(batch, fpsText, 10, Gdx.graphics.getHeight() - 10);

        String gridText = "Grid: " + (tileRenderer.isShowingGrid() ? "ON (G)" : "OFF (G)");
        font.draw(batch, gridText, Gdx.graphics.getWidth() - 150, Gdx.graphics.getHeight() - 10);

        if (showHelp) {
            drawHelpText();
        } else {
            font.draw(batch, "Press H for help", 10, Gdx.graphics.getHeight() - 30);
        }
        batch.end();
    }

    private void drawHelpText() {
        int y = Gdx.graphics.getHeight() - 70;
        int leftX = 20;

        font.draw(batch, "CONTROLS:", leftX, y);
        y -= 25;
        font.draw(batch, "WASD - Move camera", leftX, y);
        y -= 25;
        font.draw(batch, "QE - Zoom in/out", leftX, y);
        y -= 25;
        font.draw(batch, "Mouse drag - Pan camera", leftX, y);
        y -= 25;
        font.draw(batch, "Mouse wheel - Zoom", leftX, y);
        y -= 25;
        font.draw(batch, "G - Toggle grid", leftX, y);
        y -= 25;
        font.draw(batch, "H - Toggle help", leftX, y);
        y -= 25;

        y = Gdx.graphics.getHeight() - 70;
        int rightX = Gdx.graphics.getWidth() / 2 + 50;
        font.draw(batch, "MAP EDITOR:", rightX, y);
        y -= 25;
        font.draw(batch, "Click on a tile to place selected biome", rightX, y);
        y -= 25;
        font.draw(batch, "Select biomes from the bottom panel", rightX, y);
        y -= 25;
        font.draw(batch, "Choose world type and click Generate Map", rightX, y);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        camera.update();
        worldEditor.setupUI();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        tileRenderer.dispose();
    }

    // Classe interna para processar inputs gerais
    private class WorldInputProcessor extends InputAdapter {
        @Override
        public boolean keyDown(int keycode) {
            float deltaTime = Gdx.graphics.getDeltaTime();
            float speed = 400f * deltaTime * camera.zoom;

            if (keycode == Input.Keys.W) camera.position.y += speed;
            if (keycode == Input.Keys.S) camera.position.y -= speed;
            if (keycode == Input.Keys.A) camera.position.x -= speed;
            if (keycode == Input.Keys.D) camera.position.x += speed;

            if (keycode == Input.Keys.Q) camera.zoom += deltaTime * 2f;
            if (keycode == Input.Keys.E) camera.zoom -= deltaTime * 2f;
            camera.zoom = MathUtils.clamp(camera.zoom, 0.2f, 10f);

            if (keycode == Input.Keys.G) tileRenderer.toggleGrid();
            if (keycode == Input.Keys.H) showHelp = !showHelp;

            return true;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            if (!isDragging) {
                lastMousePos.set(screenX, screenY, 0);
                isDragging = true;
            }
            Vector3 currentPos = new Vector3(screenX, screenY, 0);
            Vector3 delta = currentPos.sub(lastMousePos);
            camera.position.add(-delta.x * camera.zoom, delta.y * camera.zoom, 0);
            lastMousePos.set(currentPos);
            return true;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            isDragging = false;
            return true;
        }

        @Override
        public boolean scrolled(float amountX, float amountY) {
            camera.zoom += amountY * 0.1f;
            camera.zoom = MathUtils.clamp(camera.zoom, 0.2f, 10f);
            return true;
        }
    }
}