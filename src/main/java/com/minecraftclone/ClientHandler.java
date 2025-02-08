package com.minecraftclone;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import com.google.gson.Gson;

public class ClientHandler extends SimpleChannelInboundHandler<String> {

    private static final Gson gson = new Gson(); // Per la serializzazione/deserializzazione JSON

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {
        // Gestisci il messaggio ricevuto dal client
        System.out.println("Messaggio ricevuto dal client: " + message);

        // Esempio: Deserializza un messaggio JSON
        // Supponiamo che il messaggio sia un oggetto JSON che rappresenta un'azione del giocatore
        PlayerAction action = gson.fromJson(message, PlayerAction.class);

        // Elabora l'azione del giocatore
        switch (action.getType()) {
            case "MOVE":
                System.out.println("Il giocatore si è mosso: " + action.getData());
                break;
            case "PLACE_BLOCK":
                System.out.println("Il giocatore ha piazzato un blocco: " + action.getData());
                break;
            case "REMOVE_BLOCK":
                System.out.println("Il giocatore ha rimosso un blocco: " + action.getData());
                break;
            default:
                System.out.println("Azione sconosciuta: " + action.getType());
        }

        // Invia una risposta al client (opzionale)
        String response = "Azione ricevuta: " + action.getType();
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // Gestisci eventuali errori
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Un client si è connesso
        System.out.println("Client connesso: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // Un client si è disconnesso
        System.out.println("Client disconnesso: " + ctx.channel().remoteAddress());
    }
}