package com.minecraftclone;

import java.util.Random;
import algorithms.noise.OpenSimplexNoise;

public class World {
    public static final int SIZE_X = 128;  // Aumentato
    public static final int SIZE_Z = 128;  // Aumentato
    public static final int HEIGHT = 32;
    private Block[][][] blocks;
    private OpenSimplexNoise noiseGenerator;

    public World() {
        blocks = new Block[SIZE_X][HEIGHT][SIZE_Z];
        noiseGenerator = new OpenSimplexNoise(new Random().nextLong());
        generateWorld();
    }

    private void generateWorld() {
        for (int x = 0; x < SIZE_X; x++) {
            for (int z = 0; z < SIZE_Z; z++) {
                // Rendi l'area centrale pianeggiante
                int height;
                if (Math.abs(x - SIZE_X / 2) < 20 && Math.abs(z - SIZE_Z / 2) < 20) {
                    height = 10;  // Pianura centrale
                } else {
                    height = (int) (noiseGenerator.noise(x * 0.05f, z * 0.05f) * 10 + 10);
                }

                for (int y = 0; y < HEIGHT; y++) {
                    if (y < height - 1) {
                        blocks[x][y][z] = Block.DIRT;
                    } else if (y == height - 1) {
                        blocks[x][y][z] = Block.GRASS;
                    } else {
                        blocks[x][y][z] = Block.AIR;
                    }
                }
            }
        }

        // Assicurati che la posizione iniziale della telecamera sia libera
        int startX = SIZE_X / 2;
        int startZ = SIZE_Z / 2;
        int startY = 12; // Sopra la pianura

        blocks[startX][startY][startZ] = Block.AIR;
        blocks[startX][startY + 1][startZ] = Block.AIR;

        System.out.println("✅ Posizione (" + startX + ", " + startY + ", " + startZ + ") forzata ad AIR per evitare collisioni.");
    }

    public Block getBlock(int x, int y, int z) {
        if (x < 0 || x >= SIZE_X || y < 0 || y >= HEIGHT || z < 0 || z >= SIZE_Z) {
            return Block.AIR;
        }
        return blocks[x][y][z];
    }
    
    public int getSurfaceHeight(int x, int z) {
        for (int y = HEIGHT - 1; y >= 0; y--) { // Partiamo dall'alto e scendiamo
            if (blocks[x][y][z].isSolid()) {
                return y; // Restituisce la prima altezza trovata con un blocco solido
            }
        }
        return 0; // Se nessun blocco solido è presente, restituisce il minimo possibile
    }

}
