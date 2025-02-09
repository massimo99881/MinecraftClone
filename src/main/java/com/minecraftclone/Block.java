package com.minecraftclone;

public enum Block {
    AIR(false, new float[]{0.5f, 0.7f, 1.0f}),  // Azzurro
    CLOUD(true, new float[]{0.5f, 0.3f, 0.1f}),  // Marrone
    GRASS(true, new float[]{0.1f, 0.6f, 0.1f}), // Verde
    DIRT(true, new float[]{0.5f, 0.4f, 0.1f}); 

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
