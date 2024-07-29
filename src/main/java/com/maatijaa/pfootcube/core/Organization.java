package com.maatijaa.pfootcube.core;

import com.maatijaa.pfootcube.misc.DisableCommands;
import com.maatijaa.pfootcube.system.Stats;
import com.maatijaa.pfootcube.system.UUIDConverter;
import com.maatijaa.pfootcube.pFootcube;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.World;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.bukkit.block.Sign;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Entity;
import java.io.File;
import org.bukkit.plugin.Plugin;
import org.bukkit.ChatColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.event.Listener;

public class Organization implements Listener
{
    private pFootcube plugin;
    private DisableCommands disableCommands;
    private Highscores highscores;
    public String pluginString;
    private String adminString;
    private String or;
    private String setupGuy;
    private int setupType;
    private Location setupLoc;
    private Match[] matches2v2;
    private Match[] matches3v3;
    private Match[] matches4v4;
    private int lobby2v2;
    private int lobby3v3;
    private int lobby4v4;
    private int practiceBalls;
    public HashMap<String, Integer> waitingPlayers;
    public ArrayList<String> playingPlayers;
    private HashMap<Player, Player> team;
    private HashMap<Player, Player> teamReverse;
    private HashMap<Player, Integer> teamType;
    private Player[][] waitingTeams;
    private ArrayList<Player> waitingTeamPlayers;
    private Match[] leftMatches;
    private boolean[] leftPlayerIsRed;
    private long announcementTime;
    public Stats wins;
    public Stats matches;
    public Stats ties;
    public Stats goals;
    private Stats store;
    public Stats winStreak;
    public Stats bestWinStreak;
    public UUIDConverter uuidConverter;
    private ArrayList<Player> spy;
    private ArrayList<Player> global;
    public Economy economy;
    
    public Organization(final pFootcube pl) {
        this.highscores = null;
        this.pluginString = ChatColor.DARK_AQUA + "▎ PFC ▎ ";
        this.adminString = ChatColor.AQUA + "PFC ADMIN ";
        this.or = ChatColor.YELLOW + "|" + ChatColor.AQUA;
        this.setupGuy = null;
        this.setupType = 0;
        this.setupLoc = null;
        this.matches2v2 = new Match[0];
        this.matches3v3 = new Match[0];
        this.matches4v4 = new Match[0];
        this.lobby2v2 = 0;
        this.lobby3v3 = 0;
        this.lobby4v4 = 0;
        this.practiceBalls = 0;
        this.waitingPlayers = new HashMap<String, Integer>();
        this.playingPlayers = new ArrayList<String>();
        this.team = new HashMap<Player, Player>();
        this.teamReverse = new HashMap<Player, Player>();
        this.teamType = new HashMap<Player, Integer>();
        this.waitingTeams = new Player[0][0];
        this.waitingTeamPlayers = new ArrayList<Player>();
        this.leftMatches = new Match[0];
        this.leftPlayerIsRed = new boolean[0];
        this.wins = new Stats();
        this.matches = new Stats();
        this.ties = new Stats();
        this.goals = new Stats();
        this.store = new Stats();
        this.winStreak = new Stats();
        this.bestWinStreak = new Stats();
        this.uuidConverter = new UUIDConverter();
        this.spy = new ArrayList<Player>();
        this.global = new ArrayList<Player>();
        this.economy = null;
        this.plugin = pl;
        this.disableCommands = new DisableCommands(this.plugin, this);
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this.plugin);
        final FileConfiguration cfg = this.plugin.getConfig();
        cfg.addDefault("arenas.2v2.amount", (Object)0);
        cfg.addDefault("arenas.3v3.amount", (Object)0);
        cfg.addDefault("arenas.4v4.amount", (Object)0);
        cfg.options().copyDefaults(true);
        this.plugin.saveConfig();
        this.loadArenas(cfg);
        this.wins.setup("plugins" + File.separator + "pFootcube" + File.separator + "wins.stats");
        this.matches.setup("plugins" + File.separator + "pFootcube" + File.separator + "matches.stats");
        this.ties.setup("plugins" + File.separator + "pFootcube" + File.separator + "ties.stats");
        this.goals.setup("plugins" + File.separator + "pFootcube" + File.separator + "goals.stats");
        this.store.setup("plugins" + File.separator + "pFootcube" + File.separator + "store.stats");
        this.winStreak.setup("plugins" + File.separator + "pFootcube" + File.separator + "winStreak.stats");
        this.bestWinStreak.setup("plugins" + File.separator + "pFootcube" + File.separator + "bestWinStreak.stats");
        this.uuidConverter.setup("plugins" + File.separator + "pFootcube" + File.separator + "UUID.data");
        this.wins.load();
        this.matches.load();
        this.ties.load();
        this.goals.load();
        this.store.load();
        this.winStreak.load();
        this.bestWinStreak.load();
        this.uuidConverter.load();
    
            for (Player p : Bukkit.getOnlinePlayers()) {
            	
            
            if (!this.uuidConverter.has(p.getUniqueId().toString())) {
                this.uuidConverter.put(p.getUniqueId().toString(), p.getName());
            }
            }
        
