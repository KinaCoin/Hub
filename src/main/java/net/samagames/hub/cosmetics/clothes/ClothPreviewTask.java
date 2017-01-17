package net.samagames.hub.cosmetics.clothes;

import net.minecraft.server.v1_10_R1.World;
import net.samagames.api.SamaGamesAPI;
import net.samagames.hub.Hub;
import net.samagames.tools.npc.NPCManager;
import net.samagames.tools.npc.nms.CustomNPC;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *                )\._.,--....,'``.
 * .b--.        /;   _.. \   _\  (`._ ,.
 * `=,-,-'~~~   `----(,_..'--(,_..'`-.;.'
 *
 * Created by Jérémy L. (BlueSlime) on 17/01/2017
 */
class ClothPreviewTask extends BukkitRunnable
{
    private static final double RADIUS = 5.0D;

    private final Location center;
    private final CustomNPC fakePlayer;
    private final EntityClothCamera camera;
    private double i;

    ClothPreviewTask(Hub hub, Player player, ItemStack[] armorContent)
    {
        this.center = player.getLocation().clone();

        this.fakePlayer = SamaGamesAPI.get().getNPCManager().createNPC(this.center, player.getUniqueId(), null, false);

        this.fakePlayer.getBukkitEntity().getInventory().setHelmet(armorContent[0]);
        this.fakePlayer.getBukkitEntity().getInventory().setChestplate(armorContent[1]);
        this.fakePlayer.getBukkitEntity().getInventory().setLeggings(armorContent[2]);
        this.fakePlayer.getBukkitEntity().getInventory().setBoots(armorContent[3]);

        SamaGamesAPI.get().getNPCManager().sendNPC(player, this.fakePlayer);

        World world = ((CraftWorld) hub.getWorld()).getHandle();
        this.camera = new EntityClothCamera(world);

        this.camera.setPosition(this.center.getX(), this.center.getY(), this.center.getZ());
        world.addEntity(this.camera, CreatureSpawnEvent.SpawnReason.CUSTOM);
        ((Guardian) this.camera.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));

        this.runTaskTimer(hub, 1L, 1L);
    }

    @Override
    public void run()
    {
        Location location = new Location(this.center.getWorld(), this.center.getX() + Math.cos(this.i) * RADIUS, this.center.getY() + 1D, this.center.getZ() + Math.sin(this.i) * RADIUS);
        location.setDirection(this.center.clone().subtract(location).toVector().setY(location.getY()));

        this.camera.getBukkitEntity().teleport(location);

        this.i += 0.025D;

        if (this.i > Math.PI * 2.0D)
            this.i = 0.0D;
    }

    public void stop()
    {
        SamaGamesAPI.get().getNPCManager().removeNPC(this.fakePlayer);

        this.camera.die();
        this.cancel();
    }

    public EntityClothCamera getCamera()
    {
        return this.camera;
    }

    public Location getCenter()
    {
        return this.center;
    }
}
