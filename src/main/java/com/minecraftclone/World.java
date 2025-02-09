package com.minecraftclone;

import java.util.Random;
import algorithms.noise.OpenSimplexNoise;

public class World {
    public static final int SIZE_X = 128;
    public static final int SIZE_Z = 128;
    public static final int HEIGHT = 64;
    public static final float BLOCK_SIZE = 0.25f;

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
        for (int x = 0; x < SIZE_X; x++) {
            for (int z = 0; z < SIZE_Z; z++) {
                int height = calculateHeight(x, z);
                for (int y = 0; y < HEIGHT; y++) {
                    if (y < height) {
                        blocks[x][y][z] = Block.DIRT;
                    } else if (y == height) {
                        blocks[x][y][z] = Block.GRASS;
                    } else {
                        blocks[x][y][z] = Block.AIR;
                    }
                }
            }
        }
        generateFeatures();
    }

    private int calculateHeight(int x, int z) {
        // Calcola altezza basata su noise per un terreno più realistico
        if (Math.abs(x - SIZE_X / 2) < 20 && Math.abs(z - SIZE_Z / 2) < 20) {
            return 10; // Area pianeggiante al centro
        } else {
            return (int) (noiseGenerator.noise(x * 0.05f, z * 0.05f) * 10 + 10); // Altezza variabile
        }
    }
    
    private void generateFeatures() {
        createLake();
        createPonds();
        plantTrees();
    }

    private void createLake() {
        // Creazione di un grande lago
        int lakeCenterX = SIZE_X / 2 + 20;
        int lakeCenterZ = SIZE_Z / 2 + 20;
        int lakeRadius = 10;
        for (int x = lakeCenterX - lakeRadius; x <= lakeCenterX + lakeRadius; x++) {
            for (int z = lakeCenterZ - lakeRadius; z <= lakeCenterZ + lakeRadius; z++) {
                fillWater(x, z, lakeRadius);
            }
        }
    }

    private void createPonds() {
        // Creazione di piccoli laghetti
        createPond(SIZE_X / 2 - 30, SIZE_Z / 2 - 20, 3);
    }

    private void createPond(int centerX, int centerZ, int radius) {
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                fillWater(x, z, radius);
            }
        }
    }

    private void fillWater(int x, int z, int radius) {
        if (x >= 0 && x < SIZE_X && z >= 0 && z < SIZE_Z) {
            int dx = x - (SIZE_X / 2 + 20);
            int dz = z - (SIZE_Z / 2 + 20);
            if (dx * dx + dz * dz <= radius * radius) {
                int surfaceY = getSurfaceHeight(x, z);
                blocks[x][surfaceY][z] = Block.WATER;
            }
        }
    }

    private void plantTrees() {
        // Generazione di alberi casuali
        int numberOfTrees = 20;
        for (int i = 0; i < numberOfTrees; i++) {
            int x = random.nextInt(SIZE_X);
            int z = random.nextInt(SIZE_Z);
            int y = getSurfaceHeight(x, z);
            if (blocks[x][y][z] == Block.GRASS) {
                addTree(x, y + 1, z);
            }
        }
    }

    private void addTree(int x, int y, int z) {
        // Aggiunge un albero con tronco e chioma
        int trunkHeight = 4 + random.nextInt(2);
        for (int i = 0; i < trunkHeight; i++) {
            blocks[x][y + i][z] = Block.TRUNK;
        }
        // Aggiunta della chioma
        addCanopy(x, y + trunkHeight, z);
    }

    private void addCanopy(int x, int y, int z) {
        int radius = 2;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = 0; dy <= radius; dy++) {
                    int nx = x + dx;
                    int ny = y + dy;
                    int nz = z + dz;
                    if (nx >= 0 && nx < SIZE_X && nz >= 0 && nz < SIZE_Z && ny < HEIGHT && blocks[nx][ny][nz] == Block.AIR) {
                        blocks[nx][ny][nz] = Block.LEAVES;
                    }
                }
            }
        }
    }

    // Restituisce il blocco alla posizione specificata
    public Block getBlock(int x, int y, int z) {
        if (x < 0 || x >= SIZE_X || y < 0 || y >= HEIGHT || z < 0 || z >= SIZE_Z) {
            return Block.AIR;
        }
        return blocks[x][y][z];
    }

    public int getSurfaceHeight(int x, int z) {
        int scaledX = Math.floorDiv(x, (int) (1 / BLOCK_SIZE));
        int scaledZ = Math.floorDiv(z, (int) (1 / BLOCK_SIZE));

        for (int y = HEIGHT - 1; y >= 0; y--) {
            if (blocks[scaledX][y][scaledZ] != Block.AIR) {
                return (int) (y * World.BLOCK_SIZE) + 1; // Adatta all'altezza dei blocchi più piccoli
            }
        }
        return 0;
    }

}
