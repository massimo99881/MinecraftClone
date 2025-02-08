package com.minecraftclone;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class GameClient {
    private final String host;
    private final int port;
    private EventLoopGroup group;
    private Channel channel;

    public GameClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() {
        group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                     .channel(NioSocketChannel.class)
                     .handler(new ChannelInitializer<SocketChannel>() {
                         @Override
                         protected void initChannel(SocketChannel ch) {
                             ch.pipeline().addLast(new ClientHandler());
                         }
                     });

            System.out.println("[CLIENT] Tentativo di connessione a " + host + ":" + port + "...");

            // Avvia la connessione in un thread separato per evitare il blocco
            ChannelFuture future = bootstrap.connect(host, port).addListener((ChannelFutureListener) f -> {
                if (f.isSuccess()) {
                    System.out.println("[CLIENT] Connesso al server!");
                    channel = f.channel();
                } else {
                    System.err.println("[CLIENT] Connessione fallita: " + f.cause().getMessage());
                    shutdown(); // Chiude il client se la connessione fallisce
                }
            });

            // Attendi la chiusura della connessione senza bloccare il thread principale
            future.channel().closeFuture().addListener((ChannelFutureListener) f -> shutdown());

        } catch (Exception e) {
            e.printStackTrace();
            shutdown();
        }
    }

    /**
     * Chiude il client in modo sicuro
     */
    public void shutdown() {
        if (group != null) {
            group.shutdownGracefully();
            System.out.println("[CLIENT] Chiusura del client...");
        }
    }

    /**
     * Invia un messaggio al server
     */
    public void sendMessage(String message) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(message);
        } else {
            System.err.println("[CLIENT] Tentativo di invio fallito: il client non è connesso.");
        }
    }
}
