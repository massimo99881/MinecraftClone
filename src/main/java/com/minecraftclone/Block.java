package com.minecraftclone;

public enum Block {
    AIR(false, 0),
    DIRT(true, 1),
    STONE(true, 2);

    private final boolean solid;
    private final int textureIndex; 
    // Indice che useremo nel texture atlas per sapere quale parte dell’immagine usare

    Block(boolean solid, int textureIndex) {
        this.solid = solid;
        this.textureIndex = textureIndex;
    }

    public boolean isSolid() {
        return solid;
    }

    public int getTextureIndex() {
        return textureIndex;
    }
}
