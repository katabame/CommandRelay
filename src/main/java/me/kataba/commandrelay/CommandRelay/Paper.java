package me.kataba.commandrelay.CommandRelay;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public final class Paper extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this, this);
        try (ServerSocket serverSocket = new ServerSocket(35000)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                String receivedQuery = new BufferedReader((new InputStreamReader(clientSocket.getInputStream()))).readLine();
                String executor = receivedQuery.split(":")[0];
                String command = receivedQuery.split(":")[1];

                // Execute command
                if (executor.equals(".CONSOLE.")) {
                    // console executes
                } else {
                    Player player = (Bukkit.getServer().getPlayerExact(executor));
                    if (player != null) {
                        Bukkit.getServer().dispatchCommand(player, command);
                    } else {
                        Bukkit.getLogger().info("[CommandRelay] Player not found: " + executor);
                    }
                }

                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bukkit.getLogger().info("[CommandRelay] Enabled");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getLogger().info("[CommandRelay] Disabled");
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();
        String playerName = event.getPlayer().getName();

        Bukkit.getLogger().info("[CommandRelay] Command executed: " + command + " / by: " + playerName);

        if (!Bukkit.getServer().getCommandMap().getKnownCommands().containsKey(command.split(" ")[0].substring(1).toLowerCase())) {
            Bukkit.getLogger().info("[CommandRelay] command not found: " + command);
            try {
                Socket socket = new Socket("localhost", 35000);
                PrintWriter socketOut = new PrintWriter(socket.getOutputStream(), true);
                socketOut.println(playerName + ":" + command);
                socket.close();
                Bukkit.getLogger().info("[CommandRelay] Command Sent: " + command);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        String command = event.getCommand();

        Bukkit.getLogger().info("[CommandRelay] Command executed: " + command + " / by: CONSOLE");

        try {
            Socket socket = new Socket("localhost", 35000);
            PrintWriter socketOut = new PrintWriter(socket.getOutputStream(), true);
            socketOut.println(".CONSOLE.:" + command);
            socket.close();
            Bukkit.getLogger().info("[CommandRelay] Command Sent: " + command);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
