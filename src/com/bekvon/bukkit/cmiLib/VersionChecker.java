package com.bekvon.bukkit.cmiLib;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;

public class VersionChecker {
    Residence plugin;
    private static int resource = 11480;

    public VersionChecker(Residence plugin) {
	this.plugin = plugin;
	version = Version.getCurrent();
    }

    private static Version version = Version.v1_11_R1;

    public Version getVersion() {
	return Version.getCurrent();
    }

    public Integer convertVersion(String v) {
	v = v.replaceAll("[^\\d.]", "");
	Integer version = 0;
	if (v.contains(".")) {
	    String lVersion = "";
	    for (String one : v.split("\\.")) {
		String s = one;
		if (s.length() == 1)
		    s = "0" + s;
		lVersion += s;
	    }

	    try {
		version = Integer.parseInt(lVersion);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	} else {
	    try {
		version = Integer.parseInt(v);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	return version;
    }

    public enum Version {
	v1_7_R1,
	v1_7_R2,
	v1_7_R3,
	v1_7_R4,
	v1_8_R1,
	v1_8_R2,
	v1_8_R3,
	v1_9_R1,
	v1_9_R2,
	v1_10_R1,
	v1_11_R1,
	v1_12_R1,
	v1_13_R1,
	v1_13_R2,
	v1_13_R3,
	v1_14_R1,
	v1_14_R2,
	v1_15_R1,
	v1_15_R2,
	v1_16_R1,
	v1_16_R2,
	v1_17_R1,
	v1_17_R2;

	private Integer value;
	private String shortVersion;
	private static Version current = null;

	Version() {
	    try {
		this.value = Integer.valueOf(this.name().replaceAll("[^\\d.]", ""));
	    } catch (Exception e) {
	    }
	    shortVersion = this.name().substring(0, this.name().length() - 3);
	}

	public Integer getValue() {
	    return value;
	}

	public String getShortVersion() {
	    return shortVersion;
	}

	public static Version getCurrent() {
	    if (current != null)
		return current;
	    String[] v = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
	    String vv = v[v.length - 1];
	    for (Version one : values()) {
		if (one.name().equalsIgnoreCase(vv)) {
		    current = one;
		    break;
		}
	    }
	    return current;
	}

	public boolean isLower(Version version) {
	    return getValue() < version.getValue();
	}

	public boolean isHigher(Version version) {
	    return getValue() > version.getValue();
	}

	public boolean isEqualOrLower(Version version) {
	    return getValue() <= version.getValue();
	}

	public boolean isEqualOrHigher(Version version) {
	    return getValue() >= version.getValue();
	}

	public static boolean isCurrentEqualOrHigher(Version v) {
	    return version.getValue() >= v.getValue();
	}

	public static boolean isCurrentHigher(Version v) {
	    return version.getValue() > v.getValue();
	}

	public static boolean isCurrentLower(Version v) {
	    return version.getValue() < v.getValue();
	}

	public static boolean isCurrentEqualOrLower(Version v) {
	    return version.getValue() <= v.getValue();
	}
    }

    public void VersionCheck(final Player player) {
	if (!plugin.getConfigManager().versionCheck())
	    return;

	Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
	    @Override
	    public void run() {
		String currentVersion = plugin.getDescription().getVersion();
		String newVersion = getNewVersion();
		if (newVersion == null || newVersion.equalsIgnoreCase(currentVersion))
		    return;
		List<String> msg = Arrays.asList(
		    ChatColor.GREEN + "*********************** " + plugin.getDescription().getName() + " **************************",
		    ChatColor.GREEN + "* " + newVersion + " is now available! Your version: " + currentVersion,
		    ChatColor.GREEN + "* " + ChatColor.DARK_GREEN + plugin.getDescription().getWebsite(),
		    ChatColor.GREEN + "************************************************************");
		for (String one : msg)
		    if (player != null)
			player.sendMessage(one);
		    else
			Bukkit.getConsoleSender().sendMessage(one);
	    }
	});
    }

    public String getNewVersion() {
	try {
	    HttpURLConnection con = (HttpURLConnection) new URL("https://www.spigotmc.org/api/general.php").openConnection();
	    con.setDoOutput(true);
	    con.setRequestMethod("POST");
	    con.getOutputStream().write(("key=98BE0FE67F88AB82B4C197FAF1DC3B69206EFDCC4D3B80FC83A00037510B99B4&resource=" + resource).getBytes("UTF-8"));
	    String version = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
	    if (version.length() <= 7)
		return version;
	} catch (Exception ex) {
	    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Failed to check for " + plugin.getDescription().getName() + " update on spigot web page.");
	}
	return null;
    }

}
