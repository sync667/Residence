package com.bekvon.bukkit.residence.allNms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.PolarBear;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.NMS;

import cmiLib.CMIEffect;
import cmiLib.ItemManager.CMIMaterial;
import net.minecraft.server.v1_10_R1.EnumParticle;
import net.minecraft.server.v1_10_R1.Packet;
import net.minecraft.server.v1_10_R1.PacketPlayOutWorldParticles;

public class v1_10_R1 implements NMS {
    @Override
    public List<Block> getPistonRetractBlocks(BlockPistonRetractEvent event) {
	List<Block> blocks = new ArrayList<Block>();
	blocks.addAll(event.getBlocks());
	return blocks;
    }

    @Override
    public boolean isAnimal(Entity ent) {
	return (ent instanceof Horse ||
	    ent instanceof Bat ||
	    ent instanceof Snowman ||
	    ent instanceof IronGolem ||
	    ent instanceof Ocelot ||
	    ent instanceof Pig ||
	    ent instanceof Sheep ||
	    ent instanceof Chicken ||
	    ent instanceof Wolf ||
	    ent instanceof Cow ||
	    ent instanceof Squid ||
	    ent instanceof Villager ||
	    ent instanceof Rabbit ||
	    ent instanceof PolarBear);
    }

    @Override
    public boolean isArmorStandEntity(EntityType ent) {
	return ent == EntityType.ARMOR_STAND;
    }

    @Override
    public boolean isArmorStandMaterial(Material material) {
	return material == Material.ARMOR_STAND;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isCanUseEntity_BothClick(Material mat, Block block) {
	CMIMaterial m = CMIMaterial.get(mat);
	if (m.isDoor())
	    return true;
	if (m.isButton())
	    return true;
	if (m.isGate())
	    return true;
	if (m.isTrapDoor())
	    return true;

	switch (CMIMaterial.get(mat)) {
	case LEVER:
	case PISTON:
	case STICKY_PISTON:
	case DRAGON_EGG:
	    return true;
	default:
	    return Residence.getInstance().getConfigManager().getCustomBothClick().contains(Integer.valueOf(block.getType().getId()));
	}
    }

    @Override
    public boolean isEmptyBlock(Block block) {
	CMIMaterial cm = CMIMaterial.get(block);
	switch (cm) {
	case COBWEB:
	case STRING:
	case WALL_SIGN:
	case VINE:
	case TRIPWIRE_HOOK:
	case TRIPWIRE:
	case PAINTING:
	case ITEM_FRAME:
	    return true;
	default:
	    break;
	}

	if (CMIMaterial.get(block).isSapling())
	    return true;
	if (CMIMaterial.get(block).isAir())
	    return true;
	if (CMIMaterial.get(block).isButton())
	    return true;

	return false;
    }

    @Override
    public boolean isSpectator(GameMode mode) {
	return mode == GameMode.SPECTATOR;
    }

    @Override
    public void addDefaultFlags(Map<Material, Flags> matUseFlagList) {
	/* 1.8 Doors */
	matUseFlagList.put(Material.SPRUCE_DOOR, Flags.door);
	matUseFlagList.put(Material.BIRCH_DOOR, Flags.door);
	matUseFlagList.put(Material.JUNGLE_DOOR, Flags.door);
	matUseFlagList.put(Material.ACACIA_DOOR, Flags.door);
	matUseFlagList.put(Material.DARK_OAK_DOOR, Flags.door);
	/* 1.8 Fence Gates */
	matUseFlagList.put(Material.SPRUCE_FENCE_GATE, Flags.door);
	matUseFlagList.put(Material.BIRCH_FENCE_GATE, Flags.door);
	matUseFlagList.put(Material.JUNGLE_FENCE_GATE, Flags.door);
	matUseFlagList.put(Material.ACACIA_FENCE_GATE, Flags.door);
	matUseFlagList.put(Material.DARK_OAK_FENCE_GATE, Flags.door);
	matUseFlagList.put(Material.IRON_TRAPDOOR, Flags.door);

	matUseFlagList.put(CMIMaterial.DAYLIGHT_DETECTOR.getMaterial(), Flags.diode);
    }

    @Override
    public boolean isMainHand(PlayerInteractEvent event) {
	return event.getHand() == EquipmentSlot.HAND ? true : false;
    }

    @Override
    public ItemStack itemInMainHand(Player player) {
	return player.getInventory().getItemInMainHand();
    }

    @Override
    public boolean isChorusTeleport(TeleportCause tpcause) {
	if (tpcause == TeleportCause.CHORUS_FRUIT)
	    return true;
	return false;
    }

    @Override
    public void playEffect(Player player, Location location, CMIEffect ef) {
	if (location == null || ef == null || location.getWorld() == null)
	    return;
	Packet<?> packet = null;
	if (ef.getParticle().getEffect() == null)
	    return;
	if (!ef.getParticle().isParticle()) {
//	    int packetData = effect.getId();
//	    packet = new PacketPlayOutWorldEvent(packetData, new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()), id, false);
	} else {
	    Particle effect = ef.getParticle().getParticle();

	    EnumParticle particle = ef.getParticle().getEnumParticle() == null ? null : (EnumParticle) ef.getParticle().getEnumParticle();
	    int[] extra = ef.getParticle().getExtra();
	    if (particle == null) {
		for (EnumParticle p : EnumParticle.values()) {
		    if (effect.name().replace("_", "").equalsIgnoreCase((p.toString().replace("_", "")))) {
			particle = p;
			if (ef.getParticle().getEffect().getData() != null) {
			    if (ef.getParticle().getEffect().equals(org.bukkit.Material.class)) {
				extra = new int[] { 0 };
			    } else {
				extra = new int[] { (0 << 12) | (0 & 0xFFF) };
			    }
			}
			break;
		    }
		    if (ef.getParticle().getName().replace("_", "").equalsIgnoreCase((p.toString().replace("_", "")))) {
			particle = p;
			if (ef.getParticle().getEffect().getData() != null) {
			    if (ef.getParticle().getEffect().equals(org.bukkit.Material.class)) {
				extra = new int[] { 0 };
			    } else {
				extra = new int[] { (0 << 12) | (0 & 0xFFF) };
			    }
			}
			break;
		    }
		    if (ef.getParticle().getSecondaryName().replace("_", "").equalsIgnoreCase((p.toString().replace("_", "")))) {
			particle = p;
			if (ef.getParticle().getEffect().getData() != null) {
			    if (ef.getParticle().getEffect().equals(org.bukkit.Material.class)) {
				extra = new int[] { 0 };
			    } else {
				extra = new int[] { (0 << 12) | (0 & 0xFFF) };
			    }
			}
			break;
		    }
		}
		if (extra == null) {
		    extra = new int[0];
		}
	    }

	    if (particle == null)
		return;

	    if (ef.getParticle().getEnumParticle() == null) {
		ef.getParticle().setEnumParticle(particle);
		ef.getParticle().setExtra(extra);
	    }

	    packet = new PacketPlayOutWorldParticles(particle, true, (float) location.getX(), (float) location.getY(), (float) location.getZ(), (float) ef.getOffset().getX(), (float) ef.getOffset().getY(),
		(float) ef.getOffset().getZ(), ef.getSpeed(), ef.getAmount(), extra);
	}
	CraftPlayer cPlayer = (CraftPlayer) player;
	if (cPlayer.getHandle().playerConnection == null)
	    return;

	if (!location.getWorld().equals(cPlayer.getWorld()))
	    return;

	if (packet == null)
	    return;
	cPlayer.getHandle().playerConnection.sendPacket(packet);
    }
}
