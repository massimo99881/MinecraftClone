package com.minecraftclone;

import org.lwjgl.opengl.GL11;

public class WorldRenderer {
    private World world;

    // Mesh di fill per tutti i blocchi
    private Mesh fillMesh;

    // Mesh per gli outline, separate per tipo
    private Mesh outlineMesh_Dirt;
    private Mesh outlineMesh_Grass;
    private Mesh outlineMesh_Water;
    private Mesh outlineMesh_Trunk;
    private Mesh outlineMesh_Leaves;

    public WorldRenderer(World world) {
        this.world = world;
        fillMesh = new Mesh();
        outlineMesh_Dirt = new Mesh();
        outlineMesh_Grass = new Mesh();
        outlineMesh_Water = new Mesh();
        outlineMesh_Trunk = new Mesh();
        outlineMesh_Leaves = new Mesh();
        rebuildMeshes();
    }

    public void rebuildMeshes() {
        MeshBuilder fillBuilder = new MeshBuilder();
        MeshBuilder builderOutline_Dirt = new MeshBuilder();
        MeshBuilder builderOutline_Grass = new MeshBuilder();
        MeshBuilder builderOutline_Water = new MeshBuilder();
        MeshBuilder builderOutline_Trunk = new MeshBuilder();
        MeshBuilder builderOutline_Leaves = new MeshBuilder();

        // Scorri l'intero mondo
        for (int x = 0; x < World.SIZE_X; x++) {
            for (int y = 0; y < World.HEIGHT; y++) {
                for (int z = 0; z < World.SIZE_Z; z++) {
                    Block block = world.getBlock(x, y, z);
                    if (block != Block.AIR) {  // Salta i blocchi d'aria
                        // Aggiungi il cubo di fill con il colore definito dal blocco
                        fillBuilder.addCube(x, y, z, block);

                        // Aggiungi il cubo all'outline in base al tipo
                        switch (block.getName()) {
                            case "DIRT":
                                builderOutline_Dirt.addCubeWithColor(x, y, z, new float[]{0.0f, 0.0f, 0.0f});
                                break;
                            case "GRASS":
                                builderOutline_Grass.addCubeWithColor(x, y, z, new float[]{0.0f, 0.4f, 0.0f});
                                break;
                            case "WATER":
                                builderOutline_Water.addCubeWithColor(x, y, z, new float[]{0.0f, 0.0f, 0.4f});
                                break;
                            case "TRUNK":
                                builderOutline_Trunk.addCubeWithColor(x, y, z, new float[]{0.4f, 0.2f, 0.0f});
                                break;
                            case "LEAVES":
                                builderOutline_Leaves.addCubeWithColor(x, y, z, new float[]{0.0f, 0.4f, 0.0f});
                                break;
                        }
                    }
                }
            }
        }

        // Carica i dati nelle mesh
        fillMesh.upload(fillBuilder);
        outlineMesh_Dirt.upload(builderOutline_Dirt);
        outlineMesh_Grass.upload(builderOutline_Grass);
        outlineMesh_Water.upload(builderOutline_Water);
        outlineMesh_Trunk.upload(builderOutline_Trunk);
        outlineMesh_Leaves.upload(builderOutline_Leaves);
    }

    public void render() {
        // Renderizza la mesh di fill normalmente
        fillMesh.render();

        // Attiva la modalità wireframe per disegnare gli outline
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        GL11.glLineWidth(2.0f);

        outlineMesh_Dirt.render();
        outlineMesh_Grass.render();
        outlineMesh_Water.render();
        outlineMesh_Trunk.render();
        outlineMesh_Leaves.render();

        // Ripristina la modalità fill
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
    }

    public void cleanup() {
        fillMesh.cleanUp();
        outlineMesh_Dirt.cleanUp();
        outlineMesh_Grass.cleanUp();
        outlineMesh_Water.cleanUp();
        outlineMesh_Trunk.cleanUp();
        outlineMesh_Leaves.cleanUp();
    }
}
