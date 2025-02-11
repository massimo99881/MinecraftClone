package com.minecraftclone;

public class Block {
    private String name;
    private boolean solid;
    private float[] color;

    // Definizioni dei blocchi:
    // AIR: usato per l'aria e lo sfondo (azzurro)
    // DIRT: terreno (marrone)
    // GRASS: superficie erbosa (verde)
    // WATER: acqua per i laghi (azzurro)
    // TRUNK: tronco degli alberi (marrone chiaro)
    // LEAVES: foglie degli alberi (verde)
    public static final Block AIR    = new Block("AIR", false, new float[]{0.5f, 0.7f, 1.0f});   // Azzurro (sfondo/aria)
    public static final Block DIRT   = new Block("DIRT", true, new float[]{0.5f, 0.3f, 0.1f});    // Marrone
    public static final Block GRASS = new Block("GRASS", true, new float[]{0.1f, 0.6f, 0.1f});
    public static final Block WATER  = new Block("WATER", false, new float[]{0.0f, 0.7f, 1.0f});  // Azzurro (laghi)
    public static final Block TRUNK  = new Block("TRUNK", true, new float[]{0.8f, 0.7f, 0.5f});   // Marrone chiaro (tronchi)
    public static final Block LEAVES = new Block("LEAVES", true, new float[]{0.0f, 0.8f, 0.0f});   // Verde (foglie)
    public static final Block GRAY_BLOCK = new Block("GRAY_BLOCK", true, new float[]{0.5f, 0.5f, 0.5f}); // Grigio

    
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
