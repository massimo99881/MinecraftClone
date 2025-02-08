package com.minecraftclone;

import java.util.ArrayList;
import java.util.List;

/**
 * Costruisce un set di vertici e coordinate texture per le facce di cubi.
 * Modificato per usare triangoli invece di QUADS (GL_QUADS non esiste in core profile)
 */
public class MeshBuilder {

    private List<Float> vertices = new ArrayList<>();
    private List<Float> texcoords = new ArrayList<>();
    private List<Float> colors = new ArrayList<>();

    /**
     * Aggiunge una faccia al mesh builder, divisa in due triangoli.
     */
    public void addFace(int x, int y, int z, Block block) {
        float[] color = block.getColor();
        
        vertices.add((float) x); vertices.add((float) y); vertices.add((float) z);
        vertices.add((float) x + 1); vertices.add((float) y); vertices.add((float) z);
        vertices.add((float) x + 1); vertices.add((float) y + 1); vertices.add((float) z);
        vertices.add((float) x); vertices.add((float) y + 1); vertices.add((float) z);

        for (int i = 0; i < 4; i++) {
            colors.add(color[0]);
            colors.add(color[1]);
            colors.add(color[2]);
        }
    }
    
    private void addFace(float[] v, float[] uv) {
        // Primo triangolo (vertice 0, 1, 2)
        for (int i : new int[]{0, 1, 2}) {
            vertices.add(v[i * 3]); vertices.add(v[i * 3 + 1]); vertices.add(v[i * 3 + 2]);
            texcoords.add(uv[i * 2]); texcoords.add(uv[i * 2 + 1]);
        }
        // Secondo triangolo (vertice 0, 2, 3)
        for (int i : new int[]{0, 2, 3}) {
            vertices.add(v[i * 3]); vertices.add(v[i * 3 + 1]); vertices.add(v[i * 3 + 2]);
            texcoords.add(uv[i * 2]); texcoords.add(uv[i * 2 + 1]);
        }
    }

    /**
     * Aggiunge la faccia positiva lungo X (destra).
     */
    public void addFaceXPositive(int x, int y, int z, Block block) {
        float[] v = {
            x + 1, y, z, x + 1, y, z + 1, x + 1, y + 1, z + 1, x + 1, y + 1, z
        };
        float[] uv = { 0, 0, 1, 0, 1, 1, 0, 1 };
        addFace(v, uv);
    }

    public void addFaceXNegative(int x, int y, int z, Block block) {
        float[] v = {
            x, y, z + 1, x, y, z, x, y + 1, z, x, y + 1, z + 1
        };
        float[] uv = { 0, 0, 1, 0, 1, 1, 0, 1 };
        addFace(v, uv);
    }

    public void addFaceYPositive(int x, int y, int z, Block block) {
        float[] v = {
            x, y + 1, z, x + 1, y + 1, z, x + 1, y + 1, z + 1, x, y + 1, z + 1
        };
        float[] uv = { 0, 0, 1, 0, 1, 1, 0, 1 };
        addFace(v, uv);
    }

    public void addFaceYNegative(int x, int y, int z, Block block) {
        float[] v = {
            x + 1, y, z, x, y, z, x, y, z + 1, x + 1, y, z + 1
        };
        float[] uv = { 0, 0, 1, 0, 1, 1, 0, 1 };
        addFace(v, uv);
    }

    public void addFaceZPositive(int x, int y, int z, Block block) {
        float[] v = {
            x, y, z + 1, x + 1, y, z + 1, x + 1, y + 1, z + 1, x, y + 1, z + 1
        };
        float[] uv = { 0, 0, 1, 0, 1, 1, 0, 1 };
        addFace(v, uv);
    }

    public void addFaceZNegative(int x, int y, int z, Block block) {
        float[] v = {
            x + 1, y, z, x, y, z, x, y + 1, z, x + 1, y + 1, z
        };
        float[] uv = { 0, 0, 1, 0, 1, 1, 0, 1 };
        addFace(v, uv);
    }

    public float[] getVerticesArray() {
        float[] arr = new float[vertices.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = vertices.get(i);
        }

        // Stampa i primi vertici generati per debug
        if (arr.length >= 6) { // Stampiamo solo se abbiamo almeno due vertici
            System.out.println("🔎 Primo vertice generato: (" + arr[0] + ", " + arr[1] + ", " + arr[2] + ")");
        }

        return arr;
    }

    public float[] getColorsArray() {
        float[] arr = new float[colors.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = colors.get(i);
        }
        return arr;
    }

    public float[] getTexcoordsArray() {
        float[] arr = new float[texcoords.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = texcoords.get(i);
        }
        return arr;
    }
}
