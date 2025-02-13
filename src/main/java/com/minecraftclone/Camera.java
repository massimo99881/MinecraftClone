package com.minecraftclone;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.minecraftclone.network.MyApi;
import com.minecraftclone.state.BlockState;
import com.minecraftclone.state.GameState;

public class Camera {
	
	private static class PositionUpdate {
        String email;
        float x, y, z;

        PositionUpdate(String email, float x, float y, float z) {
            this.email = email;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
	
    private float x, y, z;       // Posizione (mondo)
    private float pitch, yaw;    // Rotazioni
    private float speed = 0.005f; // Movimento WASD orizzontale

    // Bounding box per collisione
    private static final float COLLISION_WIDTH  = 0.15f;
    private static final float COLLISION_HEIGHT = 1.0f;

    // --- GRAVITÀ e SALTO ---
    private boolean useGravity = true;   // Se true, la gravità spinge in giù
    private float velocityY = 0f;        // Velocità verticale
    private final float GRAVITY = -0.0058f; // Gravità “lenta”, da tarare
    private final float JUMP_VELOCITY = 1.0f * World.BLOCK_SIZE / 0.260f; 

    private static final float POSITION_UPDATE_INTERVAL = 0.5f; 
    private float timeSinceLastUpdate = 0f; 
    // se vuoi inviare ~2 volte al secondo

    private World world;
    private WorldRenderer worldRenderer;
    
    // Coda per le posizioni da inviare
    private static ConcurrentLinkedQueue<PositionUpdate> positionQueue = new ConcurrentLinkedQueue<>();
    

    private volatile boolean running = true;

    // Modalità selezione blocco (B)
    private boolean selectingBlockMode = false;
    private int selectedBlockX, selectedBlockY, selectedBlockZ;
    private boolean wasBPressedLastFrame = false;

    public Camera(float startX, float startY, float startZ, World world, WorldRenderer worldRenderer) {
        this.x = startX;
        this.y = startY;
        this.z = startZ;
        this.world = world;
        this.worldRenderer = worldRenderer;
        
        // Avvia il thread di aggiornamento posizione
        startPositionUpdateThread();
        
        // Avvia il thread di sincronizzazione blocchi
        startBlockSyncThread();
    }
    
   
    
    private void startPositionUpdateThread() {
        Thread positionThread = new Thread(() -> {
            while (running) {
                PositionUpdate update = positionQueue.poll();
                if (update != null) {
                    MyApi.updatePosition(update.email, update.x, update.y, update.z);
                }
                try {
                    Thread.sleep(1000); // Sincronizza ogni 1 s per ridurre carico REST
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "PositionUpdateThread");

        positionThread.start();
    }
    
 // Thread che scarica e aggiorna i blocchi dal server
    private void startBlockSyncThread() {
        Thread blockSyncThread = new Thread(() -> {
            while (running) {
                List<BlockState> blocks = MyApi.getAllBlocks(); 

                if (blocks != null && !blocks.isEmpty()) {
                	boolean updated = false; // Flag per evitare update inutili
                    for (BlockState bs : blocks) {
                        Block currentBlock = world.getBlock(bs.getX(), bs.getY(), bs.getZ());

                        if (currentBlock == Block.AIR) { // Solo se è vuoto
                            world.setBlock(bs.getX(), bs.getY(), bs.getZ(), Block.GRAY_BLOCK);
                            
                            updated = true; // Abbiamo aggiornato almeno un blocco
                        }
                    }
                    if (updated) {
                    	// ✅ Accodiamo l'operazione da eseguire nel thread principale
                    	Main.requestMeshRebuild = true;
                    }
                }

                try {
                    Thread.sleep(2000); // Scarica i blocchi ogni 2 secondi
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "BlockSyncThread");

        blockSyncThread.start();
    }


    
    public void stopThreads() {
        running = false;
    }

    public void updateInput(long window) {
        // 1) Toggle B
        boolean isBPressed = (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_B) == GLFW.GLFW_PRESS);
        if (isBPressed && !wasBPressedLastFrame) {
            selectingBlockMode = !selectingBlockMode;
            if (selectingBlockMode) {
                selectBlockNearFront();
                System.out.println("[Modalità selezione: ON]");
            } else {
                System.out.println("[Modalità selezione: OFF]");
            }
        }
        wasBPressedLastFrame = isBPressed;

        // 2) Selezione blocchi (B) => spostiamo blocco, SHIFT/SPACE muove blocco
        if (selectingBlockMode) {
            handleSelectionXZMovement(window);
            handleSelectionUpDown(window);

            // ENTER => piazza blocco
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ENTER) == GLFW.GLFW_PRESS) {
                placeBlockHere();
            }
        } 
        else {
            // 3) Movimenti standard: WASD, SHIFT/SPACE => volo / salto
            handleCameraMovement(window);
            handleCameraRotation(window);
        }

        // 4) Gestione gravità personalizzata
        handleGravityAndJump(window);

        // 5) Applica movimento verticale e collisioni
        applyVerticalMovement();
    }

    /**
     * Gestisce la logica della “gravità” personalizzata:
     * - Se non premi SPACE, la gravità tira in giù
     * - Se premi SPACE una volta, fai un salto di 3 blocchi
     * - Se tieni premuto SPACE, continui a salire
     * - Se rilasci SPACE in aria, ti fermi (velocityY = 0) e rimani sospeso
     */
    private void handleGravityAndJump(long window) {
        // Se vogliamo usare la gravità
        if (useGravity) {
            // Controlliamo se premi SPACE
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS) {
                // Se velocityY <= 0 => stiamo a terra o stiamo cadendo => set jump
                if (velocityY <= 0f) {
                    // Un singolo colpo => jump di 3 blocchi
                    velocityY = JUMP_VELOCITY; 
                    // E continuiamo a salire finché tieni premuto space
                } else {
                    // Se velocityY > 0 e tieni premuto => continua a salire
                    velocityY = JUMP_VELOCITY; // reset velocity positiva
                }
            } 
            else {
                // Se RILASCI SPACE mentre velocityY > 0 => ti fermi in aria
                if (velocityY > 0f) {
                    velocityY = 0f; // rimani sospeso
                } 
                else {
                    // Altrimenti, se non stai salendo, la gravità tira giù
                    velocityY += GRAVITY;
                }
            }
        }
    }

    /**
     * Sposta la camera verticalmente in base alla velocityY.
     * Controlla collisione: se colpisco un blocco, mi fermo e setto velocityY=0.
     */
    private void applyVerticalMovement() {
        if (velocityY != 0f) {
            float oldY = y;
            float dy = velocityY * 0.016f; 
            // assumendo ~16ms/frame => v = dx/dt => dx = v*dt
            // se hai un loop a 60fps costante, ~0.016f secondi per frame

            float newY = y + dy;
            if (!collidesWithBlocks(x, newY, z)) {
                y = newY;
            } else {
                // Collisione in su o in giù => stop
                velocityY = 0f;
                // Se era un salto e colpisco la "volta", mi fermo
                // Se era caduta e colpisco il terreno, mi fermo
                y = oldY; 
            }
        }
    }

    private void handleCameraMovement(long window) {
        // Movimenti WASD (orizzontale)
        float dx = 0f, dz = 0f;

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) {
            dx += (float)Math.sin(Math.toRadians(yaw)) * speed;
            dz -= (float)Math.cos(Math.toRadians(yaw)) * speed;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) {
            dx -= (float)Math.sin(Math.toRadians(yaw)) * speed;
            dz += (float)Math.cos(Math.toRadians(yaw)) * speed;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) {
            dx -= (float)Math.cos(Math.toRadians(yaw)) * speed;
            dz -= (float)Math.sin(Math.toRadians(yaw)) * speed;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) {
            dx += (float)Math.cos(Math.toRadians(yaw)) * speed;
            dz += (float)Math.sin(Math.toRadians(yaw)) * speed;
        }

        // SHIFT => scendere come “volo” (solo se vogliamo “creative”)
        // in modo simile potresti fare: velocityY = -someValue
        // ma la specifica dice "spaziatrice" + "gravità" particolare, quindi decidi tu.
        // Nel frattempo, proviamo a muovere solo orizzontalmente
        attemptMove(dx, 0f, dz);
    }

    private void handleCameraRotation(long window) {
        // Frecce per pitch, yaw
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS) {
            pitch -= 1f;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS) {
            pitch += 1f;
        }
        // Limita pitch
        pitch = Math.max(-85f, Math.min(85f, pitch));

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS) {
            yaw -= 1f;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS) {
            yaw += 1f;
        }
    }

