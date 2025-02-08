package com.minecraftclone;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.stb.STBImage.*;

/**
 * Classe per gestire il caricamento della texture atlas.
 * Supporta il caricamento da JAR o da file esterno.
 */
public class TextureAtlas {
    private int textureId;
    private int width, height;

    /**
     * Carica la texture dall'atlante specificato
     * @param resourcePath Percorso della texture (es. "assets/atlas.png")
     */
    public TextureAtlas(String resourcePath) {
        loadTexture(resourcePath);
    }

    /**
     * Carica la texture da file o da risorsa nel JAR.
     * @param resourcePath Percorso relativo della texture (es. "assets/atlas.png")
     */
    private void loadTexture(String resourcePath) {
        ByteBuffer imageBuffer = null;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // 1️⃣ Tenta di caricare il file come risorsa dal JAR
            try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                if (in != null) {
                    System.out.println("Caricamento texture da JAR: " + resourcePath);
                    byte[] imageBytes = in.readAllBytes();
                    imageBuffer = ByteBuffer.allocateDirect(imageBytes.length);
                    imageBuffer.put(imageBytes);
                    imageBuffer.flip(); // Passiamo alla lettura
                } else {
                    // 2️⃣ Se non esiste nel JAR, tenta di caricarlo da file system
                    String absolutePath = Paths.get(resourcePath).toAbsolutePath().toString();
                    System.out.println("Caricamento texture da FILE: " + absolutePath);
                    byte[] imageBytes = Files.readAllBytes(Paths.get(absolutePath));
                    imageBuffer = ByteBuffer.allocateDirect(imageBytes.length);
                    imageBuffer.put(imageBytes);
                    imageBuffer.flip(); // Passiamo alla lettura
                }
            }

            if (imageBuffer == null) {
                throw new RuntimeException("Failed to load texture atlas " + resourcePath + " : File not found");
            }

            // Buffer per dimensioni immagine
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            // Carica immagine usando STB
            ByteBuffer image = stbi_load_from_memory(imageBuffer, w, h, comp, 4);
            if (image == null) {
                throw new RuntimeException("Failed to load texture atlas " + resourcePath + " : " + stbi_failure_reason());
            }

            width = w.get();
            height = h.get();

            // Crea una texture OpenGL
            textureId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureId);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);

            // Impostazioni di filtro e wrapping
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            stbi_image_free(image);
            glBindTexture(GL_TEXTURE_2D, 0);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load texture atlas " + resourcePath + " : " + e.getMessage());
        }
    }

    /**
     * Attiva la texture per il rendering
     */
    public void bind() {
        glBindTexture(GL_TEXTURE_2D, textureId);
    }

    /**
     * Disattiva la texture
     */
    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    /**
     * Restituisce l'ID della texture OpenGL
     */
    public int getTextureId() {
        return textureId;
    }
}
