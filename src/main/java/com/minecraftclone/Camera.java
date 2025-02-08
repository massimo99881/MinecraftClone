package com.minecraftclone;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class Camera {

    // Posizione della telecamera
    private float x, y, z;
    // Rotazioni (pitch = su/giù, yaw = sinistra/destra)
    private float pitch, yaw;
    // Velocità di movimento
    private float speed = 0.05f;
    // Sensibilità della rotazione
    private float sensitivity = 2.0f;
    // Bounding box del giocatore
    private static final float WIDTH = 0.6f;
    private static final float HEIGHT = 1.8f;
    private static final float DEPTH = 0.6f;

//    public Camera(float x, float y, float z) {
//        this.x = x;
//        this.y = y;
//        this.z = z;
//        this.pitch = 0;
//        this.yaw = 0;
//    }
    
    public Camera(float x, float y, float z) {
        this.x = x;
        this.y = y; 
        this.z = z;
        this.pitch = 0;
        this.yaw = 0;
        System.out.println("📷 Telecamera inizializzata a: (" + x + ", " + y + ", " + z + ")");
    }


    /**
     * Applica le trasformazioni per la vista della telecamera.
     * Questo metodo deve essere chiamato prima del rendering della scena.
     */
    public void applyTransformations() {
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        // Calcola la matrice di vista
        Vector3f eye = new Vector3f(x, y, z); // Posizione della camera
        Vector3f center = new Vector3f(x + (float)Math.sin(Math.toRadians(yaw)), y, z - (float)Math.cos(Math.toRadians(yaw))); // Punto verso cui guarda
        Vector3f up = new Vector3f(0, 1, 0); // Direzione "sopra"

        Matrix4f viewMatrix = new Matrix4f().lookAt(eye, center, up);

        // Converti la matrice in un FloatBuffer per OpenGL
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        viewMatrix.get(buffer);
        GL11.glLoadMatrixf(buffer);
    }



    /**
     * Gestisce l'input della telecamera (movimento e rotazione).
     */
    public void updateInput(long window, World world) {
        float dx = 0, dy = 0, dz = 0;
        
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) {
            dx += (float)Math.sin(Math.toRadians(yaw)) * speed;
            dz -= (float)Math.cos(Math.toRadians(yaw)) * speed;
            System.out.println("⬆ W premuto, spostamento: " + dx + ", " + dz);
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) {
            dx -= (float)Math.sin(Math.toRadians(yaw)) * speed;
            dz += (float)Math.cos(Math.toRadians(yaw)) * speed;
            System.out.println("⬇ S premuto, spostamento: " + dx + ", " + dz);
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) {
            dx -= (float)Math.cos(Math.toRadians(yaw)) * speed;
            dz -= (float)Math.sin(Math.toRadians(yaw)) * speed;
            System.out.println("⬅ A premuto, spostamento: " + dx + ", " + dz);
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) {
            dx += (float)Math.cos(Math.toRadians(yaw)) * speed;
            dz += (float)Math.sin(Math.toRadians(yaw)) * speed;
            System.out.println("➡ D premuto, spostamento: " + dx + ", " + dz);
        }

        // Applica lo spostamento
        attemptMove(dx, dy, dz, world);
    }



    /**
     * Prova a muovere la telecamera e impedisce il passaggio attraverso blocchi solidi.
     */
    public void attemptMove(float dx, float dy, float dz, World world) {
        float newX = x + dx;
        float newY = y + dy;
        float newZ = z + dz;

        if (!collides(newX, newY, newZ, world)) {
            x = newX;
            y = newY;
            z = newZ;
            System.out.println("✅ Movendo la telecamera a (" + x + ", " + y + ", " + z + ")");
        } else {
            System.out.println("❌ Collisione! Impossibile muoversi a (" + newX + ", " + newY + ", " + newZ + ")");
        }
    }

    private boolean collides(float nx, float ny, float nz, World world) {
        int minX = (int) Math.floor(nx);
        int maxX = (int) Math.floor(nx + 1);
        int minY = (int) Math.floor(ny);
        int maxY = (int) Math.floor(ny + 1);
        int minZ = (int) Math.floor(nz);
        int maxZ = (int) Math.floor(nz + 1);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (world.getBlock(x, y, z).isSolid()) {
                        return true;
                    }
                }
            }
        }
        
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                // Controlliamo SOLO la base del giocatore per evitare blocchi sopra la testa
                if (world.getBlock(x, (int) Math.floor(ny), z).isSolid()) {
                    return true;
                }
            }
        }
        
        return false;
    }


    // Metodi Getter per la posizione della telecamera
    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }

}