    /**
     * Spostiamo la camera orizzontalmente, controllando collisione con 18 punti
     */
    private void attemptMove(float dx, float dy, float dz) {
        float newX = x + dx;
        float newY = y + dy;
        float newZ = z + dz;

        if (!collidesWithBlocks(newX, newY, newZ)) {
            x = newX;
            y = newY;
            z = newZ;

            timeSinceLastUpdate += 0.016f; // se il loop ~60fps
            if (timeSinceLastUpdate >= POSITION_UPDATE_INTERVAL) {
                sendPositionToServer();
                timeSinceLastUpdate = 0f;
            }
        }
    }
    
    private void sendPositionToServer() {
        
     // Aggiunge una richiesta di aggiornamento alla coda
        positionQueue.add(new PositionUpdate(GameState.currentUserEmail, x, y, z));
    }

    /**
     * Collisione a 18 punti: 3 in X, 2 in Y (base e top), 3 in Z => 3×2×3 = 18
     */
    private boolean collidesWithBlocks(float nx, float ny, float nz) {
        float halfW = COLLISION_WIDTH / 2f;
        float topY  = ny + COLLISION_HEIGHT;

        // Discretizziamo su -halfW, 0, +halfW
        float[] checkX = { nx - halfW, nx, nx + halfW };
        float[] checkY = { ny, topY };
        float[] checkZ = { nz - halfW, nz, nz + halfW };

        for (float cx : checkX) {
            for (float cy : checkY) {
                for (float cz : checkZ) {
                    if (isSolidBlockAt(cx, cy, cz)) {
                        return true; 
                    }
                }
            }
        }
        return false;
    }

