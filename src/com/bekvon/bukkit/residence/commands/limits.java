package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;

public class limits implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 900)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player) && args.length < 2)
	    return false;

	if (args.length != 1 && args.length != 2)
	    return false;
	final String[] tempArgs = args;
	OfflinePlayer target;
	boolean rsadm = false;
	if (tempArgs.length == 1) {
	    target = (Player) sender;
	    rsadm = true;
	} else
	    target = plugin.getOfflinePlayer(tempArgs[1]);
	if (target == null)
	    return false;
//	plugin.getPermissionManager().updateGroupNameForPlayer(target.getName(), target.isOnline() ? target.getPlayer().getLocation().getWorld().getName() : plugin
//	    .getConfigManager().getDefaultWorld(), true);

	ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(target.getName(), true);
	rPlayer.getGroup().printLimits(sender, target, rsadm);
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	// Main command
	c.get(path + "Description", "Show your limits.");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res limits (playerName)", "Shows the limitations you have on creating and managing residences."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[playername]"));
    }
}
