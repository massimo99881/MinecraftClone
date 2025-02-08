package com.minecraftclone;

import java.util.Random;

import algorithms.noise.OpenSimplexNoise;

public class World {
	public static final int SIZE_X = 64;
    public static final int SIZE_Z = 64;
    public static final int HEIGHT = 32;
    private Block[][][] blocks;
    private OpenSimplexNoise noiseGenerator;  // Algoritmo per il rumore

    public World() {
    	blocks = new Block[SIZE_X][HEIGHT][SIZE_Z];
    	noiseGenerator = new OpenSimplexNoise(new Random().nextLong());  // Generatore casuale
        generateWorld();
    }

    private void generateWorld() {
    	for (int x = 0; x < SIZE_X; x++) {
            for (int z = 0; z < SIZE_Z; z++) {
                // Creiamo il terreno con OpenSimplexNoise
            	int height = (int) (noiseGenerator.noise(x * 0.1f, z * 0.1f) * 10 + 10);
                for (int y = 0; y < HEIGHT; y++) {
                    if (y < height - 1) {
                        blocks[x][y][z] = Block.DIRT;
                    } else if (y == height - 1) {
                        blocks[x][y][z] = Block.GRASS;
                    } else if (y > height && y < height + 3 && Math.random() > 0.85) {
                        blocks[x][y][z] = Block.CLOUD;
                    } else {
                        blocks[x][y][z] = Block.AIR;
                    }
                }
            }
        }

    	// Assicuriamoci che la posizione iniziale della telecamera sia libera
        int startX = 8, startZ = 8;
        int startY = getSurfaceHeight(startX, startZ) + 2; // Posizioniamo la telecamera sopra il terreno
        blocks[startX][startY][startZ] = Block.AIR;
        blocks[startX][startY + 1][startZ] = Block.AIR;
        System.out.println("✅ Posizione (8, " + startY + ", 8) forzata ad AIR per evitare collisioni.");
        
     // Debug: conta quanti blocchi solidi vengono generati
        int solidBlocks = 0;
        for (int x = 0; x < SIZE_X; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int z = 0; z < SIZE_Z; z++) {
                    if (blocks[x][y][z].isSolid()) {
                        solidBlocks++;
                    }
                }
            }
        }
        System.out.println("🔹 Numero di blocchi solidi generati: " + solidBlocks);

    }
    
 // Metodo per ottenere l'altezza del terreno in una posizione
    public int getSurfaceHeight(int x, int z) {
        for (int y = HEIGHT - 1; y >= 0; y--) {
            if (blocks[x][y][z] != Block.AIR) {
                return y;
            }
        }
        return 0; // Se non ci sono blocchi, ritorna 0
    }


    public Block getBlock(int x, int y, int z) {
    	if (x < 0 || x >= SIZE_X || y < 0 || y >= HEIGHT || z < 0 || z >= SIZE_Z) {
            return Block.AIR;
        }
        return blocks[x][y][z];
    }

    public void setBlock(int x, int y, int z, Block block) {
        if (x < 0 || x >= SIZE_X || y < 0 || y >= HEIGHT || z < 0 || z >= SIZE_Z) {
            return;
        }
        blocks[x][y][z] = block;
    }

    public void debugCheck() {
        int solidBlocks = 0;
        for (int x = 0; x < SIZE_X; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int z = 0; z < SIZE_Z; z++) {
                    if (blocks[x][y][z].isSolid()) {
                        solidBlocks++;
                    }
                }
            }
        }
        System.out.println("🔍 Blocco solidi nel mondo: " + solidBlocks);
    }


}
