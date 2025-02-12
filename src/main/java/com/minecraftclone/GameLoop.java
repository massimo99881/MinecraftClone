package com.minecraftclone;

import java.nio.FloatBuffer;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

/**
 * Questa classe racchiude la logica “client”:
 * - initWindow (stesso stile che avevi in Main)
 * - initScene (carica atlas, crea world, camera)
 * - loop (pulizia schermo, input camera, rendering, swap buffers)
 */
public class GameLoop {

    private long window;
    private TextureAtlas atlas;
    private WorldRenderer worldRenderer;
    private Camera camera;
    private World world;

    private boolean running = false;

    public void start() {
        initWindow();
        initScene();
        runLoop();
        cleanup();
    }

    private void initWindow() {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Impossibile inizializzare GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

        window = GLFW.glfwCreateWindow(800, 600, "Minecraft Clone", 0, 0);
        if (window == 0) {
            throw new RuntimeException("Errore nella creazione della finestra GLFW");
        }

        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        GLFW.glfwSetWindowPos(window, 
            (vidMode.width() - 800) / 2, 
            (vidMode.height() - 600) / 2);

        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();

        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

        System.out.println("✅ OpenGL inizializzato correttamente.");

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();

        float fov = 70.0f;
        float aspectRatio = (float)800 / 600;
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
        System.out.println("🔄 Caricamento Texture Atlas...");
        atlas = new TextureAtlas("assets/atlas.png");

        System.out.println("🔄 Creazione del mondo...");
        world = new World();

        System.out.println("🔄 Creazione del renderer del mondo...");
        worldRenderer = new WorldRenderer(world);

        float startX = World.SIZE_X / 2 * World.BLOCK_SIZE;
        float startZ = World.SIZE_Z / 2 * World.BLOCK_SIZE;
        int surfaceHeight = world.getSurfaceHeight((int)startX, (int)startZ);
        float startY = (surfaceHeight + 2) * World.BLOCK_SIZE;

        System.out.println("📸 Telecamera inizializzata a: " 
            + startX + ", " + startY + ", " + startZ);

        camera = new Camera(startX, startY, startZ, world, worldRenderer);
    }

    private void runLoop() {
        running = true;
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);

        while (running && !GLFW.glfwWindowShouldClose(window)) {
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

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {}
        }
    }

    private void cleanup() {
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }
}
