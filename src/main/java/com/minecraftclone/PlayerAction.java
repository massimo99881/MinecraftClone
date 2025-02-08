package com.minecraftclone;

import java.util.Map;

public class PlayerAction {
    private String type; // Tipo di azione (es. "MOVE", "PLACE_BLOCK", "REMOVE_BLOCK")
    private Map<String, Object> data; // Dati associati all'azione

    // Costruttore vuoto (necessario per la deserializzazione con Gson)
    public PlayerAction() {}

    // Costruttore con parametri
    public PlayerAction(String type, Map<String, Object> data) {
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
        return "PlayerAction{" +
                "type='" + type + '\'' +
                ", data=" + data +
                '}';
    }
}