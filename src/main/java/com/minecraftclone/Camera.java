package com.minecraftclone;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class Camera {
    private float x, y, z;      // Posizione camera (coordinate mondo)
    private float pitch, yaw;   // Rotazioni camera
    private float speed = 0.01f;

    private World world;
    private WorldRenderer worldRenderer;

    // Modalità selezione blocco
    private boolean selectingBlockMode = false;
    private int selectedBlockX, selectedBlockY, selectedBlockZ;

    // Toggle B
    private boolean wasBPressedLastFrame = false;

    public Camera(float x, float y, float z, World world, WorldRenderer worldRenderer) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.worldRenderer = worldRenderer;
    }

    /**
     * Invocata ad ogni frame per controllare input.
     */
    public void updateInput(long window) {
        // 1. Gestione tasto B (toggle)
        boolean isBPressed = (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_B) == GLFW.GLFW_PRESS);
        if (isBPressed && !wasBPressedLastFrame) {
            selectingBlockMode = !selectingBlockMode;
            if (selectingBlockMode) {
                selectBlockInFront(); // individua il blocco di fronte
                System.out.println("** Modalità selezione: ON **");
            } else {
                System.out.println("** Modalità selezione: OFF **");
            }
        }
        wasBPressedLastFrame = isBPressed;

        // 2. Se non sei in selezione, le frecce ruotano la camera,
        //    WASD e SPACE/SHIFT muovono la camera.
        //    Se sei in selezione, le frecce spostano la selezione.
        if (!selectingBlockMode) {
            handleCameraMovement(window);
            handleCameraRotation(window);
        } else {
            handleSelectionMovement(window);
        }

        // 3. Invio => piazza blocco sopra quello selezionato (solo in modalità selezione)
        if (selectingBlockMode) {
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ENTER) == GLFW.GLFW_PRESS) {
                placeBlockAboveSelection();
            }
        }
    }

    /**
     * Movimenti della camera: WASD, SPACE, SHIFT.
     */
    private void handleCameraMovement(long window) {
        float dx = 0, dy = 0, dz = 0;

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

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS) {
            dy += speed;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) {
            dy -= speed;
        }

        attemptMove(dx, 0, 0);
        attemptMove(0, dy, 0);
        attemptMove(0, 0, dz);
    }

    /**
     * Rotazioni con le frecce direzionali.
     * (Solo quando NON siamo in selezione!)
     */
    private void handleCameraRotation(long window) {
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
    }

    /**
     * Se siamo in modalità selezione, le frecce spostano la selezione
     * (±1 in X / Z). Usiamo un piccolo "debounce" (sleep) per non scorrere
     * troppo velocemente i blocchi.
     */
    private void handleSelectionMovement(long window) {
        // Freccia UP => z--
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS) {
            selectedBlockZ--;
            clampSelection();
            System.out.println("Selezione: ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
            sleep50ms();
        }
        // Freccia DOWN => z++
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS) {
            selectedBlockZ++;
            clampSelection();
            System.out.println("Selezione: ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
            sleep50ms();
        }
        // Freccia LEFT => x--
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS) {
            selectedBlockX--;
            clampSelection();
            System.out.println("Selezione: ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
            sleep50ms();
        }
        // Freccia RIGHT => x++
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS) {
            selectedBlockX++;
            clampSelection();
            System.out.println("Selezione: ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
            sleep50ms();
        }
    }

    private void sleep50ms() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Limita la selezione entro i confini del mondo.
     */
    private void clampSelection() {
        if (selectedBlockX < 0) selectedBlockX = 0;
        if (selectedBlockX >= World.SIZE_X) selectedBlockX = World.SIZE_X - 1;
        if (selectedBlockY < 0) selectedBlockY = 0;
        if (selectedBlockY >= World.HEIGHT) selectedBlockY = World.HEIGHT - 1;
        if (selectedBlockZ < 0) selectedBlockZ = 0;
        if (selectedBlockZ >= World.SIZE_Z) selectedBlockZ = World.SIZE_Z - 1;
    }

    /**
     * Individua il blocco “frontalmente” alla camera
     * e lo seleziona come “blocco attuale”.
     */
    private void selectBlockInFront() {
        float dist = 2.0f; // ad es. 2 metri davanti
        float frontX = x + (float)Math.sin(Math.toRadians(yaw)) * dist;
        float frontZ = z - (float)Math.cos(Math.toRadians(yaw)) * dist;

        int bx = (int)(frontX / World.BLOCK_SIZE);
        int bz = (int)(frontZ / World.BLOCK_SIZE);
        int surfaceY = world.getSurfaceHeight(bx, bz);

        selectedBlockX = bx;
        selectedBlockY = surfaceY;
        selectedBlockZ = bz;
        clampSelection();

        System.out.println("Blocco iniziale selezionato: ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
    }

    /**
     * Piazza il blocco grigio SOPRA il blocco selezionato
     * e ricostruisce la mesh.
     */
    private void placeBlockAboveSelection() {
        int bx = selectedBlockX;
        int by = selectedBlockY + 1; // sopra
        int bz = selectedBlockZ;

        if (world.getBlock(bx, by, bz) == Block.AIR) {
            world.setBlock(bx, by, bz, Block.GRAY_BLOCK);
            worldRenderer.rebuildMeshes();
            System.out.println("Blocco GRIGIO posizionato in: ("+bx+", "+by+", "+bz+")");
        } else {
            System.out.println("❌ Non posso piazzare sopra ("+bx+", "+by+", "+bz+"): non è aria.");
        }
    }

    private void attemptMove(float dx, float dy, float dz) {
        float newX = x + dx;
        float newY = y + dy;
        float newZ = z + dz;

        if (!isColliding(newX, newY, newZ)) {
            x = newX;
            y = newY;
            z = newZ;
        }
    }

    private boolean isColliding(float nx, float ny, float nz) {
        int bx = (int)(nx / World.BLOCK_SIZE);
        int by = (int)(ny / World.BLOCK_SIZE);
        int bz = (int)(nz / World.BLOCK_SIZE);

        if (bx < 0 || bx >= World.SIZE_X ||
            by < 0 || by >= World.HEIGHT ||
            bz < 0 || bz >= World.SIZE_Z) {
            return false;
        }
        return world.getBlock(bx, by, bz).isSolid();
    }

    public void applyTransformations() {
        float cameraOffset = 0.1f;
        GL11.glRotatef(pitch, 1, 0, 0);
        GL11.glRotatef(yaw,   0, 1, 0);
        GL11.glTranslatef(-x, -(y + cameraOffset), -z);
    }

    public boolean isSelectingBlockMode() {
        return selectingBlockMode;
    }

    public int getSelectedBlockX() { return selectedBlockX; }
    public int getSelectedBlockY() { return selectedBlockY; }
    public int getSelectedBlockZ() { return selectedBlockZ; }
}
