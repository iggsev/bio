package com.biobox;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/**
 * Classe responsável por renderizar o mapa e as entidades com suporte toroidal (wraparound)
 */
public class MapRenderer {
    private final GameMap map;
    private final int tileSize;
    private final ShapeRenderer shapeRenderer;
    
    // Configuração de renderização
    private boolean useLOD = true;
    private boolean showDebugInfo = false;
    
    public MapRenderer(GameMap map, int tileSize, ShapeRenderer shapeRenderer) {
        this.map = map;
        this.tileSize = tileSize;
        this.shapeRenderer = shapeRenderer;
    }
    
    public void setUseLOD(boolean useLOD) {
        this.useLOD = useLOD;
    }
    
    public boolean isUsingLOD() {
        return useLOD;
    }
    
    public void setShowDebugInfo(boolean showDebugInfo) {
        this.showDebugInfo = showDebugInfo;
    }
    
    /**
     * Renderiza o mapa com suporte a wraparound nas bordas
     */
    public void renderMap(OrthographicCamera camera) {
        // Configurar o shape renderer com a projeção da câmera
        shapeRenderer.setProjectionMatrix(camera.combined);
        
        // Se o zoom estiver muito longe, use LOD sempre para evitar flickering
        boolean forceLOD = camera.zoom > 2.5f;
        boolean shouldUseLOD = useLOD || forceLOD;
        
        if (shouldUseLOD) {
            renderMapLOD(camera);
        } else {
            renderMapDetailed(camera);
        }
        
        // Desenhar grade (apenas quando o zoom estiver próximo o suficiente)
        if (camera.zoom < 1.0f) {
            drawGrid(camera);
        }
    }
    
