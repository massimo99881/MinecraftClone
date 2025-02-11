package com.minecraftclone;

import java.util.Random;
import algorithms.noise.OpenSimplexNoise;

public class World {
    public static final int SIZE_X = 128;
    public static final int SIZE_Z = 128;
    public static final int HEIGHT = 64;
    public static final float BLOCK_SIZE = 0.03f;

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
        //createLakes();
    }

    private void createLakes() {
        int numLakes = 1; // Numero di laghi aumentato
        int minLakeSize = 10; // Dimensione minima di un lago
        int maxLakeSize = 100; // Dimensione massima di un lago

        for (int i = 0; i < numLakes; i++) {
            int centerX = random.nextInt(SIZE_X);
            int centerZ = random.nextInt(SIZE_Z);
            int radius = random.nextInt(maxLakeSize - minLakeSize) + minLakeSize;

            int lowestPoint = findLowestPoint(centerX, centerZ, radius);
            if (lowestPoint > 1) {
                fillWater(centerX, centerZ, lowestPoint, radius);
            }
        }
    }

    private int findLowestPoint(int x, int z, int radius) {
        int minY = HEIGHT;
        int totalHeight = 0;
        int count = 0;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int checkX = x + dx;
                int checkZ = z + dz;

                if (checkX >= 0 && checkX < SIZE_X && checkZ >= 0 && checkZ < SIZE_Z) {
                    int height = getSurfaceHeight(checkX, checkZ);
                    totalHeight += height;
                    count++;
                    if (height < minY) {
                        minY = height;
                    }
                }
            }
        }

        // Se la variazione di altezza è troppo alta, evita di creare il lago
        if (count > 0) {
            int avgHeight = totalHeight / count;
            if (avgHeight - minY > 3) {
                return -1; // Troppo disomogeneo, non creiamo il lago
            }
        }
        
        return minY;
    }

    private void fillWater(int x, int z, int y, int radius) {
        int waterLevel = y + 1; // Riempiamo un po' sopra la depressione per dare l'effetto di un lago naturale

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int checkX = x + dx;
                int checkZ = z + dz;

                if (checkX >= 0 && checkX < SIZE_X && checkZ >= 0 && checkZ < SIZE_Z) {
                    int surfaceY = getSurfaceHeight(checkX, checkZ);

                    // Riempie le zone basse con l'acqua solo se il punto è abbastanza profondo
                    if (surfaceY <= waterLevel) {
                        for (int fillY = waterLevel; fillY >= surfaceY; fillY--) {
                            if (blocks[checkX][fillY][checkZ] == Block.AIR || blocks[checkX][fillY][checkZ] == Block.DIRT) {
                                blocks[checkX][fillY][checkZ] = Block.WATER;
                            }
                        }
                    }
                }
            }
        }
    }


    private void plantTrees() {
        int numberOfClusters = 30; // Aumentato il numero dei gruppi di alberi
        int treesPerCluster = 10;  // Aumentato il numero di alberi per gruppo

        // Definizione di una zona densa di alberi (foresta)
        int denseForestStartX = SIZE_X / 4;
        int denseForestStartZ = SIZE_Z / 4;
        int denseForestEndX = SIZE_X / 2;
        int denseForestEndZ = SIZE_Z / 2;
        int denseTrees = 100; // Numero di alberi extra in questa zona

        // 🌳 Genera alberi normali sparsi per il mondo
        for (int i = 0; i < numberOfClusters; i++) {
            int clusterX = random.nextInt(SIZE_X);
            int clusterZ = random.nextInt(SIZE_Z);

            for (int j = 0; j < treesPerCluster; j++) {
                int x = clusterX + random.nextInt(6) - 3;
                int z = clusterZ + random.nextInt(6) - 3;

                // Controllo dei limiti
                x = Math.max(0, Math.min(SIZE_X - 1, x));
                z = Math.max(0, Math.min(SIZE_Z - 1, z));

                int y = getSurfaceHeight(x, z);
                if (y < HEIGHT - 5 && blocks[x][y][z] == Block.GRASS) {
                    addTree(x, y + 1, z);
                }
            }
        }

        // 🌲🌲🌲 Creazione della foresta densa 🌲🌲🌲
        for (int i = 0; i < denseTrees; i++) {
            int x = denseForestStartX + random.nextInt(denseForestEndX - denseForestStartX);
            int z = denseForestStartZ + random.nextInt(denseForestEndZ - denseForestStartZ);

            int y = getSurfaceHeight(x, z);
            if (y < HEIGHT - 5 && blocks[x][y][z] == Block.GRASS) {
                addTree(x, y + 1, z);
            }
        }
    }



    private void addTree(int x, int y, int z) {
        int trunkHeight = random.nextInt(3) + 5; // Ora tra 5 e 7 blocchi

        for (int i = 0; i < trunkHeight; i++) {
            blocks[x][y + i][z] = Block.TRUNK;
        }

        addCanopy(x, y + trunkHeight, z);
    }


    private void addCanopy(int x, int y, int z) {
        int radius = random.nextInt(2) + 2; // Raggio variabile tra 2 e 3 blocchi

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = 0; dy <= radius / 2; dy++) {
                    int nx = x + dx;
                    int ny = y + dy;
                    int nz = z + dz;

                    if (nx >= 0 && nx < SIZE_X && nz >= 0 && nz < SIZE_Z && ny < HEIGHT) {
                        // Condizione per rendere le foglie più sparse
                        if (random.nextFloat() > 0.3f || (dx == 0 && dz == 0)) {
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
        int scaledX = (int) (x / BLOCK_SIZE);
        int scaledZ = (int) (z / BLOCK_SIZE);
        
        scaledX = Math.max(0, Math.min(SIZE_X - 1, scaledX));
        scaledZ = Math.max(0, Math.min(SIZE_Z - 1, scaledZ));

        for (int y = HEIGHT - 1; y >= 0; y--) {
            if (blocks[scaledX][y][scaledZ] != Block.AIR) {
                return y;
            }
        }
        return 0;
    }
}
