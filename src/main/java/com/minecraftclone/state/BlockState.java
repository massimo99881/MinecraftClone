package com.minecraftclone.state;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // ✅ Ignora campi extra nel JSON
public class BlockState {
    private Long id;
    private int x, y, z;
    private String blockType;
    private String placedBy;
    private long lastUpdated; // ✅ Aggiunto il timestamp

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

    public long getLastUpdated() { // ✅ Getter per lastUpdated
        return lastUpdated;
    }
    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
