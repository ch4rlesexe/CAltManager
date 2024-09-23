package org.charlie.cAltManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class CAltManager extends JavaPlugin {

    private final Map<String, Set<String>> ipToPlayersMap = new HashMap<>();
    private File altsFile;
    private FileConfiguration altsConfig;

    @Override
    public void onEnable() {
        getLogger().info(ChatColor.GREEN + "CAltManager Plugin has been enabled!");

        createAltsFile();

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatColor.RED + "CAltManager Plugin has been disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("alts")) {
            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /alts <username>");
                return true;
            }

            String targetUsername = args[0];
            String targetHashedIP = getPlayerHashedIP(targetUsername);

            if (targetHashedIP == null) {
                sender.sendMessage(ChatColor.RED + "Player not found or no IP address is associated.");
                return true;
            }

            Set<String> alts = altsConfig.getConfigurationSection("ips." + targetHashedIP).getKeys(false);

            if (alts == null || alts.isEmpty()) {
                sender.sendMessage(ChatColor.YELLOW + "No alternative accounts found for this IP.");
            } else {
                sender.sendMessage(ChatColor.GREEN + "Accounts connected to the same IP as " + targetUsername + ": " + ChatColor.AQUA + String.join(", ", alts));
            }
            return true;
        }
        return false;
    }

    public void trackPlayer(Player player) {
        if (player.getAddress() == null) {
            getLogger().warning(ChatColor.RED + "Could not retrieve IP for player: " + player.getName() + " (getAddress() is null)");
            return;
        }

        String ip = player.getAddress().getAddress().getHostAddress();
        String username = player.getName();

        String hashedIP = hashIP(ip);

        if (hashedIP != null) {
            altsConfig.set("ips." + hashedIP + "." + username, true);

            try {
                altsConfig.save(altsFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            getLogger().info(ChatColor.GREEN + "Tracked hashed IP for player " + username);
        }
    }

    private String getPlayerHashedIP(String username) {
        Player player = Bukkit.getPlayer(username);

        if (player != null && player.getAddress() != null) {
            String ip = player.getAddress().getAddress().getHostAddress();
            return hashIP(ip);
        }

        for (String hashedIP : altsConfig.getConfigurationSection("ips").getKeys(false)) {
            if (altsConfig.contains("ips." + hashedIP + "." + username)) {
                return hashedIP;
            }
        }

        return null;
    }

    private void createAltsFile() {
        altsFile = new File(getDataFolder(), "alts.yml");
        if (!altsFile.exists()) {
            altsFile.getParentFile().mkdirs();
            saveResource("alts.yml", false);
        }

        altsConfig = YamlConfiguration.loadConfiguration(altsFile);
    }

    private String hashIP(String ip) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(ip.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
