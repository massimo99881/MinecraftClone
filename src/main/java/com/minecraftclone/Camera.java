package com.minecraftclone;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class Camera {
    private float x, y, z;      // Posizione camera
    private float pitch, yaw;   // Rotazioni camera
    private float speed = 0.01f;

    private World world;
    private WorldRenderer worldRenderer;

    // Modalità selezione blocco
    private boolean selectingBlockMode = false;
    private int selectedBlockX, selectedBlockY, selectedBlockZ;

    // Per gestire il toggle del tasto B
    private boolean wasBPressedLastFrame = false;

    public Camera(float x, float y, float z, World world, WorldRenderer worldRenderer) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.worldRenderer = worldRenderer;
    }

    /**
     * Viene richiamato ogni frame per gestire input e logiche di movimento
     */
    public void updateInput(long window) {
        // 1) Toggle B
        boolean isBPressed = (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_B) == GLFW.GLFW_PRESS);
        if (isBPressed && !wasBPressedLastFrame) {
            selectingBlockMode = !selectingBlockMode;
            if (selectingBlockMode) {
                // Se entriamo in selezione, individuiamo un blocco vicino
                selectBlockInFrontOfCamera();
                System.out.println("** Modalità selezione: ON **");
            } else {
                System.out.println("** Modalità selezione: OFF **");
            }
        }
        wasBPressedLastFrame = isBPressed;

        // 2) Se non siamo in selezione, movimenti/rotazioni della camera
        //    Se siamo in selezione, tasti WASD e SHIFT/SPACE assumono significato diverso
        if (!selectingBlockMode) {
            handleCameraMovement(window);
            handleCameraRotation(window);
        } else {
            // Frecce per muovere la selezione in avanti/dietro/sinistra/destra (rispetto allo yaw)
            handleSelectionXZMovement(window);

            // SHIFT e SPACE per muovere il blocco su/giù
            handleSelectionUpDown(window);

            // Se premi ENTER, piazzi il blocco grigio nella posizione selezionata
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ENTER) == GLFW.GLFW_PRESS) {
                placeBlockAtSelection();
            }
        }
    }

    /**
     * Seleziona un blocco "davanti" alla camera (distanza fissa)
     */
    private void selectBlockInFrontOfCamera() {
        float dist = 1.5f; // ad esempio 1.5 metri davanti alla camera
        float frontX = x + (float)Math.sin(Math.toRadians(yaw)) * dist;
        float frontZ = z - (float)Math.cos(Math.toRadians(yaw)) * dist;

        int bx = (int)(frontX / World.BLOCK_SIZE);
        int bz = (int)(frontZ / World.BLOCK_SIZE);

        // Trova la superficie del terreno in quelle coordinate
        int surfaceY = world.getSurfaceHeight(bx, bz);

        selectedBlockX = bx;
        selectedBlockY = surfaceY;
        selectedBlockZ = bz;
        clampSelection();

        System.out.println("Blocco selezionato iniziale: ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
    }

    /**
     * SHIFT e SPACE spostano la selezione in alto e in basso (y ± 1)
     */
    private void handleSelectionUpDown(long window) {
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS) {
            selectedBlockY += 1;
            clampSelection();
            System.out.println("Selezione spostata su: ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
            sleep50ms();
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) {
            selectedBlockY -= 1;
            clampSelection();
            System.out.println("Selezione spostata giù: ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
            sleep50ms();
        }
    }

    /**
     * In modalità selezione, le frecce spostano la selezione in piano XZ
     * in base all'orientamento (yaw) della camera
     */
    private void handleSelectionXZMovement(long window) {
        float radYaw = (float)Math.toRadians(yaw);

        // Direzione "avanti"
        int forwardDX = Math.round((float)Math.sin(radYaw));
        int forwardDZ = Math.round((float)-Math.cos(radYaw));

        // Direzione "sinistra" => yaw - 90
        float radYawLeft = (float)Math.toRadians(yaw - 90.0f);
        int leftDX = Math.round((float)Math.sin(radYawLeft));
        int leftDZ = Math.round((float)-Math.cos(radYawLeft));

        // FRECCIA UP => avanti
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS) {
            selectedBlockX += forwardDX;
            selectedBlockZ += forwardDZ;
            clampSelection();
            System.out.println("Selezione => ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
            sleep50ms();
        }
        // FRECCIA DOWN => indietro
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS) {
            selectedBlockX -= forwardDX;
            selectedBlockZ -= forwardDZ;
            clampSelection();
            System.out.println("Selezione => ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
            sleep50ms();
        }
        // FRECCIA LEFT => a sinistra
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS) {
            selectedBlockX += leftDX;
            selectedBlockZ += leftDZ;
            clampSelection();
            System.out.println("Selezione => ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
            sleep50ms();
        }
        // FRECCIA RIGHT => a destra
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS) {
            selectedBlockX -= leftDX;
            selectedBlockZ -= leftDZ;
            clampSelection();
            System.out.println("Selezione => ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
            sleep50ms();
        }
    }

    /**
     * Piazza il blocco grigio **esattamente** nella posizione selezionata
     * (niente +1).
     */
    private void placeBlockAtSelection() {
        int bx = selectedBlockX;
        int by = selectedBlockY;
        int bz = selectedBlockZ;

        // Se è aria, posizioniamo GRAY_BLOCK
        if (world.getBlock(bx, by, bz) == Block.AIR) {
            world.setBlock(bx, by, bz, Block.GRAY_BLOCK);
            worldRenderer.rebuildMeshes();
            System.out.println("🧱 Blocco GRIGIO posizionato in ("+bx+", "+by+", "+bz+")");
        } else {
            System.out.println("❌ Non posso piazzare blocco in ("+bx+", "+by+", "+bz+"): non è aria.");
        }
    }

    /**
     * Movimenti standard della camera (WASD, SPACE/SHIFT).
     * Vengono usati solo se *non* in selezione.
     */
    private void handleCameraMovement(long window) {
        float dx = 0, dy = 0, dz = 0;

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

        // In modalità standard, SHIFT e SPACE muovono la camera su/giù
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
     * Rotazioni camera con le frecce (usato solo in modalità standard).
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

    private void clampSelection() {
        if (selectedBlockX < 0) selectedBlockX = 0;
        if (selectedBlockX >= World.SIZE_X) selectedBlockX = World.SIZE_X - 1;
        if (selectedBlockY < 0) selectedBlockY = 0;
        if (selectedBlockY >= World.HEIGHT) selectedBlockY = World.HEIGHT - 1;
        if (selectedBlockZ < 0) selectedBlockZ = 0;
        if (selectedBlockZ >= World.SIZE_Z) selectedBlockZ = World.SIZE_Z - 1;
    }

    /**
     * Movimento con collisione semplice
     */
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

    /**
     * Per non spostare la selezione decine di volte col singolo key press
     */
    private void sleep50ms() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void applyTransformations() {
        // Pipeline fissa
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
