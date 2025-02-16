package com.minecraftclone;

import java.util.ArrayList;
import java.util.List;

public class MeshBuilder {

    private List<Float> vertices = new ArrayList<>();
    private List<Float> colors   = new ArrayList<>();

    public void addCube(int x, int y, int z, Block block) {
        float[] color = block.getColor();
        addFaceFront( x, y, z,    color);
        addFaceBack(  x, y, z,    color);
        addFaceLeft(  x, y, z,    color);
        addFaceRight( x, y, z,    color);
        addFaceTop(   x, y, z,    color);
        addFaceBottom(x, y, z,    color);
    }

    public void addCubeWithColor(int x, int y, int z, float[] color) {
        // Stesso discorso di addCube ma con un colore personalizzato
        addFaceFront( x, y, z,    color);
        addFaceBack(  x, y, z,    color);
        addFaceLeft(  x, y, z,    color);
        addFaceRight( x, y, z,    color);
        addFaceTop(   x, y, z,    color);
        addFaceBottom(x, y, z,    color);
    }

    public void addFaceFront(int x, int y, int z, float[] color) {
        addQuad(x,   y,   z+1,
                x+1, y,   z+1,
                x+1, y+1, z+1,
                x,   y+1, z+1, color);
    }

    public void addFaceBack(int x, int y, int z, float[] color) {
        addQuad(x+1, y,   z,
                x,   y,   z,
                x,   y+1, z,
                x+1, y+1, z,   color);
    }

    public void addFaceLeft(int x, int y, int z, float[] color) {
        addQuad(x, y,   z,
                x, y,   z+1,
                x, y+1, z+1,
                x, y+1, z,   color);
    }

    public void addFaceRight(int x, int y, int z, float[] color) {
        addQuad(x+1, y,   z+1,
                x+1, y,   z,
                x+1, y+1, z,
                x+1, y+1, z+1, color);
    }

    public void addFaceTop(int x, int y, int z, float[] color) {
        addQuad(x,   y+1, z+1,
                x+1, y+1, z+1,
                x+1, y+1, z,
                x,   y+1, z,   color);
    }

    public void addFaceBottom(int x, int y, int z, float[] color) {
        addQuad(x,   y,   z,
                x+1, y,   z,
                x+1, y,   z+1,
                x,   y,   z+1, color);
    }

    private void addQuad(float x1, float y1, float z1,
                         float x2, float y2, float z2,
                         float x3, float y3, float z3,
                         float x4, float y4, float z4,
                         float[] color)
    {
        // Scala le coordinate in base alla dimensione di blocco
        x1 *= World.BLOCK_SIZE; y1 *= World.BLOCK_SIZE; z1 *= World.BLOCK_SIZE;
        x2 *= World.BLOCK_SIZE; y2 *= World.BLOCK_SIZE; z2 *= World.BLOCK_SIZE;
        x3 *= World.BLOCK_SIZE; y3 *= World.BLOCK_SIZE; z3 *= World.BLOCK_SIZE;
        x4 *= World.BLOCK_SIZE; y4 *= World.BLOCK_SIZE; z4 *= World.BLOCK_SIZE;

        // Aggiunge i vertici
        vertices.add(x1); vertices.add(y1); vertices.add(z1);
        vertices.add(x2); vertices.add(y2); vertices.add(z2);
        vertices.add(x3); vertices.add(y3); vertices.add(z3);
        vertices.add(x4); vertices.add(y4); vertices.add(z4);

        // Aggiunge colore per ognuno dei 4 vertici
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
