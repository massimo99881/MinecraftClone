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

        GLFW.glfwShowWindow(window);
    }



    private void initScene() {
        System.out.println("🔄 Caricamento Texture Atlas...");
        atlas = new TextureAtlas("assets/atlas.png");

        System.out.println("🔄 Creazione del mondo...");
        world = new World();
        
     // 🔍 Controllo del blocco iniziale della telecamera
        Block startBlock = world.getBlock(8, 5, 8);
        System.out.println("📢 Blocco alla posizione iniziale della telecamera: " + startBlock);

        System.out.println("🔄 Creazione del renderer del mondo...");
        worldRenderer = new WorldRenderer(world);
        
        worldRenderer.getWorld().debugCheck();

        // Posizioniamo la telecamera sopra il terreno iniziale
        System.out.println("🔄 Creazione della telecamera...");
        //camera = new Camera(8.0f, 20.0f, 8.0f);
        
        camera = new Camera(8.0f, 10.0f, 8.0f);  // Prova a metterla più in alto

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
    	GL11.glClearColor(0.5f, 0.7f, 1.0f, 1.0f); // Sfondo azzurro per verificare OpenGL


        while (!GLFW.glfwWindowShouldClose(window)) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            GL11.glLoadIdentity();

            // 🔥 AGGIORNIAMO LA TELECAMERA
            camera.updateInput(window, world);
            camera.applyTransformations();

            System.out.println("📸 Posizione telecamera: (" + camera.getX() + ", " + camera.getY() + ", " + camera.getZ() + ")");

            atlas.bind();
            worldRenderer.render();
            atlas.unbind();

            GLFW.glfwSwapBuffers(window);
            
            GLFW.glfwPollEvents();
            
            GL11.glDisable(GL11.GL_CULL_FACE);

        }
    }

    public static void main(String[] args) {
        boolean isServer = args.length > 0 && args[0].equalsIgnoreCase("server");
        new Main(isServer).run();
    }
}
