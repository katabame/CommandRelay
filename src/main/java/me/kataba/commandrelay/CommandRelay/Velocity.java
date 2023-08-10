package me.kataba.commandrelay.CommandRelay;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

@Plugin(
        id = "proxy_command_relay",
        name = "ProxyCommandRelay",
        version = "1.0",
        url = "https://kataba.me",
        authors = {"katabame"}
)
public class Velocity {
    @Inject
    private Logger logger;

    @Inject
    private ProxyServer server;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        try (ServerSocket serverSocket = new ServerSocket(36000)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                String receivedQuery = new BufferedReader((new InputStreamReader(clientSocket.getInputStream()))).readLine();
                String executor = receivedQuery.split(":")[0];
                String command = receivedQuery.split(":")[1];

                // Execute command
                if (executor.equals(".CONSOLE.")) {
                    // console executes
                } else {
                    Player player = (server.getPlayer(executor).orElse(null));
                    if (player != null) {
                        server.getCommandManager().executeAsync(player, command);
                    } else {
                        logger.info("[CommandRelay] Player not found: " + executor);
                    }
                }

                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("[CommandRelay] Initialized");
    }

    @Subscribe
    public void onCommandExecute(CommandExecuteEvent event) {
        String command = event.getCommand();
        String playerName = null;
        if (event.getCommandSource() instanceof Player player) {
            playerName = player.getUsername();
            logger.info("[CommandRelay] Command executed: " + command + " by: " + playerName);
        }

        if (!server.getCommandManager().hasCommand(command)) {
            logger.info("[CommandRelay] Command not found: " + command);

            try {
                Socket socket = new Socket("localhost", 35000);
                PrintWriter socketOut = new PrintWriter(socket.getOutputStream(), true);
                if (playerName == null) {
                    socketOut.println(".CONSOLE.:" + command);
                } else {
                    socketOut.println(playerName + ":" + command);
                }

                socket.close();
                logger.info("[CommandRelay] Command Sent: " + command);

            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}