    /**
     * Renderiza o mapa em detalhe (cada tile individualmente)
     */
    private void renderMapDetailed(OrthographicCamera camera) {
        int mapWidth = map.getWidth();
        int mapHeight = map.getHeight();
        
        // Calcular a área visível considerando o wraparound
        // Vamos renderizar tiles até uma "borda virtual" além do mapa
        float viewportWidth = camera.viewportWidth * camera.zoom;
        float viewportHeight = camera.viewportHeight * camera.zoom;
        
        int startX = Math.max(0, (int)((camera.position.x - viewportWidth/2) / tileSize) - 1);
        int endX = Math.min(mapWidth * 2, (int)((camera.position.x + viewportWidth/2) / tileSize) + 2);
        int startY = Math.max(0, (int)((camera.position.y - viewportHeight/2) / tileSize) - 1);
        int endY = Math.min(mapHeight * 2, (int)((camera.position.y + viewportHeight/2) / tileSize) + 2);
        
        // Iniciar renderização de formas
        shapeRenderer.begin(ShapeType.Filled);
        
        // Desenhar tiles visíveis
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                // Aplicar transformação toroidal para as coordenadas do tile
                int wrappedX = (x % mapWidth + mapWidth) % mapWidth;
                int wrappedY = (y % mapHeight + mapHeight) % mapHeight;
                
                // Obter a cor do tile
                Color tileColor = map.getTileColor(wrappedX, wrappedY);
                shapeRenderer.setColor(tileColor);
                
                // Desenhar o tile na posição real (considerando wraparound)
                shapeRenderer.rect(x * tileSize, y * tileSize, tileSize, tileSize);
            }
        }
        
        shapeRenderer.end();
    }
    
    /**
     * Renderiza o mapa em LOD (Level of Detail) para melhor performance
     */
    private void renderMapLOD(OrthographicCamera camera) {
        int mapWidth = map.getWidth();
        int mapHeight = map.getHeight();
        int regionSize = map.getRegionSize();
        
        // Calcular regiões visíveis
        float viewportWidth = camera.viewportWidth * camera.zoom;
        float viewportHeight = camera.viewportHeight * camera.zoom;
        
        int startRegionX = Math.max(0, (int)((camera.position.x - viewportWidth/2) / (tileSize * regionSize)) - 1);
        int endRegionX = Math.min(mapWidth / regionSize * 2, (int)((camera.position.x + viewportWidth/2) / (tileSize * regionSize)) + 2);
        int startRegionY = Math.max(0, (int)((camera.position.y - viewportHeight/2) / (tileSize * regionSize)) - 1);
        int endRegionY = Math.min(mapHeight / regionSize * 2, (int)((camera.position.y + viewportHeight/2) / (tileSize * regionSize)) + 2);
        
        // Começar a renderização de formas
        shapeRenderer.begin(ShapeType.Filled);
        
        // Desenhar regiões visíveis
        for (int rx = startRegionX; rx <= endRegionX; rx++) {
            for (int ry = startRegionY; ry <= endRegionY; ry++) {
                // Aplicar transformação toroidal para as coordenadas da região
                int wrappedRx = (rx % (mapWidth / regionSize) + (mapWidth / regionSize)) % (mapWidth / regionSize);
                int wrappedRy = (ry % (mapHeight / regionSize) + (mapHeight / regionSize)) % (mapHeight / regionSize);
                
                // Obter a cor da região
                Color regionColor = map.getRegionColor(wrappedRx, wrappedRy);
                shapeRenderer.setColor(regionColor);
                
                // Desenhar a região na posição real (considerando wraparound)
                shapeRenderer.rect(
                    rx * regionSize * tileSize, 
                    ry * regionSize * tileSize, 
                    regionSize * tileSize, 
                    regionSize * tileSize
                );
            }
        }
        
        shapeRenderer.end();
    }
    
    /**
     * Desenha uma grade para visualizar os tiles
     */
    private void drawGrid(OrthographicCamera camera) {
        int mapWidth = map.getWidth();
        int mapHeight = map.getHeight();
        
        // Calcular a área visível considerando o wraparound
        float viewportWidth = camera.viewportWidth * camera.zoom;
        float viewportHeight = camera.viewportHeight * camera.zoom;
        
        int startX = Math.max(0, (int)((camera.position.x - viewportWidth/2) / tileSize) - 1);
        int endX = Math.min(mapWidth * 2, (int)((camera.position.x + viewportWidth/2) / tileSize) + 2);
        int startY = Math.max(0, (int)((camera.position.y - viewportHeight/2) / tileSize) - 1);
        int endY = Math.min(mapHeight * 2, (int)((camera.position.y + viewportHeight/2) / tileSize) + 2);
        
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 0.3f);
        
        // Desenhar linhas verticais
        for (int x = startX; x <= endX + 1; x++) {
            shapeRenderer.line(x * tileSize, startY * tileSize, x * tileSize, (endY + 1) * tileSize);
        }
        
        // Desenhar linhas horizontais
        for (int y = startY; y <= endY + 1; y++) {
            shapeRenderer.line(startX * tileSize, y * tileSize, (endX + 1) * tileSize, y * tileSize);
        }
        
        // Desenhar limite do mapa para debug
        if (showDebugInfo) {
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(0, 0, mapWidth * tileSize, mapHeight * tileSize);
        }
        
        shapeRenderer.end();
    }
    
    /**
     * Renderiza as entidades com suporte a wraparound nas bordas
     */
    public void renderEntities(OrthographicCamera camera, Array<SimpleEntity> entities, boolean editorMode) {
        // Configurar o shape renderer com a projeção da câmera
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeType.Filled);
        
        float viewportWidth = camera.viewportWidth * camera.zoom;
        float viewportHeight = camera.viewportHeight * camera.zoom;
        
        for (SimpleEntity entity : entities) {
            // Calcular posição visual com wraparound
            float visualX = entity.getVisualX(camera.position.x, viewportWidth);
            float visualY = entity.getVisualY(camera.position.y, viewportHeight);
            
            // Converter para coordenadas de pixel
            float entityX = visualX * tileSize;
            float entityY = visualY * tileSize;
            
            // Verificar se a entidade está visível
            if (entityX < camera.position.x - viewportWidth/2 - tileSize ||
                entityX > camera.position.x + viewportWidth/2 + tileSize ||
                entityY < camera.position.y - viewportHeight/2 - tileSize ||
                entityY > camera.position.y + viewportHeight/2 + tileSize) {
                continue;
            }
            
            shapeRenderer.setColor(entity.color);
            float radius = tileSize * 0.4f;
            
            // Desenhar corpo da entidade
            shapeRenderer.circle(entityX + tileSize/2, entityY + tileSize/2, radius);
            
            // Desenhar indicador de direção
            if (!editorMode) {
                float dirX = entityX + tileSize/2 + MathUtils.cos(entity.direction) * radius;
                float dirY = entityY + tileSize/2 + MathUtils.sin(entity.direction) * radius;
                shapeRenderer.setColor(entity.color.r + 0.2f, entity.color.g + 0.2f, entity.color.b + 0.2f, 1);
                shapeRenderer.circle(dirX, dirY, radius * 0.3f);
            }
            
            // Desenhar entidades que cruzam a borda (wraparound) se estiverem próximas da borda
            renderWrappedEntity(entity, camera, editorMode);
        }
        
        shapeRenderer.end();
    }
    
    /**
     * Renderiza cópias das entidades quando elas cruzam as bordas do mapa
     */
    private void renderWrappedEntity(SimpleEntity entity, OrthographicCamera camera, boolean editorMode) {
        float mapWidthPixels = map.getWidth() * tileSize;
        float mapHeightPixels = map.getHeight() * tileSize;
        float entityX = entity.x * tileSize;
        float entityY = entity.y * tileSize;
        float radius = tileSize * 0.4f;
        
        // Lista de offsets para copies que atravessam as bordas
        int[][] wrapOffsets = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1},   // Lateral edges
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}  // Corner cases
        };
        
        for (int[] offset : wrapOffsets) {
            int xOffset = offset[0];
            int yOffset = offset[1];
            
            // Verificar se precisamos desenhar uma cópia
            boolean needXWrap = (xOffset < 0 && entityX < tileSize * 2) || 
                               (xOffset > 0 && entityX > mapWidthPixels - tileSize * 2);
            boolean needYWrap = (yOffset < 0 && entityY < tileSize * 2) || 
                               (yOffset > 0 && entityY > mapHeightPixels - tileSize * 2);
            
            // Se precisar de wraparound, desenhar uma cópia
            if ((xOffset != 0 && needXWrap) || (yOffset != 0 && needYWrap)) {
                float wrappedEntityX = entityX + xOffset * mapWidthPixels;
                float wrappedEntityY = entityY + yOffset * mapHeightPixels;
                
                // Verificar se a cópia está visível na câmera
                if (isPointVisible(wrappedEntityX, wrappedEntityY, camera)) {
                    // Desenhar corpo da entidade
                    shapeRenderer.setColor(entity.color);
                    shapeRenderer.circle(wrappedEntityX + tileSize/2, wrappedEntityY + tileSize/2, radius);
                    
                    // Desenhar indicador de direção
                    if (!editorMode) {
                        float dirX = wrappedEntityX + tileSize/2 + MathUtils.cos(entity.direction) * radius;
                        float dirY = wrappedEntityY + tileSize/2 + MathUtils.sin(entity.direction) * radius;
                        shapeRenderer.setColor(entity.color.r + 0.2f, entity.color.g + 0.2f, entity.color.b + 0.2f, 1);
                        shapeRenderer.circle(dirX, dirY, radius * 0.3f);
                    }
                }
            }
        }
    }
    
    /**
     * Verifica se um ponto está visível na câmera
     */
    private boolean isPointVisible(float x, float y, OrthographicCamera camera) {
        float viewportWidth = camera.viewportWidth * camera.zoom;
        float viewportHeight = camera.viewportHeight * camera.zoom;
        
        return x >= camera.position.x - viewportWidth/2 - tileSize &&
               x <= camera.position.x + viewportWidth/2 + tileSize &&
               y >= camera.position.y - viewportHeight/2 - tileSize &&
               y <= camera.position.y + viewportHeight/2 + tileSize;
    }
}