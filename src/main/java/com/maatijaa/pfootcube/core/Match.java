
package com.maatijaa.pfootcube.core;

import com.maatijaa.pfootcube.pFootcube;
import org.bukkit.Sound;
import org.bukkit.util.Vector;
import java.util.Random;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.ChatColor;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Slime;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Score;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.HashMap;
import org.bukkit.event.Listener;

public class Match implements Listener
{
    public int matchID;
    public int type;
    public int phase;
    private int countdown;
    private int tickToSec;
    private int teams;
    private long startTime;
    private HashMap<Player, Long> sugarCooldown;
    private Location blue;
    private Location red;
    private Location mid;
    private Organization organization;
    private pFootcube plugin;
    private boolean x;
    private boolean redAboveBlue;
    private Player[] redPlayers;
    private Player[] bluePlayers;
    private ArrayList<Player> teamers;
    private ArrayList<Player> takePlace;
    public HashMap<Player, Boolean> isRed;
    private Player lastKickRed;
    private Player lastKickBlue;
    public int scoreRed;
    public int scoreBlue;
    private HashMap<Player, Integer> goals;
    private ItemStack redChestPlate;
    private ItemStack redLeggings;
    private ItemStack blueChestPlate;
    private ItemStack blueLeggings;
    private ItemStack sugar;
    public Score time;
    private Score redGoals;
    private Score blueGoals;
    private ScoreboardManager sbm;
    private Scoreboard sb;
    private Objective o;
    private Slime cube;
    
