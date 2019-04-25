package com.bekvon.bukkit.residence.commands;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.cmiLib.RawMessage;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.shopStuff.Board;
import com.bekvon.bukkit.residence.shopStuff.ShopListener;
import com.bekvon.bukkit.residence.shopStuff.ShopVote;
import com.bekvon.bukkit.residence.shopStuff.Vote;
import com.bekvon.bukkit.residence.text.help.PageInfo;

public class shop implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 1700)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	int page = 1;
	try {
	    if (args.length > 0) {
		page = Integer.parseInt(args[args.length - 1]);
	    }
	} catch (Exception ex) {
	}

	if ((args.length == 2 || args.length == 3 || args.length == 4) && (args[1].equalsIgnoreCase("votes") || args[1].equalsIgnoreCase("likes"))) {

	    int VotePage = 1;

	    ClaimedResidence res = null;
	    if (args.length == 2) {
		res = plugin.getResidenceManager().getByLoc(player.getLocation());
		if (res == null) {
		    plugin.msg(player, lm.Residence_NotIn);
		    return true;
		}
	    } else if (args.length == 3) {
		res = plugin.getResidenceManager().getByName(args[2]);
		if (res == null) {
		    try {
			VotePage = Integer.parseInt(args[2]);
			res = plugin.getResidenceManager().getByLoc(player.getLocation());
			if (res == null) {
			    plugin.msg(player, lm.Residence_NotIn);
			    return true;
			}
		    } catch (Exception ex) {
			plugin.msg(player, lm.General_UseNumbers);
			return true;
		    }
		}

	    } else if (args.length == 4) {
		res = plugin.getResidenceManager().getByName(args[2]);
		if (res == null) {
		    plugin.msg(player, lm.Residence_NotIn);
		    return true;
		}
		try {
		    VotePage = Integer.parseInt(args[3]);
		} catch (Exception ex) {
		    plugin.msg(player, lm.General_UseNumbers);
		    return true;
		}
	    }

	    if (res == null) {
		plugin.msg(player, lm.Residence_NotIn);
		return true;
	    }

	    List<ShopVote> VoteList = res.GetShopVotes();

	    String separator = plugin.msg(lm.InformationPage_SmallSeparator);

	    PageInfo pi = new PageInfo(10, VoteList.size(), page);

	    if (!pi.isPageOk()) {
		plugin.msg(sender, lm.Shop_NoVotes);
		return true;
	    }

	    plugin.msg(player, lm.Shop_VotesTopLine, separator, res.getName(), VotePage, pi.getTotalPages(), separator);

	    int position = -1;
	    for (ShopVote one : VoteList) {
		position++;
		if (position > pi.getEnd())
		    break;
		if (!pi.isInRange(position))
		    continue;

		Date dNow = new Date(one.getTime());
		SimpleDateFormat ft = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
		ft.setTimeZone(TimeZone.getTimeZone(plugin.getConfigManager().getTimeZone()));
		String timeString = ft.format(dNow);

		String message = plugin.msg(lm.Shop_VotesList, pi.getStart() + position + 1, one.getName(), (plugin.getConfigManager().isOnlyLike()
		    ? "" : one.getVote()), timeString);
		player.sendMessage(message);
	    }

	    plugin.getInfoPageManager().ShowPagination(sender, pi.getTotalPages(), page, "/res shop votes " + res.getName());

	    return true;
	}
	if ((args.length == 2 || args.length == 3) && args[1].equalsIgnoreCase("list")) {

	    int Shoppage = 1;

	    if (args.length == 3) {
		try {
		    Shoppage = Integer.parseInt(args[2]);
		} catch (Exception ex) {
		    plugin.msg(player, lm.General_UseNumbers);
		    return true;
		}
	    }

	    Map<String, Double> ShopList = plugin.getShopSignUtilManager().getSortedShopList();

	    String separator = plugin.msg(lm.InformationPage_SmallSeparator);

	    PageInfo pi = new PageInfo(10, ShopList.size(), page);

	    if (!pi.isPageOk()) {
		plugin.msg(sender, lm.Shop_NoVotes);
		return true;
	    }

	    plugin.msg(player, lm.Shop_ListTopLine, separator, Shoppage, pi.getTotalPages(), separator);

	    for (Entry<String, Double> one : ShopList.entrySet()) {
		if (!pi.isEntryOk())
		    continue;
		if (pi.isBreak())
		    break;
		ClaimedResidence res = plugin.getResidenceManager().getByName(one.getKey());
		if (res == null)
		    continue;
		
		Vote vote = plugin.getShopSignUtilManager().getAverageVote(one.getKey());
		String votestat = "";

		if (plugin.getConfigManager().isOnlyLike()) {
		    votestat = vote.getAmount() == 0 ? "" : plugin.msg(lm.Shop_ListLiked, plugin.getShopSignUtilManager().getLikes(one.getKey()));
		} else
		    votestat = vote.getAmount() == 0 ? "" : plugin.msg(lm.Shop_ListVoted, vote.getVote(), vote.getAmount());

		String owner = res.getOwner();
		String message = plugin.msg(lm.Shop_List, pi.getPositionForOutput(), one.getKey(), owner, votestat);

		String desc = res.getShopDesc() == null ? plugin.msg(lm.Shop_NoDesc) : plugin.msg(
		    lm.Shop_Desc, ChatColor.translateAlternateColorCodes('&', res.getShopDesc().replace("/n", "\n")));

		RawMessage rm = new RawMessage();
		rm.add(" " + message, desc, "/res tp " + one.getKey());
		rm.show(sender);
	    }

	    plugin.getInfoPageManager().ShowPagination(sender, pi.getTotalPages(), page, "/res shop list");

	    return true;
	}

	if (args.length == 2 && args[1].equalsIgnoreCase("DeleteBoard")) {

	    if (!resadmin) {
		plugin.msg(player, lm.General_AdminOnly);
		return true;
	    }

	    ShopListener.Delete.add(player.getName());
	    plugin.msg(player, lm.Shop_DeleteBoard);
	    return true;
	}
	if (args.length > 2 && args[1].equalsIgnoreCase("setdesc")) {

	    ClaimedResidence res = null;

	    String desc = "";
	    if (args.length >= 2) {
		res = plugin.getResidenceManager().getByLoc(player.getLocation());
		if (res == null) {
		    plugin.msg(player, lm.Residence_NotIn);
		    return true;
		}
		for (int i = 2; i < args.length; i++) {
		    desc += args[i];
		    if (i < args.length - 1)
			desc += " ";
		}
	    }

	    if (res == null)
		return true;

	    if (!res.isOwner(player) && !resadmin) {
		plugin.msg(player, lm.Residence_NonAdmin);
		return true;
	    }

	    res.setShopDesc(desc);
	    plugin.msg(player, lm.Shop_DescChange, ChatColor.translateAlternateColorCodes('&', desc));
	    return true;
	}
	if (args.length == 3 && args[1].equalsIgnoreCase("createboard")) {

	    if (!resadmin) {
		plugin.msg(player, lm.General_AdminOnly);
		return true;
	    }

	    if (!plugin.getSelectionManager().hasPlacedBoth(player.getName())) {
		plugin.msg(player, lm.Select_Points);
		return true;
	    }

	    int place = 1;
	    try {
		place = Integer.parseInt(args[2]);
	    } catch (Exception ex) {
		plugin.msg(player, lm.General_UseNumbers);
		return true;
	    }

	    if (place < 1)
		place = 1;

	    CuboidArea cuboid = plugin.getSelectionManager().getSelectionCuboid(player);

	    if (cuboid.getXSize() > 16 || cuboid.getYSize() > 16 || cuboid.getZSize() > 16) {
		plugin.msg(player, lm.Shop_ToBigSelection);
		return true;
	    }

	    if (cuboid.getXSize() != 1 && cuboid.getZSize() != 1) {
		plugin.msg(player, lm.Shop_ToDeapSelection);
		return true;
	    }

	    Location loc1 = plugin.getSelectionManager().getPlayerLoc1(player.getName());
	    Location loc2 = plugin.getSelectionManager().getPlayerLoc2(player.getName());

	    if (loc1.getBlockY() < loc2.getBlockY()) {
		plugin.msg(player, lm.Shop_InvalidSelection);
		return true;
	    }

	    Board newTemp = new Board();
	    newTemp.setStartPlace(place);
	    newTemp.setTopLoc(loc1);
	    newTemp.setBottomLoc(loc2);

	    if (plugin.getShopSignUtilManager().exist(newTemp)) {
		sender.sendMessage(plugin.msg(lm.Shop_BoardExist));
		return true;
	    }

	    plugin.getShopSignUtilManager().addBoard(newTemp);
	    plugin.msg(player, lm.Shop_NewBoard);

	    plugin.getShopSignUtilManager().BoardUpdate();
	    plugin.getShopSignUtilManager().saveSigns();

	    return true;

	}
	if ((args.length == 2 || args.length == 3 || args.length == 4) && (args[1].equalsIgnoreCase("vote") || args[1].equalsIgnoreCase("like"))) {
	    String resName = "";
	    int vote = 5;
	    ClaimedResidence res = null;
	    if (args.length == 3) {

		if (plugin.getConfigManager().isOnlyLike()) {

		    res = plugin.getResidenceManager().getByName(args[2]);
		    if (res == null) {
			plugin.msg(player, lm.Invalid_Residence);
			return true;
		    }
		    vote = plugin.getConfigManager().getVoteRangeTo();

		} else {
		    res = plugin.getResidenceManager().getByLoc(player.getLocation());
		    if (res == null) {
			plugin.msg(player, lm.Residence_NotIn);
			return true;
		    }

		    try {
			vote = Integer.parseInt(args[2]);
		    } catch (Exception ex) {
			plugin.msg(player, lm.General_UseNumbers);
			return true;
		    }
		}
	    } else if (args.length == 2 && plugin.getConfigManager().isOnlyLike()) {
		res = plugin.getResidenceManager().getByLoc(player.getLocation());
		if (res == null) {
		    plugin.msg(player, lm.Residence_NotIn);
		    return true;
		}
		vote = plugin.getConfigManager().getVoteRangeTo();
	    } else if (args.length == 4 && !plugin.getConfigManager().isOnlyLike()) {
		res = plugin.getResidenceManager().getByName(args[2]);
		if (res == null) {
		    plugin.msg(player, lm.Invalid_Residence);
		    return true;
		}
		try {
		    vote = Integer.parseInt(args[3]);
		} catch (Exception ex) {
		    plugin.msg(player, lm.General_UseNumbers);
		    return true;
		}
	    } else if (args.length == 3 && !plugin.getConfigManager().isOnlyLike()) {
		res = plugin.getResidenceManager().getByLoc(player.getLocation());
		if (res == null) {
		    plugin.msg(player, lm.Invalid_Residence);
		    return true;
		}
		try {
		    vote = Integer.parseInt(args[3]);
		} catch (Exception ex) {
		    plugin.msg(player, lm.General_UseNumbers);
		    return true;
		}
	    } else {
		return false;
	    }

	    resName = res.getName();

	    if (!res.getPermissions().has("shop", false)) {
		plugin.msg(player, lm.Shop_CantVote);
		return true;
	    }

	    if (vote < plugin.getConfigManager().getVoteRangeFrom() || vote > plugin.getConfigManager().getVoteRangeTo()) {
		plugin.msg(player, lm.Shop_VotedRange, plugin.getConfigManager().getVoteRangeFrom(), plugin.getConfigManager().getVoteRangeTo());
		return true;
	    }

//	    ConcurrentHashMap<String, List<ShopVote>> VoteList = plugin.getShopSignUtilManager().GetAllVoteList();

	    if (!res.GetShopVotes().isEmpty()) {
		List<ShopVote> list = res.GetShopVotes();
		boolean found = false;
		for (ShopVote OneVote : list) {
		    if (OneVote.getName().equalsIgnoreCase(player.getName()) || OneVote.getUuid() != null && OneVote.getUuid() == player.getUniqueId()) {
			if (plugin.getConfigManager().isOnlyLike()) {
			    plugin.msg(player, lm.Shop_AlreadyLiked, resName);
			    return true;
			}
			plugin.msg(player, lm.Shop_VoteChanged, OneVote.getVote(), vote, resName);
			OneVote.setVote(vote);
			OneVote.setName(player.getName());
			OneVote.setTime(System.currentTimeMillis());
			found = true;
			break;
		    }
		}
		if (!found) {
		    ShopVote newVote = new ShopVote(player.getName(), player.getUniqueId(), vote, System.currentTimeMillis());
		    list.add(newVote);
		    if (plugin.getConfigManager().isOnlyLike())
			plugin.msg(player, lm.Shop_Liked, resName);
		    else
			plugin.msg(player, lm.Shop_Voted, vote, resName);
		}
	    } else {
		ShopVote newVote = new ShopVote(player.getName(), player.getUniqueId(), vote, System.currentTimeMillis());
		res.addShopVote(newVote);
		if (plugin.getConfigManager().isOnlyLike())
		    plugin.msg(player, lm.Shop_Liked, resName);
		else
		    plugin.msg(player, lm.Shop_Voted, vote, resName);
	    }
	    plugin.getShopSignUtilManager().saveShopVotes();
	    plugin.getShopSignUtilManager().BoardUpdate();
	    return true;
	}
	return false;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Manage residence shop");
	c.get(path + "Info", Arrays.asList("Manages residence shop feature"));

	// Sub commands
	path += "SubCommands.";
	c.get(path + "list.Description", "Shows list of res shops");
	c.get(path + "list.Info", Arrays.asList("&eUsage: &6/res shop list", "Shows full list of all residences with shop flag"));

	c.get(path + "vote.Description", "Vote for residence shop");
	c.get(path + "vote.Info", Arrays.asList("&eUsage: &6/res shop vote <residence> [amount]", "Votes for current or defined residence"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "vote"), Arrays.asList("[residence]", "10"));

	c.get(path + "like.Description", "Give like for residence shop");
	c.get(path + "like.Info", Arrays.asList("&eUsage: &6/res shop like <residence>", "Gives like for residence shop"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "like"), Arrays.asList("[residenceshop]"));

	c.get(path + "votes.Description", "Shows res shop votes");
	c.get(path + "votes.Info", Arrays.asList("&eUsage: &6/res shop votes <residence> <page>", "Shows full vote list of current or defined residence shop"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "votes"), Arrays.asList("[residenceshop]"));

	c.get(path + "likes.Description", "Shows res shop likes");
	c.get(path + "likes.Info", Arrays.asList("&eUsage: &6/res shop likes <residence> <page>", "Shows full like list of current or defined residence shop"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "likes"), Arrays.asList("[residenceshop]"));

	c.get(path + "setdesc.Description", "Sets residence shop description");
	c.get(path + "setdesc.Info", Arrays.asList("&eUsage: &6/res shop setdesc [text]", "Sets residence shop description. Color code supported. For new line use /n"));

	c.get(path + "createboard.Description", "Create res shop board");
	c.get(path + "createboard.Info", Arrays.asList("&eUsage: &6/res shop createboard [place]",
	    "Creates res shop board from selected area. Place - position from which to start filling board"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "createboard"), Arrays.asList("1"));

	c.get(path + "deleteboard.Description", "Deletes res shop board");
	c.get(path + "deleteboard.Info", Arrays.asList("&eUsage: &6/res shop deleteboard", "Deletes res shop board bi right clicking on one of signs"));
    }
}
