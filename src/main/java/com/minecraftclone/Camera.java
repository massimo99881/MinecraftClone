package com.minecraftclone;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class Camera {
    private float x, y, z;  // Posizione in coordinate mondo
    private float pitch, yaw;
    private float speed = 0.01f;
    
    private static final float WIDTH = 0.09f;
    private static final float HEIGHT = 0.3f;
    private static final float DEPTH = 0.09f;

    boolean placingBlockMode = false;  // Se attivo, mostriamo anteprima di blocco
    float targetX;   // Coordinate (in INDICI blocco) dell'anteprima
	float targetY;
	float targetZ;
    private World world;
    private WorldRenderer worldRenderer;

    // Variabile per gestire il toggle di B
    private boolean bKeyWasPressed = false; // <-- MOD

    public Camera(float x, float y, float z, World world, WorldRenderer wr) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.worldRenderer = wr;
    }

    public void updateInput(long window, World world) {
        float dx = 0, dy = 0, dz = 0;

        // --- LOGICA DI ATTIVAZIONE con Toggle su B ---
        boolean bKeyPressed = (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_B) == GLFW.GLFW_PRESS);
        if (bKeyPressed && !bKeyWasPressed) {
            placingBlockMode = !placingBlockMode;
            if (placingBlockMode) {
                updateBlockPreview();
                System.out.println("🟨 Modalità posizionamento blocco: ON (B premuto)");
            } else {
                System.out.println("🟨 Modalità posizionamento blocco: OFF (B premuto)");
            }
        }
        bKeyWasPressed = bKeyPressed;
        // --- Fine toggle B ---

        // Conferma del posizionamento (ENTER)
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ENTER) == GLFW.GLFW_PRESS && placingBlockMode) {
            placeBlock();
            // Annulliamo la modalità dopo il posizionamento:
            placingBlockMode = false; 
        }

        // Movimenti orizzontali (WASD)
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) {
            dx += (float) Math.sin(Math.toRadians(yaw)) * speed;
            dz -= (float) Math.cos(Math.toRadians(yaw)) * speed;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) {
            dx -= (float) Math.sin(Math.toRadians(yaw)) * speed;
            dz += (float) Math.cos(Math.toRadians(yaw)) * speed;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) {
            dx -= (float) Math.cos(Math.toRadians(yaw)) * speed;
            dz -= (float) Math.sin(Math.toRadians(yaw)) * speed;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) {
            dx += (float) Math.cos(Math.toRadians(yaw)) * speed;
            dz += (float) Math.sin(Math.toRadians(yaw)) * speed;
        }

        // Movimenti verticali (Salto e Discesa)
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS) {
            dy += speed;  // Salto
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) {
            dy -= speed;  // Discesa
        }

        // Rotazione con le frecce direzionali
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS) {
            pitch -= 1.0f;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS) {
            pitch += 1.0f;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS) {
            yaw -= 1.0f;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS) {
            yaw += 1.0f;
        }

        // Controlli collisioni e muovi la camera
        attemptMove(dx, 0, 0, world);
        attemptMove(0, dy, 0, world);
        attemptMove(0, 0, dz, world);
    }
    
    /**
     * Calcola la posizione "in avanti" della camera, trova la superficie del terreno
     * e stabilisce dove far apparire il blocco di anteprima.
     */
    private void updateBlockPreview() {
        // lookX, lookZ in coordinate MONDO (float)
        float lookX = x + (float) Math.sin(Math.toRadians(yaw)) * 1.5f;
        float lookZ = z - (float) Math.cos(Math.toRadians(yaw)) * 1.5f;
        
        // Ottieni l'altezza del terreno (metodo vuole parametri in blocchi o in mondo?)
        // La firma dice: getSurfaceHeight(int x, int z) ma all'interno del World
        // c'è un ulteriore divisione per BLOCK_SIZE. Quindi qui passiamo "mondo" in int.
        int groundY = world.getSurfaceHeight((int) lookX, (int) lookZ);
        
        // targetX, Y, Z in INDICI di blocco
        targetX = (int) (lookX / World.BLOCK_SIZE);
        targetY = groundY + 1;  
        targetZ = (int) (lookZ / World.BLOCK_SIZE);

        System.out.println("🔎 Anteprima posizionata su: ("
                            + (int)targetX + ", " 
                            + (int)targetY + ", " 
                            + (int)targetZ + ")");
    }
    
    /**
     * Piazza il blocco grigio nella posizione calcolata da updateBlockPreview().
     */
    private void placeBlock() {
        int bx = (int) targetX;
        int by = (int) targetY;
        int bz = (int) targetZ;

        if (world.getBlock(bx, by, bz) == Block.AIR) {
            world.setBlock(bx, by, bz, Block.GRAY_BLOCK);
            System.out.println("🧱 Blocco GRIGIO posizionato in ("+bx+", "+by+", "+bz+")");
            worldRenderer.rebuildMeshes();
        } else {
            System.out.println("❌ Spazio già occupato! Blocco non posizionato.");
        }
    }

    private void attemptMove(float dx, float dy, float dz, World world) {
        float newX = x + dx;
        float newY = y + dy;
        float newZ = z + dz;

        float oldY = y; // Salviamo la posizione precedente della Y

        if (!collides(newX, newY, newZ, world)) {
            x = newX;
            y = newY;
            z = newZ;
        } else {
            // Se è collisione verso il basso, verifichiamo se stiamo sprofondando
            if (dy < 0) {
                int surfaceHeight = world.getSurfaceHeight(
                        (int) Math.floor(newX / World.BLOCK_SIZE),
                        (int) Math.floor(newZ / World.BLOCK_SIZE));
                
                float expectedY = surfaceHeight * World.BLOCK_SIZE - 0.1f;
                if (y < expectedY) {
                    y = expectedY;
                    System.out.println("🚨 Telecamera riallineata alla superficie del terreno!");
                } else {
                    y = oldY;
                }
            }
        }
    }

    private boolean collides(float nx, float ny, float nz, World world) {
        // Controllo semplificato a 5 punti
        return checkCollision(nx, ny, nz, world) ||
               checkCollision(nx + WIDTH, ny, nz, world) ||
               checkCollision(nx, ny + HEIGHT, nz, world) ||
               checkCollision(nx, ny, nz + DEPTH, world) ||
               checkCollision(nx + WIDTH, ny + HEIGHT, nz + DEPTH, world);
    }

    private boolean checkCollision(float x, float y, float z, World world) {
        int blockX = (int) Math.floor(x / World.BLOCK_SIZE);
        int blockY = (int) Math.floor(y / World.BLOCK_SIZE);
        int blockZ = (int) Math.floor(z / World.BLOCK_SIZE);

        return world.getBlock(blockX, blockY, blockZ).isSolid();
    }

    /**
     * Applica rotazioni e traslazioni OpenGL in base a pitch,yaw e (x,y,z) della camera.
     */
    public void applyTransformations() {
        float cameraOffset = 0.1f; 
        GL11.glRotatef(pitch, 1, 0, 0);
        GL11.glRotatef(yaw,   0, 1, 0);
        GL11.glTranslatef(-x, -(y + cameraOffset), -z);
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }
}
