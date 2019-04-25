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

public class bank implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 3400)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if ((args.length != 3) && (args.length != 4)) {
	    return false;
	}
	ClaimedResidence res = null;
	if (args.length == 4) {
	    res = plugin.getResidenceManager().getByName(args[2]);
	    if (res == null) {
		plugin.msg(sender, lm.Invalid_Residence);
		return true;
	    }
	} else if ((sender instanceof Player)) {
	    res = plugin.getResidenceManager().getByLoc(((Player) sender).getLocation());
	}
	if (res == null) {
	    plugin.msg(sender, lm.Residence_NotIn);
	    return true;
	}
	double amount = 0D;
	try {
	    if (args.length == 3)
		amount = Double.parseDouble(args[2]);
	    else
		amount = Double.parseDouble(args[3]);
	} catch (Exception ex) {
	    plugin.msg(sender, lm.Invalid_Amount);
	    return true;
	}
	if (args[1].equals("deposit"))
	    res.getBank().deposit(sender, amount, resadmin);
	else if (args[1].equals("withdraw"))
	    res.getBank().withdraw(sender, amount, resadmin);
	else
	    return false;

	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Manage money in a Residence");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res bank [deposit/withdraw] <residence> [amount]",
	    "You must be standing in a Residence or provide residence name",
	    "You must have the +bank flag."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("deposit%%withdraw", "[residence]"));
    }
}
