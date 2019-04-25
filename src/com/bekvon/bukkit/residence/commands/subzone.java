package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class subzone implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 2100)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	if (args.length != 2 && args.length != 3) {
	    return false;
	}
	String zname;
	ClaimedResidence res = null;
	if (args.length == 2) {
	    res = plugin.getResidenceManager().getByLoc(player.getLocation());
	    zname = args[1];
	} else {
	    res = plugin.getResidenceManager().getByName(args[1]);
	    zname = args[2];
	}
	if (res == null) {
	    plugin.msg(player, lm.Invalid_Residence);
	    return true;
	}
	if (plugin.getWorldEdit() != null) {
	    if (plugin.getWorldEditTool() == plugin.getConfigManager().getSelectionTool()) {
		plugin.getSelectionManager().worldEdit(player);
	    }
	}
	if (plugin.getSelectionManager().hasPlacedBoth(player)) {
	    if (!resadmin && !plugin.hasPermission(player, "residence.create.subzone", lm.Subzone_CantCreate))
		return true;

	    res.addSubzone(player, plugin.getSelectionManager().getPlayerLoc1(player), plugin.getSelectionManager().getPlayerLoc2(player),
		zname, resadmin);
	} else {
	    plugin.msg(player, lm.Select_Points);
	}
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Create subzones in residences.");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res subzone <residence> [subzone name]",
	    "If residence name is left off, will attempt to use residence your standing in."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]"));
    }

}
