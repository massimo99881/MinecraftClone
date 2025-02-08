package com.minecraftclone;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import org.lwjgl.opengl.GL11;

public class Mesh {

    private int vaoId;
    private int vboVertices;
    private int vboColors;
    private int vertexCount;

    public Mesh() {
        // Generazione VAO e VBO all'inizio per evitare problemi di inizializzazione
        vaoId = glGenVertexArrays();
        vboVertices = glGenBuffers();
        vboColors = glGenBuffers();

        if (vaoId == 0 || vboVertices == 0 ) {
            throw new RuntimeException("Errore nella creazione di VAO/VBO!");
        }
    }

    public void upload(MeshBuilder builder) {
        float[] vertices = builder.getVerticesArray();
        float[] colors = builder.getColorsArray();
        vertexCount = vertices.length / 3;

        if (vertexCount == 0) {
            System.out.println("⚠ Mesh vuota: nessun vertice da caricare.");
            return;
        }

        System.out.println("✅ Caricamento della mesh con " + vertexCount + " vertici.");
        System.out.println("🔹 Primo vertice: (" + vertices[0] + ", " + vertices[1] + ", " + vertices[2] + ")");

        glBindVertexArray(vaoId);

        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, vboColors);
        glBufferData(GL_ARRAY_BUFFER, colors, GL_STATIC_DRAW);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        
        System.out.println("🔹 Numero di vertici caricati nella mesh: " + vertexCount);
        if (vertexCount > 0) {
            System.out.println("🔹 Primo vertice: " + vertices[0] + ", " + vertices[1] + ", " + vertices[2]);
        }

    }


    public void render() {
        if (vertexCount == 0) {
            System.out.println("⚠ Tentativo di renderizzare una mesh vuota!");
            return;
        }

        System.out.println("🎮 Disegnando mesh con " + vertexCount + " vertici...");

        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawArrays(GL_QUADS, 0, vertexCount);

        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);

        System.out.println("✅ Rendering completato.");
    }


    public void cleanUp() {
        System.out.println("🔄 Pulizia della mesh...");
        if (vaoId != 0) {
            glDeleteVertexArrays(vaoId);
        }
        if (vboVertices != 0) {
            glDeleteBuffers(vboVertices);
        }
        if (vboColors != 0) {
            glDeleteBuffers(vboColors);
        }
    }
    
    public int getVertexCount() {
        return vertexCount;
    }

}
