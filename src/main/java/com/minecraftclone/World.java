package com.minecraftclone;

import java.util.Random;
import algorithms.noise.OpenSimplexNoise;

public class World {
    public static final int SIZE_X = 128;
    public static final int SIZE_Z = 128;
    public static final int HEIGHT = 64;
    public static final float BLOCK_SIZE = 0.25f; //0.025f;

    private Block[][][] blocks;
    private OpenSimplexNoise noiseGenerator;
    private Random random;

    public World() {
    	random = new Random();
        blocks = new Block[SIZE_X][HEIGHT][SIZE_Z];
        long fixedSeed = 1; // Seed fisso per garantire terreno uguale ad ogni esecuzione
        //long fixedSeed = random.nextLong();
        noiseGenerator = new OpenSimplexNoise(fixedSeed);

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

//    private int calculateHeight(int x, int z) {
//        int baseHeight = 6; // Altezza base del terreno
//        return baseHeight + (int) (noiseGenerator.noise(x * 0.05f, z * 0.05f) * 10);
//    }

    private void generateFeatures() {
        System.out.println("🌳 Generazione Alberi...");
        plantTrees();

        System.out.println("🌊 Generazione Laghi...");
        createLakes();
    }

    private void createLakes() {
        int numLakes = 5; // Numero di laghi aumentato
        int lakeRadius = (int) (4 / BLOCK_SIZE); // Raggio dei laghi

        for (int i = 0; i < numLakes; i++) {
            int centerX = random.nextInt(SIZE_X);
            int centerZ = random.nextInt(SIZE_Z);
            int centerY = findLowestPoint(centerX, centerZ, lakeRadius);

            if (centerY > 1) {
                fillWater(centerX, centerZ, centerY, lakeRadius);
            }
        }
    }

    private int findLowestPoint(int x, int z, int radius) {
        int minY = HEIGHT;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int checkX = x + dx;
                int checkZ = z + dz;

                if (checkX >= 0 && checkX < SIZE_X && checkZ >= 0 && checkZ < SIZE_Z) {
                    int height = getSurfaceHeight(checkX, checkZ);
                    if (height < minY) {
                        minY = height;
                    }
                }
            }
        }
        return minY;
    }

    private void fillWater(int x, int z, int y, int radius) {
        int waterLevel = y - 1; // Abbassiamo il livello dell'acqua di 1 per evitare problemi di posizionamento

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int checkX = x + dx;
                int checkZ = z + dz;

                if (checkX >= 0 && checkX < SIZE_X && checkZ >= 0 && checkZ < SIZE_Z) {
                    int surfaceY = getSurfaceHeight(checkX, checkZ);

                    // Riempie le zone basse con l'acqua e le cavità sottostanti
                    if (surfaceY <= waterLevel) {
                        for (int fillY = waterLevel; fillY >= surfaceY; fillY--) {
                            if (blocks[checkX][fillY][checkZ] == Block.AIR) {
                                blocks[checkX][fillY][checkZ] = Block.WATER;
                            }
                        }
                    }
                }
            }
        }
    }

    private void plantTrees() {
        int numberOfTrees = 20;

        for (int i = 0; i < numberOfTrees; i++) {
            int x = random.nextInt(SIZE_X);
            int z = random.nextInt(SIZE_Z);
            int y = getSurfaceHeight(x, z);

            if (y < HEIGHT - 5 && blocks[x][y][z] == Block.GRASS) {
                addTree(x, y + 1, z);
            }
        }
    }

    private void addTree(int x, int y, int z) {
        int trunkHeight = random.nextInt(2) + 3; // Tronco tra 3 e 4 blocchi

        for (int i = 0; i < trunkHeight; i++) {
            blocks[x][y + i][z] = Block.TRUNK;
        }

        addCanopy(x, y + trunkHeight, z);
    }

    private void addCanopy(int x, int y, int z) {
        int radius = 2;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = 0; dy <= radius / 2; dy++) {
                    int nx = x + dx;
                    int ny = y + dy;
                    int nz = z + dz;

                    if (nx >= 0 && nx < SIZE_X && nz >= 0 && nz < SIZE_Z && ny < HEIGHT) {
                        if (Math.abs(dx) + Math.abs(dz) < radius) {
                            blocks[nx][ny][nz] = Block.LEAVES;
                        }
                    }
                }
            }
        }
    }

    public Block getBlock(int x, int y, int z) {
        if (x < 0 || x >= SIZE_X || y < 0 || y >= HEIGHT || z < 0 || z >= SIZE_Z) {
            return Block.AIR;
        }
        return blocks[x][y][z];
    }

    public int getSurfaceHeight(int x, int z) {
//    	int scaledX = Math.floorDiv(x, (int) (1 / BLOCK_SIZE));
//      int scaledZ = Math.floorDiv(z, (int) (1 / BLOCK_SIZE));

        int scaledX = Math.min(SIZE_X - 1, Math.max(0, (int) (x / BLOCK_SIZE)));
        int scaledZ = Math.min(SIZE_Z - 1, Math.max(0, (int) (z / BLOCK_SIZE)));

        for (int y = HEIGHT - 1; y >= 0; y--) {
            if (blocks[scaledX][y][scaledZ] != Block.AIR) {
                return y + 1;
                //return (int) (y * World.BLOCK_SIZE) + 1; // Adatta all'altezza dei blocchi più piccoli
            }
        }
        return 0;
    }
}