        this.setupEconomy();
        if (cfg.contains("arenas.world")) {
            for (final Entity e : this.plugin.getServer().getWorld(cfg.getString("arenas.world")).getEntities()) {
                if (e instanceof Slime) {
                    ((Slime)e).setHealth(0.0);
                }
            }
        }
        this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)this.plugin, (Runnable)new Runnable() {
            public void run() {
                Organization.this.update();
            }
        }, 1L, 1L);
    }
    
    public void command(final CommandSender sender, final Command cmd, final String c, final String[] args) {
        this.disableCommands.command(sender, cmd, c, args);
        final Player p = (Player)sender;
        if (cmd.getName().equalsIgnoreCase("spy") && p.hasPermission("pfootcube.spy")) {
            if (this.spy.contains(p)) {
                this.spy.remove(p);
                p.sendMessage(String.valueOf(this.pluginString) + ChatColor.GREEN + "Prisluskivanje je uspesno iskljuceno.");
            }
            else {
                this.spy.add(p);
                p.sendMessage(String.valueOf(this.pluginString) + ChatColor.GREEN + "Prisluskivanje je uspesno ukljuceno.");
            }
        }
        if (cmd.getName().equalsIgnoreCase("g")) {
            this.global.add(p);
            String message = new StringBuilder().append(ChatColor.YELLOW).append(ChatColor.BOLD).append("G> ").append(ChatColor.RESET).toString();
            for (final String s : args) {
                message = String.valueOf(message) + s + " ";
            }
            p.chat(message);
        }
        if (cmd.getName().equalsIgnoreCase("tc")) {
            String message = "";
            for (final String s : args) {
                message = String.valueOf(message) + s + " ";
            }
            Match[] matches3v3;
            for (int length3 = (matches3v3 = this.matches3v3).length, n = 0; n < length3; ++n) {
                final Match m = matches3v3[n];
                m.teamchat(p, message);
            }
            Match[] matches4v4;
            for (int length4 = (matches4v4 = this.matches4v4).length, n2 = 0; n2 < length4; ++n2) {
                final Match m = matches4v4[n2];
                m.teamchat(p, message);
            }
        }
        if (cmd.getName().equalsIgnoreCase("footcube")) {
            boolean success = true;
            if (args.length < 1) {
                success = false;
            }
            else if (args[0].equalsIgnoreCase("join")) {
                if (this.waitingPlayers.containsKey(p.getName()) || this.playingPlayers.contains(p.getName())) {
                    p.sendMessage(ChatColor.DARK_RED + "Vec si u utakmici");
                }
                else if (this.waitingTeamPlayers.contains(p)) {
                    p.sendMessage(ChatColor.DARK_RED + "Ti si u timu ne mozes uci u utakmicu.");
                }
                else if (args.length < 2) {
                    p.sendMessage(ChatColor.DARK_RED + "Specifikuj u koji GameMode zelis uci.");
                    p.sendMessage(ChatColor.DARK_AQUA + "/fc join [2v2/3v3/4v4]");

                }
                else if (args[1].equalsIgnoreCase("2v2")) {
                    this.matches2v2[this.lobby2v2].join(p, true);
                    this.waitingPlayers.put(p.getName(), 2);
                }
                else if (args[1].equalsIgnoreCase("3v3")) {
                    this.matches3v3[this.lobby3v3].join(p, true);
                    this.waitingPlayers.put(p.getName(), 3);
                    this.removeTeam(p);
                }
                else if (args[1].equalsIgnoreCase("4v4")) {
                    this.matches4v4[this.lobby4v4].join(p, true);
                    this.waitingPlayers.put(p.getName(), 4);
                    this.removeTeam(p);
                }
                else {
                    p.sendMessage(ChatColor.DARK_RED + args[1] + " To nije validna vrsta arene.");
                    p.sendMessage(ChatColor.AQUA + "/fc setuparena [2v2/3v3/4v4]");
                }
            }
            else if (args[0].equalsIgnoreCase("best")) {
                this.updateHighscores(p);
            }

            // Team Method will NOT be used. Just Inputed here for Work. And Rewritted.

            else if (args[0].equalsIgnoreCase("best")) {
                this.updateHighscores(p);
            }
            else if (args[0].equalsIgnoreCase("team")) {
                if (args.length < 2) {
                    p.sendMessage(ChatColor.AQUA + "/fc team [2v2/3v3/4v4] [Igrac]");
                    p.sendMessage(ChatColor.AQUA + "/fc team accept/decline/cancel");
                }
                else if (args[1].equalsIgnoreCase("2v2") || (args[1].equalsIgnoreCase("3v3") || args[1].equalsIgnoreCase("4v4"))) {
                    if (this.waitingPlayers.containsKey(p.getName()) || this.playingPlayers.contains(p.getName())) {
                        p.sendMessage(ChatColor.DARK_RED + "Greska: Ne mozes slati requestove za timovanje dok si u utakmici.");
                    }
                    else if (this.waitingTeamPlayers.contains(p)) {
                        p.sendMessage(ChatColor.DARK_RED + "Greska: Vec Si u timu.");
                    }
                    else if (this.team.containsKey(p)) {
                        final String matchType = this.teamType.get(this.team.get(p)) + "v" + this.teamType.get(this.team.get(p));
                        p.sendMessage(ChatColor.DARK_RED + "Greska: Vec si dobio zahtev za timanje od " + this.team.get(p).getName() + " za " + matchType + " utakmicu.");
                        p.sendMessage(ChatColor.AQUA + "/fc team accept" + ChatColor.GREEN + " ili " + ChatColor.AQUA + "/fc team decline" + ChatColor.GREEN + " da odgovoris na zahtev za timovanje.");
                    }
                    else if (this.teamReverse.containsKey(p)) {
                        final String matchType = this.teamType.get(p) + "v" + this.teamType.get(p);
                        p.sendMessage(ChatColor.DARK_RED + "Greska: Vec si poslao zahtev za timanje igracu " + this.teamReverse.get(p).getName() + " za " + matchType + " utakmicu.");
                    }
                    else if (args.length < 3) {
                        p.sendMessage(ChatColor.GOLD + "Koriscenje: /fc team " + args[1] + " [Igrac]");
                    }
                    else if (this.isOnlinePlayer(args[2])) {
                        final Player player = this.plugin.getServer().getPlayer(args[2]);
                        if (this.waitingTeamPlayers.contains(player)) {
                            p.sendMessage(ChatColor.DARK_RED + args[2] + " je vec u timu.");
                        }
                        else if (this.waitingPlayers.containsKey(player.getName()) || this.playingPlayers.contains(player.getName())) {
                            p.sendMessage(ChatColor.DARK_RED + args[2] + " je vec u utakmici.");
                        }
                        else if (this.team.containsKey(player)) {
                            p.sendMessage(ChatColor.DARK_RED + args[2] + " ima vec zahtev za timovanje.");
                        }
                        else if (this.teamReverse.containsKey(player)) {
                            p.sendMessage(ChatColor.DARK_RED + args[2] + " vec je poslao zahtev za timovanje nekom.");
                        }
                        else {
                            this.team.put(player, p);
                            this.teamReverse.put(p, player);
                            int matchType2 = 3;
                            if (args[1].equalsIgnoreCase("4v4")) {
                                matchType2 = 4;
                            }
                            this.teamType.put(p, matchType2);
                            player.sendMessage(String.valueOf(this.pluginString) + ChatColor.GREEN + p.getName() + " wants to team with you on a " + matchType2 + "v" + matchType2 + " match");
                            player.sendMessage(ChatColor.AQUA + "/fc team accept" + ChatColor.GREEN + " or " + ChatColor.AQUA + "/fc team decline" + ChatColor.GREEN + " to answer the team request");
                            p.sendMessage(String.valueOf(this.pluginString) + ChatColor.GREEN + "You succesfully sent " + player.getName() + " a team request for a " + matchType2 + "v" + matchType2 + " match");
                            p.sendMessage(ChatColor.AQUA + "/fc team cancel" + ChatColor.GREEN + " to cancel this");
                        }
                    }
                    else {
                        p.sendMessage(ChatColor.DARK_RED + args[2] + " nije onlajn");
                    }
                }
                else if (args[1].equalsIgnoreCase("cancel")) {
                    if (this.teamReverse.containsKey(p)) {
                        final Player player = this.teamReverse.get(p);
                        player.sendMessage(String.valueOf(this.pluginString) + ChatColor.GREEN + p.getName() + " cancelled his request to team with you");
                        p.sendMessage(String.valueOf(this.pluginString) + ChatColor.GREEN + "You successfully cancelled your team request");
                        this.teamType.remove(p);
                        this.teamReverse.remove(p);
                        this.team.remove(player);
                    }
                    else {
                        p.sendMessage(ChatColor.RED + "You haven't sent a team request");
                    }
                }
                // IZMENJENO
                else if (args[1].equalsIgnoreCase("accept")) {
                    if (this.team.containsKey(p)) {
                        final Player player = this.team.get(p);
                        if (this.teamType.get(player) == 3) {
                            this.waitingPlayers.put(p.getName(), 3);
                            this.waitingPlayers.put(player.getName(), 3);
                            if (!this.matches3v3[this.lobby3v3].team(p, player)) {
                                this.waitingPlayers.remove(p.getName());
                                this.waitingPlayers.remove(player.getName());
                                this.waitingTeams = this.extendArray(this.waitingTeams, new Player[] { p, player, null });
                                this.waitingTeamPlayers.add(p);
                                this.waitingTeamPlayers.add(player);
                                p.sendMessage(String.valueOf(this.pluginString) + ChatColor.GREEN + "You successfully teamed with " + player.getName());
                                p.sendMessage(ChatColor.GREEN + "You must wait for there to be place for a team, this won't take long");
                                player.sendMessage(String.valueOf(this.pluginString) + ChatColor.GREEN + "You successfully teamed with " + p.getName());
                                player.sendMessage(ChatColor.GREEN + "You must wait for there to be place for a team, this won't take long");
                            }
                        } else {
                            this.waitingPlayers.put(p.getName(), 4);
                            this.waitingPlayers.put(player.getName(), 4);
                            if (!this.matches4v4[this.lobby4v4].team(p, player)) {
                                this.waitingPlayers.remove(p.getName());
                                this.waitingPlayers.remove(player.getName());
                                this.waitingTeams = this.extendArray(this.waitingTeams, new Player[] { p, player });
                                this.waitingTeamPlayers.add(p);
                                this.waitingTeamPlayers.add(player);
                                p.sendMessage(String.valueOf(this.pluginString) + ChatColor.GREEN + "You successfully teamed with " + player.getName());
                                p.sendMessage(ChatColor.GREEN + "You must wait for there to be place for a team, this won't take long");
                                player.sendMessage(String.valueOf(this.pluginString) + ChatColor.GREEN + "You successfully teamed with " + p.getName());
                                player.sendMessage(ChatColor.GREEN + "You must wait for there to be place for a team, this won't take long");
                            }
                        }
                        // IZMENJENO
                        this.team.remove(p);
                        this.teamReverse.remove(player);
                        this.teamType.remove(player);
                    }
                    else {
                        p.sendMessage(ChatColor.RED + "There is no team request to accept");
                    }
                }
                else if (args[1].equalsIgnoreCase("decline")) {
                    if (this.team.containsKey(p)) {
                        final Player player = this.team.get(p);
                        player.sendMessage(String.valueOf(this.pluginString) + ChatColor.GREEN + p.getName() + " declined your team request");
                        p.sendMessage(String.valueOf(this.pluginString) + ChatColor.GREEN + "You successfully declined the team request");
                        this.teamType.remove(player);
                        this.teamReverse.remove(player);
                        this.team.remove(p);
                    }
                    else {
                        p.sendMessage(ChatColor.RED + "There is no team request to decline");
                    }
                }
                else {
                    p.sendMessage(ChatColor.AQUA + "/fc team [3v3" + this.or + "4v4] [player]");
                    p.sendMessage(ChatColor.AQUA + "/fc team accept/decline/cancel");
                }
            }

            // This is just Line Separator || Team Method Mafaka

            else if (args[0].equalsIgnoreCase("takeplace")) {
                if (this.leftMatches.length > 0) {
                    if (this.waitingPlayers.containsKey(p.getName()) || this.playingPlayers.contains(p.getName())) {
                        p.sendMessage(ChatColor.DARK_RED + "Greska: Vec si u utakmici.");
                    }
                    else {
                        this.leftMatches[0].takePlace(p);
                        this.playingPlayers.add(p.getName());
                        final Match[] newL = new Match[this.leftMatches.length - 1];
                        final boolean[] newB = new boolean[this.leftMatches.length - 1];
                        for (int i = 0; i < newL.length; ++i) {
                            newL[i] = this.leftMatches[i + 1];
                            newB[i] = this.leftPlayerIsRed[i + 1];
                        }
                        this.leftMatches = newL;
                        this.leftPlayerIsRed = newB;
                    }
                }
                else {
                    p.sendMessage(ChatColor.DARK_RED + "Nema mesta koje bih mogao preuzeti ostajes na klupi :(");
                }
            }
            else if (args[0].equalsIgnoreCase("stats")) {
                if (args.length > 1) {
                    if (this.uuidConverter.hasValue(args[1])) {
                        final String uuid = this.uuidConverter.getKey(args[1]);
                        this.checkStats(uuid, p);
                    }
                    else {
                        p.sendMessage(ChatColor.DARK_RED + args[1] + " nikad nije igrao Fudbal.");
                        this.checkStats(p.getUniqueId().toString(), p);
                    }
                }
                else {
                    this.checkStats(p.getUniqueId().toString(), p);
                }
            }
            // AA
            else if (args[0].equalsIgnoreCase("leave")) {
                if (this.waitingPlayers.containsKey(p.getName())) {
                    if (this.waitingPlayers.get(p.getName()) == 2) {
                        this.matches2v2[this.lobby2v2].leave(p);
                        this.waitingPlayers.remove(p.getName());
                        int team = -1;
                        for (int j = 0; j < this.waitingTeams.length; ++j) {
                            if (this.waitingTeams[j].length == 1) {
                                team = j;
                                break;
                            }
                        }
                        if (team > -1 && this.matches2v2[this.lobby2v2].team(this.waitingTeams[team][0], this.waitingTeams[team][1])) {
                            this.waitingTeamPlayers.remove(this.waitingTeams[team][0]);
                            this.reduceArray(this.waitingTeams, this.waitingTeams[team][0]);
                        }
                    } else if (this.waitingPlayers.get(p.getName()) == 3) {
                        this.matches3v3[this.lobby3v3].leave(p);
                        this.waitingPlayers.remove(p.getName());
                        int team = -1;
                        for (int j = 0; j < this.waitingTeams.length; ++j) {
                            if (this.waitingTeams[j].length > 2) {
                                team = j;
                                break;
                            }
                        }
                        if (team > -1 && this.matches3v3[this.lobby3v3].team(this.waitingTeams[team][0], this.waitingTeams[team][1])) {
                            this.waitingTeamPlayers.remove(this.waitingTeams[team][0]);
                            this.waitingTeamPlayers.remove(this.waitingTeams[team][1]);
                            this.reduceArray(this.waitingTeams, this.waitingTeams[team][0]);
                        }
                    } else {
                        this.matches4v4[this.lobby4v4].leave(p);
                        this.waitingPlayers.remove(p.getName());
                        int team = -1;
                        for (int j = 0; j < this.waitingTeams.length; ++j) {
                            if (this.waitingTeams[j].length < 3) {
                                team = j;
                                break;
                            }
                        }
                        if (team > -1 && this.matches4v4[this.lobby4v4].team(this.waitingTeams[team][0], this.waitingTeams[team][1])) {
                            this.waitingTeamPlayers.remove(this.waitingTeams[team][0]);
                            this.waitingTeamPlayers.remove(this.waitingTeams[team][1]);
                            this.reduceArray(this.waitingTeams, this.waitingTeams[team][0]);
                        }
                    }
                }
            // AA
                else if (!this.playingPlayers.contains(p.getName())) {
                    p.sendMessage(ChatColor.DARK_RED + "Greska: Nisi u utakmici.");
                }
                else {
                    p.sendMessage(ChatColor.DARK_RED + "Greska: Ne mozes napustiti utakmicu koja je tek pocela.");
                }
            }
            else if (args[0].equalsIgnoreCase("undo") && this.setupGuy == p.getName()) {
                this.setupGuy = null;
                this.setupType = 0;
                this.setupLoc = null;
                p.sendMessage(String.valueOf(this.pluginString) + ChatColor.GREEN + "Undo uspesan.");
            }
            else if (args[0].equalsIgnoreCase("clearStore") && p.hasPermission("pfootcube.admin")) {
                if (args.length > 1) {
                    if (Bukkit.getOfflinePlayer(args[1]).hasPlayedBefore()) {
                        final String s = Bukkit.getOfflinePlayer(args[1]).getUniqueId().toString();
                        if (this.store.has(s)) {
                            this.store.put(s, 0);
                            p.sendMessage(String.valueOf(this.pluginString) + ChatColor.GREEN + "Uspesno si mu ocistio " + args[1] + " prodavnicu.");
                        }
                        else {
                            p.sendMessage(ChatColor.DARK_RED + args[1] + " nema prodavnicin akaunt.");
                        }
                    }
                    else {
                        p.sendMessage(ChatColor.DARK_RED + args[1] + " nema prodavnicin akaunt.");
                    }
                }
                else {
                    p.sendMessage(ChatColor.AQUA + "/fc clearStore [Igrac]");
                }
            }
            else if (args[0].equalsIgnoreCase("statSet") && p.hasPermission("pfootcube.admin")) {
                if (args.length < 4) {
                    p.sendMessage(ChatColor.AQUA + "/fc statSet [Igrac] [Statistika] [Kolicina" + this.or + "ocisti(clear)]");
                }
                else if (this.uuidConverter.hasValue(args[1])) {
                    int amount = 0;
                    try {
                        amount = Integer.parseInt(args[3]);
                    }
                    catch (Exception e) {
                        if (!args[3].equalsIgnoreCase("clear")) {
                            p.sendMessage(ChatColor.RED + args[3] + " is not a number");
                            return;
                        }
                    }
                    final String uuid2 = this.plugin.getServer().getPlayer(args[1]).getUniqueId().toString();
                    if (args[2].equalsIgnoreCase("wins")) {
                        this.wins.put(uuid2, amount);
                    }
                    else if (args[2].equalsIgnoreCase("matches")) {
                        this.matches.put(uuid2, amount);
                    }
                    else if (args[2].equalsIgnoreCase("ties")) {
                        this.ties.put(uuid2, amount);
                    }
                    else if (args[2].equalsIgnoreCase("goals")) {
                        this.goals.put(uuid2, amount);
                    }
                    else if (args[2].equalsIgnoreCase("streak")) {
                        this.winStreak.put(uuid2, amount);
                        this.bestWinStreak.put(uuid2, amount);
                    }
                    else if (args[2].equalsIgnoreCase("store")) {
                        this.store.put(uuid2, amount);
                    }
                    else if (args[2].equalsIgnoreCase("all")) {
                        this.wins.put(uuid2, amount);
                        this.matches.put(uuid2, amount);
                        this.ties.put(uuid2, amount);
                        this.goals.put(uuid2, amount);
                        this.winStreak.put(uuid2, amount);
                        this.bestWinStreak.put(uuid2, amount);
                        this.store.put(uuid2, amount);
                    }
                    else {
                        p.sendMessage(ChatColor.DARK_RED + args[2] + " nije stats, bira od:");
                        p.sendMessage(ChatColor.GRAY + "wins, matches, ties, goals, streak, store, all");
                    }
                }
                else {
                    p.sendMessage(ChatColor.DARK_AQUA + args[1] + " does not have an account");
                }
            }
            else if (args[0].equalsIgnoreCase("setuparena") && p.hasPermission("pfootcube.admin")) {
                if (this.setupGuy == null) {
                    if (args.length < 2) {
                        p.sendMessage(ChatColor.RED + "Specifikuj vrstu arene.");
                        p.sendMessage(ChatColor.AQUA + "/fc setuparena [2v2/3v3/4v4]");
                    }
                    else {
                        if (args[1].equalsIgnoreCase("2v2")) {
                            this.setupType = 2;
                        }
                        if (args[1].equalsIgnoreCase("3v3")) {
                            this.setupType = 3;
                        }
                        else if (args[1].equalsIgnoreCase("4v4")) {
                            this.setupType = 4;
                        }
                        else {
                            p.sendMessage(ChatColor.RED + args[1] + " nije validna vrsta arene.");
                            p.sendMessage(ChatColor.AQUA + "/fc setuparena [2v2/3v3/4v4]");
                        }
                        if (this.setupType > 0) {
                            this.setupGuy = p.getName();
                            p.sendMessage(String.valueOf(this.pluginString) + ChatColor.GREEN + "Poceo si da podesavas arenu.");
                            p.sendMessage(ChatColor.GREEN + "Ako si greskom napisao /fc setuparena, napisi " + ChatColor.AQUA + "/fc undo");
                            p.sendMessage(ChatColor.GREEN + "Tutorial: Stani u sredinu blok-a gola pa posle stani iza linije u golu " + "plavog gola pa pogledaj ga crvenom golu, pa napisi " + ChatColor.AQUA + "/fc set");
                        }
                    }
                }
                else {
                    p.sendMessage(ChatColor.RED + this.setupGuy + " vec podesava arene.");
                }
            }
            else if (args[0].equalsIgnoreCase("cleararenas") && p.hasPermission("pfootcube.admin")) {
                final FileConfiguration cfg = this.plugin.getConfig();
                cfg.set("arenas", (Object)null);
                cfg.addDefault("arenas.2v2.amount", (Object)0);
                cfg.addDefault("arenas.3v3.amount", (Object)0);
                cfg.addDefault("arenas.4v4.amount", (Object)0);
                cfg.options().copyDefaults(true);
                this.plugin.saveConfig();
                this.matches2v2 = new Match[0];
                this.matches3v3 = new Match[0];
                this.matches4v4 = new Match[0];
                p.sendMessage(String.valueOf(this.pluginString) + ChatColor.GREEN + "Uspesno si ocistio sve arene.");
            }
            else if (args[0].equalsIgnoreCase("set") && this.setupGuy == p.getName()) {
                if (this.setupLoc == null) {
                    this.setupLoc = p.getLocation();
                    p.sendMessage(String.valueOf(this.pluginString) + ChatColor.GREEN + "Prva lokacija uspesno namestena.");
                    p.sendMessage(ChatColor.GREEN + "Uradi isto takodje za crveni gol.");
                }
                else {
                    final FileConfiguration cfg = this.plugin.getConfig();
                    final String v = String.valueOf(this.setupType) + "v" + this.setupType;
                    final int arena = cfg.getInt("arenas." + v + ".amount") + 1;
                    final String blue = "arenas." + v + "." + arena + ".blue.";
                    final String red = "arenas." + v + "." + arena + ".red.";
                    final Location b = this.setupLoc;
                    final Location r = p.getLocation();
                    cfg.set("arenas." + v + ".amount", (Object)arena);
                    cfg.set("arenas.world", (Object)p.getWorld().getName());
                    cfg.set(String.valueOf(blue) + "x", (Object)b.getX());
                    cfg.set(String.valueOf(blue) + "y", (Object)b.getY());
                    cfg.set(String.valueOf(blue) + "z", (Object)b.getZ());
                    cfg.set(String.valueOf(blue) + "pitch", (Object)b.getPitch());
                    cfg.set(String.valueOf(blue) + "yaw", (Object)b.getYaw());
                    cfg.set(String.valueOf(red) + "x", (Object)r.getX());
                    cfg.set(String.valueOf(red) + "y", (Object)r.getY());
                    cfg.set(String.valueOf(red) + "z", (Object)r.getZ());
                    cfg.set(String.valueOf(red) + "pitch", (Object)r.getPitch());
                    cfg.set(String.valueOf(red) + "yaw", (Object)r.getYaw());
                    this.plugin.saveConfig();
                    this.addArena(this.setupType, b, r);
                    this.setupGuy = null;
                    this.setupType = 0;
                    this.setupLoc = null;
                    p.sendMessage(String.valueOf(this.pluginString) + ChatColor.GREEN + "Tutorijal: Uspesno si podesio arene.");
                }
            }
            else {
                success = false;
            }
            if (!success) {
                p.sendMessage(String.valueOf(this.pluginString) + ChatColor.WHITE + "Lista dostupnih Komandi." + ChatColor.GOLD + " /" + ChatColor.YELLOW + "fc");
                p.sendMessage(ChatColor.YELLOW + "/fc join [2v2/3v3/4v4]");
                p.sendMessage(ChatColor.YELLOW + "/fc takeplace");
                p.sendMessage(ChatColor.YELLOW + "/fc stats");
                p.sendMessage(ChatColor.YELLOW + "/fc best");
                if (p.hasPermission("pfootcube.admin")) {
                    p.sendMessage(String.valueOf(this.adminString) + ChatColor.AQUA + "/fc setuparena [2v2/3v3/4v4]");
                    p.sendMessage(String.valueOf(this.adminString) + ChatColor.AQUA + "/fc cleararenas");
                }
            }
        }
    }

    @EventHandler
    public void onSignChange(final SignChangeEvent e) {
        if (e.getLine(0).equalsIgnoreCase("[pFootcube]")) {
            e.setLine(0, ChatColor.RED + "Koristi [fc]");
        }
        else if (e.getPlayer().hasPermission("pfootcube.admin") && e.getLine(0).equalsIgnoreCase("[fc]")) {
            if (e.getLine(1).equalsIgnoreCase("join")) {
                if (e.getLine(2).equalsIgnoreCase("2v2")) {
                    e.setLine(0, "[pFootCube]");
                    e.setLine(1, ChatColor.AQUA + "join");
                    e.setLine(2, ChatColor.GREEN + "2v2");
                    e.setLine(3, "");
                }
                if (e.getLine(2).equalsIgnoreCase("3v3")) {
                    e.setLine(0, "[pFootCube]");
                    e.setLine(1, ChatColor.AQUA + "join");
                    e.setLine(2, ChatColor.GREEN + "3v3");
                    e.setLine(3, "");
                }
                else if (e.getLine(2).equalsIgnoreCase("4v4")) {
                    e.setLine(0, "[pFootCube]");
                    e.setLine(1, ChatColor.AQUA + "join");
                    e.setLine(2, ChatColor.GREEN + "4v4");
                    e.setLine(3, "");
                }
            }
            else if (e.getLine(1).equalsIgnoreCase("stats")) {
                e.setLine(0, "[pFootcube]");
                e.setLine(1, ChatColor.AQUA + "stats");
                e.setLine(2, "See how much");
                e.setLine(3, "you score & win");
            }
            else if (e.getLine(1).equalsIgnoreCase("cube")) {
                e.setLine(0, "[pFootcube]");
                e.setLine(1, ChatColor.AQUA + "cube");
                e.setLine(2, "Spawn a");
                e.setLine(3, "cube");
            }
            else if (e.getLine(1).equalsIgnoreCase("store")) {
                e.setLine(0, "[pFootcube]");
                e.setLine(1, ChatColor.AQUA + "store");
                e.setLine(2, "Buy skill");
                e.setLine(3, "upgrades");
            }
            else if (e.getLine(1).equalsIgnoreCase("money")) {
                e.setLine(0, "[pFootcube]");
                e.setLine(1, ChatColor.AQUA + "money");
                e.setLine(2, "Check your");
                e.setLine(3, "balance");
            }
            else if (e.getLine(1).equalsIgnoreCase("highscores")) {
                e.setLine(0, "[pFootcube]");
                e.setLine(1, ChatColor.AQUA + "highscores");
                e.setLine(2, "Check all");
                e.setLine(3, "highscores");
            }
        }
    }
    
    @EventHandler
    public void onIntaract(final PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        final Action a = e.getAction();
        if (a == Action.RIGHT_CLICK_BLOCK || a == Action.LEFT_CLICK_BLOCK) {
            final Block cb = e.getClickedBlock();
            if (cb.getState() instanceof Sign) {
                final Sign s = (Sign)cb.getState();
                if (s.getLine(0).equalsIgnoreCase("[pFootcube]")) {
                    if (s.getLine(1).equalsIgnoreCase(ChatColor.AQUA + "join")) {
                        if (this.waitingTeamPlayers.contains(p)) {
                            p.sendMessage(ChatColor.RED + "U timu si nemozes uci u utakmicu.");
                            return;
                        }
                            if (s.getLine(2).equalsIgnoreCase(ChatColor.GREEN + "2v2")) {
                                this.matches2v2[this.lobby2v2].join(p, true);
                                this.waitingPlayers.put(p.getName(), 2);
                                this.removeTeam(p);
                            } else if (s.getLine(2).equalsIgnoreCase(ChatColor.GREEN + "3v3")) {
                                this.matches3v3[this.lobby3v3].join(p, true);
                                this.waitingPlayers.put(p.getName(), 3);
                                this.removeTeam(p);
                            } else {
                                this.matches4v4[this.lobby4v4].join(p, true);
                                this.waitingPlayers.put(p.getName(), 4);
                                this.removeTeam(p);
                            }
                        }
                    else if (s.getLine(1).equalsIgnoreCase(ChatColor.AQUA + "stats")) {
                        this.checkStats(p.getUniqueId().toString(), p);
                    }
                    else if (s.getLine(1).equalsIgnoreCase(ChatColor.AQUA + "cube")) {
                        if (this.practiceBalls < 10) {
                            ++this.practiceBalls;
                            this.plugin.spawnCube(p.getLocation().add(new Vector(0, 1, 0)));
                        }
                        else {
                            p.sendMessage(ChatColor.RED + "Ima vec dovoljno lopti.");
                        }
                    }
                    else if (s.getLine(1).equalsIgnoreCase(ChatColor.AQUA + "money")) {
                        p.sendMessage(String.valueOf(this.pluginString) + ChatColor.GREEN + "Trenutno imas €" + this.economy.getBalance(p.getName()));
                    }
                    else if (s.getLine(1).equalsIgnoreCase(ChatColor.AQUA + "highscores")) {
                        this.updateHighscores(p);
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onJoin(final PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        final String uuid = p.getUniqueId().toString();
        this.clearInventory(p);
        if (!this.store.has(uuid)) {
            this.store.put(uuid, 0);
        }
        if (!this.winStreak.has(uuid)) {
            this.winStreak.put(uuid, 0);
        }
        if (!this.bestWinStreak.has(uuid)) {
            this.bestWinStreak.put(uuid, 0);
        }
        this.uuidConverter.put(p.getUniqueId().toString(), p.getName());
        this.checkDonate(p);
    }
    
    @EventHandler
    public void onQuit(final PlayerQuitEvent e) {
        final Player p = e.getPlayer();
        if (this.waitingPlayers.containsKey(p.getName())) {
            this.waitingPlayers.remove(p.getName());
        }
        if (this.playingPlayers.contains(p.getName())) {
            this.playingPlayers.remove(p.getName());
        }
        if (this.team.containsKey(p)) {
            final Player player = this.team.get(p);
            player.sendMessage(String.valueOf(this.pluginString) + ChatColor.GREEN + p.getName() + " je odbio zahtev za timovanje.");
            this.teamType.remove(player);
            this.teamReverse.remove(player);
            this.team.remove(p);
        }
        else if (this.teamReverse.containsKey(p)) {
            final Player player = this.teamReverse.get(p);
            player.sendMessage(String.valueOf(this.pluginString) + ChatColor.GREEN + p.getName() + " je odbio vas zahteb za timovanje.");
            this.teamType.remove(p);
            this.teamReverse.remove(p);
            this.team.remove(player);
        }
        else if (this.waitingTeamPlayers.contains(p)) {
            for (int i = 0; i < this.waitingTeams.length; ++i) {
                if (this.waitingTeams[i][0] == p) {
                    final Player player2 = this.waitingTeams[i][1];
                    player2.sendMessage(String.valueOf(this.pluginString) + ChatColor.GREEN + "Tvoj igrac je izasao, timovanje otkazano!");
                    this.waitingTeams = this.reduceArray(this.waitingTeams, p);
                    this.waitingTeamPlayers.remove(p);
                    this.waitingTeamPlayers.remove(player2);
                }
                else if (this.waitingTeams[i][1] == p) {
                    final Player player2 = this.waitingTeams[i][0];
                    player2.sendMessage(String.valueOf(this.pluginString) + ChatColor.GREEN + "Tvoj igrac je izasao, timovanje otkazano!");
                    this.waitingTeams = this.reduceArray(this.waitingTeams, p);
                    this.waitingTeamPlayers.remove(p);
                    this.waitingTeamPlayers.remove(player2);
                }
            }
        }
    }
    
    @EventHandler
    public void onChat(final PlayerChatEvent e) {
        final Player p = e.getPlayer();
        if (!this.global.contains(p)) {
            if (ChatColor.stripColor(e.getMessage()).split(" ")[0].equalsIgnoreCase("GLOBAL CHAT > ")) {
                p.sendMessage(String.valueOf(this.pluginString) + ChatColor.GREEN + "Koristi " + ChatColor.AQUA + "/g [Poruka] " + ChatColor.GREEN + "za globalni chat");
            }
            if (this.waitingPlayers.containsKey(p.getName()) || this.playingPlayers.contains(p.getName())) {
                Match m = null;

                Match[] matches2v2;
                for (int length = (matches2v2 = this.matches2v2).length, i = 0; i < length; ++i) {
                    final Match match = matches2v2[i];
                    if (match.isRed.containsKey(p)) {
                        m = match;
                        break;
                    }
                }

                if (m == null) {
                    Match[] matches3v3;
                    for (int length = (matches3v3 = this.matches3v3).length, i = 0; i < length; ++i) {
                        final Match match = matches3v3[i];
                        if (match.isRed.containsKey(p)) {
                            m = match;
                            break;
                        }
                    }
                }

                if (m == null) {
                    Match[] matches4v4;
                    for (int length2 = (matches4v4 = this.matches4v4).length, j = 0; j < length2; ++j) {
                        final Match match = matches4v4[j];
                        if (match.isRed.containsKey(p)) {
                            m = match;
                            break;
                        }
                    }
                }
                // DA
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!this.spy.contains(player) && !m.isRed.containsKey(player)) {
                        e.getRecipients().remove(player);
                    }
                }
            
            }
            else {
            	  for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!this.spy.contains(player) && (this.waitingPlayers.containsKey(player.getName()) || this.playingPlayers.contains(player.getName()))) {
                        e.getRecipients().remove(player);
                    }
                }
            }
        
        }
        else {
            this.global.remove(p);
        }
    }
    
    private void checkDonate(final Player p) {
        final String uuid = p.getUniqueId().toString();
        if (p.hasPermission("pfootcube.god")) {
            this.donate(p, uuid, 5, 5, 5);
        }
        else if (p.hasPermission("pfootcube.herobrine")) {
            this.donate(p, uuid, 4, 4, 5);
        }
        else if (p.hasPermission("pfootcube.titan")) {
            this.donate(p, uuid, 4, 3, 4);
        }
        else if (p.hasPermission("pfootcube.legend")) {
            this.donate(p, uuid, 3, 3, 3);
        }
        else if (p.hasPermission("pfootcube.premium")) {
            this.donate(p, uuid, 3, 2, 3);
        }
        else if (p.hasPermission("pfootcube.vipplus")) {
            this.donate(p, uuid, 2, 2, 2);
        }
        else if (p.hasPermission("pfootcube.vip")) {
            this.donate(p, uuid, 2, 1, 2);
        }
    }
    
    private void donate(final Player p, final String uuid, final int c, final int x, final int s) {
        while (this.getStoreNumber(p, 5) < c) {
            this.store.put(uuid, this.store.get(uuid) + 10000);
        }
        while (this.getStoreNumber(p, 3) < x) {
            this.store.put(uuid, this.store.get(uuid) + 100);
        }
        while (this.getStoreNumber(p, 1) < s) {
            this.store.put(uuid, this.store.get(uuid) + 1);
        }
    }
    
    public void matchStart(final int type) {
        // OVDE ISTO jebote
        if (type == 2) {
            for (int i = 0; i < this.matches2v2.length; ++i) {
                if (this.matches2v2[i].phase == 1) {
                    this.lobby2v2 = i;
                    break;
                }
            }
            for (int i = 0; i < this.waitingTeams.length; ++i) {
                if (this.waitingTeams[i].length > 1 && this.matches2v2[this.lobby2v2].team(this.waitingTeams[i][0], this.waitingTeams[i][1])) {
                    this.waitingTeamPlayers.remove(this.waitingTeams[i][0]);
                    this.waitingTeamPlayers.remove(this.waitingTeams[i][1]);
                    this.waitingTeams = this.reduceArray(this.waitingTeams, this.waitingTeams[i][0]);
                }
            }
        } else if (type == 3) {
            for (int i = 0; i < this.matches3v3.length; ++i) {
                if (this.matches3v3[i].phase == 1) {
                    this.lobby3v3 = i;
                    break;
                }
            }
            for (int i = 0; i < this.waitingTeams.length; ++i) {
                if (this.waitingTeams[i].length > 2 && this.matches3v3[this.lobby3v3].team(this.waitingTeams[i][0], this.waitingTeams[i][1])) {
                    this.waitingTeamPlayers.remove(this.waitingTeams[i][0]);
                    this.waitingTeamPlayers.remove(this.waitingTeams[i][1]);
                    this.waitingTeams = this.reduceArray(this.waitingTeams, this.waitingTeams[i][0]);
                }
            }
        } else {
            for (int i = 0; i < this.matches4v4.length; ++i) {
                if (this.matches4v4[i].phase == 1) {
                    this.lobby4v4 = i;
                    break;
                }
            }
            for (int i = 0; i < this.waitingTeams.length; ++i) {
                if (this.waitingTeams[i].length < 3 && this.matches4v4[this.lobby4v4].team(this.waitingTeams[i][0], this.waitingTeams[i][1])) {
                    this.waitingTeamPlayers.remove(this.waitingTeams[i][0]);
                    this.waitingTeamPlayers.remove(this.waitingTeams[i][1]);
                    this.waitingTeams = this.reduceArray(this.waitingTeams, this.waitingTeams[i][0]);
                }
            }
        }
    }
    // Promena ovde radjena xd
    public void playerLeaves(final Match m, final boolean red) {
        this.leftMatches = this.extendArray(this.leftMatches, m);
        this.leftPlayerIsRed = this.extendArray(this.leftPlayerIsRed, red);
        if (this.leftMatches.length < 2) {
            this.announcementTime = System.currentTimeMillis();
            String v = null;
            if (red) {
                v = String.valueOf(m.scoreRed) + "-" + m.scoreBlue;
                if (m.scoreRed > m.scoreBlue) {
                    v = String.valueOf(v) + " in the lead";
                }
                else if (m.scoreRed < m.scoreBlue) {
                    v = String.valueOf(v) + " behind";
                }
            }
            else {
                v = String.valueOf(m.scoreBlue) + "-" + m.scoreRed;
                if (m.scoreRed < m.scoreBlue) {
                    v = String.valueOf(v) + " in the lead";
                }
                else if (m.scoreRed > m.scoreBlue) {
                    v = String.valueOf(v) + " behind";
                }
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!this.playingPlayers.contains(p.getName()) && !this.waitingPlayers.containsKey(p.getName())) {
                    if (m.time.getScore() < 0) {
                        p.sendMessage(String.valueOf(this.pluginString) + ChatColor.GOLD + "OBAVESTENJE: " + ChatColor.GREEN + "Igrac je izaso iz " + m.type + "v" + m.type + " utakmicu tokom pripremne faze.");
                    }
                    else {
                        p.sendMessage(String.valueOf(this.pluginString) + ChatColor.GOLD + "OBAVESTENJE: " + ChatColor.GREEN + "Igrac je izaso iz " + m.type + "v" + m.type + " utakmicu, igrao je sa " + v + " preostalih " + m.time.getScore() + " sekundi igre..");
                    }
                    p.sendMessage(ChatColor.GREEN + "Napisi " + ChatColor.AQUA + "/fc takeplace" + ChatColor.GREEN + " da preuzmes njegovo mesto!");
                }
            }
        }
    }
    
    public void undoTakePlace(final Match m) {
        int matches = 0;
        Match[] leftMatches;
        for (int length = (leftMatches = this.leftMatches).length, k = 0; k < length; ++k) {
            final Match match = leftMatches[k];
            if (m.equals(match)) {
                ++matches;
            }
        }
        final Match[] newL = new Match[this.leftMatches.length - matches];
        final boolean[] newB = new boolean[this.leftMatches.length - matches];
        int i = 0;
        int j = 0;
        while (i < this.leftMatches.length) {
            if (!this.leftMatches[i].equals(m)) {
                newL[j] = this.leftMatches[i];
                newB[j] = this.leftPlayerIsRed[i];
                ++j;
            }
            ++i;
        }
        this.leftMatches = newL;
        this.leftPlayerIsRed = newB;
    }
    
    public void endMatch(final Player p) {
        this.playingPlayers.remove(p.getName());
    }
    
    public void playerStarts(final Player p) {
        this.playingPlayers.add(p.getName());
        this.waitingPlayers.remove(p.getName());
    }
    
    public void ballTouch(final Player p) {
        Match[] matches2v2;
        for (int lenght = (matches2v2 = this.matches2v2).length, f = 0; f < lenght; ++f) {
            final Match m = matches2v2[f];
            m.kick(p);
        }
        Match[] matches3v3;
        for (int length = (matches3v3 = this.matches3v3).length, i = 0; i < length; ++i) {
            final Match m = matches3v3[i];
            m.kick(p);
        }
        Match[] matches4v4;
        for (int length2 = (matches4v4 = this.matches4v4).length, j = 0; j < length2; ++j) {
            final Match m = matches4v4[j];
            m.kick(p);
        }
    }
    
    public ItemStack createComplexItem(final Material material, final String name, final String[] lore) {
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        final ArrayList<String> loreArray = new ArrayList<String>();
        for (final String s : lore) {
            loreArray.add(s);
        }
        meta.setLore((List)loreArray);
        item.setItemMeta(meta);
        return item;
    }
    
    public void clearInventory(final Player p) {
        final PlayerInventory inv = p.getInventory();
        inv.setChestplate(new ItemStack(Material.AIR));
        inv.setLeggings(new ItemStack(Material.AIR));
        for (int i = 0; i < inv.getContents().length; ++i) {
            final ItemStack is = inv.getContents()[i];
            if (is != null && is.getType() != Material.DIAMOND) {
                inv.setItem(i, new ItemStack(Material.AIR));
            }
        }
    }
    
    public int getStoreNumber(final Player p, final int digit) {
        final int storeContent = this.store.get(p.getUniqueId().toString());
        return (int)((storeContent % Math.pow(10.0, digit + 1) - storeContent % Math.pow(10.0, digit - 1)) / Math.pow(10.0, digit - 1));
    }

    
    private void removeTeam(final Player p) {
        if (this.team.containsKey(p)) {
            final Player player = this.team.get(p);
            player.sendMessage(String.valueOf(this.pluginString) + ChatColor.GREEN + p.getName() + " declined your team request");
            p.sendMessage(String.valueOf(this.pluginString) + ChatColor.GREEN + "You declined the team request because you joined a match");
            this.teamType.remove(player);
            this.teamReverse.remove(player);
            this.team.remove(p);
        }
        if (this.teamReverse.containsKey(p)) {
            final Player player = this.teamReverse.get(p);
            player.sendMessage(String.valueOf(this.pluginString) + ChatColor.GREEN + p.getName() + " cancelled his team request");
            p.sendMessage(String.valueOf(this.pluginString) + ChatColor.GREEN + "You cancelled your team request because you joined a match");
            this.teamType.remove(p);
            this.teamReverse.remove(p);
            this.team.remove(player);
        }
    }
    
    private Player[][] extendArray(final Player[][] oldL, final Player[] add) {
        final Player[][] newL = new Player[0][oldL.length + 1];
        for (int i = 0; i < oldL.length; ++i) {
            newL[i] = oldL[i];
        }
        newL[oldL.length] = add;
        return newL;
    }
    
    private Player[][] reduceArray(final Player[][] oldL, final Player remove) {
        final Player[][] newL = new Player[0][oldL.length - 1];
        int i = 0;
        int j = 0;
        while (i < newL.length) {
            if (oldL[i][0] != remove && oldL[i][1] != remove) {
                newL[j] = oldL[i];
                ++j;
            }
            ++i;
        }
        return newL;
    }
    
    private Match[] extendArray(final Match[] oldL, final Match add) {
        final Match[] newL = new Match[oldL.length + 1];
        for (int i = 0; i < oldL.length; ++i) {
            newL[i] = oldL[i];
        }
        newL[oldL.length] = add;
        return newL;
    }
    
    private boolean[] extendArray(final boolean[] oldL, final boolean add) {
        final boolean[] newL = new boolean[oldL.length + 1];
        for (int i = 0; i < oldL.length; ++i) {
            newL[i] = oldL[i];
        }
        newL[oldL.length] = add;
        return newL;
    }
    
    private boolean isOnlinePlayer(final String s) {
    	  for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }
    
    private void addArena(final int type, final Location b, final Location r) {
        final Location m = new Location(b.getWorld(), (b.getX() + r.getX()) / 2.0, (b.getY() + r.getY()) / 2.0 + 2.0, (b.getZ() + r.getZ()) / 2.0);
        if (type == 2) {
            this.matches2v2 = this.extendArray(this.matches2v2, new Match(this, this.plugin, 2, b, r, m, this.matches2v2.length + this.matches3v3.length + this.matches4v4.length));
        } else if (type == 3) {
            this.matches3v3 = this.extendArray(this.matches3v3, new Match(this, this.plugin, 3, b, r, m, this.matches3v3.length + this.matches4v4.length));
        } else {
            this.matches4v4 = this.extendArray(this.matches4v4, new Match(this, this.plugin, 4, b, r, m, this.matches3v3.length + this.matches4v4.length));
        }
    }
    
    private void loadArenas(final FileConfiguration cfg) {
        for (int i = 1; i <= cfg.getInt("arenas.2v2.amount"); ++i) {
            final World world = this.plugin.getServer().getWorld(cfg.getString("arenas.world"));
            final String blue = "arenas.2v2." + i + ".blue.";
            final String red = "arenas.2v2." + i + ".red.";
            final Location b = new Location(world, cfg.getDouble(String.valueOf(blue) + "x"), cfg.getDouble(String.valueOf(blue) + "y"), cfg.getDouble(String.valueOf(blue) + "z"));
            b.setPitch((float)cfg.getDouble(String.valueOf(blue) + "pitch"));
            b.setYaw((float)cfg.getDouble(String.valueOf(blue) + "yaw"));
            final Location r = new Location(world, cfg.getDouble(String.valueOf(red) + "x"), cfg.getDouble(String.valueOf(red) + "y"), cfg.getDouble(String.valueOf(red) + "z"));
            r.setPitch((float)cfg.getDouble(String.valueOf(red) + "pitch"));
            r.setYaw((float)cfg.getDouble(String.valueOf(red) + "yaw"));
            this.addArena(3, b, r);
        }
        for (int i = 1; i <= cfg.getInt("arenas.3v3.amount"); ++i) {
            final World world = this.plugin.getServer().getWorld(cfg.getString("arenas.world"));
            final String blue = "arenas.3v3." + i + ".blue.";
            final String red = "arenas.3v3." + i + ".red.";
            final Location b = new Location(world, cfg.getDouble(String.valueOf(blue) + "x"), cfg.getDouble(String.valueOf(blue) + "y"), cfg.getDouble(String.valueOf(blue) + "z"));
            b.setPitch((float)cfg.getDouble(String.valueOf(blue) + "pitch"));
            b.setYaw((float)cfg.getDouble(String.valueOf(blue) + "yaw"));
            final Location r = new Location(world, cfg.getDouble(String.valueOf(red) + "x"), cfg.getDouble(String.valueOf(red) + "y"), cfg.getDouble(String.valueOf(red) + "z"));
            r.setPitch((float)cfg.getDouble(String.valueOf(red) + "pitch"));
            r.setYaw((float)cfg.getDouble(String.valueOf(red) + "yaw"));
            this.addArena(3, b, r);
        }
        for (int i = 1; i <= cfg.getInt("arenas.4v4.amount"); ++i) {
            final World world = this.plugin.getServer().getWorld(cfg.getString("arenas.world"));
            final String blue = "arenas.4v4." + i + ".blue.";
            final String red = "arenas.4v4." + i + ".red.";
            final Location b = new Location(world, cfg.getDouble(String.valueOf(blue) + "x"), cfg.getDouble(String.valueOf(blue) + "y"), cfg.getDouble(String.valueOf(blue) + "z"));
            b.setPitch((float)cfg.getDouble(String.valueOf(blue) + "pitch"));
            b.setYaw((float)cfg.getDouble(String.valueOf(blue) + "yaw"));
            final Location r = new Location(world, cfg.getDouble(String.valueOf(red) + "x"), cfg.getDouble(String.valueOf(red) + "y"), cfg.getDouble(String.valueOf(red) + "z"));
            r.setPitch((float)cfg.getDouble(String.valueOf(red) + "pitch"));
            r.setYaw((float)cfg.getDouble(String.valueOf(red) + "yaw"));
            this.addArena(4, b, r);
        }
    }
    
    private void checkStats(final String uuid, final Player asker) {
        String You = "You";
        String you = "you";
        String Your = "Your";
        String have = "have";
        String them = "you";
        if (!uuid.equals(asker.getUniqueId().toString())) {
            You = (you = this.uuidConverter.get(uuid));
            Your = String.valueOf(you) + "'s";
            have = "has";
            them = "them";
        }
        if (this.matches.has(uuid)) {
            final int m = this.matches.get(uuid);
            final int w = this.wins.get(uuid);
            final int t = this.ties.get(uuid);
            final int s = this.bestWinStreak.get(uuid);
            final int l = m - w - t;
            double mw = m;
            if (w > 0) {
                mw = 100 * m / w / 100.0;
            }
            final int g = this.goals.get(uuid);
            double gm = 0.0;
            if (m > 0) {
                gm = 100 * g / m / 100.0;
            }
            final double multiplier = 1.0 - Math.pow(0.9, m);
            double goalBonus = 0.5;
            if (m > 0) {
                goalBonus = 1.0 - multiplier * Math.pow(0.2, g / (double)m) - 0.5 / Math.pow(1.1111111111111112, m);
            }
            double addition = 0.0;
            if (m > 0 && w + t > 0) {
                addition = 8.0 * (1.0 / (100 * m / (w + 0.5 * t) / 100.0)) - 4.0;
            }
            else if (m > 0) {
                addition = -4.0;
            }
            final double skillLevel = (int)(100.0 * (5.0 + goalBonus + addition * multiplier)) / 100.0;
            final int rank = (int)(skillLevel * 2.0 - 0.5);
            String rang = null;
            switch (rank) {
                case 1: {
                    rang = "Noob";
                    break;
                }
                case 2: {
                    rang = "Loser";
                    break;
                }
                case 3: {
                    rang = "Baby";
                    break;
                }
                case 4: {
                    rang = "Pupil";
                    break;
                }
                case 5: {
                    rang = "Bad";
                    break;
                }
                case 6: {
                    rang = "Sadface";
                    break;
                }
                case 7: {
                    rang = "Meh";
                    break;
                }
                case 8: {
                    rang = "Player";
                    break;
                }
                case 9: {
                    rang = "Ok";
                    break;
                }
                case 10: {
                    rang = "Average";
                    break;
                }
                case 11: {
                    rang = "Well";
                    break;
                }
                case 12: {
                    rang = "Good";
                    break;
                }
                case 13: {
                    rang = "King";
                    break;
                }
                case 14: {
                    rang = "Superb";
                    break;
                }
                case 15: {
                    rang = "Pro";
                    break;
                }
                case 16: {
                    rang = "Maradona";
                    break;
                }
                case 17: {
                    rang = "Superman";
                    break;
                }
                case 18: {
                    rang = "God";
                    break;
                }
                case 19: {
                    rang = "Hacker";
                    break;
                }
            }
            asker.sendMessage(String.valueOf(this.pluginString) + ChatColor.GOLD + Your + " Statistika:");
            asker.sendMessage(ChatColor.GRAY + "Ti si igrao" + m + " utakmica.");
            asker.sendMessage(ChatColor.GRAY + "Ti si imao" + w + " pobeda, " + l + " izgubljenih i " + t + " izjednacenih");
            if (w > 0) {
                asker.sendMessage(ChatColor.GRAY + "To sve ukupno imas " + mw + " utakmica pobedjenih");
            }
            asker.sendMessage(ChatColor.GRAY + "Tvoj" + " najveci dugi win streak je bio " + s + " pobeda u nizu.");
            asker.sendMessage(ChatColor.GRAY + "U ovim utakmicama ti si postigao "  + g + " golova.");
            asker.sendMessage(ChatColor.GRAY + "Sto znaci da si imao " + gm + " golova po utakmici");
            asker.sendMessage(ChatColor.GRAY + " Tvoj skill level trenutno je. " + skillLevel + " sto ti daje " + " rank '" + rang + "'");
        }
        else {
            asker.sendMessage(ChatColor.DARK_RED + "Nisi nazalost igrao ni jednu utakmicu.");
        }
    }
    
    private void updateHighscores(final Player p) {
        if (this.highscores == null) {
            this.highscores = new Highscores(this, this.plugin, p);
        }
        else if (this.highscores.needsUpdate()) {
            this.highscores.update(p);
        }
        else if (this.highscores.isUpdating) {
            this.highscores.addWaitingPlayer(p);
        }
        else {
            this.highscores.showHighscores(p);
        }
    }
    
    private void update() {
        for (int i = 0; i < this.matches2v2.length; ++i) {
            this.matches2v2[i].update();
        }
        for (int i = 0; i < this.matches3v3.length; ++i) {
            this.matches3v3[i].update();
        }
        for (int i = 0; i < this.matches4v4.length; ++i) {
            this.matches4v4[i].update();
        }
        if (this.leftMatches.length > 0 && System.currentTimeMillis() - this.announcementTime > 30000L) {
            final Match m = this.leftMatches[0];
            this.announcementTime = System.currentTimeMillis();
            String v = null;
            if (this.leftPlayerIsRed[0]) {
                v = String.valueOf(m.scoreRed) + "-" + m.scoreBlue;
                if (m.scoreRed > m.scoreBlue) {
                    v = String.valueOf(v) + " in the lead";
                }
                else if (m.scoreRed < m.scoreBlue) {
                    v = String.valueOf(v) + " behind";
                }
            }
            else {
                v = String.valueOf(m.scoreBlue) + "-" + m.scoreRed;
                if (m.scoreRed < m.scoreBlue) {
                    v = String.valueOf(v) + " in the lead";
                }
                else if (m.scoreRed > m.scoreBlue) {
                    v = String.valueOf(v) + " behind";
                }
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!this.playingPlayers.contains(p.getName()) && !this.waitingPlayers.containsKey(p.getName())) {
                    if (m.time.getScore() < 0) {
                        p.sendMessage(String.valueOf(this.pluginString) + ChatColor.GOLD + "OBAVESTENJE: " + ChatColor.GREEN + "Igrac je napustio " + m.type + "v" + m.type + " utakmicu tokom faze zagrevanja.");
                    }
                    else {
                        p.sendMessage(String.valueOf(this.pluginString) + ChatColor.GOLD + "OBAVESTENJE: " + ChatColor.GREEN + "Igrac je napustio " + m.type + "v" + m.type + " utakmicu, preostalo mu je vremena " + v + " jos " + m.time.getScore() + " sekudni za igrat.");
                    }
                    p.sendMessage(ChatColor.GREEN + "Koristi " + ChatColor.AQUA + "/fc takeplace" + ChatColor.GREEN + " da mu preuzmes mesto");
                }
            }
        }
    }
    
    private boolean setupEconomy() {
        final RegisteredServiceProvider<Economy> economyProvider = (RegisteredServiceProvider<Economy>)this.plugin.getServer().getServicesManager().getRegistration((Class)Economy.class);
        if (economyProvider != null) {
            this.economy = (Economy)economyProvider.getProvider();
        }
        return this.economy != null;
    }
}
