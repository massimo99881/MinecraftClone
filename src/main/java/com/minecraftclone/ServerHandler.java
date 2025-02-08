package com.minecraftclone;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import com.google.gson.Gson;

public class ServerHandler extends SimpleChannelInboundHandler<String> {

    private static final Gson gson = new Gson(); // Per la serializzazione/deserializzazione JSON

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {
        // Gestisci il messaggio ricevuto dal server
        System.out.println("Messaggio ricevuto dal server: " + message);

        // Esempio: Deserializza un messaggio JSON
        // Supponiamo che il messaggio sia un oggetto JSON che rappresenta un aggiornamento di gioco
        GameUpdate update = gson.fromJson(message, GameUpdate.class);

        // Elabora l'aggiornamento di gioco
        switch (update.getType()) {
            case "PLAYER_JOINED":
                System.out.println("Un nuovo giocatore si è unito: " + update.getData());
                break;
            case "PLAYER_LEFT":
                System.out.println("Un giocatore ha lasciato il gioco: " + update.getData());
                break;
            case "WORLD_UPDATE":
                System.out.println("Aggiornamento del mondo: " + update.getData());
                break;
            default:
                System.out.println("Aggiornamento sconosciuto: " + update.getType());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // Gestisci eventuali errori
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Connessione al server stabilita
        System.out.println("Connesso al server: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // Connessione al server persa
        System.out.println("Disconnesso dal server: " + ctx.channel().remoteAddress());
    }
}