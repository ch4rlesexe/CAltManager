package org.charlie.cAltManager;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerLoginListener implements Listener {

    private final CAltManager plugin;

    public PlayerLoginListener(CAltManager plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        plugin.trackPlayer(event.getPlayer());
    }
}
