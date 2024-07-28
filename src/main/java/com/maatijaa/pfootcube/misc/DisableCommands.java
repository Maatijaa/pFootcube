package com.maatijaa.pfootcube.misc;

import com.maatijaa.pfootcube.core.Organization;
import com.maatijaa.pfootcube.pFootcube;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import java.util.ArrayList;
import org.bukkit.event.Listener;

public class DisableCommands implements Listener
{
    private pFootcube plugin;
    private Organization organization;
    private ArrayList<String> commands;
    
    public DisableCommands(final pFootcube pl, final Organization org) {
        this.commands = new ArrayList<String>();
        this.plugin = pl;
        this.organization = org;
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this.plugin);
        final FileConfiguration cfg = this.plugin.getConfig();
        cfg.addDefault("enabledCommands", (Object)"");
        this.plugin.saveConfig();
        String[] split;
        for (int length = (split = cfg.getString("enabledCommands").split(" ")).length, i = 0; i < length; ++i) {
            final String s = split[i];
            this.commands.add(s);
        }
    }
    
    public void command(final CommandSender sender, final Command cmd, final String c, final String[] args) {
        final Player p = (Player)sender;
        if (cmd.getName().equalsIgnoreCase("commandDisabler") && p.hasPermission("pfootcube.admin")) {
            if (args.length < 1) {
                p.sendMessage(ChatColor.DARK_AQUA + "pFootcube Onesposobjivac komandi.");
                p.sendMessage(ChatColor.AQUA + "/cd add [Komanda]");
                p.sendMessage(ChatColor.AQUA + "/cd remove [Komanda]");
                p.sendMessage(ChatColor.AQUA + "/cd list");
            }
            else {
                final FileConfiguration cfg = this.plugin.getConfig();
                if (args[0].equalsIgnoreCase("add")) {
                    if (args.length < 2) {
                        p.sendMessage(ChatColor.AQUA + "/cd add [Komanda]");
                    }
                    else if (this.commands.contains(args[1])) {
                        p.sendMessage(ChatColor.RED + "Ova komanda je vec dodana.");
                    }
                    else {
                        this.commands.add(args[1]);
                        String cfgString = "";
                        for (final String s : this.commands) {
                            cfgString = String.valueOf(cfgString) + s + " ";
                        }
                        cfg.set("enabledCommands", (Object)cfgString);
                        this.plugin.saveConfig();
                        p.sendMessage(ChatColor.GREEN + "Uspesno ste dodali komandu /" + args[1] + " na listu izkljucenih komandi.");
                    }
                }
                else if (args[0].equalsIgnoreCase("remove")) {
                    if (args.length < 2) {
                        p.sendMessage(ChatColor.AQUA + "/cd remove [Komanda]");
                    }
                    else if (this.commands.contains(args[1])) {
                        this.commands.remove(args[1]);
                        String cfgString = "";
                        for (final String s : this.commands) {
                            cfgString = String.valueOf(cfgString) + s + " ";
                        }
                        cfg.set("enabledCommands", (Object)cfgString);
                        this.plugin.saveConfig();
                        p.sendMessage(ChatColor.GREEN + "Uspesno si uklonio komandu /" + args[1] + " iz liste iskljucenih komandi.");
                    }
                    else {
                        p.sendMessage(ChatColor.RED + "Ova komanda nije ni dodata bila.");
                    }
                }
                else if (args[0].equalsIgnoreCase("list")) {
                    p.sendMessage(ChatColor.GOLD + "Lista onespobljenih komandi.:");
                    for (final String s2 : this.commands) {
                        p.sendMessage(ChatColor.GRAY + s2);
                    }
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreprocess(final PlayerCommandPreprocessEvent e) {
        final Player p = e.getPlayer();
        if (!p.hasPermission("pfootcube.admin") && (this.organization.playingPlayers.contains(p.getName()) || this.organization.waitingPlayers.containsKey(p.getName()))) {
            final String cmd = e.getMessage().substring(1).split(" ")[0];
            boolean allowed = true;
            for (final String command : this.commands) {
                if (cmd.equalsIgnoreCase(command)) {
                    allowed = false;
                    break;
                }
            }
            if (!allowed) {
                p.sendMessage(ChatColor.RED + "Nemozes koristiti ovu komandu tokom utakmice.");
                e.setCancelled(true);
            }
        }
    }
}
