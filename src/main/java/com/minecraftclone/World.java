package com.minecraftclone;

import java.util.Random;
import algorithms.noise.OpenSimplexNoise;

public class World {
    public static final int SIZE_X = 128;
    public static final int SIZE_Z = 128;
    public static final int HEIGHT = 32;
    private Block[][][] blocks;
    private OpenSimplexNoise noiseGenerator;
    private Random random;

    public World() {
        blocks = new Block[SIZE_X][HEIGHT][SIZE_Z];
        noiseGenerator = new OpenSimplexNoise(new Random().nextLong());
        random = new Random();
        generateWorld();
    }

    private void generateWorld() {
        // Genera il terreno base
        for (int x = 0; x < SIZE_X; x++) {
            for (int z = 0; z < SIZE_Z; z++) {
                int height;
                // Area centrale pianeggiante
                if (Math.abs(x - SIZE_X / 2) < 20 && Math.abs(z - SIZE_Z / 2) < 20) {
                    height = 10;
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
        int startY = 12;
        blocks[startX][startY][startZ] = Block.AIR;
        blocks[startX][startY + 1][startZ] = Block.AIR;

        // Creazione di un grande lago
        int lakeCenterX = SIZE_X / 2 + 20;
        int lakeCenterZ = SIZE_Z / 2 + 20;
        int lakeRadius = 10;
        for (int x = lakeCenterX - lakeRadius; x <= lakeCenterX + lakeRadius; x++) {
            for (int z = lakeCenterZ - lakeRadius; z <= lakeCenterZ + lakeRadius; z++) {
                if (x < 0 || x >= SIZE_X || z < 0 || z >= SIZE_Z) continue;
                int dx = x - lakeCenterX;
                int dz = z - lakeCenterZ;
                if (dx * dx + dz * dz <= lakeRadius * lakeRadius) {
                    int surfaceY = getSurfaceHeight(x, z);
                    blocks[x][surfaceY][z] = Block.WATER;
                }
            }
        }

        // Creazione di piccoli laghetti
        int pondCenterX1 = SIZE_X / 2 - 30;
        int pondCenterZ1 = SIZE_Z / 2 - 20;
        int pondRadius1 = 3;
        for (int x = pondCenterX1 - pondRadius1; x <= pondCenterX1 + pondRadius1; x++) {
            for (int z = pondCenterZ1 - pondRadius1; z <= pondCenterZ1 + pondRadius1; z++) {
                if (x < 0 || x >= SIZE_X || z < 0 || z >= SIZE_Z) continue;
                int dx = x - pondCenterX1;
                int dz = z - pondCenterZ1;
                if (dx * dx + dz * dz <= pondRadius1 * pondRadius1) {
                    int surfaceY = getSurfaceHeight(x, z);
                    blocks[x][surfaceY][z] = Block.WATER;
                }
            }
        }

        // Generazione di alcuni alberi
        int numberOfTrees = 20;
        for (int i = 0; i < numberOfTrees; i++) {
            int treeX = random.nextInt(SIZE_X);
            int treeZ = random.nextInt(SIZE_Z);
            int surfaceY = getSurfaceHeight(treeX, treeZ);
            // Posiziona un albero solo se la superficie è GRASS
            if (blocks[treeX][surfaceY][treeZ] == Block.GRASS) {
                addTree(treeX, surfaceY + 1, treeZ);
            }
        }

        System.out.println("Generazione del mondo completata.");
    }

    /**
     * Aggiunge un albero in posizione (x, y, z).
     * L’albero è composto da un tronco e una chioma di foglie.
     */
    private void addTree(int x, int y, int z) {
        // Parametri per l’albero
        int trunkHeight = 4 + random.nextInt(2); // altezza del tronco compresa tra 4 e 5 blocchi
        // Posiziona il tronco
        for (int i = 0; i < trunkHeight; i++) {
            if (y + i < HEIGHT) {
                blocks[x][y + i][z] = Block.TRUNK;
            }
        }
        // Crea una chioma semplice attorno alla parte superiore del tronco
        int canopyStartY = y + trunkHeight;
        int canopyRadius = 2; // raggi della chioma
        for (int dx = -canopyRadius; dx <= canopyRadius; dx++) {
            for (int dz = -canopyRadius; dz <= canopyRadius; dz++) {
                for (int dy = 0; dy <= canopyRadius; dy++) {
                    int nx = x + dx;
                    int ny = canopyStartY + dy;
                    int nz = z + dz;
                    if (nx >= 0 && nx < SIZE_X && nz >= 0 && nz < SIZE_Z && ny < HEIGHT) {
                        // Inserisce foglie solo se lo spazio è vuoto (AIR)
                        if (blocks[nx][ny][nz] == Block.AIR) {
                            blocks[nx][ny][nz] = Block.LEAVES;
                        }
                    }
                }
            }
        }
    }

    /**
     * Restituisce il blocco nella posizione (x, y, z).
     * Se la posizione è fuori dai limiti, restituisce AIR.
     */
    public Block getBlock(int x, int y, int z) {
        if (x < 0 || x >= SIZE_X || y < 0 || y >= HEIGHT || z < 0 || z >= SIZE_Z) {
            return Block.AIR;
        }
        return blocks[x][y][z];
    }

    /**
     * Restituisce l’altezza della superficie (il livello dell’ultimo blocco solido)
     * per le coordinate (x, z).
     */
    public int getSurfaceHeight(int x, int z) {
        for (int y = HEIGHT - 1; y >= 0; y--) {
            if (blocks[x][y][z].isSolid()) {
                return y;
            }
        }
        return 0;
    }
}
