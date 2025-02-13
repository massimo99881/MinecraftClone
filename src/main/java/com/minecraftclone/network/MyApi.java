package com.minecraftclone.network;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minecraftclone.state.BlockState;
import com.minecraftclone.state.PlayerState;

public class MyApi {
    private static final String BASE_URL = "http://localhost:5051/api";
    private static final ObjectMapper mapper = new ObjectMapper();

    // LOGIN/REGISTER
    public static String login(String email) {
        return doPost(BASE_URL + "/login?email=" + enc(email));
    }
    public static String register(String email) {
        return doPost(BASE_URL + "/register?email=" + enc(email));
    }

    // PLAYERS
    public static String connect(String email) {
        return doPost(BASE_URL + "/players/connect?email=" + enc(email));
    }
    public static String disconnect(String email) {
        return doDelete(BASE_URL + "/players/disconnect?email=" + enc(email));
    }
    public static List<PlayerState> getPlayers() {
        String json = doGet(BASE_URL + "/players");
        if (json.startsWith("ERROR")) {
            return Collections.emptyList();
        }
        try {
            PlayerState[] arr = mapper.readValue(json, PlayerState[].class);
            return Arrays.asList(arr);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // BLOCKS
    public static List<BlockState> getAllBlocks() {
        String json = doGet(BASE_URL + "/blocks");
        if (json.startsWith("ERROR")) {
            return Collections.emptyList();
        }
        try {
            BlockState[] arr = mapper.readValue(json, BlockState[].class);
            return Arrays.asList(arr);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    public static List<BlockState> getUpdatedBlocks(long lastTimestamp){
        String json = doGet(BASE_URL + "/blocks/updates?since=" + lastTimestamp);
        if (json.startsWith("ERROR")) return Collections.emptyList();
        
        try {
            BlockState[] arr = mapper.readValue(json, BlockState[].class);
            return Arrays.asList(arr);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }


    // placeBlock
    public static String placeBlock(int x, int y, int z, String blockType, String placedBy) {
        String url = BASE_URL + "/blocks";

        // Creiamo il JSON da inviare
        String jsonBody = "{"
            + "\"x\": " + x + ","
            + "\"y\": " + y + ","
            + "\"z\": " + z + ","
            + "\"blockType\": \"" + blockType + "\","
            + "\"placedBy\": \"" + placedBy + "\""
            + "}";

        return doPostJson(url, jsonBody);
    }
    
    // updatePosition
    public static String updatePosition(String email, float x, float y, float z) {
        String url = BASE_URL + "/players/updatePos?email=" + enc(email)
                   + "&x=" + x + "&y=" + y + "&z=" + z;
        return doPost(url);
    }

    // Metodi HTTP
    private static String doGet(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            int code = conn.getResponseCode();
            if (code == 200) {
                return new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            }
            return "ERROR_HTTP_" + code;
        } catch (Exception e) {
            return "ERROR_" + e.getMessage();
        }
    }

    private static String doPost(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.connect();
            int code = conn.getResponseCode();
            if (code == 200) {
                return new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            }
            return "ERROR_HTTP_" + code;
        } catch (Exception e) {
            return "ERROR_" + e.getMessage();
        }
    }
    
    private static String doPostJson(String url, String jsonBody) {
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line.trim());
                    }
                    return response.toString();
                }
            } else {
                return "Errore: " + responseCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Errore di connessione";
        }
    }

    private static String doDelete(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.connect();
            int code = conn.getResponseCode();
            if (code == 200) {
                return new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            }
            return "ERROR_HTTP_" + code;
        } catch (Exception e) {
            return "ERROR_" + e.getMessage();
        }
    }

    private static String enc(String val) {
        try {
            return URLEncoder.encode(val, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return val;
        }
    }
}
