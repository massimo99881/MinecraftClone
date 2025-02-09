package com.minecraftclone;

import org.lwjgl.opengl.GL11;

public class WorldRenderer {
    private World world;
    private Mesh brownMesh;
    private Mesh otherMesh;

    public WorldRenderer(World world) {
        this.world = world;
        this.brownMesh = new Mesh();
        this.otherMesh = new Mesh();
        rebuildMesh();
    }

    public void rebuildMesh() {
    	MeshBuilder brownBuilder = new MeshBuilder();
        MeshBuilder otherBuilder = new MeshBuilder();

        for (int x = 0; x < World.SIZE_X; x++) {
            for (int y = 0; y < World.HEIGHT; y++) {
                for (int z = 0; z < World.SIZE_Z; z++) {
                    Block block = world.getBlock(x, y, z);
                    if (block.isSolid()) {
                        // Se il blocco è marrone (DIRT o CLOUD) va nel builder dei blocchi marroni
                        if (block == Block.DIRT || block == Block.CLOUD) {
                            brownBuilder.addFace(x, y, z, block);
                        } else {
                            otherBuilder.addFace(x, y, z, block);
                        }
                    }
                }
            }
        }

        brownMesh.upload(brownBuilder);
        otherMesh.upload(otherBuilder);
    }

    public void render() {
        // Renderizza gli altri blocchi normalmente
        otherMesh.render();
        // Renderizza i blocchi marroni in modalità fill
        brownMesh.render();

        // Ora, per disegnare il bordo nero sui blocchi marroni:
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        GL11.glLineWidth(2.0f);
        // Imposta il colore nero (assicurati di avere il colore corretto per la modalità fixed pipeline)
        GL11.glColor3f(0.0f, 0.0f, 0.0f);
        brownMesh.render();
        // Ripristina la modalità fill
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        // (Opzionale) Ripristina il colore a bianco per il resto del rendering
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
    }
}
