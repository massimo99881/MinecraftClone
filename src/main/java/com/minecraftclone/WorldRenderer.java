package com.minecraftclone;

import org.lwjgl.opengl.GL11;

public class WorldRenderer {
    private World world;

    private Mesh fillMesh;
    private Mesh outlineMesh_Dirt;
    private Mesh outlineMesh_Grass;
    private Mesh outlineMesh_Water;
    private Mesh outlineMesh_Trunk;
    private Mesh outlineMesh_Leaves;
    private Mesh outlineMesh_Gray; // <-- per i blocchi grigi

    public WorldRenderer(World world) {
        this.world = world;
        fillMesh = new Mesh();
        outlineMesh_Dirt   = new Mesh();
        outlineMesh_Grass  = new Mesh();
        outlineMesh_Water  = new Mesh();
        outlineMesh_Trunk  = new Mesh();
        outlineMesh_Leaves = new Mesh();
        outlineMesh_Gray   = new Mesh();

        rebuildMeshes();
    }

    public void rebuildMeshes() {
        MeshBuilder fillBuilder = new MeshBuilder();
        MeshBuilder outlineBuilderDirt   = new MeshBuilder();
        MeshBuilder outlineBuilderGrass  = new MeshBuilder();
        MeshBuilder outlineBuilderWater  = new MeshBuilder();
        MeshBuilder outlineBuilderTrunk  = new MeshBuilder();
        MeshBuilder outlineBuilderLeaves = new MeshBuilder();
        MeshBuilder outlineBuilderGray   = new MeshBuilder();

        for (int x = 0; x < World.SIZE_X; x++) {
            for (int y = 0; y < World.HEIGHT; y++) {
                for (int z = 0; z < World.SIZE_Z; z++) {
                    Block block = world.getBlock(x, y, z);
                    if (block != Block.AIR) {
                        fillBuilder.addCube(x, y, z, block);
                        checkAndAddOutline(x, y, z, block,
                                outlineBuilderDirt, outlineBuilderGrass,
                                outlineBuilderWater, outlineBuilderTrunk,
                                outlineBuilderLeaves, outlineBuilderGray);
                    }
                }
            }
        }

        fillMesh.upload(fillBuilder);
        outlineMesh_Dirt.upload(outlineBuilderDirt);
        outlineMesh_Grass.upload(outlineBuilderGrass);
        outlineMesh_Water.upload(outlineBuilderWater);
        outlineMesh_Trunk.upload(outlineBuilderTrunk);
        outlineMesh_Leaves.upload(outlineBuilderLeaves);
        outlineMesh_Gray.upload(outlineBuilderGray);
    }

    private void checkAndAddOutline(int x, int y, int z, Block block,
            MeshBuilder dirt, MeshBuilder grass, MeshBuilder water,
            MeshBuilder trunk, MeshBuilder leaves, MeshBuilder gray) 
    {
        // Controlla i 6 vicini e, se è aria o blocco diverso, aggiungi un outline
        checkFace(x, y, z, block, dirt, grass, water, trunk, leaves, gray,  0,  0,  1);
        checkFace(x, y, z, block, dirt, grass, water, trunk, leaves, gray,  0,  0, -1);
        checkFace(x, y, z, block, dirt, grass, water, trunk, leaves, gray, -1,  0,  0);
        checkFace(x, y, z, block, dirt, grass, water, trunk, leaves, gray,  1,  0,  0);
        checkFace(x, y, z, block, dirt, grass, water, trunk, leaves, gray,  0,  1,  0);
        checkFace(x, y, z, block, dirt, grass, water, trunk, leaves, gray,  0, -1,  0);
    }

    private void checkFace(int x, int y, int z, Block block,
            MeshBuilder dirt, MeshBuilder grass, MeshBuilder water,
            MeshBuilder trunk, MeshBuilder leaves, MeshBuilder gray,
            int dx, int dy, int dz)
    {
        int nx = x + dx;
        int ny = y + dy;
        int nz = z + dz;

        Block neighbor = Block.AIR;
        if (nx >= 0 && nx < World.SIZE_X && ny >= 0 && ny < World.HEIGHT && nz >= 0 && nz < World.SIZE_Z) {
            neighbor = world.getBlock(nx, ny, nz);
        }

        if (neighbor == Block.AIR || !neighbor.getName().equals(block.getName())) {
            float[] outlineColor = getOutlineColor(block);
            MeshBuilder target = getOutlineBuilder(block, dirt, grass, water, trunk, leaves, gray);
            // Aggiunge la faccia corrispondente
            if (dx == 0 && dy == 0 && dz == 1) {
                target.addFaceFront(x, y, z, outlineColor);
            } else if (dx == 0 && dy == 0 && dz == -1) {
                target.addFaceBack(x, y, z, outlineColor);
            } else if (dx == -1 && dy == 0 && dz == 0) {
                target.addFaceLeft(x, y, z, outlineColor);
            } else if (dx == 1 && dy == 0 && dz == 0) {
                target.addFaceRight(x, y, z, outlineColor);
            } else if (dx == 0 && dy == 1 && dz == 0) {
                target.addFaceTop(x, y, z, outlineColor);
            } else if (dx == 0 && dy == -1 && dz == 0) {
                target.addFaceBottom(x, y, z, outlineColor);
            }
        }
    }

