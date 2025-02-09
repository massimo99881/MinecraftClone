package com.minecraftclone;

import org.lwjgl.opengl.GL11;

public class WorldRenderer {
    private World world;
    private Mesh terrainMesh;  // Mesh per i blocchi del terreno (ad es. DIRT e GRASS)
    private Mesh otherMesh;    // Mesh per gli altri blocchi (ad es. WATER, TRUNK, LEAVES)

    public WorldRenderer(World world) {
        this.world = world;
        this.terrainMesh = new Mesh();
        this.otherMesh = new Mesh();
        rebuildMesh();
    }

    public void rebuildMesh() {
        MeshBuilder terrainBuilder = new MeshBuilder();
        MeshBuilder otherBuilder = new MeshBuilder();

        for (int x = 0; x < World.SIZE_X; x++) {
            for (int y = 0; y < World.HEIGHT; y++) {
                for (int z = 0; z < World.SIZE_Z; z++) {
                    Block block = world.getBlock(x, y, z);
                    if (block != Block.AIR) {  // Salta i blocchi d'aria
                        // Inserisce i blocchi relativi al terreno (ad esempio DIRT e GRASS)
                        if (block == Block.DIRT || block == Block.GRASS) {
                            terrainBuilder.addCube(x, y, z, block);
                        } else {
                            otherBuilder.addCube(x, y, z, block);
                        }
                    }
                }
            }
        }

        terrainMesh.upload(terrainBuilder);
        otherMesh.upload(otherBuilder);
    }

    public void render() {
        // Renderizza gli altri blocchi normalmente
        otherMesh.render();
        // Renderizza i blocchi del terreno in modalità fill
        terrainMesh.render();

        // Ora disegniamo un contorno nero attorno ai blocchi del terreno:
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        GL11.glLineWidth(2.0f);
        GL11.glColor3f(0.0f, 0.0f, 0.0f);
        terrainMesh.render();
        // Ripristina la modalità fill e il colore bianco per il rendering successivo
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
    }
}
