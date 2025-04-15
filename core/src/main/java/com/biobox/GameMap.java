package com.biobox;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

/**
 * Classe que gerencia o mapa de tiles do jogo com suporte a mapa toroidal (wraparound)
 */
public class GameMap {
    private TileType[][] tiles;
    private Color[][] tileColors;
    private final int width;
    private final int height;
    
    // Para LOD rendering
    private Color[][] regionColors;
    private int regionSize;
    
    // Constante para tamanho do tile (exposto para ser acessado por outras classes)
    public static final int TILE_SIZE = 16;
    
    public GameMap(int width, int height, int regionSize) {
        this.width = width;
        this.height = height;
        this.regionSize = regionSize;
        this.tiles = new TileType[width][height];
        this.tileColors = new Color[width][height];
        
        // Calcular regiões para renderização LOD
        int regionsX = width / regionSize + (width % regionSize > 0 ? 1 : 0);
        int regionsY = height / regionSize + (height % regionSize > 0 ? 1 : 0);
        this.regionColors = new Color[regionsX][regionsY];
        
        // Inicializar o mapa com grama e cores variadas
        fillWithGrass();
    }
    
    public void fillWithGrass() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = TileType.GRASS;
                
                // Criar uma cor levemente variada para cada tile
                Color baseColor = TileType.GRASS.getColor();
                tileColors[x][y] = new Color(
                    MathUtils.clamp(baseColor.r + MathUtils.random(-0.05f, 0.05f), 0, 1),
                    MathUtils.clamp(baseColor.g + MathUtils.random(-0.05f, 0.05f), 0, 1),
                    MathUtils.clamp(baseColor.b + MathUtils.random(-0.05f, 0.05f), 0, 1),
                    1f
                );
            }
        }
        
        // Atualizar cores das regiões para LOD
        updateAllRegionColors();
    }
    
    /**
     * Get tile with toroidal wrapping (positions are automatically wrapped)
     */
    public TileType getTile(int x, int y) {
        // Apply toroidal (wraparound) transformation
        x = (x % width + width) % width;
        y = (y % height + height) % height;
        
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return tiles[x][y];
        }
        return TileType.WALL; // Fallback, should never happen with proper wrapping
    }
    
    /**
     * Set tile with toroidal wrapping (positions are automatically wrapped)
     */
    public void setTile(int x, int y, TileType type) {
        // Apply toroidal (wraparound) transformation
        x = (x % width + width) % width;
        y = (y % height + height) % height;
        
        if (x >= 0 && x < width && y >= 0 && y < height) {
            tiles[x][y] = type;
            
            // Atualizar cor do tile com variação
            Color baseColor = type.getColor();
            tileColors[x][y] = new Color(
                MathUtils.clamp(baseColor.r + MathUtils.random(-0.05f, 0.05f), 0, 1),
                MathUtils.clamp(baseColor.g + MathUtils.random(-0.05f, 0.05f), 0, 1),
                MathUtils.clamp(baseColor.b + MathUtils.random(-0.05f, 0.05f), 0, 1),
                1f
            );
            
            // Atualizar cores de região para renderização LOD
            int regionX = x / regionSize;
            int regionY = y / regionSize;
            if (regionX < regionColors.length && regionY < regionColors[0].length) {
                updateRegionColor(regionX, regionY);
            }
        }
    }
    
    private void updateAllRegionColors() {
        int regionsX = width / regionSize + (width % regionSize > 0 ? 1 : 0);
        int regionsY = height / regionSize + (height % regionSize > 0 ? 1 : 0);
        
        for (int rx = 0; rx < regionsX; rx++) {
            for (int ry = 0; ry < regionsY; ry++) {
                updateRegionColor(rx, ry);
            }
        }
    }
    
    private void updateRegionColor(int regionX, int regionY) {
        // Calcular limites da região
        int startX = regionX * regionSize;
        int startY = regionY * regionSize;
        int endX = Math.min(startX + regionSize, width);
        int endY = Math.min(startY + regionSize, height);
        
        // Média das cores na região
        float r = 0, g = 0, b = 0;
        int count = 0;
        
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                Color tileColor = tileColors[x][y];
                r += tileColor.r;
                g += tileColor.g;
                b += tileColor.b;
                count++;
            }
        }
        
        if (count > 0) {
            regionColors[regionX][regionY] = new Color(r / count, g / count, b / count, 1f);
        } else {
            regionColors[regionX][regionY] = new Color(0.2f, 0.7f, 0.2f, 1f); // Verde padrão
        }
    }
    
    /**
     * Get tile color with toroidal wrapping
     */
    public Color getTileColor(int x, int y) {
        // Apply toroidal (wraparound) transformation
        x = (x % width + width) % width;
        y = (y % height + height) % height;
        
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return tileColors[x][y];
        }
        return Color.BLACK;
    }
    
    /**
     * Get region color for LOD rendering
     */
    public Color getRegionColor(int regionX, int regionY) {
        // Apply toroidal (wraparound) transformation
        regionX = (regionX % regionColors.length + regionColors.length) % regionColors.length;
        regionY = (regionY % regionColors[0].length + regionColors[0].length) % regionColors[0].length;
        
        if (regionX >= 0 && regionX < regionColors.length && 
            regionY >= 0 && regionY < regionColors[0].length) {
            return regionColors[regionX][regionY];
        }
        return Color.BLACK;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public int getRegionSize() {
        return regionSize;
    }
    
    /**
     * Returns true if the position is within map boundaries
     * after applying toroidal wrapping
     */
    public boolean isInBounds(int x, int y) {
        // No need to check with toroidal wrapping as all coordinates are valid
        // after modulo transformation
        return true;
    }
}