    private MeshBuilder getOutlineBuilder(Block block,
            MeshBuilder dirt, MeshBuilder grass, MeshBuilder water,
            MeshBuilder trunk, MeshBuilder leaves, MeshBuilder gray)
    {
        switch (block.getName()) {
            case "DIRT":   return dirt;
            case "GRASS":  return grass;
            case "WATER":  return water;
            case "TRUNK":  return trunk;
            case "LEAVES": return leaves;
            case "GRAY_BLOCK": return gray;  // <-- Usato per GRAY_BLOCK
        }
        return null;
    }

    /**
     * Ritorna il colore del contorno.
     * Nota: per "GRAY_BLOCK" ritorniamo nero => (0,0,0)
     */
    private float[] getOutlineColor(Block block) {
        switch (block.getName()) {
            case "DIRT":        return new float[] {0.0f, 0.0f, 0.0f};
            case "GRASS":       return new float[] {0.0f, 0.4f, 0.0f};
            case "WATER":       return new float[] {0.0f, 0.0f, 0.4f};
            case "TRUNK":       return new float[] {0.4f, 0.2f, 0.0f};
            case "LEAVES":      return new float[] {0.0f, 0.4f, 0.0f};
            case "GRAY_BLOCK":  return new float[] {0.0f, 0.0f, 0.0f}; // BORDO NERO!
            default:            return new float[] {1.0f, 1.0f, 1.0f};
        }
    }

    /**
     * Render finale: disegna prima i fill, poi le outline
     */
    public void render() {
        // 1) Disegno delle “facce piene”
        fillMesh.render();

        // 2) Disegno outline (con polygon offset)
        GL11.glEnable(GL11.GL_POLYGON_OFFSET_LINE);
        GL11.glPolygonOffset(-1.0f, -1.0f);
        //GL11.glPolygonOffset(-0.01f, -0.01f);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        GL11.glLineWidth(1.0f);

        outlineMesh_Dirt.render();
        outlineMesh_Grass.render();
        outlineMesh_Water.render();
        outlineMesh_Trunk.render();
        outlineMesh_Leaves.render();
        outlineMesh_Gray.render(); // contorno nero per i blocchi grigi

        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_LINE);
    }

    /**
     * Disegna un cubo wireframe di colore GIALLO alla posizione indicata
     * (per mostrare la selezione).
     */
    public void renderBlockHighlight(int bx, int by, int bz) {
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        GL11.glDisable(GL11.GL_CULL_FACE); // per vedere i bordi “dietro”

        MeshBuilder builder = new MeshBuilder();
        float[] yellow = {1.0f, 1.0f, 0.0f};
        builder.addCubeWithColor(bx, by, bz, yellow);

        Mesh highlightMesh = new Mesh();
        highlightMesh.upload(builder);
        highlightMesh.render();
        highlightMesh.cleanUp();

        // Ripristino
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
    }

    public void cleanup() {
        fillMesh.cleanUp();
        outlineMesh_Dirt.cleanUp();
        outlineMesh_Grass.cleanUp();
        outlineMesh_Water.cleanUp();
        outlineMesh_Trunk.cleanUp();
        outlineMesh_Leaves.cleanUp();
        outlineMesh_Gray.cleanUp();
    }
    
    public void updateBlockMesh(int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        if (block == Block.AIR) return; // Se è aria, non serve aggiornare

        MeshBuilder builder = new MeshBuilder();
        builder.addCube(x, y, z, block);

        Mesh targetMesh = getTargetMesh(block);
        if (targetMesh != null) {
            targetMesh.upload(builder); // Aggiorna solo la mesh di quel blocco
        }
    }
    
    private Mesh getTargetMesh(Block block) {
        switch (block.getName()) {
            case "DIRT":       return outlineMesh_Dirt;
            case "GRASS":      return outlineMesh_Grass;
            case "WATER":      return outlineMesh_Water;
            case "TRUNK":      return outlineMesh_Trunk;
            case "LEAVES":     return outlineMesh_Leaves;
            case "GRAY_BLOCK": return outlineMesh_Gray;  // Per i blocchi posizionati dai giocatori
            default:           return fillMesh; // Default: mesh di riempimento
        }
    }


}
