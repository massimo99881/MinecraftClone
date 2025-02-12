package com.minecraftclone;

import java.nio.FloatBuffer;
import java.util.List;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import com.minecraftclone.font.TrueTypeFont;  // la tua classe per i font, se esiste
import com.minecraftclone.login.LoginFrame;
import com.minecraftclone.network.MyApi;
import com.minecraftclone.state.GameState;
import com.minecraftclone.state.PlayerState;

public class Main {
    private long window; 
    private TextureAtlas atlas;
    private WorldRenderer worldRenderer;
    private Camera camera;
    private World world;
    private TrueTypeFont fontRenderer;  // se vuoi usare la classe STB-based

    private int width = 800;
    private int height = 600;

    public Main() {
    }

    public static void main(String[] args) {
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);

        while (loginFrame.isVisible()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
        }

        if (!loginFrame.isLoginOk()) {
            System.out.println("Login fallito. Uscita dall'applicazione.");
            System.exit(0);
        }

        new Main().runClient();
    }

    private void runClient() {
        initWindow();  
        initScene();   
        loop();        
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    private void initWindow() {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Impossibile inizializzare GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

        window = GLFW.glfwCreateWindow(width, height, "Minecraft Clone", 0, 0);
        if (window == 0) {
            throw new RuntimeException("Errore nella creazione della finestra");
        }

        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        GLFW.glfwSetWindowPos(window, 
            (vidMode.width() - width) / 2, 
            (vidMode.height() - height) / 2);

        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();

        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

        System.out.println("✅ OpenGL inizializzato correttamente.");

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();

        float fov = 70.0f;
        float aspectRatio = (float) width / height;
        float near = 0.05f;
        float far  = 1000.0f;

        Matrix4f projectionMatrix = new Matrix4f()
                .perspective((float)Math.toRadians(fov), aspectRatio, near, far);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        projectionMatrix.get(buffer);
        GL11.glLoadMatrixf(buffer);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        GL11.glClearColor(0.5f, 0.7f, 1.0f, 1.0f);

        GLFW.glfwShowWindow(window);
    }

    private void initScene() {
        atlas = new TextureAtlas("assets/atlas.png");
        world = new World();
        worldRenderer = new WorldRenderer(world);

        float startX = World.SIZE_X / 2 * World.BLOCK_SIZE; 
        float startZ = World.SIZE_Z / 2 * World.BLOCK_SIZE;
        int surfaceHeight = world.getSurfaceHeight((int)(startX), (int)(startZ));
        float startY = (surfaceHeight + 2) * World.BLOCK_SIZE;

        // Esempio: fontRenderer se hai la tua classe STB-based
        fontRenderer = new TrueTypeFont("/assets/font/LiberationSans-Bold.ttf", 32.0f);

        camera = new Camera(startX, startY, startZ, world, worldRenderer);
    }

    private void loop() {
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);

        while (!GLFW.glfwWindowShouldClose(window)) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            GL11.glLoadIdentity();

            camera.updateInput(window);
            camera.applyTransformations();

            atlas.bind();
            worldRenderer.render();
            atlas.unbind();

            if (camera.isSelectingBlockMode()) {
                worldRenderer.renderBlockHighlight(
                    camera.getSelectedBlockX(),
                    camera.getSelectedBlockY(),
                    camera.getSelectedBlockZ()
                );
            }

            // Disegna i giocatori
            renderOtherPlayers();

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {}
        }
    }

    private void renderOtherPlayers() {
        List<PlayerState> players = MyApi.getPlayers();
        for (PlayerState p : players) {
            if (!p.getEmail().equalsIgnoreCase(GameState.currentUserEmail)) {
                drawPlayerAvatar(p);
            }
        }
    }

    private void drawPlayerAvatar(PlayerState p) {
        GL11.glPushMatrix();
        GL11.glTranslatef(p.getX(), p.getY(), p.getZ());

        float[] c = convertColor(p.getColor());
        GL11.glColor3f(c[0], c[1], c[2]);

        float s = 0.06f;
        GL11.glBegin(GL11.GL_QUADS);
          GL11.glVertex3f(0,0,s); GL11.glVertex3f(s,0,s); GL11.glVertex3f(s,s,s); GL11.glVertex3f(0,s,s);
          GL11.glVertex3f(s,0,0); GL11.glVertex3f(0,0,0); GL11.glVertex3f(0,s,0); GL11.glVertex3f(s,s,0);
          GL11.glVertex3f(0,0,0); GL11.glVertex3f(0,0,s); GL11.glVertex3f(0,s,s); GL11.glVertex3f(0,s,0);
          GL11.glVertex3f(s,0,s); GL11.glVertex3f(s,0,0); GL11.glVertex3f(s,s,0); GL11.glVertex3f(s,s,s);
          GL11.glVertex3f(0,s,s); GL11.glVertex3f(s,s,s); GL11.glVertex3f(s,s,0); GL11.glVertex3f(0,s,0);
          GL11.glVertex3f(0,0,0); GL11.glVertex3f(s,0,0); GL11.glVertex3f(s,0,s); GL11.glVertex3f(0,0,s);
        GL11.glEnd();

        GL11.glPopMatrix();

        // Ora disegno la label 2D
        drawLabel2D(p.getEmail(), p.getX()+s/2, p.getY()+s, p.getZ()+s/2);
    }

    private void drawLabel2D(String text, float wx, float wy, float wz) {
        // Salviamo le matrici correnti (modelview/projection)
        GL11.glPushMatrix();

        // Otteniamo le matrici di proiezione e modelview
        float[] proj = new float[16];
        float[] modl = new float[16];
        // Legge la matrice di proiezione
        GL11.glGetFloatv(GL11.GL_PROJECTION_MATRIX, proj);
        // Legge la matrice di modelview
        GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, modl);

        // Creiamo il vettore
        Vector4f pos = new Vector4f(wx, wy, wz, 1.0f);
        // Moltiplichiamo per la modelview
        pos = multiplyMatrixVector(modl, pos);
        // Poi per la proiezione
        pos = multiplyMatrixVector(proj, pos);

        // Divisione prospettica
        if (pos.w != 0f) {
            pos.x /= pos.w;
            pos.y /= pos.w;
            pos.z /= pos.w;
        }

        // Convertiamo da NDC a coordinate finestra (width, height)
        float sx = (pos.x * 0.5f + 0.5f) * this.width;
        float sy = (1.0f - (pos.y * 0.5f + 0.5f)) * this.height;

        // Passiamo in modalità 2D (ortho), per disegnare overlay
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0, this.width, this.height, 0, -1, 1);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

        // Disegno la stringa in 2D
        drawString2D(text, (int)sx, (int)sy);

        // Ripristino
        GL11.glPopMatrix(); // esce da modelview
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix(); // esce da projection
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        GL11.glPopMatrix(); // esce dal pushMatrix iniziale
    }


    private void drawString2D(String text, int x, int y) {
        // Se hai un contesto OpenGL compat, potresti fare:
        // GL11.glRasterPos2i(x, y);
        // for (int i = 0; i < text.length(); i++) {
        //    drawChar(text.charAt(i)); 
        // }
        //
        // Oppure se stai usando un TrueTypeFont STB:
        if (fontRenderer != null) {
            // es. fontRenderer.drawString(x, y, text, r,g,b)
            //fontRenderer.drawString(x, y, text, 1f, 1f, 1f);
        	fontRenderer.drawString(text, x, y);
        } else {
            // Stub: non fa nulla, ma previene l'errore
        }
    }

    
    private Vector4f multiplyMatrixVector(float[] m, Vector4f v) {
        Vector4f out = new Vector4f();
        out.x = m[0]*v.x + m[4]*v.y + m[8]*v.z + m[12]*v.w;
        out.y = m[1]*v.x + m[5]*v.y + m[9]*v.z + m[13]*v.w;
        out.z = m[2]*v.x + m[6]*v.y + m[10]*v.z+ m[14]*v.w;
        out.w = m[3]*v.x + m[7]*v.y + m[11]*v.z+ m[15]*v.w;
        return out;
    }

    private float[] convertColor(String color) {
        if (color == null) {
            return new float[]{1,1,1};
        }
        switch(color.toUpperCase()) {
            case "RED": return new float[]{1f,0f,0f};
            case "GREEN": return new float[]{0f,1f,0f};
            case "BLUE": return new float[]{0f,0f,1f};
            case "YELLOW": return new float[]{1f,1f,0f};
            default: return new float[]{1f,1f,1f};
        }
    }
}