    public Match(final Organization org, final pFootcube pl, final int t, final Location b, final Location r, final Location m, final int id) {
        this.sugarCooldown = new HashMap<Player, Long>();
        this.redPlayers = new Player[0];
        this.bluePlayers = new Player[0];
        this.teamers = new ArrayList<Player>();
        this.takePlace = new ArrayList<Player>();
        this.isRed = new HashMap<Player, Boolean>();
        this.lastKickRed = null;
        this.lastKickBlue = null;
        this.goals = new HashMap<Player, Integer>();
        this.matchID = id;
        this.organization = org;
        this.plugin = pl;
        this.type = t;
        this.blue = b;
        this.red = r;
        this.mid = m;
        this.phase = 1;
        this.scoreRed = 0;
        this.scoreBlue = 0;
        this.startTime = 0L;
        this.redChestPlate = this.createColoredArmour(Material.LEATHER_CHESTPLATE, Color.RED);
        this.redLeggings = this.createColoredArmour(Material.LEATHER_LEGGINGS, Color.RED);
        this.blueChestPlate = this.createColoredArmour(Material.LEATHER_CHESTPLATE, Color.BLUE);
        this.blueLeggings = this.createColoredArmour(Material.LEATHER_LEGGINGS, Color.BLUE);
        this.sugar = this.organization.createComplexItem(Material.SUGAR, "Speed boost", new String[] { "Right click to get speed" });
        this.sbm = Bukkit.getScoreboardManager();
        this.sb = this.sbm.getNewScoreboard();
        boolean objectiveExists = false;
        for (final Objective ob : this.sb.getObjectives()) {
            if (ob.getName().equalsIgnoreCase("Match")) {
                objectiveExists = true;
                break;
            }
        }
        if (objectiveExists) {
            (this.o = this.sb.getObjective("Match")).setDisplayName("Match");
        }
        else {
            (this.o = this.sb.registerNewObjective("Match", "dummy")).setDisplaySlot(DisplaySlot.SIDEBAR);
            this.o.setDisplayName("Match");
        }
        (this.time = this.o.getScore(Bukkit.getOfflinePlayer("Time left:"))).setScore(300);
        (this.redGoals = this.o.getScore(Bukkit.getOfflinePlayer("Red team:"))).setScore(0);
        (this.blueGoals = this.o.getScore(Bukkit.getOfflinePlayer("Blue team:"))).setScore(0);
        this.x = (Math.abs(b.getX() - r.getX()) > Math.abs(b.getZ() - r.getZ()));
        if (this.x) {
            if (r.getX() > b.getX()) {
                this.redAboveBlue = true;
            }
            else {
                this.redAboveBlue = false;
            }
        }
        else if (r.getZ() > b.getZ()) {
            this.redAboveBlue = true;
        }
        else {
            this.redAboveBlue = false;
        }
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this.plugin);
    }
    
    @EventHandler
    public void onQuit(final PlayerQuitEvent e) {
        final Player p = e.getPlayer();
        if (this.isRed.containsKey(p)) {
            this.organization.clearInventory(p);
            if (this.phase != 1) {
                this.organization.playerLeaves(this, this.isRed.get(p));
            }
            if (this.isRed.get(p)) {
                this.redPlayers = this.reduceArray(this.redPlayers, p);
            }
            else {
                this.bluePlayers = this.reduceArray(this.bluePlayers, p);
            }
            this.isRed.remove(p);
        }
    }
    
    @EventHandler
    public void onInteract(final PlayerInteractEvent e) {
        final Action a = e.getAction();
        if (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
            final Player p = e.getPlayer();
            if (p.getInventory().getItemInHand().getType() == Material.SUGAR && this.isRed.containsKey(p)) {
                p.getInventory().setItemInHand(new ItemStack(Material.AIR));
                String s = "";
                if (this.organization.getStoreNumber(p, 7) == 1) {
                    s = "II ";
                }
                p.sendMessage(String.valueOf(this.organization.pluginString) + ChatColor.GREEN + "You got speed " + s + "for " + (5 + this.organization.getStoreNumber(p, 9)) + " seconds, you can use this again in " + (60 - 3 * this.organization.getStoreNumber(p, 1)) + " seconds");
                this.sugarCooldown.put(p, System.currentTimeMillis());
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100 + 20 * this.organization.getStoreNumber(p, 9), this.organization.getStoreNumber(p, 7)));
            }
        }
    }
    
    public boolean equals(final Match m) {
        return m.matchID == this.matchID;
    }
    
    private ItemStack createColoredArmour(final Material material, final Color color) {
        final ItemStack is = new ItemStack(material);
        if (is.getItemMeta() instanceof LeatherArmorMeta) {
            final LeatherArmorMeta meta = (LeatherArmorMeta)is.getItemMeta();
            meta.setColor(color);
            is.setItemMeta((ItemMeta)meta);
        }
        return is;
    }
    
    private Player[] extendArray(final Player[] oldL, final Player add) {
        final Player[] newL = new Player[oldL.length + 1];
        for (int i = 0; i < oldL.length; ++i) {
            newL[i] = oldL[i];
        }
        newL[oldL.length] = add;
        return newL;
    }
    
    private Player[] reduceArray(final Player[] oldL, final Player remove) {
        final Player[] newL = new Player[oldL.length - 1];
        int i = 0;
        int j = 0;
        while (i < newL.length) {
            if (oldL[i] != remove) {
                newL[j] = oldL[i];
                ++j;
            }
            ++i;
        }
        return newL;
    }
    
    public void join(final Player p, final boolean b) {
        if (!this.organization.matches.has(p.getUniqueId().toString())) {
            this.organization.matches.put(p.getUniqueId().toString(), 0);
            this.organization.wins.put(p.getUniqueId().toString(), 0);
            this.organization.ties.put(p.getUniqueId().toString(), 0);
            this.organization.goals.put(p.getUniqueId().toString(), 0);
        }
        if (this.redPlayers.length < this.type && !b) {
            this.redPlayers = this.extendArray(this.redPlayers, p);
            this.isRed.put(p, true);
            p.teleport(this.red);
            p.sendMessage(String.valueOf(this.organization.pluginString) + ChatColor.GREEN + "Welcome to the match, you are on the red team");
        }
        else {
            this.bluePlayers = this.extendArray(this.bluePlayers, p);
            this.isRed.put(p, false);
            p.teleport(this.blue);
            p.sendMessage(String.valueOf(this.organization.pluginString) + ChatColor.GREEN + "Welcome to the match, you are on the blue team");
        }
        if (this.bluePlayers.length >= this.type && this.redPlayers.length >= this.type) {
            this.phase = 2;
            this.countdown = 30;
            this.tickToSec = 20;
            this.organization.matchStart(this.type);
            for (final Player player : this.isRed.keySet()) {
                player.setLevel(30);
                this.organization.matches.rise(player.getUniqueId().toString());
                player.getInventory().setItemInHand(this.sugar);
                if (this.isRed.get(player)) {
                    player.getInventory().setChestplate(this.redChestPlate);
                    player.getInventory().setLeggings(this.redLeggings);
                }
                else {
                    player.getInventory().setChestplate(this.blueChestPlate);
                    player.getInventory().setLeggings(this.blueLeggings);
                }
                player.sendMessage(String.valueOf(this.organization.pluginString) + ChatColor.GREEN + "There are enough players to start, " + "the match will start in 30 seconds. You now have time to discuss about your strategy.");
                player.sendMessage(ChatColor.DARK_GREEN + "TIP: " + ChatColor.GREEN + "Choose someone to be goalkeeper.");
                player.sendMessage(ChatColor.GREEN + "Use " + ChatColor.AQUA + "/tc [Message]" + ChatColor.GREEN + " for teamchat.");
            }
        }
        else {
            p.sendMessage(ChatColor.GREEN + "Use " + ChatColor.AQUA + "/fc leave" + ChatColor.GREEN + " to leave this room");
        }
    }
    
    public void leave(final Player p) {
        if (this.isRed.get(p)) {
            this.redPlayers = this.reduceArray(this.redPlayers, p);
        }
        else {
            this.bluePlayers = this.reduceArray(this.bluePlayers, p);
        }
        this.isRed.remove(p);
        p.teleport(p.getWorld().getSpawnLocation());
    }
    
    public void takePlace(final Player p) {
        this.takePlace.add(p);
        if (this.redPlayers.length < this.type) {
            this.redPlayers = this.extendArray(this.redPlayers, p);
            this.isRed.put(p, true);
            p.teleport(this.red);
            p.sendMessage(String.valueOf(this.organization.pluginString) + ChatColor.GREEN + "Welcome to the match, you are on the red team");
        }
        else {
            this.bluePlayers = this.extendArray(this.bluePlayers, p);
            this.isRed.put(p, false);
            p.teleport(this.blue);
            p.sendMessage(String.valueOf(this.organization.pluginString) + ChatColor.GREEN + "Welcome to the match, you are on the blue team");
        }
        p.getInventory().setItemInHand(this.sugar);
        if (this.isRed.get(p)) {
            p.getInventory().setChestplate(this.redChestPlate);
            p.getInventory().setLeggings(this.redLeggings);
        }
        else {
            p.getInventory().setChestplate(this.blueChestPlate);
            p.getInventory().setLeggings(this.blueLeggings);
        }
        if (this.phase > 2) {
            p.setScoreboard(this.sb);
        }
    }
    
    public void kick(final Player p) {
        if (this.isRed.containsKey(p)) {
            if (this.isRed.get(p)) {
                this.lastKickRed = p;
            }
            else {
                this.lastKickBlue = p;
            }
        }
    }
    
    public void teamchat(final Player p, final String message) {
        if (this.isRed.containsKey(p)) {
            if (this.isRed.get(p)) {
                Player[] redPlayers;
                for (int length = (redPlayers = this.redPlayers).length, i = 0; i < length; ++i) {
                    final Player player = redPlayers[i];
                    player.sendMessage(ChatColor.RED + "TC " + p.getName() + ChatColor.WHITE + ": " + message);
                }
            }
            else {
                Player[] bluePlayers;
                for (int length2 = (bluePlayers = this.bluePlayers).length, j = 0; j < length2; ++j) {
                    final Player player = bluePlayers[j];
                    player.sendMessage(ChatColor.BLUE + "TC " + p.getName() + ChatColor.WHITE + ": " + message);
                }
            }
        }
    }
    
    public boolean team(final Player p0, final Player p1) {
        if (this.redPlayers.length + this.bluePlayers.length > 2 * this.type - 2 || (this.teams >= 2 && this.type == 3)) {
            return false;
        }
        p0.sendMessage(String.valueOf(this.organization.pluginString) + ChatColor.GREEN + "You succesfully teamed with " + p1.getName());
        p1.sendMessage(String.valueOf(this.organization.pluginString) + ChatColor.GREEN + "You succesfully teamed with " + p0.getName());
        this.teamers.add(p0);
        this.teamers.add(p1);
        ++this.teams;
        if (this.type - this.redPlayers.length >= 2) {
            this.join(p0, false);
            this.join(p1, false);
        }
        else if (this.type - this.bluePlayers.length >= 2) {
            this.join(p0, true);
            this.join(p1, true);
        }
        else {
            boolean rare = true;
            Player[] bluePlayers;
            for (int length = (bluePlayers = this.bluePlayers).length, i = 0; i < length; ++i) {
                final Player p2 = bluePlayers[i];
                if (!this.teamers.contains(p2)) {
                    this.bluePlayers = this.reduceArray(this.bluePlayers, p2);
                    this.redPlayers = this.extendArray(this.redPlayers, p2);
                    this.isRed.put(p2, true);
                    p2.teleport(this.red);
                    p2.sendMessage(String.valueOf(this.organization.pluginString) + ChatColor.GREEN + "You switched teams so that " + p0.getName() + " and " + p1.getName() + " could team");
                    this.join(p0, true);
                    this.join(p1, true);
                    rare = false;
                    break;
                }
            }
            if (rare) {
                Player[] redPlayers;
                for (int length2 = (redPlayers = this.redPlayers).length, j = 0; j < length2; ++j) {
                    final Player p2 = redPlayers[j];
                    if (!this.teamers.contains(p2)) {
                        this.redPlayers = this.reduceArray(this.redPlayers, p2);
                        this.bluePlayers = this.extendArray(this.bluePlayers, p2);
                        this.isRed.put(p2, true);
                        p2.teleport(this.blue);
                        p2.sendMessage(String.valueOf(this.organization.pluginString) + ChatColor.GREEN + "You switched teams so that " + p0.getName() + " and " + p1.getName() + " could team");
                        this.join(p0, false);
                        this.join(p1, false);
                        break;
                    }
                }
            }
        }
        return true;
    }
    
    public void update() {
        --this.tickToSec;
        if (this.phase == 3) {
            final Location l = this.cube.getLocation();
            if (this.x) {
                if (((this.redAboveBlue && l.getBlockX() >= this.red.getBlockX()) || (!this.redAboveBlue && this.red.getBlockX() >= l.getBlockX())) && l.getY() < this.red.getY() + 3.0 && l.getZ() < this.red.getZ() + 4.0 && l.getZ() > this.red.getZ() - 4.0) {
                    this.score(false);
                }
                else if (((this.redAboveBlue && l.getBlockX() <= this.blue.getBlockX()) || (!this.redAboveBlue && this.blue.getBlockX() <= l.getBlockX())) && l.getY() < this.blue.getY() + 3.0 && l.getZ() < this.blue.getZ() + 4.0 && l.getZ() > this.blue.getZ() - 4.0) {
                    this.score(true);
                }
            }
            else if (((this.redAboveBlue && l.getBlockZ() >= this.red.getBlockZ()) || (!this.redAboveBlue && this.red.getBlockZ() >= l.getBlockZ())) && l.getY() < this.red.getY() + 3.0 && l.getX() < this.red.getX() + 4.0 && l.getX() > this.red.getX() - 4.0) {
                this.score(false);
            }
            else if (((this.redAboveBlue && l.getBlockZ() <= this.blue.getBlockZ()) || (!this.redAboveBlue && this.blue.getBlockZ() <= l.getBlockZ())) && l.getY() < this.blue.getY() + 3.0 && l.getX() < this.blue.getX() + 4.0 && l.getX() > this.blue.getX() - 4.0) {
                this.score(true);
            }
        }
        if ((this.phase == 2 || this.phase == 4) && this.tickToSec == 0) {
            --this.countdown;
            this.tickToSec = 20;
            for (final Player p : this.isRed.keySet()) {
                p.setLevel(this.countdown);
            }
            if (this.countdown <= 0) {
                String message;
                if (this.phase == 2) {
                    message = String.valueOf(this.organization.pluginString) + ChatColor.GREEN + "The match has started, good luck";
                    this.startTime = System.currentTimeMillis();
                    this.redGoals.setScore(0);
                    this.blueGoals.setScore(0);
                    for (final Player p2 : this.isRed.keySet()) {
                        this.organization.playerStarts(p2);
                        p2.setScoreboard(this.sb);
                    }
                }
                else {
                    message = String.valueOf(this.organization.pluginString) + ChatColor.GREEN + "The match will now proceed";
                }
                this.phase = 3;
                this.cube = this.plugin.spawnCube(this.mid);
                final Random random = new Random();
                final double vertical = 0.3 * random.nextDouble() + 0.2;
                double horizontal = 0.3 * random.nextDouble() + 0.3;
                if (random.nextBoolean()) {
                    horizontal *= -1.0;
                }
                if (this.x) {
                    this.cube.setVelocity(new Vector(0.0, vertical, horizontal));
                }
                else {
                    this.cube.setVelocity(new Vector(horizontal, vertical, 0.0));
                }
                for (final Player p3 : this.isRed.keySet()) {
                    p3.sendMessage(message);
                    if (this.isRed.get(p3)) {
                        p3.teleport(this.red);
                    }
                    else {
                        p3.teleport(this.blue);
                    }
                    p3.playSound(p3.getLocation(), Sound.EXPLODE, 1.0f, 1.0f);
                }
            }
            else if (this.countdown <= 3) {
                for (final Player p : this.isRed.keySet()) {
                    p.playSound(p.getLocation(), Sound.NOTE_STICKS, 1.0f, 1.0f);
                }
            }
        }
        this.time.setScore(300 - (int)(System.currentTimeMillis() - this.startTime) / 1000);
        if (this.time.getScore() <= 0 && this.phase > 2) {
            for (final Player p : this.isRed.keySet()) {
                final String uuid = p.getUniqueId().toString();
                this.organization.endMatch(p);
                p.setScoreboard(this.sbm.getNewScoreboard());
                p.teleport(p.getWorld().getSpawnLocation());
                this.organization.clearInventory(p);
                if (this.scoreRed > this.scoreBlue) {
                    p.sendMessage(String.valueOf(this.organization.pluginString) + ChatColor.GREEN + "Time's up! The red team has won");
                    if (this.isRed.get(p) && !this.takePlace.contains(p)) {
                        this.organization.wins.rise(uuid);
                        this.organization.winStreak.rise(uuid);
                        if (this.organization.winStreak.get(uuid) > this.organization.bestWinStreak.get(uuid)) {
                            this.organization.bestWinStreak.put(uuid, this.organization.winStreak.get(uuid));
                        }
                        this.organization.economy.depositPlayer(p.getName(), 15.0);
                        p.sendMessage(String.valueOf(this.organization.pluginString) + ChatColor.GREEN + "You got 15 credits for winning");
                        if (this.organization.winStreak.get(uuid) % 5 != 0) {
                            continue;
                        }
                        this.organization.economy.depositPlayer(p.getName(), 100.0);
                        p.sendMessage(String.valueOf(this.organization.pluginString) + ChatColor.GOLD + ChatColor.BOLD + "You get 100 credits bonus for winning " + this.organization.winStreak.get(uuid) + " times in a row!!!");
                    }
                    else {
                        this.organization.winStreak.put(uuid.toString(), 0);
                    }
                }
                else if (this.scoreRed < this.scoreBlue) {
                    p.sendMessage(String.valueOf(this.organization.pluginString) + ChatColor.GREEN + "Time's up! The blue team has won");
                    if (!this.isRed.get(p) && !this.takePlace.contains(p)) {
                        this.organization.wins.rise(uuid.toString());
                        this.organization.winStreak.rise(uuid.toString());
                        if (this.organization.winStreak.get(uuid) > this.organization.bestWinStreak.get(uuid)) {
                            this.organization.bestWinStreak.put(uuid, this.organization.winStreak.get(uuid));
                        }
                        this.organization.economy.depositPlayer(p.getName(), 15.0);
                        p.sendMessage(String.valueOf(this.organization.pluginString) + ChatColor.GREEN + "You got 15 credits for winning");
                        if (this.organization.winStreak.get(uuid) % 5 != 0) {
                            continue;
                        }
                        this.organization.economy.depositPlayer(p.getName(), 100.0);
                        p.sendMessage(String.valueOf(this.organization.pluginString) + ChatColor.GOLD + ChatColor.BOLD + "You get 100 credits bonus for winning " + this.organization.winStreak.get(uuid) + " times in a row!!!");
                    }
                    else {
                        this.organization.winStreak.put(uuid, 0);
                    }
                }
                else {
                    p.sendMessage(String.valueOf(this.organization.pluginString) + ChatColor.GREEN + "Time's up! The game is tied");
                    if (this.takePlace.contains(p)) {
                        continue;
                    }
                    this.organization.ties.rise(uuid);
                    this.organization.winStreak.put(uuid, 0);
                    this.organization.economy.depositPlayer(p.getName(), 5.0);
                    p.sendMessage(String.valueOf(this.organization.pluginString) + ChatColor.GREEN + "You got 5 credits for ending tied");
                }
            }
            this.phase = 1;
            this.cube.setHealth(0.0);
            this.organization.undoTakePlace(this);
            this.scoreRed = 0;
            this.scoreBlue = 0;
            this.teams = 0;
            this.redPlayers = new Player[0];
            this.bluePlayers = new Player[0];
            this.teamers = new ArrayList<Player>();
            this.isRed = new HashMap<Player, Boolean>();
            this.takePlace.clear();
            this.goals.clear();
            this.sugarCooldown.clear();
        }
        for (final Player p : this.sugarCooldown.keySet()) {
            if (System.currentTimeMillis() - (60000 - 3000 * this.organization.getStoreNumber(p, 1)) > this.sugarCooldown.get(p)) {
                this.sugarCooldown.remove(p);
                p.getInventory().setItemInHand(this.sugar);
            }
        }
    }
    
    private void score(final boolean red) {
        this.phase = 4;
        this.tickToSec = 20;
        this.countdown = 10;
        this.cube.setHealth(0.0);
        Player scorer = null;
        String team = null;
        if (red) {
            scorer = this.lastKickRed;
            team = "red";
            ++this.scoreRed;
            this.redGoals.setScore(this.redGoals.getScore() + 1);
        }
        else {
            scorer = this.lastKickBlue;
            team = "blue";
            ++this.scoreBlue;
            this.blueGoals.setScore(this.blueGoals.getScore() + 1);
        }
        if (!this.takePlace.contains(scorer)) {
            this.organization.goals.rise(scorer.getUniqueId().toString());
            this.organization.economy.depositPlayer(scorer.getName(), 10.0);
            if (this.goals.containsKey(scorer)) {
                this.goals.put(scorer, this.goals.get(scorer) + 1);
            }
            else {
                this.goals.put(scorer, 1);
            }
            scorer.sendMessage(String.valueOf(this.organization.pluginString) + ChatColor.GREEN + "You got 10 credits for scoring");
            if (this.goals.get(scorer) == 3) {
                scorer.sendMessage(String.valueOf(this.organization.pluginString) + ChatColor.GOLD + ChatColor.BOLD + "You get 100 credits bonus for making a hat-trick");
                this.organization.economy.depositPlayer(scorer.getName(), 100.0);
            }
        }
        for (final Player p : this.isRed.keySet()) {
            final String uuid = p.getUniqueId().toString();
            p.sendMessage(String.valueOf(this.organization.pluginString) + ChatColor.GOLD + ChatColor.BOLD + "GOAL!!! " + ChatColor.RESET + ChatColor.GREEN + scorer.getName() + " scored a goal for the " + team + " team");
            if (this.scoreRed >= 3 || this.scoreBlue >= 3) {
                this.organization.endMatch(p);
                p.setScoreboard(this.sbm.getNewScoreboard());
                if (this.isRed.get(p) == red && !this.takePlace.contains(p)) {
                    this.organization.wins.rise(uuid);
                    this.organization.winStreak.rise(uuid);
                    if (this.organization.winStreak.get(uuid) > this.organization.bestWinStreak.get(uuid)) {
                        this.organization.bestWinStreak.put(uuid, this.organization.winStreak.get(uuid));
                    }
                    this.organization.economy.depositPlayer(p.getName(), 15.0);
                    p.sendMessage(String.valueOf(this.organization.pluginString) + ChatColor.GREEN + "You got 15 credits for winning");
                    if (this.organization.winStreak.get(uuid) % 5 == 0) {
                        this.organization.economy.depositPlayer(p.getName(), 100.0);
                        p.sendMessage(String.valueOf(this.organization.pluginString) + ChatColor.GOLD + ChatColor.BOLD + "You get 100 credits bonus for winning " + this.organization.winStreak.get(uuid) + " times in a row!!!");
                    }
                }
                else if (!this.takePlace.contains(p)) {
                    this.organization.winStreak.put(uuid, 0);
                }
                p.sendMessage(ChatColor.GREEN + "The " + team + " team has won the match");
                p.teleport(p.getWorld().getSpawnLocation());
                this.organization.clearInventory(p);
            }
            else {
                p.setLevel(10);
                p.sendMessage(ChatColor.GREEN + "It is now " + this.scoreRed + "-" + this.scoreBlue + " Red-Blue");
                p.sendMessage(ChatColor.GREEN + "The match will continue in 10 seconds");
            }
        }
        if (this.scoreRed >= 3 || this.scoreBlue >= 3) {
            this.phase = 1;
            this.organization.undoTakePlace(this);
            this.scoreRed = 0;
            this.scoreBlue = 0;
            this.teams = 0;
            this.redPlayers = new Player[0];
            this.bluePlayers = new Player[0];
            this.teamers = new ArrayList<Player>();
            this.isRed = new HashMap<Player, Boolean>();
            this.takePlace.clear();
            this.goals.clear();
            this.sugarCooldown.clear();
        }
    }
}
