package com.minecraftclone;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Mesh {
    private int vaoId;
    private int vboVertices;
    private int vboColors;
    private int vertexCount;

    public Mesh() {
        vaoId = glGenVertexArrays();
        vboVertices = glGenBuffers();
        vboColors   = glGenBuffers();

        if (vaoId == 0 || vboVertices == 0) {
            throw new RuntimeException("Errore nella creazione di VAO/VBO!");
        }
    }

    public void upload(MeshBuilder builder) {
        float[] vertices = builder.getVerticesArray();
        float[] colors   = builder.getColorsArray();
        vertexCount = vertices.length / 3;

        if (vertexCount == 0) {
            // Nessun vertice
            return;
        }

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
    }

    public void render() {
        if (vertexCount == 0) {
            return;
        }

        glBindVertexArray(vaoId);

        // Uso pipeline fissa per i colori
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);

        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
        glVertexPointer(3, GL_FLOAT, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, vboColors);
        glColorPointer(3, GL_FLOAT, 0, 0);

        glDrawArrays(GL_QUADS, 0, vertexCount);

        glDisableClientState(GL_COLOR_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void cleanUp() {
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