    private boolean isSolidBlockAt(float wx, float wy, float wz) {
        int bx = (int)Math.floor(wx / World.BLOCK_SIZE);
        int by = (int)Math.floor(wy / World.BLOCK_SIZE);
        int bz = (int)Math.floor(wz / World.BLOCK_SIZE);

        if (bx < 0 || bx >= World.SIZE_X ||
            by < 0 || by >= World.HEIGHT ||
            bz < 0 || bz >= World.SIZE_Z) {
            return false;
        }
        return world.getBlock(bx, by, bz).isSolid();
    }

    /**
     * B mode: SHIFT/SPACE spostano Y del blocco selezionato
     */
    private void handleSelectionUpDown(long window) {
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS) {
            selectedBlockY++;
            clampSelection();
            System.out.println("Selezione su: ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
            sleep50ms();
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) {
            selectedBlockY--;
            clampSelection();
            System.out.println("Selezione giù: ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
            sleep50ms();
        }
    }

    /**
     * B mode: frecce spostano XZ in base a yaw
     */
    private void handleSelectionXZMovement(long window) {
        float rad = (float)Math.toRadians(yaw);
        int forwardDX = Math.round((float)Math.sin(rad));
        int forwardDZ = Math.round((float)-Math.cos(rad));

        float radLeft = (float)Math.toRadians(yaw - 90f);
        int leftDX = Math.round((float)Math.sin(radLeft));
        int leftDZ = Math.round((float)-Math.cos(radLeft));

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS) {
            selectedBlockX += forwardDX;
            selectedBlockZ += forwardDZ;
            clampSelection();
            System.out.println("Selezione: ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
            sleep50ms();
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS) {
            selectedBlockX -= forwardDX;
            selectedBlockZ -= forwardDZ;
            clampSelection();
            System.out.println("Selezione: ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
            sleep50ms();
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS) {
            selectedBlockX += leftDX;
            selectedBlockZ += leftDZ;
            clampSelection();
            System.out.println("Selezione: ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
            sleep50ms();
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS) {
            selectedBlockX -= leftDX;
            selectedBlockZ -= leftDZ;
            clampSelection();
            System.out.println("Selezione: ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
            sleep50ms();
        }
    }

    private void selectBlockNearFront() {
        float dist = 3.0f * World.BLOCK_SIZE;
        float frontX = x + (float)Math.sin(Math.toRadians(yaw)) * dist;
        float frontZ = z - (float)Math.cos(Math.toRadians(yaw)) * dist;

        int bx = (int)(frontX / World.BLOCK_SIZE);
        int bz = (int)(frontZ / World.BLOCK_SIZE);

        int surfaceY = world.getSurfaceHeight(bx, bz);
        selectedBlockX = bx;
        selectedBlockY = surfaceY;
        selectedBlockZ = bz;
        clampSelection();

        System.out.println("Blocco selezionato iniziale: ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
    }

    private void placeBlockHere() {
        int bx = selectedBlockX;
        int by = selectedBlockY;
        int bz = selectedBlockZ;

        if (world.getBlock(bx, by, bz) == Block.AIR) {
            world.setBlock(bx, by, bz, Block.GRAY_BLOCK);
            //TODO non so se è meglio: worldRenderer.updateBlockMesh(bx, by, bz); // 🔥 Aggiorna solo il blocco modificato!
            worldRenderer.rebuildMeshes();
            
            // 🚀 Invio asincrono per non rallentare il gioco
            new Thread(() -> MyApi.placeBlock(bx, by, bz, "GRAY_BLOCK", GameState.currentUserEmail)).start();
            
            System.out.println("🧱 Blocco GRIGIO posizionato in (" + bx + ", " + by + ", " + bz + ")");
        } else {
            System.out.println("❌ Non posso piazzare in (" + bx + ", " + by + ", " + bz + "): non è aria");
        }
    }

    private void clampSelection() {
        if (selectedBlockX < 0) selectedBlockX = 0;
        if (selectedBlockX >= World.SIZE_X) selectedBlockX = World.SIZE_X - 1;

        if (selectedBlockY < 0) selectedBlockY = 0;
        if (selectedBlockY >= World.HEIGHT) selectedBlockY = World.HEIGHT - 1;

        if (selectedBlockZ < 0) selectedBlockZ = 0;
        if (selectedBlockZ >= World.SIZE_Z) selectedBlockZ = World.SIZE_Z - 1;
    }

    private void sleep50ms() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {}
    }

    public void applyTransformations() {
        GL11.glRotatef(pitch, 1, 0, 0);
        GL11.glRotatef(yaw,   0, 1, 0);
        // offset: alziamo di 0.1f per non intersecare col terreno
        GL11.glTranslatef(-x, -(y + 0.1f), -z);
    }

    public boolean isSelectingBlockMode() {
        return selectingBlockMode;
    }
    public int getSelectedBlockX() { return selectedBlockX; }
    public int getSelectedBlockY() { return selectedBlockY; }
    public int getSelectedBlockZ() { return selectedBlockZ; }
}
