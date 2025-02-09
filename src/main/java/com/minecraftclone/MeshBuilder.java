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
    
    /**
     * Aggiunge un cubo completo (6 facce) nella posizione (x, y, z)
     * usando il colore specificato (per l'outline).
     */
    public void addCubeWithColor(int x, int y, int z, float[] color) {
        // Front face (normale +Z)
        addQuad(x, y, z + 1, 
                x + 1, y, z + 1, 
                x + 1, y + 1, z + 1, 
                x, y + 1, z + 1, color);

        // Back face (normale -Z)
        addQuad(x + 1, y, z, 
                x, y, z, 
                x, y + 1, z, 
                x + 1, y + 1, z, color);

        // Left face (normale -X)
        addQuad(x, y, z + 1, 
                x, y, z, 
                x, y + 1, z, 
                x, y + 1, z + 1, color);

        // Right face (normale +X)
        addQuad(x + 1, y, z, 
                x + 1, y, z + 1, 
                x + 1, y + 1, z + 1, 
                x + 1, y + 1, z, color);

        // Top face (normale +Y)
        addQuad(x, y + 1, z + 1, 
                x + 1, y + 1, z + 1, 
                x + 1, y + 1, z, 
                x, y + 1, z, color);

        // Bottom face (normale -Y)
        addQuad(x, y, z, 
                x + 1, y, z, 
                x + 1, y, z + 1, 
                x, y, z + 1, color);
    }

    
    /**
     * Aggiunge un cubo completo (6 facce) nella posizione (x, y, z)
     * con il colore fornito dal blocco.
     *
     * L'ordine dei vertici per ciascuna faccia è impostato in modo
     * che la faccia sia visibile (winding order counter-clockwise
     * visto dall'esterno).
     */
    public void addCube(int x, int y, int z, Block block) {
        float[] color = block.getColor();

        // Front face (facccia con z = z+1; normale +Z)
        addQuad(x, y, z + 1, 
                x + 1, y, z + 1, 
                x + 1, y + 1, z + 1, 
                x, y + 1, z + 1, color);

        // Back face (facccia con z = z; normale -Z)
        addQuad(x + 1, y, z, 
                x, y, z, 
                x, y + 1, z, 
                x + 1, y + 1, z, color);

        // Left face (facccia con x = x; normale -X)
        addQuad(x, y, z + 1, 
                x, y, z, 
                x, y + 1, z, 
                x, y + 1, z + 1, color);

        // Right face (facccia con x = x+1; normale +X)
        addQuad(x + 1, y, z, 
                x + 1, y, z + 1, 
                x + 1, y + 1, z + 1, 
                x + 1, y + 1, z, color);

        // Top face (facccia con y = y+1; normale +Y)
        addQuad(x, y + 1, z + 1, 
                x + 1, y + 1, z + 1, 
                x + 1, y + 1, z, 
                x, y + 1, z, color);

        // Bottom face (facccia con y = y; normale -Y)
        addQuad(x, y, z, 
                x + 1, y, z, 
                x + 1, y, z + 1, 
                x, y, z + 1, color);
    }

    /**
     * Aggiunge una faccia (quad) alla mesh.
     * I 4 vertici vengono aggiunti insieme ai relativi colori.
     */
    private void addQuad(float x1, float y1, float z1,
                         float x2, float y2, float z2,
                         float x3, float y3, float z3,
                         float x4, float y4, float z4,
                         float[] color) {
        // Aggiunge i 4 vertici
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

}
