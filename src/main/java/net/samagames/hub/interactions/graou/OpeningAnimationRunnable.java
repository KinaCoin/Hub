package net.samagames.hub.interactions.graou;

import net.minecraft.server.v1_10_R1.PathEntity;
import net.minecraft.server.v1_10_R1.WorldServer;
import net.samagames.hub.Hub;
import net.samagames.hub.utils.ProximityUtils;
import net.samagames.tools.BlockUtils;
import net.samagames.tools.ItemUtils;
import net.samagames.tools.ParticleEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftSquid;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;
import java.util.Random;

class OpeningAnimationRunnable implements Runnable
{
    private static final String[] PRESENT_TEXTURES = new String[] {
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWUzYThmZDA4NTI5Nzc0NDRkOWZkNzc5N2NhYzA3YjhkMzk0OGFkZGM0M2YwYmI1Y2UyNWFlNzJkOTVkYyJ9fX0=", // Orange
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjczYTIxMTQxMzZiOGVlNDkyNmNhYTUxNzg1NDE0MDM2YTJiNzZlNGYxNjY4Y2I4OWQ5OTcxNmM0MjEifX19", // Red
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzg1MzZhNDYxNjg0ZmM3YTYzYjU0M2M1ZGYyMzQ4Y2Q5NjhiZjU1ODM1OTFiMWJiY2M1ZjBkYjgzMTY2ZGM3In19fQ==", // Grey blue
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDNlMTY1ODU3YzRjMmI3ZDZkOGQ3MGMxMDhjYzZkNDY0ZjdmMzBlMDRkOTE0NzMxYjU5ZTMxZDQyNWEyMSJ9fX0=", // Dark blue
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODRlMWM0MmYxMTM4M2I5ZGM4ZTY3ZjI4NDZmYTMxMWIxNjMyMGYyYzJlYzdlMTc1NTM4ZGJmZjFkZDk0YmI3In19fQ==", // Light blue
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWMzODIxZDRmNjFiMTdmODJmMGQ3YThlNTMxMjYwOGZmNTBlZGUyOWIxYjRkYzg5ODQ3YmU5NDI3ZDM2In19fQ==" // Grey
    };

    private final Hub hub;
    private final Graou graou;
    private final Player player;
    private final Location[] treasureLocations;
    private final Location[] openingLocations;
    private Item present;

    OpeningAnimationRunnable(Hub hub, Graou graou, Player player, Location[] treasureLocations, Location[] openingLocations)
    {
        this.hub = hub;
        this.graou = graou;
        this.player = player;
        this.treasureLocations = treasureLocations;
        this.openingLocations = openingLocations;
    }

