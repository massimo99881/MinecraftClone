package com.minecraftclone;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.opengl.GL11;

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
        int faceCount = 0;

        for (int x = 0; x < World.SIZE_X; x++) {
            for (int y = 0; y < World.HEIGHT; y++) {
                for (int z = 0; z < World.SIZE_Z; z++) {
                    Block block = world.getBlock(x, y, z);
                    if (block.isSolid()) {
                    	builder.addFace(x, y, z, block);
                    	
                    	System.out.println("🟫 Blocco solido a (" + x + ", " + y + ", " + z + ")");

//                        if (!isBlockSolid(x+1, y, z)) { builder.addFaceXPositive(x, y, z, block); faceCount++; }
//                        if (!isBlockSolid(x-1, y, z)) { builder.addFaceXNegative(x, y, z, block); faceCount++; }
//                        if (!isBlockSolid(x, y+1, z)) { builder.addFaceYPositive(x, y, z, block); faceCount++; }
//                        if (!isBlockSolid(x, y-1, z)) { builder.addFaceYNegative(x, y, z, block); faceCount++; }
//                        if (!isBlockSolid(x, y, z+1)) { builder.addFaceZPositive(x, y, z, block); faceCount++; }
//                        if (!isBlockSolid(x, y, z-1)) { builder.addFaceZNegative(x, y, z, block); faceCount++; }
                    }
                }
            }
        }

        mesh.upload(builder);
        System.out.println("✅ Facce generate per il mondo: " + faceCount);
        
//        float[] vertici = builder.getVerticesArray();
//        if (vertici.length > 0) {
//            System.out.println("🔹 Primo vertice generato: (" + vertici[0] + ", " + vertici[1] + ", " + vertici[2] + ")");
//        }

    }



    private boolean isBlockSolid(int x, int y, int z) {
        return world.getBlock(x, y, z).isSolid();
    }

    public void render() {
        //System.out.println("Rendering del mondo...");
        
        // Disegna il triangolo per testare OpenGL
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glColor3f(1.0f, 0.0f, 0.0f);
        GL11.glVertex3f(-0.5f, -0.5f, 0);
        GL11.glColor3f(0.0f, 1.0f, 0.0f);
        GL11.glVertex3f(0.5f, -0.5f, 0);
        GL11.glColor3f(0.0f, 0.0f, 1.0f);
        GL11.glVertex3f(0.0f, 0.5f, 0);
        GL11.glEnd();
//
        GL11.glFlush(); // Forza il rendering immediato
//        
//        System.out.println("🎮 Rendering blocchi... Numero vertici mesh: " + mesh.getVertexCount());

        
        mesh.render();
    }



    public World getWorld() {
        return world;
    }
}
