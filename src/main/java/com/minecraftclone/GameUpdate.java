package com.minecraftclone;

import java.util.Map;

public class GameUpdate {
    private String type; // Tipo di aggiornamento (es. "PLAYER_JOINED", "PLAYER_LEFT", "WORLD_UPDATE")
    private Map<String, Object> data; // Dati associati all'aggiornamento

    // Costruttore vuoto (necessario per la deserializzazione con Gson)
    public GameUpdate() {}

    // Costruttore con parametri
    public GameUpdate(String type, Map<String, Object> data) {
        this.type = type;
        this.data = data;
    }

    // Getter e Setter
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "GameUpdate{" +
                "type='" + type + '\'' +
                ", data=" + data +
                '}';
    }
}