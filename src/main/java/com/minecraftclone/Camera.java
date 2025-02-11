package com.minecraftclone;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class Camera {
    private float x, y, z;  // Posizione della camera
    private float pitch, yaw; // Rotazioni
    private float speed = 0.01f; // Velocità di movimento
    
    private static final float WIDTH = 0.09f;
    private static final float HEIGHT = 0.3f;
    private static final float DEPTH = 0.09f;

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

        float oldY = y; // Salviamo la posizione precedente della Y

        if (!collides(newX, newY, newZ, world)) {
            x = newX;
            y = newY;
            z = newZ;
        } else {
            // 🔥 **Nuovo controllo: evita il sinking sotto il terreno**
            if (dy < 0) {
                int surfaceHeight = world.getSurfaceHeight((int) Math.floor(newX / World.BLOCK_SIZE),
                                                           (int) Math.floor(newZ / World.BLOCK_SIZE));
                float expectedY = surfaceHeight * World.BLOCK_SIZE - 0.1f; // Aggiungiamo un piccolo margine per evitare oscillazioni

                if (y < expectedY) {
                    y = expectedY;
                    System.out.println("🚨 Telecamera riallineata alla superficie del terreno!");
                } else {
                    y = oldY; // Se la collisione è contro una parete o altro, ripristiniamo la posizione precedente
                }
            }
        }
    }



    private boolean collides(float nx, float ny, float nz, World world) {
        // Aumenta la precisione del controllo collisioni
        float step = World.BLOCK_SIZE / 4;  // Più efficiente di /100 ma sufficiente
        
        // Controlla solo i bordi della hitbox per prestazioni
        return checkCollision(nx, ny, nz, world) || 
               checkCollision(nx + WIDTH, ny, nz, world) ||
               checkCollision(nx, ny + HEIGHT, nz, world) ||
               checkCollision(nx, ny, nz + DEPTH, world);
    }

    private boolean checkCollision(float x, float y, float z, World world) {
        int blockX = (int) Math.floor(x / World.BLOCK_SIZE);
        int blockY = (int) Math.floor(y / World.BLOCK_SIZE);
        int blockZ = (int) Math.floor(z / World.BLOCK_SIZE);
        
        return world.getBlock(blockX, blockY, blockZ).isSolid();
    }



    public void applyTransformations() {
        float cameraOffset = 0.1f; // Offset per alzare la telecamera
        GL11.glRotatef(pitch, 1, 0, 0);
        GL11.glRotatef(yaw, 0, 1, 0);
        GL11.glTranslatef(-x, -(y + cameraOffset), -z); // 🔥 Alziamo leggermente la Y
    }


    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }
}
