package com.biobox.platform;

/**
 * Interface para adaptadores de plataforma
 * Define métodos para operações específicas de plataforma
 */
public interface PlatformAdapter {
    
    /**
     * Exibe uma mensagem ao usuário
     * @param message mensagem a ser exibida
     */
    void showMessage(String message);
    
    /**
     * Exibe uma mensagem de erro ao usuário
     * @param title título da mensagem
     * @param message detalhes do erro
     */
    void showError(String title, String message);
    
    /**
     * Verifica se a plataforma suporta entrada de teclado
     * @return true se suportar
     */
    boolean hasKeyboard();
    
    /**
     * Verifica se a plataforma suporta entrada de toque
     * @return true se suportar
     */
    boolean hasTouch();
    
    /**
     * Obtém a densidade de pixels da tela
     * @return densidade (1.0 para densidade normal)
     */
    float getDisplayDensity();
    
    /**
     * Obtém a largura física da tela em polegadas
     * @return largura em polegadas
     */
    float getPhysicalWidth();
    
    /**
     * Obtém a altura física da tela em polegadas
     * @return altura em polegadas
     */
    float getPhysicalHeight();
    
    /**
     * Verifica se o dispositivo está em modo paisagem
     * @return true se estiver em modo paisagem
     */
    boolean isLandscape();
    
    /**
     * Obtém a pasta de assets
     * @return caminho para a pasta de assets
     */
    String getAssetsPath();
    
    /**
     * Obtém a pasta para armazenamento de dados
     * @return caminho para a pasta de dados
     */
    String getStoragePath();
    
    /**
     * Salva dados na plataforma
     * @param key chave para os dados
     * @param value valor a ser salvo
     */
    void savePreference(String key, String value);
    
    /**
     * Carrega dados da plataforma
     * @param key chave para os dados
     * @param defaultValue valor padrão se a chave não existir
     * @return valor carregado
     */
    String loadPreference(String key, String defaultValue);
}
