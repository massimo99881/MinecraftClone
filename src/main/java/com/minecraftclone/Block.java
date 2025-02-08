package com.minecraftclone;

public enum Block {
    AIR(false, new float[]{1.0f, 1.0f, 1.0f}),  // Bianco per il cielo e le nuvole
    DIRT(true, new float[]{0.6f, 0.4f, 0.2f}),  // Marrone per il terreno
    GRASS(true, new float[]{0.0f, 0.8f, 0.0f}), // Verde per l'erba
    CLOUD(true, new float[]{0.9f, 0.9f, 0.9f}); // Bianco per le nuvole

    private final boolean solid;
    private final float[] color;

    Block(boolean solid, float[] color) {
        this.solid = solid;
        this.color = color;
    }

    public boolean isSolid() {
        return solid;
    }

    public float[] getColor() {
        return color;
    }
}
