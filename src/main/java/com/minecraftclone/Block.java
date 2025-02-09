package com.minecraftclone;

public class Block {
    private String name;
    private boolean solid;
    private float[] color;

    // Blocchi disponibili
    public static final Block AIR    = new Block("AIR", false, new float[]{0.5f, 0.7f, 1.0f});   // Sfondo/aria (azzurro)
    public static final Block DIRT   = new Block("DIRT", true, new float[]{0.5f, 0.3f, 0.1f});    // Terra (marrone)
    public static final Block GRASS  = new Block("GRASS", true, new float[]{0.1f, 0.6f, 0.1f});   // Superficie erbosa (verde)
    public static final Block WATER  = new Block("WATER", false, new float[]{0.0f, 0.5f, 1.0f});  // Acqua (azzurro)
    public static final Block TRUNK  = new Block("TRUNK", true, new float[]{0.8f, 0.7f, 0.5f});   // Tronco (marrone chiaro)
    public static final Block LEAVES = new Block("LEAVES", true, new float[]{0.0f, 0.8f, 0.0f});   // Foglie (verde brillante)

    public Block(String name, boolean solid, float[] color) {
        this.name = name;
        this.solid = solid;
        this.color = color;
    }

    public boolean isSolid() {
        return solid;
    }

    public float[] getColor() {
        return color;
    }

    public String getName() {
        return name;
    }
}
