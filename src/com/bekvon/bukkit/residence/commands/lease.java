package com.bekvon.bukkit.residence.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.text.help.PageInfo;

public class lease implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 3900)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	if (args.length == 2 || args.length == 3 || args.length == 4) {

	    if (args[1].equals("set")) {
		if (!resadmin) {
		    plugin.msg(player, lm.General_NoPermission);
		    return true;
		}
		if (args[3].equals("infinite")) {
		    if (plugin.getLeaseManager().isLeased(plugin.getResidenceManager().getByName(args[2]))) {
			plugin.getLeaseManager().removeExpireTime(plugin.getResidenceManager().getByName(args[2]));
			plugin.msg(player, lm.Economy_LeaseInfinite);
		    } else {
			plugin.msg(player, lm.Economy_LeaseNotExpire);
		    }
		    return true;
		}
		int days;
		try {
		    days = Integer.parseInt(args[3]);
		} catch (Exception ex) {
		    plugin.msg(player, lm.Invalid_Days);
		    return true;
		}
		plugin.getLeaseManager().setExpireTime(player, plugin.getResidenceManager().getByName(args[2]), days);
		return true;
	    }
	    if (args[1].equals("expires")) {
		ClaimedResidence res = null;
		if (args.length == 2) {
		    res = plugin.getResidenceManager().getByLoc(player.getLocation());
		    if (res == null) {
			plugin.msg(player, lm.Residence_NotIn);
			return true;
		    }
		} else {
		    res = plugin.getResidenceManager().getByName(args[2]);
		    if (res == null) {
			plugin.msg(player, lm.Invalid_Residence);
			return true;
		    }
		}

		String until = plugin.getLeaseManager().getExpireTime(res);
		if (until != null)
		    plugin.msg(player, lm.Economy_LeaseRenew, until);
		return true;
	    }
	    if (args[1].equals("renew")) {
		if (args.length == 3) {
		    plugin.getLeaseManager().renewArea(plugin.getResidenceManager().getByName(args[2]), player);
		} else {
		    ClaimedResidence res = plugin.getResidenceManager().getByLoc(player.getLocation());
		    if (res != null)
			plugin.getLeaseManager().renewArea(res, player);
		    else
			return false;
		}
		return true;
	    }
	    if (args[1].equals("list")) {
		ClaimedResidence res = null;
		int page = -1;
		if (args.length > 2)
		    try {
			page = Integer.parseInt(args[2]);
		    } catch (Exception e) {
			res = plugin.getResidenceManager().getByName(args[2]);
		    }
		if (args.length > 3 && page == -1)
		    try {
			page = Integer.parseInt(args[3]);
		    } catch (Exception e) {
			res = plugin.getResidenceManager().getByName(args[3]);
		    }

		if (res == null)
		    res = plugin.getResidenceManager().getByLoc(player.getLocation());

		if (res == null)
		    return false;

		List<ClaimedResidence> list = new ArrayList<ClaimedResidence>();

		if (plugin.getLeaseManager().isLeased(res))
		    list.add(res);

		for (ClaimedResidence one : res.getSubzones()) {
		    if (plugin.getLeaseManager().isLeased(one))
			list.add(one);
		}

		PageInfo pi = new PageInfo(3, list.size(), page);

		plugin.msg(player, lm.General_Separator);
		for (ClaimedResidence one : list) {
		    if (!pi.isEntryOk())
			continue;

		    if (pi.isBreak())
			break;
		    
		    if (res.isOwner(player))
			plugin.msg(player, lm.Economy_LeaseList, pi.getPositionForOutput(), one.getName(), plugin.getLeaseManager().getExpireTime(one), one.getOwner());
		    else
			plugin.msg(player, lm.Economy_LeaseList, pi.getPositionForOutput(), one.getName(), "", "");
		}

		plugin.getInfoPageManager().ShowPagination(sender, pi.getTotalPages(), pi.getCurrentPage(), "res lease list " + res.getName());

		return true;
	    }
	    if (args[1].equals("cost")) {
		if (args.length == 3) {
		    ClaimedResidence res = plugin.getResidenceManager().getByName(args[2]);
		    if (res == null || plugin.getLeaseManager().isLeased(res)) {
			double cost = plugin.getLeaseManager().getRenewCostD(res);
			plugin.msg(player, lm.Economy_LeaseRenewalCost, args[2], plugin.getEconomyManager().format(cost));
		    } else {
			plugin.msg(player, lm.Economy_LeaseNotExpire);
		    }
		    return true;
		}
		ClaimedResidence res = plugin.getResidenceManager().getByLoc(player.getLocation());
		if (res == null) {
		    plugin.msg(player, lm.Invalid_Area);
		    return true;
		}
		String area = res.getName();
		if (plugin.getLeaseManager().isLeased(res)) {
		    double cost = plugin.getLeaseManager().getRenewCostD(res);
		    plugin.msg(player, lm.Economy_LeaseRenewalCost, area, plugin.getEconomyManager().format(cost));
		} else {
		    plugin.msg(player, lm.Economy_LeaseNotExpire);
		}
		return true;
	    }
	}
	return false;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Manage residence leases");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res lease [renew/cost] [residence]",
	    "/res lease cost will show the cost of renewing a residence lease.", "/res lease renew will renew the residence provided you have enough money."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("renew%%cost", "[residence]"));

	// Sub commands
	path += "SubCommands.";
	c.get(path + "set.Description", "Set the lease time");
	c.get(path + "set.Info", Arrays.asList("&eUsage: &6/resadmin lease set [residence] [#days/infinite]",
	    "Sets the lease time to a specified number of days, or infinite."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "set"), Arrays.asList("[residence]"));

	c.get(path + "renew.Description", "Renew the lease time");
	c.get(path + "renew.Info", Arrays.asList("&eUsage: &6/resadmin lease renew <residence>", "Renews the lease time for current or specified residence."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "renew"), Arrays.asList("[residence]"));

	c.get(path + "list.Description", "Show lease list of current residence");
	c.get(path + "list.Info", Arrays.asList("&eUsage: &6/resadmin lease list <residence> <page>", "Prints out all subzones lease times"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "list"), Arrays.asList("[residence]"));

	c.get(path + "expires.Description", "Lease end date");
	c.get(path + "expires.Info", Arrays.asList("&eUsage: &6/resadmin lease expires <residence>", "Shows when expires residence lease time."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "expires"), Arrays.asList("[residence]"));

	c.get(path + "cost.Description", "Shows renew cost");
	c.get(path + "cost.Info", Arrays.asList("&eUsage: &6/resadmin lease cost <residence>", "Shows how much money you need to renew residence lease."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "cost"), Arrays.asList("[residence]"));
    }
}
