package com.minecraftclone;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import org.lwjgl.opengl.GL11;

public class Mesh {

    private int vaoId;
    private int vboVertices;
    private int vboTexcoords;
    private int vertexCount;

    public Mesh() {
        // Generazione VAO e VBO all'inizio per evitare problemi di inizializzazione
        vaoId = glGenVertexArrays();
        vboVertices = glGenBuffers();
        vboTexcoords = glGenBuffers();

        if (vaoId == 0 || vboVertices == 0 || vboTexcoords == 0) {
            throw new RuntimeException("Errore nella creazione di VAO/VBO!");
        }
    }

    public void upload(MeshBuilder builder) {
        float[] vertices = builder.getVerticesArray();
        float[] texcoords = builder.getTexcoordsArray();
        vertexCount = vertices.length / 3;

        if (vertexCount == 0) {
            System.out.println("⚠ Mesh vuota: nessun vertice da caricare.");
            return;
        }

        System.out.println("✅ Caricamento della mesh con " + vertexCount + " vertici.");
        System.out.println("🔹 Primo vertice: (" + vertices[0] + ", " + vertices[1] + ", " + vertices[2] + ")");
        System.out.println("🔹 Prima coordinata texture: (" + texcoords[0] + ", " + texcoords[1] + ")");

        glBindVertexArray(vaoId);

        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, vboTexcoords);
        glBufferData(GL_ARRAY_BUFFER, texcoords, GL_STATIC_DRAW);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

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

        // Attiviamo il rendering delle texture
        glEnable(GL_TEXTURE_2D);

        // Disegniamo la mesh
        System.out.println("🎨 Chiamata a glDrawArrays con " + vertexCount + " vertici...");
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);

        glDisable(GL_TEXTURE_2D);
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
        if (vboTexcoords != 0) {
            glDeleteBuffers(vboTexcoords);
        }
    }
    
    public int getVertexCount() {
        return vertexCount;
    }

}
