package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ConfigReader;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;

import cmiLib.ItemManager.CMIMaterial;

public class tool implements cmd {

    @SuppressWarnings("deprecation")
    @Override
    @CommandAnnotation(simple = true, priority = 1600)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	plugin.msg(player, lm.General_Separator);
	plugin.msg(player, lm.Select_Tool, CMIMaterial.get(plugin.getConfigManager().getSelectionTooldID()).getName());
	plugin.msg(player, lm.General_InfoTool, CMIMaterial.get(plugin.getConfigManager().getInfoToolID()).getName());
	plugin.msg(player, lm.General_Separator);
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Shows residence selection and info tool names");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res tool"));
    }
}
