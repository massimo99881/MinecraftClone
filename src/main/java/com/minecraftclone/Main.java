package com.minecraftclone;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import com.minecraftclone.font.TrueTypeFont;
import com.minecraftclone.login.LoginFrame;
import com.minecraftclone.network.MyApi;
import com.minecraftclone.state.BlockState;
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

    // Elenco dei players e dei blocchi ricevuti
    private volatile List<PlayerState> globalPlayers = new ArrayList<>();
    private volatile List<BlockState> globalBlocks  = new ArrayList<>();
    static ConcurrentLinkedQueue<BlockState> blockUpdatesQueue = new ConcurrentLinkedQueue<>();
    public static volatile boolean requestMeshRebuild = false;

    private boolean running = true;

    public Main() {
    }

    public static void main(String[] args) {
        // 1) Mostra finestra login
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);

        // 2) Aspetta che la finestra si chiuda
        while (loginFrame.isVisible()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
        }

        // 3) Se login fallito, esci
        if (!loginFrame.isLoginOk()) {
            System.out.println("Login fallito. Esco.");
            System.exit(0);
        }

        // 4) Avvia il client
        new Main().runClient();
    }

    private void runClient() {
        initWindow();  
        initScene();  
        
        loadInitialBlocks();

        // Avvia un thread separato per le chiamate REST periodiche
        startNetworkThread();

        loop();        

        // Termina
        running = false; // ferma network thread
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }
    
    private void loadInitialBlocks() {
        System.out.println("🔄 Caricamento blocchi dal database...");
        List<BlockState> blocks = MyApi.getAllBlocks();

        if (blocks != null) {
            for (BlockState bs : blocks) {
                world.setBlock(bs.getX(), bs.getY(), bs.getZ(), Block.GRAY_BLOCK);
            }
            // ✅ Aggiorna la mesh una volta caricati tutti i blocchi
            worldRenderer.rebuildMeshes();
            System.out.println("✅ Blocchi caricati e mesh aggiornata!");
        }
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
        
        // 🛑 Intercetta la chiusura della finestra
        GLFW.glfwSetWindowCloseCallback(window, (win) -> {
            System.out.println("❌ Chiusura finestra rilevata. Terminazione processi...");
            terminateGame();
        });

        GLFW.glfwShowWindow(window);
    }

    /**
     * 🛑 Questo metodo assicura che tutti i processi vengano terminati
     */
    private void terminateGame() {
        running = false; // Ferma il loop principale

        // Disconnessione del giocatore dal server
        if (GameState.currentUserEmail != null) {
            System.out.println("🔌 Disconnessione del giocatore: " + GameState.currentUserEmail);
            MyApi.disconnect(GameState.currentUserEmail);
        }

        // Ferma i thread della Camera
        if (camera != null) {
            camera.stopThreads();
        }

        // Chiudi GLFW correttamente
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();

        System.out.println("✅ Tutti i processi terminati. Uscita dal gioco.");
        System.exit(0);
    }
    
    private void initScene() {
        atlas = new TextureAtlas("assets/atlas.png");
        world = new World();
        worldRenderer = new WorldRenderer(world);

     // Ottieni la posizione salvata dal database
        PlayerState savedPlayer = getSavedPlayerPosition();
        
        float startX, startY, startZ;
        
        if (savedPlayer != null && isValidSpawn(savedPlayer.getX(), savedPlayer.getY(), savedPlayer.getZ())) {
            // 🔥 Se il giocatore ha una posizione salvata, usala
            startX = savedPlayer.getX();
            startY = savedPlayer.getY();
            startZ = savedPlayer.getZ();
            System.out.println("📍 Ripristinata posizione salvata: " + startX + ", " + startY + ", " + startZ);
        } else {
        	// 🏗️ Se è un nuovo giocatore o ha coordinate (0,0,0), assegna una posizione casuale sopra il terreno
            startX = getRandomSpawnX();
            startZ = getRandomSpawnZ();
            startY = 1+world.getSurfaceHeight((int) startX, (int) startZ) * World.BLOCK_SIZE + (2 * World.BLOCK_SIZE);
            
            System.out.println("🆕 Nuovo giocatore! Spawn in posizione casuale: " + startX + ", " + startY + ", " + startZ);
        }

        // 🎨 Inizializza il renderer per il testo
        fontRenderer = new TrueTypeFont("/assets/font/LiberationSans-Bold.ttf", 32.0f);

        // 🏃 Crea la camera con la posizione trovata
        camera = new Camera(startX, startY, startZ, world, worldRenderer);
    }
    
    /**
     * 🎲 Genera una posizione X casuale valida nel mondo.
     */
    private float getRandomSpawnX() {
        return (float) (Math.random() * (World.SIZE_X * World.BLOCK_SIZE));
    }

    /**
     * 🎲 Genera una posizione Z casuale valida nel mondo.
     */
    private float getRandomSpawnZ() {
        return (float) (Math.random() * (World.SIZE_Z * World.BLOCK_SIZE));
    }
    
    /**
     * ✅ Controlla se la posizione di spawn è valida:
     * - Deve essere sopra il terreno.
     * - Non deve essere sopra un blocco occupato.
     */
    private boolean isValidSpawn(float x, float y, float z) {
        int bx = (int) (x / World.BLOCK_SIZE);
        int by = (int) (y / World.BLOCK_SIZE);
        int bz = (int) (z / World.BLOCK_SIZE);

        if (bx < 0 || bx >= World.SIZE_X || bz < 0 || bz >= World.SIZE_Z) {
            return false; // 🚫 Fuori dal mondo
        }

        Block blockBelow = world.getBlock(bx, by - 1, bz);
        Block blockAtSpawn = world.getBlock(bx, by, bz);

        return (blockBelow != Block.AIR && blockAtSpawn == Block.AIR);
    }
    
    /**
     * 🔍 Recupera la posizione salvata del giocatore dal database.
     * Se esiste, restituisce il PlayerState, altrimenti null.
     */
    private PlayerState getSavedPlayerPosition() {
        List<PlayerState> players = MyApi.getPlayers();

        for (PlayerState p : players) {
            if (p.getEmail().equalsIgnoreCase(GameState.currentUserEmail)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Thread separato per fetchare players + blocks ogni 2 secondi
     */
    private void startNetworkThread() {
        Thread networkThread = new Thread(() -> {
            while (running) {
                // Chiama MyApi per fetchare
                List<PlayerState> newPlayers = MyApi.getPlayers();
                List<BlockState> newBlocks  = MyApi.getAllBlocks();

                // Memorizza in campi condivisi (volatile)
                globalPlayers = newPlayers;
                globalBlocks  = newBlocks;

                try {
                    Thread.sleep(5000); // fetch ogni 5 secondi
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "NetworkThread");

        networkThread.start();
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

            if (requestMeshRebuild) {
                System.out.println("🔄 Ricaricamento mesh nel main thread...");
                worldRenderer.rebuildMeshes();  // 🛑 Ora è sicuro, siamo nel main thread
                requestMeshRebuild = false; // Reset della flag
            }
            
            // 🚀 Applica gli aggiornamenti della coda solo nel thread principale
            processBlockUpdates();
            // Integra i players e i blocchi nel main thread
            integrateNetworkData();

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {}
        }
    }

    private void processBlockUpdates() {
        boolean updated = false;
        int updateCount = 0; // Conta i blocchi aggiornati

        while (!blockUpdatesQueue.isEmpty()) {
            BlockState bs = blockUpdatesQueue.poll();
            if (bs != null) {
                world.setBlock(bs.getX(), bs.getY(), bs.getZ(), Block.GRAY_BLOCK);
                worldRenderer.updateBlockMesh(bs.getX(), bs.getY(), bs.getZ()); // ✅ Aggiorna SOLO il blocco modificato
                updated = true;
                updateCount++;
            }
        }

        // Se abbiamo aggiornato più di 10 blocchi, ricarichiamo tutte le mesh per sicurezza
        if (updated && updateCount > 10) {
            System.out.println("🔄 Troppi blocchi aggiornati, ricarico tutte le mesh...");
            worldRenderer.rebuildMeshes(); // 🛑 Ricostruiamo solo se ci sono molte modifiche
        }
    }


    /**
     * Integra i dati presi dal thread di rete:
     * - player positions (non ricalcolate?), 
     * - blocchi: setBlock su world (solo i cambi)
     */
    private void integrateNetworkData() {
        // Aggiorniamo i blocchi
        for (BlockState bs : globalBlocks) {
            
            world.setBlock(bs.getX(), bs.getY(), bs.getZ(), Block.GRAY_BLOCK);
            //TODO non sono sicuro che è da fare: MyApi.placeBlock(bs.getX(),bs.getY(),bs.getZ(), "GRAY_BLOCK", GameState.currentUserEmail);
        }
        // Rebuild mesh
        //TODO: non sono sicuro che è da fare: worldRenderer.rebuildMeshes();

        // la posizione di players, se usi p.x/p.y/p.z
        renderOtherPlayers();
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
