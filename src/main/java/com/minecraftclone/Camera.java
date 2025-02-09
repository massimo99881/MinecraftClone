package com.minecraftclone;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class Camera {
    private float x, y, z;  // Posizione della camera
    private float pitch, yaw; // Rotazioni
    private float speed = 0.01f; // Velocità di movimento
    
    private static final float WIDTH = 0.25f;
    private static final float HEIGHT = 0.45f;
    private static final float DEPTH = 0.25f;

    public Camera(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void updateInput(long window, World world) {
        float dx = 0, dy = 0, dz = 0;

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

        // Controlliamo se la nuova posizione collide con il terreno
        attemptMove(dx, 0, 0, world);
        attemptMove(0, dy, 0, world);
        attemptMove(0, 0, dz, world);

        System.out.println("📸 Posizione telecamera: (" + x + ", " + y + ", " + z + ")");
    }

    private void attemptMove(float dx, float dy, float dz, World world) {
        float newX = x + dx;
        float newY = y + dy;
        float newZ = z + dz;

        if (!collides(newX, newY, newZ, world)) {
            x = newX;
            y = newY;
            z = newZ;
        } else {
            System.out.println("❌ Collisione! Impossibile muoversi a (" + newX + ", " + newY + ", " + newZ + ")");
        }
    }

    private boolean collides(float nx, float ny, float nz, World world) {
        int minX = (int) Math.floor(nx / World.BLOCK_SIZE);
        int maxX = (int) Math.floor((nx + WIDTH) / World.BLOCK_SIZE);
        int minY = (int) Math.floor(ny / World.BLOCK_SIZE);
        int maxY = (int) Math.floor((ny + HEIGHT) / World.BLOCK_SIZE);
        int minZ = (int) Math.floor(nz / World.BLOCK_SIZE);
        int maxZ = (int) Math.floor((nz + DEPTH) / World.BLOCK_SIZE);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (world.getBlock(x, y, z).isSolid()) {
                        System.out.println("Collision at: " + x + "," + y + "," + z);
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public void applyTransformations() {
        GL11.glRotatef(pitch, 1, 0, 0);
        GL11.glRotatef(yaw, 0, 1, 0);
        GL11.glTranslatef(-x, -y, -z);
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }
}
