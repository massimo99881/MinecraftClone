package com.minecraftclone;

import java.util.ArrayList;
import java.util.List;

/**
 * Costruisce un set di vertici e coordinate texture per le facce di cubi.
 * Modificato per usare triangoli invece di QUADS (GL_QUADS non esiste in core profile)
 */
public class MeshBuilder {

    private List<Float> vertices = new ArrayList<>();
    private List<Float> colors = new ArrayList<>();

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

    public float[] getVerticesArray() {
        float[] arr = new float[vertices.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = vertices.get(i);
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
}
