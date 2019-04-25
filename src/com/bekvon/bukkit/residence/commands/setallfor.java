package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagState;

public class setallfor implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 700)
    public boolean perform(final Residence plugin, final String[] args, final boolean resadmin, Command command, final CommandSender sender) {
	if (args.length != 4)
	    return false;

	String playerName = args[1];
	String flag = args[2];

	Flags f = Flags.getFlag(flag);
	if (f != null)
	    flag = f.toString();
	
	FlagState state = FlagPermissions.stringToFlagState(args[3]);
	ResidencePlayer resPlayer = plugin.getPlayerManager().getResidencePlayer(playerName);
	if (resPlayer == null)
	    return false;
	FlagPermissions GlobalFlags = Residence.getInstance().getPermissionManager().getAllFlags();

	if (flag == null || !GlobalFlags.checkValidFlag(flag.toLowerCase(), true)) {
	    plugin.msg(sender, lm.Invalid_Flag);
	    return true;
	}

	if (state.equals(FlagState.INVALID)) {
	    plugin.msg(sender, lm.Invalid_FlagState);
	    return true;
	}

	int count = 0;

	for (ClaimedResidence one : resPlayer.getResList()) {
	    if (one.getPermissions().setFlag(sender, flag, state, true, false))
		count++;
	}

	plugin.msg(sender, lm.Flag_ChangedForOne, count, resPlayer.getPlayerName());

	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Set general flags on all residences owned by particular player");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res setallfor [playerName] [flag] [true/false/remove]"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[playername]", "[flag]", "true%%false%%remove"));
    }
}
