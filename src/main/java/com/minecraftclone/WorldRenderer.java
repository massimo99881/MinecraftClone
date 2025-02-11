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
					if (block != Block.AIR) {
						// Aggiungi fill
						fillBuilder.addCube(x, y, z, block);

						// Controlla ogni faccia per gli outline
						checkAndAddOutline(x, y, z, block, builderOutline_Dirt, builderOutline_Grass,
								builderOutline_Water, builderOutline_Trunk, builderOutline_Leaves);
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

	private void checkAndAddOutline(int x, int y, int z, Block block, MeshBuilder dirtBuilder, MeshBuilder grassBuilder,
			MeshBuilder waterBuilder, MeshBuilder trunkBuilder, MeshBuilder leavesBuilder) {
// Controlla tutte le direzioni
		checkFace(x, y, z, block, dirtBuilder, grassBuilder, waterBuilder, trunkBuilder, leavesBuilder, 0, 0, 1); // Front
		checkFace(x, y, z, block, dirtBuilder, grassBuilder, waterBuilder, trunkBuilder, leavesBuilder, 0, 0, -1); // Back
		checkFace(x, y, z, block, dirtBuilder, grassBuilder, waterBuilder, trunkBuilder, leavesBuilder, -1, 0, 0); // Left
		checkFace(x, y, z, block, dirtBuilder, grassBuilder, waterBuilder, trunkBuilder, leavesBuilder, 1, 0, 0); // Right
		checkFace(x, y, z, block, dirtBuilder, grassBuilder, waterBuilder, trunkBuilder, leavesBuilder, 0, 1, 0); // Top
		checkFace(x, y, z, block, dirtBuilder, grassBuilder, waterBuilder, trunkBuilder, leavesBuilder, 0, -1, 0); // Bottom
	}

	private void checkFace(int x, int y, int z, Block block, MeshBuilder dirtBuilder, MeshBuilder grassBuilder,
			MeshBuilder waterBuilder, MeshBuilder trunkBuilder, MeshBuilder leavesBuilder, int dx, int dy, int dz) {
		int nx = x + dx;
		int ny = y + dy;
		int nz = z + dz;

		Block neighbor = Block.AIR;
		if (nx >= 0 && nx < World.SIZE_X && ny >= 0 && ny < World.HEIGHT && nz >= 0 && nz < World.SIZE_Z) {
			neighbor = world.getBlock(nx, ny, nz);
		}

		if (neighbor == Block.AIR || !neighbor.getName().equals(block.getName())) {
			float[] outlineColor = getOutlineColor(block);
			MeshBuilder targetBuilder = getOutlineBuilder(block, dirtBuilder, grassBuilder, waterBuilder, trunkBuilder,
					leavesBuilder);

			if (targetBuilder != null) {
				addFaceToBuilder(x, y, z, dx, dy, dz, targetBuilder, outlineColor);
			}
		}
	}

	private void addFaceToBuilder(int x, int y, int z, int dx, int dy, int dz, MeshBuilder builder, float[] color) {
		if (dx == 0 && dy == 0 && dz == 1) {
			builder.addFaceFront(x, y, z, color);
		} else if (dx == 0 && dy == 0 && dz == -1) {
			builder.addFaceBack(x, y, z, color);
		} else if (dx == -1 && dy == 0 && dz == 0) {
			builder.addFaceLeft(x, y, z, color);
		} else if (dx == 1 && dy == 0 && dz == 0) {
			builder.addFaceRight(x, y, z, color);
		} else if (dx == 0 && dy == 1 && dz == 0) {
			builder.addFaceTop(x, y, z, color);
		} else if (dx == 0 && dy == -1 && dz == 0) {
			builder.addFaceBottom(x, y, z, color);
		}
	}

	private MeshBuilder getOutlineBuilder(Block block, MeshBuilder dirt, MeshBuilder grass, MeshBuilder water,
			MeshBuilder trunk, MeshBuilder leaves) {
		switch (block.getName()) {
		case "DIRT":
			return dirt;
		case "GRASS":
			return grass;
		case "WATER":
			return water;
		case "TRUNK":
			return trunk;
		case "LEAVES":
			return leaves;
		default:
			return null;
		}
	}

	private float[] getOutlineColor(Block block) {
		switch (block.getName()) {
		case "DIRT":
			return new float[] { 0.0f, 0.0f, 0.0f };
		case "GRASS":
			return new float[] { 0.0f, 0.4f, 0.0f };
		case "WATER":
			return new float[] { 0.0f, 0.0f, 0.4f };
		case "TRUNK":
			return new float[] { 0.4f, 0.2f, 0.0f };
		case "LEAVES":
			return new float[] { 0.0f, 0.4f, 0.0f };
		default:
			return new float[] { 1.0f, 1.0f, 1.0f };
		}
	}

	public void render() {
	    fillMesh.render();

	    GL11.glEnable(GL11.GL_POLYGON_OFFSET_LINE);
	    GL11.glPolygonOffset(-1.0f, -1.0f); // Regola questi valori se necessario

	    GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
	    GL11.glLineWidth(0.7f);

	    outlineMesh_Dirt.render();
	    outlineMesh_Grass.render();
	    outlineMesh_Water.render();
	    outlineMesh_Trunk.render();
	    outlineMesh_Leaves.render();

	    GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
	    GL11.glDisable(GL11.GL_POLYGON_OFFSET_LINE);
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
