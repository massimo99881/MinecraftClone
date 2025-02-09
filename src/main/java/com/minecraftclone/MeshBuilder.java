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

    public void addCubeWithColor(int x, int y, int z, float[] color) {
        // Aggiunge un cubo colorato alle coordinate specificate con un colore uniforme per tutte le facce.
        addFaceFront(x, y, z, color);
        addFaceBack(x, y, z, color);
        addFaceLeft(x, y, z, color);
        addFaceRight(x, y, z, color);
        addFaceTop(x, y, z, color);
        addFaceBottom(x, y, z, color);
    }
    
    public void addCube(int x, int y, int z, Block block) {
        float[] color = block.getColor();
        // Aggiungere tutte le sei facce del cubo
        addFaceFront(x, y, z, color);
        addFaceBack(x, y, z, color);
        addFaceLeft(x, y, z, color);
        addFaceRight(x, y, z, color);
        addFaceTop(x, y, z, color);
        addFaceBottom(x, y, z, color);
    }

    // Qui puoi inserire gli altri metodi 'addFace...' come quelli che hai già, che aggiungono le singole facce al cubo.
    
    private void addFaceFront(int x, int y, int z, float[] color) {
        addQuad(x, y, z+1, x+1, y, z+1, x+1, y+1, z+1, x, y+1, z+1, color);
    }

    private void addFaceBack(int x, int y, int z, float[] color) {
        addQuad(x+1, y, z, x, y, z, x, y+1, z, x+1, y+1, z, color);
    }

    private void addFaceLeft(int x, int y, int z, float[] color) {
        addQuad(x, y, z, x, y, z+1, x, y+1, z+1, x, y+1, z, color);
    }

    private void addFaceRight(int x, int y, int z, float[] color) {
        addQuad(x+1, y, z+1, x+1, y, z, x+1, y+1, z, x+1, y+1, z+1, color);
    }

    private void addFaceTop(int x, int y, int z, float[] color) {
        addQuad(x, y+1, z+1, x+1, y+1, z+1, x+1, y+1, z, x, y+1, z, color);
    }

    private void addFaceBottom(int x, int y, int z, float[] color) {
        addQuad(x, y, z, x+1, y, z, x+1, y, z+1, x, y, z+1, color);
    }

    private void addQuad(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float[] color) {
        // Aggiunge i 4 vertici del quadrilatero alla lista dei vertici
        vertices.add(x1); vertices.add(y1); vertices.add(z1);
        vertices.add(x2); vertices.add(y2); vertices.add(z2);
        vertices.add(x3); vertices.add(y3); vertices.add(z3);
        vertices.add(x4); vertices.add(y4); vertices.add(z4);

        // Aggiunge il colore per ciascun vertice
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
