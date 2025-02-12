package com.minecraftclone.font;


import org.lwjgl.stb.*;
import org.lwjgl.system.MemoryStack;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryUtil.*;

public class TrueTypeFont {
    private static final int BITMAP_W = 512;
    private static final int BITMAP_H = 512;

    private STBTTFontinfo fontInfo;
    private STBTTBakedChar.Buffer cdata;
    private int texID;
    private float fontSize;

    public TrueTypeFont(String ttfPath, float fontSize) {
        this.fontSize = fontSize;
        ByteBuffer fontData = null;
        try {
            fontData = ioResourceToByteBuffer(ttfPath, 160*1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fontInfo = STBTTFontinfo.malloc();
        if (!stbtt_InitFont(fontInfo, fontData)) {
            throw new IllegalStateException("Failed to init font");
        }

        cdata = STBTTBakedChar.malloc(96); // ASCII 32..126
        ByteBuffer bitmap = memAlloc(BITMAP_W * BITMAP_H);
        stbtt_BakeFontBitmap(fontData, fontSize, bitmap, BITMAP_W, BITMAP_H, 32, cdata);

        texID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, BITMAP_W, BITMAP_H, 0, GL_ALPHA, GL_UNSIGNED_BYTE, bitmap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        memFree(bitmap);
    }

    // Disegna una stringa 2D alla posizione (x,y) con la pipeline fissa
    // in coordinate ortho
    public void drawString(String text, float x, float y) {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, texID);
        glBegin(GL_QUADS);

        float[] xp = new float[]{x};
        float[] yp = new float[]{y};

        for (int i=0; i<text.length(); i++) {
            char c = text.charAt(i);
            if (c < 32 || c >= 128) continue; // fuori range
            STBTTAlignedQuad q = STBTTAlignedQuad.malloc();
            stbtt_GetBakedQuad(cdata, BITMAP_W, BITMAP_H, c-32, xp, yp, q, true);

            glTexCoord2f(q.s0(), q.t0());
            glVertex2f(q.x0(), q.y0());
            glTexCoord2f(q.s1(), q.t0());
            glVertex2f(q.x1(), q.y0());
            glTexCoord2f(q.s1(), q.t1());
            glVertex2f(q.x1(), q.y1());
            glTexCoord2f(q.s0(), q.t1());
            glVertex2f(q.x0(), q.y1());

            q.free();
        }

        glEnd();
        glDisable(GL_TEXTURE_2D);
    }

    // Funzione di utilità LWJGL: carica un file in ByteBuffer
    private static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
        java.io.InputStream source = TrueTypeFont.class.getResourceAsStream(resource);
        if (source == null) throw new IOException("Resource not found: " + resource);

        byte[] data = source.readAllBytes();
        ByteBuffer buffer = memAlloc(data.length);
        buffer.put(data);
        buffer.flip();
        source.close();
        return buffer;
    }
}

