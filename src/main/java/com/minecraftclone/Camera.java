package com.minecraftclone;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class Camera {
    // Coordinate “mondo” della camera
    private float x, y, z;
    private float pitch, yaw;
    private float speed = 0.01f;

    // Dimensioni bounding box della camera (larghezza, altezza)
    // per evitare di entrare nei blocchi
    private static final float COLLISION_WIDTH  = 0.15f;  // diametro “personaggio”
    private static final float COLLISION_HEIGHT = 1.0f;  // altezza

    private World world;
    private WorldRenderer worldRenderer;

    // Modalità selezione blocco
    private boolean selectingBlockMode = false;
    private int selectedBlockX, selectedBlockY, selectedBlockZ;

    // Per gestire toggle B
    private boolean wasBPressedLastFrame = false;

    public Camera(float startX, float startY, float startZ, World world, WorldRenderer worldRenderer) {
        this.x = startX;
        this.y = startY;
        this.z = startZ;
        this.world = world;
        this.worldRenderer = worldRenderer;
    }

    /**
     * Viene chiamato ad ogni frame per gestire input e logica di movimento/posizionamento.
     */
    public void updateInput(long window) {
        // Toggle B
        boolean isBPressed = (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_B) == GLFW.GLFW_PRESS);
        if (isBPressed && !wasBPressedLastFrame) {
            selectingBlockMode = !selectingBlockMode;
            if (selectingBlockMode) {
                // Se entriamo in modalità selezione, individua un blocco vicino (3 blocchi davanti)
                selectBlockNearFront();
                System.out.println("[Modalità selezione: ON]");
            } else {
                System.out.println("[Modalità selezione: OFF]");
            }
        }
        wasBPressedLastFrame = isBPressed;

        if (!selectingBlockMode) {
            // 1) Se NON siamo in selezione: WASD e SHIFT/SPACE muovono la camera, frecce ruotano
            handleCameraMovement(window);
            handleCameraRotation(window);
        } else {
            // 2) Se SIAMO in selezione: frecce spostano la selezione in piano XZ,
            //    SHIFT/SPACE la spostano su/giù
            handleSelectionXZMovement(window);
            handleSelectionUpDown(window);

            // ENTER -> posiziona blocco
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ENTER) == GLFW.GLFW_PRESS) {
                placeBlockHere();
            }
        }
    }

    /**
     * Quando entriamo in modalità selezione, prendiamo un blocco “abbastanza vicino”
     * a ~3 blocchi di distanza in direzione YAW. Se non è aria, o se è aria, non importa:
     * l’importante è avere “qualcosa di visibile”. Se non troviamo “il blocco esatto di fronte”,
     * va bene uno vicino.
     */
    private void selectBlockNearFront() {
        // Calcoliamo una distanza di 3 blocchi davanti a noi
        float dist = 3.0f * World.BLOCK_SIZE;
        float frontX = x + (float)Math.sin(Math.toRadians(yaw)) * dist;
        float frontZ = z - (float)Math.cos(Math.toRadians(yaw)) * dist;

        int bx = (int)(frontX / World.BLOCK_SIZE);
        int bz = (int)(frontZ / World.BLOCK_SIZE);

        // Troviamo la superficie. Se è acqua o aria, in ogni caso, la Y è la massima “non AIR”.
        int surfaceY = world.getSurfaceHeight(bx, bz);

        selectedBlockX = bx;
        selectedBlockY = surfaceY;
        selectedBlockZ = bz;
        clampSelection();

        System.out.println("Blocco selezionato iniziale: ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
    }

    /**
     * Selezione: SHIFT/SPACE muovono la Y del blocco selezionato
     */
    private void handleSelectionUpDown(long window) {
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS) {
            selectedBlockY++;
            clampSelection();
            System.out.println("Selezione spostata in alto a ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
            sleep50ms();
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) {
            selectedBlockY--;
            clampSelection();
            System.out.println("Selezione spostata in basso a ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
            sleep50ms();
        }
    }

    /**
     * Selezione: le frecce si basano sull’orientamento yaw della camera,
     * spostando ±1 in X/Z in direzione avanti/indietro/sinistra/destra
     */
    private void handleSelectionXZMovement(long window) {
        float rad = (float)Math.toRadians(yaw);
        int forwardDX = Math.round((float)Math.sin(rad));
        int forwardDZ = Math.round((float)-Math.cos(rad));

        float radLeft = (float)Math.toRadians(yaw - 90.0f);
        int leftDX = Math.round((float)Math.sin(radLeft));
        int leftDZ = Math.round((float)-Math.cos(radLeft));

        // UP => avanti
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS) {
            selectedBlockX += forwardDX;
            selectedBlockZ += forwardDZ;
            clampSelection();
            System.out.println("Selezione => ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
            sleep50ms();
        }
        // DOWN => indietro
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS) {
            selectedBlockX -= forwardDX;
            selectedBlockZ -= forwardDZ;
            clampSelection();
            System.out.println("Selezione => ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
            sleep50ms();
        }
        // LEFT => sinistra
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS) {
            selectedBlockX += leftDX;
            selectedBlockZ += leftDZ;
            clampSelection();
            System.out.println("Selezione => ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
            sleep50ms();
        }
        // RIGHT => destra
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS) {
            selectedBlockX -= leftDX;
            selectedBlockZ -= leftDZ;
            clampSelection();
            System.out.println("Selezione => ("+selectedBlockX+", "+selectedBlockY+", "+selectedBlockZ+")");
            sleep50ms();
        }
    }

    /**
     * Piazza il blocco GRIGIO **esattamente** nella posizione selezionata.
     */
    private void placeBlockHere() {
        int bx = selectedBlockX;
        int by = selectedBlockY;
        int bz = selectedBlockZ;

        if (world.getBlock(bx, by, bz) == Block.AIR) {
            world.setBlock(bx, by, bz, Block.GRAY_BLOCK);
            worldRenderer.rebuildMeshes();
            System.out.println("Blocco GRIGIO posizionato in ("+bx+", "+by+", "+bz+")");
        } else {
            System.out.println("❌ Non posso piazzare in ("+bx+", "+by+", "+bz+"): non è aria");
        }
    }

    /**
     * Movimenti standard della camera (WASD, SHIFT/SPACE) se *non* siamo in selezione.
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

        // SHIFT/SPACE muovono la camera su/giù (solo se non in selezione)
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS) {
            dy += speed;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) {
            dy -= speed;
        }

        // Proviamo a muoverci su x, y, z con controlli di collisione bounding box
        attemptMove(dx, 0, 0);
        attemptMove(0, dy, 0);
        attemptMove(0, 0, dz);
    }

    /**
     * Ruotiamo la camera con le frecce direzionali (solo se non in selezione).
     */
    private void handleCameraRotation(long window) {
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS) {
            pitch -= 1f;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS) {
            pitch += 1f;
        }
        pitch = Math.max(-85f, Math.min(pitch, 85f));
        
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS) {
            yaw -= 1f;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS) {
            yaw += 1f;
        }
    }

    /**
     * Spostiamo la camera di (dx, dy, dz) controllando collisioni a bounding box,
     * così non entriamo dentro i blocchi e non “vediamo dentro”.
     */
    private void attemptMove(float dx, float dy, float dz) {
        float newX = x + dx;
        float newY = y + dy;
        float newZ = z + dz;

        if (!collidesWithBlocks(newX, newY, newZ)) {
            x = newX;
            y = newY;
            z = newZ;
        }
    }

    /**
     * Controlla se il bounding box della camera in (nx, ny, nz) interseca un blocco solido.
     * Usiamo 8 “angoli” della hitbox (COLLISION_WIDTH/2 x COLLISION_HEIGHT) per verificare.
     */
    private boolean collidesWithBlocks(float nx, float ny, float nz) {
        // Estremi bounding box
        float halfW = COLLISION_WIDTH / 2f;
        float topY  = ny + COLLISION_HEIGHT;

        // Angoli (x±halfW, y e topY, z±halfW)
        float[] cornerXs = { nx - halfW, nx + halfW };
        float[] cornerYs = { ny, topY };
        float[] cornerZs = { nz - halfW, nz + halfW };

        for (float cx : cornerXs) {
            for (float cy : cornerYs) {
                for (float cz : cornerZs) {
                    if (isSolidBlockAt(cx, cy, cz)) {
                        return true; // c’è collisione
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
     * Converte (x, y, z) in coordinate mondo in indice di blocco e verifica se è solido.
     */
    private boolean isBlockSolidAt(float wx, float wy, float wz) {
        int bx = (int)Math.floor(wx / World.BLOCK_SIZE);
        int by = (int)Math.floor(wy / World.BLOCK_SIZE);
        int bz = (int)Math.floor(wz / World.BLOCK_SIZE);

        // Se fuori dal mondo, consideriamo "non solido"
        if (bx < 0 || bx >= World.SIZE_X ||
            by < 0 || by >= World.HEIGHT ||
            bz < 0 || bz >= World.SIZE_Z) {
            return false;
        }
        return world.getBlock(bx, by, bz).isSolid();
    }

    /**
     * Assicuriamo che la selezione rimanga nei confini del mondo.
     */
    private void clampSelection() {
        if (selectedBlockX < 0) selectedBlockX = 0;
        if (selectedBlockX >= World.SIZE_X) selectedBlockX = World.SIZE_X - 1;

        if (selectedBlockY < 0) selectedBlockY = 0;
        if (selectedBlockY >= World.HEIGHT) selectedBlockY = World.HEIGHT - 1;

        if (selectedBlockZ < 0) selectedBlockZ = 0;
        if (selectedBlockZ >= World.SIZE_Z) selectedBlockZ = World.SIZE_Z - 1;
    }

    private void sleep50ms() {
        try { Thread.sleep(50); } catch (InterruptedException e) {}
    }

    /**
     * Applica le trasformazioni (rotazioni e traslazioni) della camera
     * in pipeline fissa OpenGL.
     */
    public void applyTransformations() {
        GL11.glRotatef(pitch, 1, 0, 0);
        GL11.glRotatef(yaw,   0, 1, 0);
        // Spostiamo la camera un pochino in alto (0.1f) per evitare
        // di "intersecare" il terreno
        GL11.glTranslatef(-x, -(y + 0.1f), -z);
    }

    // Getter
    public boolean isSelectingBlockMode() { return selectingBlockMode; }
    public int getSelectedBlockX() { return selectedBlockX; }
    public int getSelectedBlockY() { return selectedBlockY; }
    public int getSelectedBlockZ() { return selectedBlockZ; }
}
