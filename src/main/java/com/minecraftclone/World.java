package com.minecraftclone;

import java.util.Random;

public class World {
    public static final int SIZE = 16;
    private Block[][][] blocks;

    public World() {
        blocks = new Block[SIZE][SIZE][SIZE];
        generateWorld();
    }

    private void generateWorld() {
        Random rand = new Random();
        
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                    if (y < SIZE / 2) {
                        if (rand.nextFloat() < 0.3f) {
                            blocks[x][y][z] = Block.STONE;
                        } else {
                            blocks[x][y][z] = Block.DIRT;
                        }
                    } else {
                        blocks[x][y][z] = Block.AIR;
                    }
                }
            }
        }

        // 🚨 Assicura che la posizione (8,5,8) sia sempre aria per evitare collisioni
        blocks[8][5][8] = Block.AIR;
        blocks[8][6][8] = Block.AIR; // Libera l'area sopra per evitare di essere "incastrati"
        blocks[8][7][8] = Block.AIR;

        System.out.println("✅ Posizione (8,5,8) forzata ad AIR per evitare collisioni con la telecamera.");
        
     // Debug: conta quanti blocchi solidi vengono generati
        int solidBlocks = 0;
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                    if (blocks[x][y][z].isSolid()) {
                        solidBlocks++;
                    }
                }
            }
        }
        System.out.println("🔹 Numero di blocchi solidi generati: " + solidBlocks);

    }


    public Block getBlock(int x, int y, int z) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE || z < 0 || z >= SIZE) {
            return Block.AIR;
        }
        return blocks[x][y][z];
    }

    public void setBlock(int x, int y, int z, Block block) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE || z < 0 || z >= SIZE) {
            return;
        }
        blocks[x][y][z] = block;
    }

    public void debugCheck() {
        int solidBlocks = 0;
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                    if (blocks[x][y][z].isSolid()) {
                        solidBlocks++;
                    }
                }
            }
        }
        System.out.println("🔍 Blocco solidi nel mondo: " + solidBlocks);
    }


}
