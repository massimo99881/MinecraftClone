package com.minecraftclone;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class Camera {
    private float x, y, z;    // Posizione (mondo)
    private float pitch, yaw; // Rotazioni
    private float speed = 0.01f;

    private World world;
    private WorldRenderer worldRenderer;

    // Modalità selezione blocco
    private boolean selectingBlockMode = false;
    private int selectedBlockX, selectedBlockY, selectedBlockZ;

    // Toggle tasto B
    private boolean wasBPressedLastFrame = false;

    public Camera(float x, float y, float z, World world, WorldRenderer worldRenderer) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.worldRenderer = worldRenderer;
    }

    public void updateInput(long window) {
        boolean isBPressed = (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_B) == GLFW.GLFW_PRESS);
        if (isBPressed && !wasBPressedLastFrame) {
            selectingBlockMode = !selectingBlockMode;
            if (selectingBlockMode) {
                // Appena entro in selezione, individuo il TERZO blocco davanti alla camera
                selectBlockInFrontOfCamera();
                System.out.println("** Modalità selezione: ON **");
            } else {
                System.out.println("** Modalità selezione: OFF **");
            }
        }
        wasBPressedLastFrame = isBPressed;

        if (!selectingBlockMode) {
            // Comportamento standard: WASD per muoversi, frecce per ruotare
            handleCameraMovement(window);
            handleCameraRotation(window);
        } else {
            // Selezione blocchi con le frecce (avanti/indietro = ±1 nella direzione del yaw, ecc.)
            handleSelectionMovement(window);

            // ENTER => piazza blocco grigio
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ENTER) == GLFW.GLFW_PRESS) {
                placeBlockAboveSelection();
            }
        }
    }

    /**
     * Quando entriamo in modalità selezione, puntiamo al 3° blocco davanti alla camera
     */
    private void selectBlockInFrontOfCamera() {
        // 3 blocchi davanti, in base a BLOCK_SIZE
        int blocksAhead = 3;
        float dist = blocksAhead * World.BLOCK_SIZE;

        float frontX = x + (float)Math.sin(Math.toRadians(yaw)) * dist;
        float frontZ = z - (float)Math.cos(Math.toRadians(yaw)) * dist;

        int bx = (int)(frontX / World.BLOCK_SIZE);
        int bz = (int)(frontZ / World.BLOCK_SIZE);

        int surfaceY = world.getSurfaceHeight(bx, bz);

        selectedBlockX = bx;
        selectedBlockY = surfaceY;
        selectedBlockZ = bz;
        clampSelection();

        System.out.println("Blocco iniziale selezionato: (" 
            + selectedBlockX + ", " + selectedBlockY + ", " + selectedBlockZ + ")");
    }

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
     * Se siamo in selezione, le frecce spostano la selezione
     * in base al yaw (avanti/indietro, sinistra/destra).
     */
    private void handleSelectionMovement(long window) {
        float radYaw = (float)Math.toRadians(yaw);

        // Avanti
        int forwardDX = Math.round((float)Math.sin(radYaw));
        int forwardDZ = Math.round((float)-Math.cos(radYaw));

        // Sinistra (yaw - 90°)
        float radYawLeft = (float)Math.toRadians(yaw - 90.0f);
        int leftDX = Math.round((float)Math.sin(radYawLeft));
        int leftDZ = Math.round((float)-Math.cos(radYawLeft));

        // Freccia UP => selezione avanti
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS) {
            selectedBlockX += forwardDX;
            selectedBlockZ += forwardDZ;
            clampSelection();
            System.out.println("Selezione => ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
            sleep50ms();
        }
        // Freccia DOWN => selezione indietro
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS) {
            selectedBlockX -= forwardDX;
            selectedBlockZ -= forwardDZ;
            clampSelection();
            System.out.println("Selezione => ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
            sleep50ms();
        }
        // Freccia LEFT => selezione a sinistra
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS) {
            selectedBlockX += leftDX;
            selectedBlockZ += leftDZ;
            clampSelection();
            System.out.println("Selezione => ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
            sleep50ms();
        }
        // Freccia RIGHT => selezione a destra
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS) {
            selectedBlockX -= leftDX;
            selectedBlockZ -= leftDZ;
            clampSelection();
            System.out.println("Selezione => ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
            sleep50ms();
        }
    }

    private void sleep50ms() {
        try { Thread.sleep(50); } catch (InterruptedException e) {}
    }

    private void clampSelection() {
        if (selectedBlockX < 0) selectedBlockX = 0;
        if (selectedBlockX >= World.SIZE_X) selectedBlockX = World.SIZE_X - 1;
        if (selectedBlockY < 0) selectedBlockY = 0;
        if (selectedBlockY >= World.HEIGHT) selectedBlockY = World.HEIGHT - 1;
        if (selectedBlockZ < 0) selectedBlockZ = 0;
        if (selectedBlockZ >= World.SIZE_Z) selectedBlockZ = World.SIZE_Z - 1;
    }

    /**
     * Posiziona il blocco GRIGIO sopra il blocco selezionato
     * e ricostruisce la mesh.
     */
    private void placeBlockAboveSelection() {
        int bx = selectedBlockX;
        int by = selectedBlockY + 1; // sopra
        int bz = selectedBlockZ;

        if (world.getBlock(bx, by, bz) == Block.AIR) {
            world.setBlock(bx, by, bz, Block.GRAY_BLOCK);
            worldRenderer.rebuildMeshes();
            System.out.println("🧱 Blocco GRIGIO in ("+bx+", "+by+", "+bz+")");
        } else {
            System.out.println("❌ Non posso piazzare sopra: ("+bx+", "+by+", "+bz+"): non è aria.");
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
        GL11.glRotatef(pitch, 1, 0, 0);
        GL11.glRotatef(yaw,   0, 1, 0);
        // Aggiungiamo un piccolo offset in y
        GL11.glTranslatef(-x, -(y + 0.1f), -z);
    }

    // Getter
    public boolean isSelectingBlockMode() { return selectingBlockMode; }
    public int getSelectedBlockX() { return selectedBlockX; }
    public int getSelectedBlockY() { return selectedBlockY; }
    public int getSelectedBlockZ() { return selectedBlockZ; }
}