    @Override
    public void run()
    {
        this.graou.toggleHolograms(false);
        this.graou.getGraouEntity().setSitting(false);

        this.walk(this.treasureLocations[1]);

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Optional<Entity> entity = ProximityUtils.getNearbyEntities(OpeningAnimationRunnable.this.treasureLocations[0], 3.0D).stream()
                        .filter(e -> e.getUniqueId() != null)
                        .filter(e -> e.getUniqueId() == OpeningAnimationRunnable.this.graou.getGraouEntity().getUniqueID())
                        .findAny();

                if (entity.isPresent())
                {
                    OpeningAnimationRunnable.this.arrivedAtTreasure();
                    this.cancel();
                }
            }
        }.runTaskTimer(this.hub, 5L, 5L);
    }

    private void arrivedAtTreasure()
    {
        this.treasureLocations[0].getWorld().playSound(this.treasureLocations[0], Sound.BLOCK_CHEST_OPEN, 1.0F, 1.0F);

        this.hub.getServer().getScheduler().runTaskLater(this.hub, () ->
        {
            this.treasureLocations[0].getWorld().playSound(this.treasureLocations[0], Sound.BLOCK_CHEST_CLOSE, 1.0F, 1.0F);

            final String selectedPresentTexture = PRESENT_TEXTURES[new Random().nextInt(PRESENT_TEXTURES.length)];

            this.present = this.treasureLocations[0].getWorld().dropItem(this.treasureLocations[0], ItemUtils.getCustomHead(selectedPresentTexture));
            this.graou.getGraouEntity().getBukkitEntity().setPassenger(this.present);

            this.walk(this.openingLocations[1]);

            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    Optional<Entity> entity = ProximityUtils.getNearbyEntities(OpeningAnimationRunnable.this.openingLocations[0], 2.0D).stream()
                            .filter(e -> e.getUniqueId() != null)
                            .filter(e -> e.getUniqueId() == OpeningAnimationRunnable.this.graou.getGraouEntity().getUniqueID())
                            .findAny();

                    if (entity.isPresent())
                    {
                        BlockUtils.setCustomSkull(OpeningAnimationRunnable.this.openingLocations[0].getBlock(), selectedPresentTexture);

                        OpeningAnimationRunnable.this.graou.getGraouEntity().getBukkitEntity().eject();
                        OpeningAnimationRunnable.this.present.remove();

                        OpeningAnimationRunnable.this.graou.respawn();
                        OpeningAnimationRunnable.this.graou.getGraouEntity().setSitting(true);

                        OpeningAnimationRunnable.this.placedPresent();

                        this.cancel();
                    }
                }
            }.runTaskTimer(this.hub, 5L, 5L);
        }, 15L);
    }

    private void placedPresent()
    {
        new BukkitRunnable()
        {
            private Squid fakeTarget;
            private EntityGraouLaser[] lasers;
            private long time = 0;
            private double angle = 0.0D;

            @Override
            public void run()
            {
                if (this.time == 20L * 3)
                {
                    OpeningAnimationRunnable.this.openingLocations[0].getWorld().createExplosion(OpeningAnimationRunnable.this.openingLocations[0].getBlockX(), OpeningAnimationRunnable.this.openingLocations[0].getBlockY(), OpeningAnimationRunnable.this.openingLocations[0].getBlockZ(), 2.0F, false, false);
                    OpeningAnimationRunnable.this.openingLocations[0].getBlock().setType(Material.AIR);
                    OpeningAnimationRunnable.this.graou.animationFinished(OpeningAnimationRunnable.this.player);

                    for (EntityGraouLaser laser : this.lasers)
                        laser.getBukkitEntity().remove();

                    this.cancel();
                }
                else
                {
                    if (this.time == 0)
                    {
                        OpeningAnimationRunnable.this.openingLocations[0].getWorld().playSound(OpeningAnimationRunnable.this.openingLocations[0], Sound.ENTITY_CREEPER_PRIMED, 1.0F, 0.85F);

                        this.fakeTarget = OpeningAnimationRunnable.this.openingLocations[0].getWorld().spawn(OpeningAnimationRunnable.this.openingLocations[0], Squid.class);
                        this.fakeTarget.setGravity(false);
                        this.fakeTarget.setInvulnerable(true);
                        ((CraftSquid) this.fakeTarget).getHandle().setInvisible(true);

                        this.lasers = new EntityGraouLaser[4];

                        for (int i = 0; i < 4; i++)
                        {
                            WorldServer world = ((CraftWorld) OpeningAnimationRunnable.this.openingLocations[0].getWorld()).getHandle();
                            EntityGraouLaser laser = new EntityGraouLaser(world);

                            laser.setPosition(OpeningAnimationRunnable.this.openingLocations[0].getX(), OpeningAnimationRunnable.this.openingLocations[0].getY(), OpeningAnimationRunnable.this.openingLocations[0].getZ());
                            world.addEntity(laser, CreatureSpawnEvent.SpawnReason.CUSTOM);
                            laser.setGoalTarget(((CraftSquid) this.fakeTarget).getHandle(), EntityTargetEvent.TargetReason.CUSTOM, false);

                            this.lasers[i] = laser;
                        }
                    }

                    for (int i = 0; i < 4; i++)
                        this.lasers[i].getBukkitEntity().teleport(OpeningAnimationRunnable.this.openingLocations[0].clone().add(Math.cos(this.angle + Math.PI * i / 2), 3, Math.sin(this.angle + Math.PI * i / 2)));

                    ParticleEffect.FIREWORKS_SPARK.display(0.25F, 0.25F, 0.25F, 0.5F, 15, OpeningAnimationRunnable.this.openingLocations[0], 30.0D);

                    this.angle += 0.25D;

                    if (this.angle > Math.PI * 2.0D)
                        this.angle = 0.0D;

                    this.time += 5;
                }
            }
        }.runTaskTimer(this.hub, 5L, 5L);
    }

    private void walk(Location location)
    {
        PathEntity path = this.graou.getGraouEntity().getNavigation().a(location.getX(), location.getY(), location.getZ());

        if (!this.graou.getGraouEntity().getNavigation().a(path, 1.0D))
        {
            this.graou.respawn();
            this.graou.animationFinished(this.player);
        }

    }
}