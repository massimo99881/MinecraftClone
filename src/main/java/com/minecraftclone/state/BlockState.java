package com.minecraftclone.state;

public class BlockState {
    private Long id;
    private int x, y, z;
    private String blockType;
    private String placedBy;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }
    public void setZ(int z) {
        this.z = z;
    }

    public String getBlockType() {
        return blockType;
    }
    public void setBlockType(String blockType) {
        this.blockType = blockType;
    }

    public String getPlacedBy() {
        return placedBy;
    }
    public void setPlacedBy(String placedBy) {
        this.placedBy = placedBy;
    }
}
