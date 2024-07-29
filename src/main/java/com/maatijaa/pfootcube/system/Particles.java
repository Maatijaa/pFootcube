package com.maatijaa.pfootcube.system;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import com.maatijaa.pfootcube.pFootcube;
import net.milkbowl.vault.chat.Chat;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Particles implements CommandExecutor, Listener {
    private pFootcube plugin;

    public Particles(pFootcube instance) {
        this.plugin = instance;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Komanda nemoze biti izvrsena u konzoli.");
            return true;
        }
        Player p = (Player)sender;
        inventory(p);
        return true;
    }

    public void createStack(Inventory inv, int slot, Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        ArrayList<String> itemLore = new ArrayList<>();
        itemLore.add(lore);
        itemMeta.setDisplayName(name);
        itemMeta.setLore(itemLore);
        item.setItemMeta(itemMeta);
        inv.setItem(slot, item);
    }

    public void inventory(Player p) {
        Inventory inv = Bukkit.getServer().createInventory(null, 27, "pFootcube Partikli.");
        createStack(inv, 0, Material.BLAZE_POWDER, "Flames", "Klikni da ukljucis.");
        createStack(inv, 1, Material.EMERALD, "Green", "Klikni da ukljucis.");
        createStack(inv, 22, Material.BARRIER, "Disable", "Klikni da iskljucis sve partikle.");
        if (p.hasPermission("pfootcube.cubeparticles")) {
            createStack(inv, 2, Material.APPLE, "Hearts", "Klikni da ukljucis.");
            createStack(inv, 3, Material.FEATHER, "Sparky", "Klikni da ukljucis.");
            if (p.hasPermission("pfootcube.legendcubeparticles"))
                createStack(inv, 4, Material.REDSTONE, "Rainbow", "Klikni da ukljucis.");
        }
        p.openInventory(inv);
    }

    @EventHandler
    public void invClick(InventoryClickEvent e) {
        Player p = (Player)e.getWhoClicked();
        UUID uuid = e.getWhoClicked().getUniqueId();
        File userFile = new File("plugins" + File.separator + "pFootcube" + File.separator + "/users/" + uuid + ".yml");
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(userFile);
        String effect = "";
        if (e.getCurrentItem().hasItemMeta())
            effect = e.getCurrentItem().getItemMeta().getDisplayName();
        if (e.getInventory().getName() != "pFootcube Partikli.")
            return;
        e.setCancelled(true);
        String s, str1;
        switch ((str1 = s = effect).hashCode()) {
            case -2137389811:
                if (!str1.equals("Hearts"))
                    break;
                yamlConfiguration.set("particles", "Hearts");
                try {
                    yamlConfiguration.save(userFile);
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                p.sendMessage(ChatColor.GREEN + "▎ " + ChatColor.WHITE + " Uspesno si aktivirao '" + effect + "' partiklu!");
                break;
            case -959006008:
                if (!str1.equals("Disable"))
                    break;
                yamlConfiguration.set("particles", "Disable");
                try {
                    yamlConfiguration.save(userFile);
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                p.sendMessage(ChatColor.GREEN + "▎ " + ChatColor.WHITE + " Uspesno ste iskljucili sve partikle/efekte.");
                break;
            case 69066467:
                if (!str1.equals("Green"))
                    break;
                yamlConfiguration.set("particles", "Green");
                try {
                    yamlConfiguration.save(userFile);
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                p.sendMessage(ChatColor.GREEN + "▎ " + ChatColor.WHITE + " Uspesno si ukljucio '" + effect + "' partiklu!");
                break;
            case 2106778560:
                if (!str1.equals("Flames"))
                    break;
                yamlConfiguration.set("particles", "Flames");
                try {
                    yamlConfiguration.save(userFile);
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                p.sendMessage(ChatColor.GREEN + "▎ " + ChatColor.WHITE + " Uspesno si aktivirao '" + effect + "' partiklu!");
                break;
        }
        if (p.hasPermission("pfootcube.cubeparticles")) {
            String s2;
            String str2;
            switch ((str2 = s2 = effect).hashCode()) {
                case -1812310692:
                    if (!str2.equals("Sparky"))
                        break;
                    yamlConfiguration.set("particles", "Sparky");
                    try {
                        yamlConfiguration.save(userFile);
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                    p.sendMessage(ChatColor.GREEN + "▎ " + ChatColor.WHITE + " Uspesno si aktivirao '" + effect + "' partiklu!");
                    break;
            }
        }
        if (p.hasPermission("pfootcube.legendcubeparticles")) {
            String s3;
            String str2;
            switch ((str2 = s3 = effect).hashCode()) {
                case -1656737386:
                    if (!str2.equals("Rainbow"))
                        break;
                    yamlConfiguration.set("particles", "Rainbow");
                    try {
                        yamlConfiguration.save(userFile);
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                    p.sendMessage(ChatColor.GREEN + "▎ " + ChatColor.WHITE + " Uspesno si aktivirao '" + effect + "' partiklu!");
                    break;
            }
        }
    }

    public void cubeEffect() {
        Collection<? extends Player> onlinePlayers;
        for (int length = (onlinePlayers = this.plugin.getServer().getOnlinePlayers()).size(), k = 0; k < length; k++) {
            Player p = (Player)onlinePlayers.toArray()[k];
            UUID uuid = p.getUniqueId();
            for (Slime cube : this.plugin.cubes) {
                if (!cube.isDead()) {
                    Particle hearts, sparky, rainbow, green, flames;
                    Location loc = cube.getLocation();
                    File userFile = new File("plugins" + File.separator + "pFootcube" + File.separator + "/users/" + uuid + ".yml");
                    YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(userFile);
                    String effect = yamlConfiguration.getString("particles");
                    String s, str1;
                    switch ((str1 = s = effect).hashCode()) {
                        case -2137389811:
                            if (!str1.equals("Hearts"))
                                break;
                            hearts = new Particle(EnumParticle.HEART, loc, true, 0.0F, 0.0F, 0.0F, 100.0F, 0);
                            hearts.toPlayer(p);
                            continue;
                        case -1812310692:
                            if (!str1.equals("Sparky"))
                                break;
                            sparky = new Particle(EnumParticle.FIREWORKS_SPARK, loc, true, 0.0F, 0.0F, 0.0F, 100.0F, 0);
                            sparky.toPlayer(p);
                            continue;
                        case -1656737386:
                            if (!str1.equals("Rainbow"))
                                break;
                            rainbow = new Particle(EnumParticle.REDSTONE, loc, true, 0.0F, 0.0F, 0.0F, 100.0F, 0);
                            rainbow.toPlayer(p);
                            continue;
                        case -959006008:
                            if (!str1.equals("Disable"))
                                break;
                            continue;
                        case 69066467:
                            if (!str1.equals("Green"))
                                break;
                            green = new Particle(EnumParticle.VILLAGER_HAPPY, loc, true, 0.0F, 0.0F, 0.0F, 100.0F, 0);
                            green.toPlayer(p);
                            continue;
                        case 2106778560:
                            if (!str1.equals("Flames"))
                                break;
                            flames = new Particle(EnumParticle.FLAME, loc, true, 0.0F, 0.0F, 0.0F, 100.0F, 0);
                            flames.toPlayer(p);
                            continue;
                    }
                    return;
                }
            }
        }
    }
}
