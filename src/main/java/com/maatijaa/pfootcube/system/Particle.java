package com.maatijaa.pfootcube.system;

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class Particle {
    EnumParticle particleType;

    Location loc;

    boolean longDistance;

    float x;

    float y;

    float z;

    float speed;

    int amount;

    public Particle(EnumParticle particleType, Location loc, boolean longDistance, float x, float y, float z, float speed, int amount) {
        this.particleType = particleType;
        this.loc = loc;
        this.longDistance = longDistance;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.z = z;
        this.amount = amount;
    }

    public void toPlayer(Player p) {
        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(this.particleType, this.longDistance, (float)this.loc.getX(), (float)this.loc.getY(), (float)this.loc.getZ(), this.x, this.y, this.z, this.speed, this.amount, new int[0]);
        (((CraftPlayer)p).getHandle()).playerConnection.sendPacket((Packet)packet);
    }
}
