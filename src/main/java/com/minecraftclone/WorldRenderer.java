package com.minecraftclone;

import static org.lwjgl.opengl.GL11.*;

public class WorldRenderer {
    private World world;
    private Mesh mesh;

    public WorldRenderer(World world) {
        this.world = world;
        this.mesh = new Mesh();
        rebuildMesh();
    }

    public void rebuildMesh() {
        MeshBuilder builder = new MeshBuilder();

        for (int x = 0; x < World.SIZE_X; x++) {
            for (int y = 0; y < World.HEIGHT; y++) {
                for (int z = 0; z < World.SIZE_Z; z++) {
                    Block block = world.getBlock(x, y, z);
                    if (block.isSolid()) {
                        builder.addFace(x, y, z, block);
                    }
                }
            }
        }

        mesh.upload(builder);
        System.out.println("✅ Mesh aggiornata con " + mesh.getVertexCount() + " vertici.");
    }

    public void render() {
        mesh.render();
    }
}
