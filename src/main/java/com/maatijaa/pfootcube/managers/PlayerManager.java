package com.maatijaa.pfootcube.managers;

import java.io.File;
import java.io.IOException;

import com.maatijaa.pfootcube.pFootcube;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerManager implements Listener {
    public File userFile;

    public YamlConfiguration configFile;

    private pFootcube plugin;

    public PlayerManager(pFootcube instance) {
        this.plugin = (pFootcube)pFootcube.getPlugin(pFootcube.class);
        this.plugin = instance;
    }

    public void createConfig(Player p) {
        String fileName = p.getUniqueId().toString();
        this.userFile = new File("plugins" + File.separator + "pFootcube" + File.separator + "/users/" + fileName + ".yml");
        this.configFile = YamlConfiguration.loadConfiguration(this.userFile);
        this.configFile.options().header("Player Particle");
        this.configFile.addDefault("player-name", p.getName());
        this.configFile.addDefault("particles", "Disable");
        this.configFile.options().copyDefaults(true);
        try {
            this.plugin.saveConfig();
            this.configFile.save(this.userFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onJoinCreate(PlayerJoinEvent e) {
        createConfig(e.getPlayer());
        try {
            this.configFile.save(this.userFile);
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }
}
