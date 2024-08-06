package com.maatijaa.pfootcube;

import com.maatijaa.pfootcube.core.Organization;
import com.maatijaa.pfootcube.managers.PlayerManager;
import com.maatijaa.pfootcube.system.Particles;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.entity.Entity;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import java.util.UUID;
import java.util.Collection;
import java.util.HashMap;
import org.bukkit.entity.Slime;
import java.util.HashSet;
import java.util.logging.Logger;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class pFootcube extends JavaPlugin implements Listener {
    private Logger logger;
    private HashSet<Slime> cubes;
    private HashMap<UUID, Vector> velocities;
    private HashMap<String, Long> kicked;
    private HashMap<String, Double> speed;
    private HashMap<String, Double> charges;
    private Organization organization;
    private Particles particles;

    public pFootcube() {
        getLogger().info("888888  888888   8888");
        getLogger().info("88  88  88      88  88");
        getLogger().info("888888  888888  88 ");
        getLogger().info("88      88      88  88");
        getLogger().info("88      88       888");

        getLogger().info("pFootcube se ucitava.");

        this.logger = Logger.getLogger("Minecraft");
        this.cubes = new HashSet<>();
        this.velocities = new HashMap<>();
        this.kicked = new HashMap<>();
        this.speed = new HashMap<>();
        this.charges = new HashMap<>();
    }

    public void onDisable() {
        final PluginDescriptionFile pdfFile = this.getDescription();
        this.logger.info(String.valueOf(pdfFile.getName()) + " Has Been Disabled!");
    }

    public void onEnable() {
        getLogger().info("Starting pFootcube..");

        getLogger().info("Loading Commands...");
        registerCommands();
        getLogger().info("Commands successfully loaded.");

        getLogger().info("Loading Listeners");
        registerListeners();

        getLogger().info("pFootcube successfully started.");

        final PluginDescriptionFile pdfFile = this.getDescription();
        this.logger.info(String.valueOf(pdfFile.getName()) + " V" + pdfFile.getVersion() + " Has Been Enabled!");
        this.getServer().getPluginManager().registerEvents(this, this);
        this.organization = new Organization(this);
        this.particles = new Particles(this);
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                pFootcube.this.particles.cubeEffect();
                pFootcube.this.update();
            }
        }, 0L, 0L);
    }

    public void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new Particles(this), this);
        pm.registerEvents(new PlayerManager(this), this);
    }

    public void registerCommands() {
        getCommand("particles").setExecutor(new Particles(this));
    }

    public HashSet<Slime> getCubes() {
        return cubes;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String c, final String[] args) {
        this.organization.command(sender, cmd, c, args);
        final Player p = (Player) sender;
        if (cmd.getName().equalsIgnoreCase("cube") && p.getWorld().getDifficulty() != Difficulty.PEACEFUL) {
            p.sendMessage(ChatColor.GREEN + "▎ " + ChatColor.WHITE + "Lopta uspesno stvorena.");
            final Location loc = p.getLocation().add(0, 1, 0);
            this.spawnCube(loc);
        }
        if (cmd.getName().equalsIgnoreCase("clearCubes")) {
            int i = 0;
            for (final Slime cube : this.cubes) {
                cube.setHealth(0.0);
                ++i;
            }
            this.cubes.clear();
            String s = "s";
            if (i == 1) {
                s = "";
            }
            p.sendMessage(ChatColor.GREEN + "▎ " + ChatColor.WHITE + "Obrisane " + i + " lopte.");
        }
        if (cmd.getName().equalsIgnoreCase("clearCube")) {
            double distance = 999999;
            Slime s2 = null;
            for (final Slime cube2 : this.cubes) {
                if (this.getDistance(cube2.getLocation(), p.getLocation()) < distance) {
                    distance = this.getDistance(cube2.getLocation(), p.getLocation());
                    s2 = cube2;
                }
            }
            if (s2 != null) {
                s2.setHealth(0.0);
            }
        }
        return false;
    }

    @EventHandler
    public void onMove(final PlayerMoveEvent e) {
        final Location to = e.getTo();
        final Location from = e.getFrom();
        final double x = Math.abs(to.getX() - from.getX());
        final double y = Math.abs(to.getY() - from.getY()) / 2.0;
        final double z = Math.abs(to.getZ() - from.getZ());
        this.speed.put(e.getPlayer().getName(), Math.sqrt(x * x + y * y + z * z));
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent e) {
        this.speed.remove(e.getPlayer().getName());
        this.charges.remove(e.getPlayer().getName());
    }

    @EventHandler
    public void onUnloadChunk(final ChunkUnloadEvent e) {
        Entity[] entities;
        for (int length = (entities = e.getChunk().getEntities()).length, i = 0; i < length; ++i) {
            final Entity entity = entities[i];
            if (entity instanceof Slime && this.cubes.contains(entity)) {
                this.cubes.remove(entity);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(final EntityDamageEvent e) {
        if (e.getEntity() instanceof Slime && this.cubes.contains(e.getEntity())) {
            if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                e.setCancelled(true);
            }
            e.setDamage(0.0);
        }
    }

    @EventHandler
    public void onRightClick(final PlayerInteractEntityEvent e) {
        final Entity entity = e.getRightClicked();
        if (entity instanceof Slime && this.cubes.contains(entity) && !this.kicked.containsKey(e.getPlayer().getName())) {
            final Slime cube = (Slime) entity;
            cube.setVelocity(cube.getVelocity().add(new Vector(0.0, 0.7, 0.0)));
            cube.getWorld().playSound(cube.getLocation(), Sound.SLIME_WALK, 1.0f, 1.0f);
            this.kicked.put(e.getPlayer().getName(), System.currentTimeMillis());
            this.organization.ballTouch(e.getPlayer());
        }
    }

    @EventHandler
    public void onSneak(final PlayerToggleSneakEvent e) {
        final Player p = e.getPlayer();
        if (e.isSneaking()) {
            this.charges.put(p.getName(), 0.0);
        } else {
            p.setExp(0.0f);
            this.charges.remove(p.getName());
        }
    }

    @EventHandler
    public void onSlamSlime(final EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Slime && this.cubes.contains(e.getEntity()) && e.getDamager() instanceof Player && e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            final Slime cube = (Slime) e.getEntity();
            final Player p = (Player) e.getDamager();
            double charge = 0.5;
            if (this.charges.containsKey(p.getName())) {
                charge += this.charges.get(p.getName()) * 4.0;
            }
            final double power = this.speed.get(p.getName()) * 2.0 + 0.4;
            final Vector kick = p.getLocation().getDirection().normalize().multiply(power * charge * (1.0 + 0.05 * this.organization.getStoreNumber(p, 3))).setY(0.3);
            cube.setVelocity(cube.getVelocity().add(kick));
            cube.getWorld().playSound(cube.getLocation(), Sound.SLIME_WALK, 1.0f, 1.0f);
            this.organization.ballTouch(p);
            e.setCancelled(true);
        }
    }

    private double getDistance(final Location loc1, final Location loc2) {
        return Math.sqrt(Math.pow(loc1.getX() - loc2.getX(), 2) + Math.pow(loc1.getY() - loc2.getY(), 2) + Math.pow(loc1.getZ() - loc2.getZ(), 2));
    }

    public void spawnCube(final Location loc) {
        final Slime slime = (Slime) loc.getWorld().spawnEntity(loc, EntityType.SLIME);
        slime.setSize(1);
        this.cubes.add(slime);
    }

    private void update() {
        if (this.kicked.size() > 0) {
            for (final String s : this.kicked.keySet()) {
                if (System.currentTimeMillis() > this.kicked.get(s) + 1000L) {
                    this.kicked.remove(s);
                }
            }
        }
        Collection<? extends Player> onlinePlayers;
        for (int length3 = (onlinePlayers = getServer().getOnlinePlayers()).size(), k = 0; k < length3; k++) {
            Player p = (Player) onlinePlayers.toArray()[k];
            p.setHealth(20.0D);
            p.setSaturation(100.0F);
            p.setExhaustion(20.0F);
        }
        for (final String s : this.charges.keySet()) {
            final Player p2 = this.getServer().getPlayer(s);
            final double charge = this.charges.get(s);
            final double nextCharge = 1.0 - (1.0 - charge) * (0.95 - this.organization.getStoreNumber(p2, 5) * 0.005);
            this.charges.put(s, nextCharge);
            p2.setExp((float) nextCharge);
        }
        for (final Slime cube : this.cubes) {
            final UUID id = cube.getUniqueId();
            Vector oldV = cube.getVelocity();
            if (this.velocities.containsKey(id)) {
                oldV = this.velocities.get(id);
            }
            if (!cube.isDead()) {
                boolean sound = false;
                boolean kicked = false;
                final Vector newV = cube.getVelocity();
                Collection<? extends Player> onlinePlayers2;
                for (int length2 = (onlinePlayers2 = getServer().getOnlinePlayers()).size(), j = 0; j < length2; j++) {
                    Player p3 = (Player) onlinePlayers2.toArray()[j];
                    final double delta = this.getDistance(cube.getLocation(), p3.getLocation());
                    if (delta < 1.2) {
                        if (delta < 0.8 && newV.length() > 0.5) {
                            newV.multiply(0.5 / newV.length());
                        }
                        final double power = this.speed.get(p3.getName()) / 3.0 + oldV.length() / 6.0;
                        newV.add(p3.getLocation().getDirection().setY(0).normalize().multiply(power));
                        this.organization.ballTouch(p3);
                        kicked = true;
                        if (power > 0.15) {
                            sound = true;
                        }
                    }
                }
                if (newV.getX() == 0.0) {
                    newV.setX(-oldV.getX() * 0.8);
                    if (Math.abs(oldV.getX()) > 0.3) {
                        sound = true;
                    }
                } else if (!kicked && Math.abs(oldV.getX() - newV.getX()) < 0.1) {
                    newV.setX(oldV.getX() * 0.98);
                }
                if (newV.getZ() == 0.0) {
                    newV.setZ(-oldV.getZ() * 0.8);
                    if (Math.abs(oldV.getZ()) > 0.3) {
                        sound = true;
                    }
                } else if (!kicked && Math.abs(oldV.getZ() - newV.getZ()) < 0.1) {
                    newV.setZ(oldV.getZ() * 0.98);
                }
                if (newV.getY() < 0.0 && oldV.getY() < 0.0 && oldV.getY() < newV.getY() - 0.05) {
                    newV.setY(-oldV.getY() * 0.8);
                    if (Math.abs(oldV.getY()) > 0.3) {
                        sound = true;
                    }
                }
                if (sound) {
                    cube.getWorld().playSound(cube.getLocation(), Sound.SLIME_WALK, 1.0f, 1.0f);
                }
                cube.setMaxHealth(20.0);
                cube.setHealth(20.0);
                cube.setVelocity(newV);
                cube.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 10, -3, true), true);
                cube.playEffect(EntityEffect.HURT);
                this.velocities.put(id, newV);
            }
        }
    }
}
