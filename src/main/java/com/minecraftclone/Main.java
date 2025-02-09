package com.minecraftclone;

import java.nio.FloatBuffer;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public class Main {
    private long window; // ID della finestra GLFW
    private boolean isServer;
    private TextureAtlas atlas;
    private WorldRenderer worldRenderer;
    private Camera camera;
    private World world;

    public Main(boolean isServer) {
        this.isServer = isServer;
    }

    public void run() {
        if (isServer) {
            runServer();
        } else {
            runClient();
        }
    }

    private void runServer() {
        try {
            System.out.println("[SERVER] Avvio del server...");
            new GameServer(8087).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runClient() {
        initWindow();  // Inizializza la finestra OpenGL
        initScene();   // Carica le risorse (mondo, texture, mesh)
        connectToServer(); // Connessione al server
        loop();        // Avvia il loop di rendering

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

        window = GLFW.glfwCreateWindow(800, 600, "Minecraft Clone", 0, 0);
        if (window == 0) {
            throw new RuntimeException("Errore nella creazione della finestra");
        }

        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        GLFW.glfwSetWindowPos(window, (vidMode.width() - 800) / 2, (vidMode.height() - 600) / 2);

        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();

        // Imposta il texture environment in MODULATE
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

        System.out.println("✅ OpenGL inizializzato correttamente.");

        // Imposta la matrice di proiezione con JOML
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();

        float fov = 70.0f;
        float aspectRatio = (float) 800 / 600;
        float near = 0.1f;
        float far = 1000.0f;

        Matrix4f projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(fov), aspectRatio, near, far);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        projectionMatrix.get(buffer);
        GL11.glLoadMatrixf(buffer);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        // Imposta lo sfondo ad azzurro
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

        float startX = 30.844f, startZ = 15.284f;
        //int startY = Math.max(2, world.getSurfaceHeight(startX, startZ) + 2);
        float startY = 4.589f;// world.getSurfaceHeight(startX, startZ);

        System.out.println("📸 Telecamera inizializzata a: (" + startX + ", " + startY + ", " + startZ + ")");
        camera = new Camera(startX, startY, startZ);
    }

    private void connectToServer() {
        try {
            System.out.println("[CLIENT] Connessione al server...");
            new GameClient("localhost", 8087).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loop() {
        GL11.glEnable(GL11.GL_DEPTH_TEST); // Attiva il depth test per il rendering 3D
        GL11.glEnable(GL11.GL_CULL_FACE); // Ottimizzazione: nasconde facce non visibili

        while (!GLFW.glfwWindowShouldClose(window)) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            GL11.glLoadIdentity();

            // Gestione della telecamera
            camera.updateInput(window, world);
            camera.applyTransformations();

            System.out.println("📸 Posizione telecamera: (" + camera.getX() + ", " + camera.getY() + ", " + camera.getZ() + ")");

            // Render del mondo
            atlas.bind();
            worldRenderer.render();
            atlas.unbind();

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();

            try {
                Thread.sleep(16); // Circa 60 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        boolean isServer = args.length > 0 && args[0].equalsIgnoreCase("server");
        new Main(isServer).run();
    }
}
