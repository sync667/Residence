/**
 * Copyright (C) 2017 Zrips
 */

package com.bekvon.bukkit.cmiLib;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.bekvon.bukkit.cmiLib.VersionChecker.Version;


public class ItemReflection {

    private static Class<?> CraftServerClass;
    private static Object CraftServer;
    private static Class<?> CraftItemStack;
    private static Class<?> Item;
    private static Class<?> IStack;

    static {
	initialize();
    }

//    public ItemReflection() {
//	initialize();
//    }

    private static void initialize() {
	try {
	    CraftServerClass = getBukkitClass("CraftServer");
	} catch (ClassNotFoundException | SecurityException | IllegalArgumentException e) {
	    e.printStackTrace();
	}
	try {
	    CraftServer = CraftServerClass.cast(Bukkit.getServer());
	} catch (SecurityException | IllegalArgumentException e) {
	    e.printStackTrace();
	}
	try {
	    CraftItemStack = getBukkitClass("inventory.CraftItemStack");
	} catch (ClassNotFoundException | SecurityException | IllegalArgumentException e) {
	    e.printStackTrace();
	}
	try {
	    Item = getMinecraftClass("Item");
	} catch (ClassNotFoundException | SecurityException | IllegalArgumentException e) {
	    e.printStackTrace();
	}
	try {
	    IStack = getMinecraftClass("ItemStack");
	} catch (ClassNotFoundException | SecurityException | IllegalArgumentException e) {
	    e.printStackTrace();
	}
    }

    private static Class<?> getBukkitClass(String nmsClassString) throws ClassNotFoundException {
	return Class.forName("org.bukkit.craftbukkit." + Version.getCurrent() + "." + nmsClassString);
    }

    public static Class<?> getMinecraftClass(String nmsClassString) throws ClassNotFoundException {
	return Class.forName("net.minecraft.server." + Version.getCurrent() + "." + nmsClassString);
    }

    public static String getItemMinecraftName(ItemStack item) {
	try {

	    Object nmsStack = asNMSCopy(item);
	    
	    if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
		Object pre = nmsStack.getClass().getMethod("getItem").invoke(nmsStack);
		Object n = pre.getClass().getMethod("getName").invoke(pre);
		Class<?> ll = Class.forName("net.minecraft.server." + Version.getCurrent() + ".LocaleLanguage");
		Object lla = ll.getMethod("a").invoke(ll);
		return (String) lla.getClass().getMethod("a", String.class).invoke(lla, (String) n);
	    }
	    
	    Field field = Item.getField("REGISTRY");
	    Object reg = field.get(field);
	    Method meth = reg.getClass().getMethod("b", Object.class);
	    meth.setAccessible(true);
	    Method secmeth = nmsStack.getClass().getMethod("getItem");
	    Object res2 = secmeth.invoke(nmsStack);
	    Object res = meth.invoke(reg, res2);
	    return res.toString();
	} catch (Exception e) {
	    return null;
	}
    }

    public static String getItemRealName(ItemStack item) {
	try {
	    Object nmsStack = asNMSCopy(item);
	    Method itemMeth = Item.getMethod("getById", int.class);
	    Object res = itemMeth.invoke(Item, item.getType().getId());

	    String ff = "b";
	    switch (Version.getCurrent()) {
	    case v1_10_R1:
	    case v1_9_R1:
	    case v1_9_R2:
	    case v1_8_R1:
	    case v1_8_R3:
	    case v1_8_R2:
		ff = "a";
		break;
	    case v1_11_R1:
	    case v1_12_R1:
		ff = "b";
		break;
	    case v1_13_R2:
	    case v1_13_R1:
	    case v1_14_R2:
	    case v1_14_R1:
	    case v1_15_R2:
	    case v1_15_R1:
		ff = "h";
		break;
	    case v1_7_R1:
	    case v1_7_R2:
	    case v1_7_R3:
	    case v1_7_R4:
		ff = "n";
		break;
	    default:
		break;
	    }

	    Method meth2 = res.getClass().getMethod(ff, IStack);
	    Object name = meth2.invoke(res, nmsStack);
	    return name.toString();
	} catch (Exception e) {
	    return item != null ? item.getType().name() : "";
	}
    }

    public String getItemMinecraftNamePath(ItemStack item) {
	try {
	    Object nmsStack = asNMSCopy(item);
	    Method itemMeth = Item.getMethod("getById", int.class);
	    Object res = itemMeth.invoke(Item, item.getType().getId());
	    Method nameThingy = Item.getMethod("j", IStack);
	    Object resThingy = nameThingy.invoke(res, nmsStack);
	    return resThingy.toString();
	} catch (Exception e) {
	    return null;
	}
    }

    public static Object asNMSCopy(ItemStack item) {
	try {
	    Method meth = CraftItemStack.getMethod("asNMSCopy", ItemStack.class);
	    return meth.invoke(CraftItemStack, item);
	} catch (Exception e) {
	    return null;
	}
    }

    public Object asBukkitCopy(Object item) {
	try {
	    Method meth = CraftItemStack.getMethod("asBukkitCopy", IStack);
	    return meth.invoke(CraftItemStack, item);
	} catch (Exception e) {
	    return null;
	}
    }

    public Object getCraftServer() {
	return CraftServer;
    }

    public static ItemStack getItemInOffHand(Player player) {
	if (Version.getCurrent().isLower(Version.v1_9_R1))
	    return null;
	return player.getInventory().getItemInOffHand();
    }

    public void setEndermiteActive(Entity ent, boolean state) {

    }

}
