package com.bekvon.bukkit.residence.protection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.MinimizeFlags;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;

import cmiLib.ItemManager.CMIMaterial;
import cmiLib.VersionChecker.Version;

public class FlagPermissions {

    protected static ArrayList<String> validFlags = new ArrayList<>();
    protected static ArrayList<String> validPlayerFlags = new ArrayList<>();
    protected static ArrayList<String> validAreaFlags = new ArrayList<>();
    final static Map<Material, Flags> matUseFlagList = new EnumMap<>(Material.class);
    protected Map<UUID, String> cachedPlayerNameUUIDs = new HashMap<UUID, String>();
    protected Map<String, Map<String, Boolean>> playerFlags = new HashMap<String, Map<String, Boolean>>();
    protected Map<String, Map<String, Boolean>> groupFlags = new HashMap<String, Map<String, Boolean>>();
    protected Map<String, Boolean> cuboidFlags = new HashMap<String, Boolean>();
    protected FlagPermissions parent;
    protected static HashMap<String, ArrayList<String>> validFlagGroups = new HashMap<>();

    public FlagPermissions() {
	cuboidFlags = Collections.synchronizedMap(new HashMap<String, Boolean>());
	playerFlags = Collections.synchronizedMap(new HashMap<String, Map<String, Boolean>>());
	groupFlags = Collections.synchronizedMap(new HashMap<String, Map<String, Boolean>>());
	cachedPlayerNameUUIDs = Collections.synchronizedMap(new HashMap<UUID, String>());
    }

    public static enum FlagCombo {
	OnlyTrue, OnlyFalse, TrueOrNone, FalseOrNone
    }

    public static enum FlagState {
	TRUE, FALSE, NEITHER, INVALID;
	public String getName() {
	    return name().toLowerCase();
	}
    }

    public static void addMaterialToUseFlag(Material mat, Flags flag) {
	matUseFlagList.put(mat, flag);
    }

    public static void removeMaterialFromUseFlag(Material mat) {
	matUseFlagList.remove(mat);
    }

    public static EnumMap<Material, Flags> getMaterialUseFlagList() {
	return (EnumMap<Material, Flags>) matUseFlagList;
    }

    public static void addFlag(Flags flag) {
	addFlag(flag.name());
    }

    public static void addFlag(String flag) {
	flag = flag.toLowerCase();
	if (!validFlags.contains(flag)) {
	    validFlags.add(flag);
	}
	if (validFlagGroups.containsKey(flag)) {
	    validFlagGroups.remove(flag);
	}
    }

    public static void addPlayerOrGroupOnlyFlag(Flags flag) {
	addPlayerOrGroupOnlyFlag(flag.name());
    }

    public static void addPlayerOrGroupOnlyFlag(String flag) {
	flag = flag.toLowerCase();
	if (!validPlayerFlags.contains(flag)) {
	    validPlayerFlags.add(flag);
	}
	if (validFlagGroups.containsKey(flag)) {
	    validFlagGroups.remove(flag);
	}
    }

    public static void addResidenceOnlyFlag(Flags flag) {
	addResidenceOnlyFlag(flag.name());
    }

    public static void addResidenceOnlyFlag(String flag) {
	flag = flag.toLowerCase();
	if (!validAreaFlags.contains(flag)) {
	    validAreaFlags.add(flag);
	}
	if (validFlagGroups.containsKey(flag)) {
	    validFlagGroups.remove(flag);
	}
    }

    public static void addFlagToFlagGroup(String group, String flag) {
	if (!FlagPermissions.validFlags.contains(group) && !FlagPermissions.validAreaFlags.contains(group) && !FlagPermissions.validPlayerFlags.contains(group)) {
	    if (!validFlagGroups.containsKey(group)) {
		validFlagGroups.put(group, new ArrayList<String>());
	    }
	    ArrayList<String> flags = validFlagGroups.get(group);
	    flags.add(flag);
	}
    }

    public static void removeFlagFromFlagGroup(String group, String flag) {
	if (validFlagGroups.containsKey(group)) {
	    ArrayList<String> flags = validFlagGroups.get(group);
	    flags.remove(flag);
	    if (flags.isEmpty()) {
		validFlagGroups.remove(group);
	    }
	}
    }

    public static boolean flagGroupExists(String group) {
	return validFlagGroups.containsKey(group);
    }

    public static void initValidFlags() {
	validAreaFlags.clear();
	validPlayerFlags.clear();
	validFlags.clear();
	validFlagGroups.clear();

	for (Flags flag : Flags.values()) {
	    switch (flag.getFlagMode()) {
	    case Both:
		addFlag(flag);
		break;
	    case Player:
		addPlayerOrGroupOnlyFlag(flag);
		break;
	    case Residence:
		addResidenceOnlyFlag(flag);
		break;
	    default:
		break;
	    }
	}

	Residence.getInstance().getConfigManager().UpdateGroupedFlagsFile();

	// All these flags are moved to flags.yml as of 2.9.11.0 version for option to customize them
//	addFlagToFlagGroup("redstone", "note");
//	addFlagToFlagGroup("redstone", "pressure");
//	addFlagToFlagGroup("redstone", "lever");
//	addFlagToFlagGroup("redstone", "button");
//	addFlagToFlagGroup("redstone", "diode");
//	addFlagToFlagGroup("craft", "brew");
//	addFlagToFlagGroup("craft", "table");
//	addFlagToFlagGroup("craft", "enchant");
//	addFlagToFlagGroup("trusted", "use");
//	addFlagToFlagGroup("trusted", "tp");
//	addFlagToFlagGroup("trusted", "build");
//	addFlagToFlagGroup("trusted", "container");
//	addFlagToFlagGroup("trusted", "bucket");
//	addFlagToFlagGroup("trusted", "move");
//	addFlagToFlagGroup("trusted", "leash");
//	addFlagToFlagGroup("trusted", "animalkilling");
//	addFlagToFlagGroup("trusted", "mobkilling");
//	addFlagToFlagGroup("trusted", "shear");
//	addFlagToFlagGroup("trusted", "chat");
//	addFlagToFlagGroup("fire", "ignite");
//	addFlagToFlagGroup("fire", "firespread");

	addMaterialToUseFlag(CMIMaterial.REPEATER.getMaterial(), Flags.diode);
//	addMaterialToUseFlag(CMIMaterial.DIODE_BLOCK_OFF.getMaterial(), Flags.diode);
//	addMaterialToUseFlag(CMIMaterial.DIODE_BLOCK_ON.getMaterial(), Flags.diode);
	addMaterialToUseFlag(CMIMaterial.COMPARATOR.getMaterial(), Flags.diode);
//	addMaterialToUseFlag(CMIMaterial.REDSTONE_COMPARATOR_OFF.getMaterial(), Flags.diode);
//	addMaterialToUseFlag(CMIMaterial.REDSTONE_COMPARATOR_ON.getMaterial(), Flags.diode);
	addMaterialToUseFlag(Material.DAYLIGHT_DETECTOR, Flags.diode);
	addMaterialToUseFlag(CMIMaterial.CRAFTING_TABLE.getMaterial(), Flags.table);
	addMaterialToUseFlag(CMIMaterial.OAK_DOOR.getMaterial(), Flags.door);

	Residence.getInstance().getNms().addDefaultFlags(matUseFlagList);

//	addMaterialToUseFlag(CMIMaterial.FENCE_GATE, Flags.door);
//	addMaterialToUseFlag(Material.NETHER_FENCE, Flags.door);
//	addMaterialToUseFlag(Material.TRAP_DOOR, Flags.door);
	addMaterialToUseFlag(CMIMaterial.ENCHANTING_TABLE.getMaterial(), Flags.enchant);
	addMaterialToUseFlag(Material.STONE_BUTTON, Flags.button);
	addMaterialToUseFlag(Material.LEVER, Flags.lever);

	for (CMIMaterial one : CMIMaterial.values()) {
	    if (!one.isBed())
		continue;
	    addMaterialToUseFlag(one.getMaterial(), Flags.bed);
	}

	addMaterialToUseFlag(Material.BREWING_STAND, Flags.brew);
	addMaterialToUseFlag(Material.CAKE, Flags.cake);
	addMaterialToUseFlag(Material.NOTE_BLOCK, Flags.note);
	addMaterialToUseFlag(Material.DRAGON_EGG, Flags.egg);
	addMaterialToUseFlag(CMIMaterial.COMMAND_BLOCK.getMaterial(), Flags.commandblock);
	addMaterialToUseFlag(CMIMaterial.OAK_BUTTON.getMaterial(), Flags.button);
	addMaterialToUseFlag(Material.ANVIL, Flags.anvil);
	addMaterialToUseFlag(Material.FLOWER_POT, Flags.flowerpot);

	if (Version.isCurrentEqualOrHigher(Version.v1_13_R1))
	    for (CMIMaterial one : CMIMaterial.values()) {
		if (!one.isPottedFlower())
		    continue;
		if (one.getMaterial() == null)
		    continue;
		addMaterialToUseFlag(one.getMaterial(), Flags.flowerpot);
	    }

	addMaterialToUseFlag(Material.BEACON, Flags.beacon);
	addMaterialToUseFlag(Material.JUKEBOX, Flags.container);
	addMaterialToUseFlag(Material.CHEST, Flags.container);
	addMaterialToUseFlag(Material.TRAPPED_CHEST, Flags.container);
	addMaterialToUseFlag(Material.HOPPER, Flags.container);
	addMaterialToUseFlag(Material.DROPPER, Flags.container);
	addMaterialToUseFlag(Material.FURNACE, Flags.container);
	addMaterialToUseFlag(CMIMaterial.FURNACE.getMaterial(), Flags.container);
	addMaterialToUseFlag(Material.DISPENSER, Flags.container);
	addMaterialToUseFlag(CMIMaterial.CAKE.getMaterial(), Flags.cake);
    }

    public static FlagPermissions parseFromConfigNode(String name, ConfigurationSection node) {
	FlagPermissions list = new FlagPermissions();

	if (!node.isConfigurationSection(name))
	    return list;

	Set<String> keys = node.getConfigurationSection(name).getKeys(false);
	if (keys == null)
	    return list;

	for (String key : keys) {
	    boolean state = node.getBoolean(name + "." + key, false);
	    key = key.toLowerCase();
	    Flags f = Flags.getFlag(key);
	    if (f != null)
		f.setEnabled(state);
	    if (state) {
		list.setFlag(key, FlagState.TRUE);
	    } else {
		list.setFlag(key, FlagState.FALSE);
	    }
	}
	return list;
    }

    public static FlagPermissions parseFromConfigNodeAsList(String node, String stage) {
	FlagPermissions list = new FlagPermissions();
	if (node.equalsIgnoreCase("true")) {
	    list.setFlag(node, FlagState.valueOf(stage));
	} else {
	    list.setFlag(node, FlagState.FALSE);
	}

	return list;
    }

    protected Map<String, Boolean> getPlayerFlags(String player, boolean allowCreate)//this function works with uuid in string format as well, instead of player name
    {

	Map<String, Boolean> flags = null;

	if (!Residence.getInstance().getConfigManager().isOfflineMode()) {
	    UUID uuid = null;
	    if (player.length() == 36) {
		try {
		    uuid = UUID.fromString(player);
		} catch (Exception e) {

		}
		String resolvedName = Residence.getInstance().getPlayerName(uuid);
		if (resolvedName != null)
		    player = resolvedName;
		else if (cachedPlayerNameUUIDs.containsKey(uuid))
		    player = cachedPlayerNameUUIDs.get(uuid);
	    } else
		uuid = Residence.getInstance().getPlayerUUID(player);

	    if (uuid == null) {
		Set<Entry<UUID, String>> values = cachedPlayerNameUUIDs.entrySet();
		for (Entry<UUID, String> value : values) {
		    if (value.getValue().equals(player)) {
			uuid = value.getKey();
			break;
		    }
		}
	    }

	    if (uuid != null)
		flags = playerFlags.get(uuid.toString());
	    if (flags == null) {
		flags = playerFlags.get(player);
		if (uuid != null && flags != null) {
		    flags = playerFlags.remove(player);
		    playerFlags.put(uuid.toString(), flags);
		    cachedPlayerNameUUIDs.put(uuid, player);
		}
	    } else
		cachedPlayerNameUUIDs.put(uuid, player);

	    if (flags == null && allowCreate) {
		if (uuid != null) {
		    flags = Collections.synchronizedMap(new HashMap<String, Boolean>());
		    playerFlags.put(uuid.toString(), flags);
		    cachedPlayerNameUUIDs.put(uuid, player);
		} else {
		    flags = Collections.synchronizedMap(new HashMap<String, Boolean>());
		    playerFlags.put(player, flags);
		}
	    }
	} else {
	    for (Entry<String, Map<String, Boolean>> one : playerFlags.entrySet()) {
		if (!one.getKey().equalsIgnoreCase(player))
		    continue;
		// Updating players name to correct capitalization
		if (!one.getKey().equals(player)) {
		    Map<String, Boolean> r = playerFlags.remove(one.getKey());
		    playerFlags.put(player, r);
		}
		flags = one.getValue();
		break;
	    }
	    if (flags == null && allowCreate) {
		flags = Collections.synchronizedMap(new HashMap<String, Boolean>());
		playerFlags.put(player, flags);
	    }
	}
	return flags;
    }

    public boolean setPlayerFlag(String player, String flag, FlagState state) {
	Map<String, Boolean> map = this.getPlayerFlags(player, state != FlagState.NEITHER);
	if (map == null)
	    return true;
	if (state == FlagState.FALSE) {
	    map.put(flag, false);
	} else if (state == FlagState.TRUE) {
	    map.put(flag, true);
	} else if (state == FlagState.NEITHER) {
	    if (map.containsKey(flag)) {
		map.remove(flag);
	    }
	}
	if (map.isEmpty())
	    this.removeAllPlayerFlags(player);
	return true;
    }

    public void removeAllPlayerFlags(String player) {//this function works with uuid in string format as well, instead of player name
	// player = player.toLowerCase();

	if (!Residence.getInstance().getConfigManager().isOfflineMode()) {
	    UUID uuid = Residence.getInstance().getPlayerUUID(player);
	    if (uuid == null)
		for (Entry<UUID, String> entry : cachedPlayerNameUUIDs.entrySet())
		    if (entry.getValue().equals(player)) {
			uuid = entry.getKey();
			break;
		    }

	    if (uuid != null) {
		playerFlags.remove(uuid.toString());
		cachedPlayerNameUUIDs.remove(uuid);
	    }
	    return;
	}
	playerFlags.remove(player);
	cachedPlayerNameUUIDs.remove(player);

    }

    public void removeAllGroupFlags(String group) {
	groupFlags.remove(group);
    }

    public boolean setGroupFlag(String group, String flag, FlagState state) {
	group = group.toLowerCase();
	if (!groupFlags.containsKey(group)) {
	    groupFlags.put(group, Collections.synchronizedMap(new HashMap<String, Boolean>()));
	}
	Map<String, Boolean> map = groupFlags.get(group);
	if (state == FlagState.FALSE) {
	    map.put(flag, false);
	} else if (state == FlagState.TRUE) {
	    map.put(flag, true);
	} else if (state == FlagState.NEITHER) {
	    if (map.containsKey(flag)) {
		map.remove(flag);
	    }
	}
	if (map.isEmpty()) {
	    groupFlags.remove(group);
	}
	return true;
    }

    public boolean setFlag(String flag, FlagState state) {
	if (state == FlagState.FALSE) {
	    cuboidFlags.put(flag, false);
	} else if (state == FlagState.TRUE) {
	    cuboidFlags.put(flag, true);
	} else if (state == FlagState.NEITHER) {
	    if (cuboidFlags.containsKey(flag)) {
		cuboidFlags.remove(flag);
	    }
	}
	return true;
    }

    public static FlagState stringToFlagState(String flagstate) {
	if (flagstate.equalsIgnoreCase("true") || flagstate.equalsIgnoreCase("t")) {
	    return FlagState.TRUE;
	} else if (flagstate.equalsIgnoreCase("false") || flagstate.equalsIgnoreCase("f")) {
	    return FlagState.FALSE;
	} else if (flagstate.equalsIgnoreCase("remove") || flagstate.equalsIgnoreCase("r")) {
	    return FlagState.NEITHER;
	} else {
	    return FlagState.INVALID;
	}
    }

    public boolean playerHas(Player player, Flags flag, boolean def) {
	if (player == null)
	    return false;

	ResidencePlayer resPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer(player);
	PermissionGroup group = resPlayer.getGroup();
	return this.playerCheck(player.getName(), flag.getName(), this.groupCheck(group, flag.getName(), this.has(flag, def)));
    }

    public boolean playerHas(Player player, String world, Flags flag, boolean def) {
	if (player == null)
	    return false;

	ResidencePlayer resPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer(player);
	PermissionGroup group = resPlayer.getGroup(world);
	return this.playerCheck(player.getName(), flag.getName(), this.groupCheck(group, flag.getName(), this.has(flag, def)));
    }

//    public boolean playerHas(String player, String world, Flags flag, boolean def) {
//	return playerHas(player, world, flag.getName(), def);
//    }

    @Deprecated
    public boolean playerHas(String player, String world, String flag, boolean def) {
	ResidencePlayer resPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer(player);
	PermissionGroup group = resPlayer.getGroup(world);
	return this.playerCheck(player, flag, this.groupCheck(group, flag, this.has(flag, def)));
    }

    public boolean groupHas(String group, String flag, boolean def) {
	return this.groupCheck(group, flag, this.has(flag, def));
    }

    private boolean playerCheck(String player, String flag, boolean def) {
	Map<String, Boolean> pmap = this.getPlayerFlags(player, false);
	if (pmap != null) {
	    if (pmap.containsKey(flag)) {
		return pmap.get(flag);
	    }
	}
	if (parent != null) {
	    return parent.playerCheck(player, flag, def);
	}
	return def;
    }

    private boolean groupCheck(PermissionGroup group, String flag, boolean def) {
	if (group == null)
	    return def;
	return groupCheck(group.getGroupName(), flag, def);
    }

    private boolean groupCheck(String group, String flag, boolean def) {
	if (groupFlags.containsKey(group)) {
	    Map<String, Boolean> gmap = groupFlags.get(group);
	    if (gmap.containsKey(flag)) {
		return gmap.get(flag);
	    }
	}
	if (parent != null) {
	    return parent.groupCheck(group, flag, def);
	}
	return def;
    }

    public boolean has(Flags flag, FlagCombo f) {
	switch (f) {
	case FalseOrNone:
	    return !has(flag, false);
	case OnlyFalse:
	    return !has(flag, true);
	case OnlyTrue:
	    return has(flag, false);
	case TrueOrNone:
	    return has(flag, true);
	default:
	    return false;
	}
    }

    public boolean has(Flags flag, boolean def) {
	return has(flag, def, true);
    }

    public boolean has(Flags flag, boolean def, boolean checkParent) {
	if (cuboidFlags.containsKey(flag.getName())) {
	    return cuboidFlags.get(flag.getName());
	}
	if (checkParent && parent != null) {
	    return parent.has(flag, def);
	}
	return def;
    }

    @Deprecated
    public boolean has(String flag, boolean def) {
	return has(flag, def, true);
    }

    @Deprecated
    public boolean has(String flag, boolean def, boolean checkParent) {
	if (cuboidFlags.containsKey(flag)) {
	    return cuboidFlags.get(flag);
	}
	if (checkParent && parent != null) {
	    return parent.has(flag, def);
	}
	return def;
    }

    public boolean isPlayerSet(String player, String flag) {
	Map<String, Boolean> flags = this.getPlayerFlags(player, false);
	if (flags == null)
	    return false;
	return flags.containsKey(flag);
    }

    public boolean inheritanceIsPlayerSet(String player, String flag) {
	Map<String, Boolean> flags = this.getPlayerFlags(player, false);
	if (flags == null) {
	    return parent == null ? false : parent.inheritanceIsPlayerSet(player, flag);
	}
	return flags.containsKey(flag) ? true : parent == null ? false : parent.inheritanceIsPlayerSet(player, flag);
    }

    public boolean isGroupSet(String group, String flag) {
	group = group.toLowerCase();
	Map<String, Boolean> flags = groupFlags.get(group);
	if (flags == null) {
	    return false;
	}
	return flags.containsKey(flag);
    }

    public boolean inheritanceIsGroupSet(String group, String flag) {
	group = group.toLowerCase();
	Map<String, Boolean> flags = groupFlags.get(group);
	if (flags == null) {
	    return parent == null ? false : parent.inheritanceIsGroupSet(group, flag);
	}
	return flags.containsKey(flag) ? true : parent == null ? false : parent.inheritanceIsGroupSet(group, flag);
    }

    public boolean isSet(String flag) {
	return cuboidFlags.containsKey(flag);
    }

    public boolean inheritanceIsSet(String flag) {
	return cuboidFlags.containsKey(flag) ? true : parent == null ? false : parent.inheritanceIsSet(flag);
    }

    public boolean checkValidFlag(String flag, boolean globalflag) {
	if (validFlags.contains(flag)) {
	    return true;
	}
	if (globalflag) {
	    if (validAreaFlags.contains(flag)) {
		return true;
	    }
	} else {
	    if (validPlayerFlags.contains(flag)) {
		return true;
	    }
	}
	return false;
    }

    public Map<String, Object> save(String world) {
	Map<String, Object> root = new LinkedHashMap<>();

	// Putting uuid's to main cache for later save

	if (Residence.getInstance().getConfigManager().isNewSaveMechanic()) {
	    Map<String, Object> playerFlagsClone = new HashMap<String, Object>();
	    for (Entry<String, Map<String, Boolean>> one : playerFlags.entrySet()) {
		MinimizeFlags min = Residence.getInstance().getResidenceManager().addFlagsTempCache(world, one.getValue());
		playerFlagsClone.put(one.getKey(), min.getId());
	    }
	    root.put("PlayerFlags", playerFlagsClone);

	    if (!groupFlags.isEmpty()) {
		Map<String, Object> GroupFlagsClone = new HashMap<String, Object>();
		for (Entry<String, Map<String, Boolean>> one : groupFlags.entrySet()) {
		    MinimizeFlags min = Residence.getInstance().getResidenceManager().addFlagsTempCache(world, one.getValue());
		    GroupFlagsClone.put(one.getKey(), min.getId());
		}
		root.put("GroupFlags", GroupFlagsClone);
	    }

	    MinimizeFlags min = Residence.getInstance().getResidenceManager().addFlagsTempCache(world, cuboidFlags);
	    if (min == null) {
		// Cloning map to fix issue for yml anchors being created	
		root.put("AreaFlags", new HashMap<String, Boolean>(cuboidFlags));
	    } else {
		root.put("AreaFlags", min.getId());
	    }

	} else {
	    root.put("PlayerFlags", clone(playerFlags));
	    if (!groupFlags.isEmpty()) {
		root.put("GroupFlags", clone(this.groupFlags));
	    }

	    // Cloning map to fix issue for yml anchors being created
	    root.put("AreaFlags", new HashMap<String, Boolean>(cuboidFlags));
	}

	return root;
    }

    private static HashMap<String, Map<String, Boolean>> clone(Map<String, Map<String, Boolean>> map) {
	HashMap<String, Map<String, Boolean>> nm = new HashMap<String, Map<String, Boolean>>();
	for (Entry<String, Map<String, Boolean>> one : map.entrySet()) {
	    nm.put(one.getKey(), new HashMap<String, Boolean>(one.getValue()));
	}
	return nm;
    }

    public static FlagPermissions load(Map<String, Object> root) throws Exception {
	FlagPermissions newperms = new FlagPermissions();
	return FlagPermissions.load(root, newperms);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected static FlagPermissions load(Map<String, Object> root, FlagPermissions newperms) throws Exception {
	if (root.containsKey("LastKnownPlayerNames"))
	    newperms.cachedPlayerNameUUIDs = (Map) root.get("LastKnownPlayerNames");

	if (root.containsKey("PlayerFlags")) {
	    boolean old = true;
	    for (Entry<String, Object> one : ((HashMap<String, Object>) root.get("PlayerFlags")).entrySet()) {
		if (one.getValue() instanceof Integer)
		    old = false;
		break;
	    }
	    if (old)
		newperms.playerFlags = (Map) root.get("PlayerFlags");
	    else {
		if (newperms instanceof ResidencePermissions) {
		    Map<String, Map<String, Boolean>> t = new HashMap<String, Map<String, Boolean>>();
		    Map<String, Boolean> ft = new HashMap<String, Boolean>();
		    for (Entry<String, Integer> one : ((HashMap<String, Integer>) root.get("PlayerFlags")).entrySet()) {
			ft = Residence.getInstance().getResidenceManager().getChacheFlags(((ResidencePermissions) newperms).getWorld(), one.getValue());
			if (ft != null && !ft.isEmpty())
			    t.put(one.getKey(), new HashMap<String, Boolean>(ft));
		    }
		    if (!t.isEmpty())
			newperms.playerFlags = t;
		}

	    }
	}

	for (Entry<String, Map<String, Boolean>> one : newperms.playerFlags.entrySet()) {
	    if (one.getKey().length() != 32)
		continue;

	    try {
		UUID uuid = UUID.fromString(one.getKey());
		OfflinePlayer player = Residence.getInstance().getOfflinePlayer(uuid);
		newperms.cachedPlayerNameUUIDs.put(uuid, player.getName());
	    } catch (Exception e) {
		continue;
	    }

	}

	if (root.containsKey("GroupFlags")) {
	    boolean old = true;
	    for (Entry<String, Object> one : ((HashMap<String, Object>) root.get("GroupFlags")).entrySet()) {
		if (one.getValue() instanceof Integer)
		    old = false;
		break;
	    }
	    if (old)
		newperms.groupFlags = (Map) root.get("GroupFlags");
	    else {
		if (newperms instanceof ResidencePermissions) {
		    Map<String, Map<String, Boolean>> t = new HashMap<String, Map<String, Boolean>>();
		    Map<String, Boolean> ft = new HashMap<String, Boolean>();
		    for (Entry<String, Integer> one : ((HashMap<String, Integer>) root.get("GroupFlags")).entrySet()) {
			ft = Residence.getInstance().getResidenceManager().getChacheFlags(((ResidencePermissions) newperms).getWorld(), one.getValue());
			if (ft != null && !ft.isEmpty())
			    t.put(one.getKey(), new HashMap<String, Boolean>(ft));
		    }
		    if (!t.isEmpty()) {
			newperms.groupFlags = t;
		    }
		}
	    }
	}

//	if (root.containsKey("GroupFlags"))
//	    newperms.groupFlags = (Map) root.get("GroupFlags");

	if (root.containsKey("AreaFlags")) {
	    boolean old = true;
	    if (root.get("AreaFlags") instanceof Integer)
		old = false;
	    if (old)
		newperms.cuboidFlags = (Map) root.get("AreaFlags");
	    else {
		if (newperms instanceof ResidencePermissions) {
		    Map<String, Boolean> ft = new HashMap<String, Boolean>();
		    ft = Residence.getInstance().getResidenceManager().getChacheFlags(((ResidencePermissions) newperms).getWorld(), (Integer) root.get("AreaFlags"));
		    if (ft != null && !ft.isEmpty())
			newperms.cuboidFlags = new HashMap<String, Boolean>(ft);
		}
	    }

//	    newperms.cuboidFlags = (Map) root.get("AreaFlags");
	} else
	    newperms.cuboidFlags = Residence.getInstance().getConfigManager().getGlobalResidenceDefaultFlags().getFlags();

	String ownerName = null;
	String uuid = null;

	if (root.containsKey("OwnerLastKnownName")) {
	    ownerName = (String) root.get("OwnerLastKnownName");
	    if (root.containsKey("OwnerUUID"))
		uuid = (String) root.get("OwnerUUID");
	    else
		uuid = Residence.getInstance().getTempUserUUID();
	}

	if (Residence.getInstance().getConfigManager().isOfflineMode())
	    newperms.convertFlagsUUIDsToPlayerNames();
	else
	    newperms.convertPlayerNamesToUUIDs(ownerName, uuid);

	return newperms;
    }

    private void convertFlagsUUIDsToPlayerNames() {
	HashMap<String, String> converts = new HashMap<>();
	for (String keyset : playerFlags.keySet()) {
	    if (keyset.length() == 36) {
		String uuid = keyset;
		if (uuid.equalsIgnoreCase(Residence.getInstance().getServerLandUUID()))
		    converts.put(uuid, Residence.getInstance().getServerLandname());
		else {
		    String name = Residence.getInstance().getPlayerName(uuid);
		    if (name != null)
			converts.put(uuid, name);
		}
	    }
	}

	for (Entry<String, String> one : converts.entrySet()) {
	    if (playerFlags.containsKey(one.getKey())) {
		Map<String, Boolean> replace = playerFlags.get(one.getKey());
		playerFlags.remove(one.getKey());
		playerFlags.put(one.getValue(), replace);
	    }
	}

    }

    private void convertPlayerNamesToUUIDs(String OwnerName, String owneruuid) {
	HashMap<String, String> converts = new HashMap<>();

	List<String> Toremove = new ArrayList<String>();

	for (String keyset : playerFlags.keySet()) {
	    if (keyset.length() != 36) {
		String uuid = null;
		if (OwnerName != null && OwnerName.equals(keyset) && !owneruuid.equals(Residence.getInstance().getTempUserUUID()))
		    uuid = owneruuid;
		else
		    uuid = Residence.getInstance().getPlayerUUIDString(keyset);
		//				if (OwnerName.equals(keyset)) {
		if (uuid != null)
		    converts.put(keyset, uuid);
		else if (OwnerName != null && !OwnerName.equals(keyset))
		    Toremove.add(keyset);
		//				}
	    } else {
		String pname = Residence.getInstance().getPlayerName(keyset);
		if (pname != null) {
		    try {
			UUID uuid = UUID.fromString(keyset);
			this.cachedPlayerNameUUIDs.put(uuid, pname);
		    } catch (Exception e) {
		    }
		}
	    }
	}
	for (String one : Toremove) {
	    playerFlags.remove(one);
	}
	for (Entry<String, String> convert : converts.entrySet()) {
	    playerFlags.put(convert.getValue(), playerFlags.remove(convert.getKey()));
	    try {
		UUID uuid = UUID.fromString(convert.getValue());
		cachedPlayerNameUUIDs.put(uuid, convert.getKey());
	    } catch (Exception e) {
	    }
	}

    }

    public String listFlags() {
	return listFlags(0, 0);
    }

    public String listFlags(Integer split) {
	return listFlags(split, 0);
    }

    public String listFlags(Integer split, Integer totalShow) {
	StringBuilder sbuild = new StringBuilder();
	Set<Entry<String, Boolean>> set = cuboidFlags.entrySet();
	synchronized (set) {
	    Iterator<Entry<String, Boolean>> it = set.iterator();
	    int i = -1;
	    int t = 0;
	    while (it.hasNext()) {
		Entry<String, Boolean> next = it.next();
		String fname = next.getKey();

		Flags flag = Flags.getFlag(fname);

		if (flag != null && !flag.isGlobalyEnabled())
		    continue;

		i++;
		t++;

		if (totalShow > 0 && t > totalShow) {
		    break;
		}

		if (split > 0 && i >= split) {
		    i = 0;
		    sbuild.append("\n");
		}

		if (next.getValue()) {
		    sbuild.append("&2").append("+").append(flag);
		    if (it.hasNext()) {
			sbuild.append(" ");
		    }
		} else {
		    sbuild.append("&3").append("-").append(flag);
		    if (it.hasNext()) {
			sbuild.append(" ");
		    }
		}

	    }
	}
	if (sbuild.length() == 0) {
	    sbuild.append("none");
	}
	return ChatColor.translateAlternateColorCodes('&', sbuild.toString());
    }

    public Map<String, Boolean> getFlags() {
	return cuboidFlags;
    }

    public Map<String, Boolean> getPlayerFlags(String player) {
	return this.getPlayerFlags(player, false);
    }

    public Set<String> getposibleFlags() {
	Set<String> t = new HashSet<String>();
	t.addAll(FlagPermissions.validFlags);
	t.addAll(FlagPermissions.validPlayerFlags);
	return t;
    }

    public ArrayList<String> getposibleAreaFlags() {
	return FlagPermissions.validAreaFlags;
    }

    public List<String> getPosibleFlags(Player player, boolean residence, boolean resadmin) {
	List<String> flags = new ArrayList<String>();
	for (Entry<String, Boolean> one : Residence.getInstance().getPermissionManager().getAllFlags().getFlags().entrySet()) {
	    if (!one.getValue() && !resadmin && !player.hasPermission(new Permission("residence.flag." + one.getKey().toLowerCase(), PermissionDefault.FALSE)))
		continue;

	    if (!residence && !getposibleFlags().contains(one.getKey()))
		continue;

	    String fname = one.getKey();

	    Flags flag = Flags.getFlag(fname);

	    if (flag != null && !flag.isGlobalyEnabled())
		continue;

	    flags.add(one.getKey());
	}

	return flags;
    }

    public String listPlayerFlags(String player) {
	Map<String, Boolean> flags = this.getPlayerFlags(player, false);
	if (flags != null) {
	    return this.printPlayerFlags(flags);
	}
	return "none";
    }

    protected String printPlayerFlags(Map<String, Boolean> flags) {
	StringBuilder sbuild = new StringBuilder();
	if (flags == null)
	    return "none";
	Set<Entry<String, Boolean>> set = flags.entrySet();
	synchronized (flags) {
	    Iterator<Entry<String, Boolean>> it = set.iterator();
	    while (it.hasNext()) {
		Entry<String, Boolean> next = it.next();
		if (next.getValue()) {
		    sbuild.append("&2").append("+").append(next.getKey());
		    if (it.hasNext()) {
			sbuild.append(" ");
		    }
		} else {
		    sbuild.append("&3").append("-").append(next.getKey());
		    if (it.hasNext()) {
			sbuild.append(" ");
		    }
		}
	    }
	}
	if (sbuild.length() == 0) {
	    sbuild.append("none");
	}
	return ChatColor.translateAlternateColorCodes('&', sbuild.toString());
    }

    public String listOtherPlayersFlags(String player) {
//	player = player.toLowerCase();
	String uuids = Residence.getInstance().getPlayerUUIDString(player);
	StringBuilder sbuild = new StringBuilder();
	Set<Entry<String, Map<String, Boolean>>> set = playerFlags.entrySet();
	synchronized (set) {
	    Iterator<Entry<String, Map<String, Boolean>>> it = set.iterator();
	    while (it.hasNext()) {
		Entry<String, Map<String, Boolean>> nextEnt = it.next();
		String next = nextEnt.getKey();
		if (!Residence.getInstance().getConfigManager().isOfflineMode() && !next.equals(player) && !next.equals(uuids) || Residence.getInstance().getConfigManager().isOfflineMode() && !next
		    .equals(player)) {
		    String perms = printPlayerFlags(nextEnt.getValue());
		    if (next.length() == 36) {
			String resolvedName = Residence.getInstance().getPlayerName(next);
			if (resolvedName != null) {
			    try {
				UUID uuid = UUID.fromString(next);
				this.cachedPlayerNameUUIDs.put(uuid, resolvedName);
			    } catch (Exception e) {
			    }
			    next = resolvedName;
			} else if (this.cachedPlayerNameUUIDs.containsKey(next))
			    next = this.cachedPlayerNameUUIDs.get(next);
		    }
		    if (!perms.equals("none")) {
			sbuild.append(next).append(ChatColor.WHITE).append("[").append(perms).append(ChatColor.WHITE).append("] ");
		    }
		}
	    }
	}
	return sbuild.toString();
    }

    public String listPlayersFlags() {
	StringBuilder sbuild = new StringBuilder();
	Set<Entry<String, Map<String, Boolean>>> set = playerFlags.entrySet();
	synchronized (set) {
	    Iterator<Entry<String, Map<String, Boolean>>> it = set.iterator();
	    while (it.hasNext()) {
		Entry<String, Map<String, Boolean>> nextEnt = it.next();
		String next = nextEnt.getKey();

		String perms = printPlayerFlags(nextEnt.getValue());
		if (next.length() == 36) {
		    String resolvedName = Residence.getInstance().getPlayerName(next);
		    if (resolvedName != null) {
			try {
			    UUID uuid = UUID.fromString(next);
			    this.cachedPlayerNameUUIDs.put(uuid, resolvedName);
			} catch (Exception e) {
			}
			next = resolvedName;
		    } else if (this.cachedPlayerNameUUIDs.containsKey(next))
			next = this.cachedPlayerNameUUIDs.get(next);
		}

		if (next.equalsIgnoreCase(Residence.getInstance().getServerLandname()))
		    continue;

		if (!perms.equals("none")) {
		    sbuild.append(next).append(ChatColor.WHITE).append("[").append(perms).append(ChatColor.WHITE).append("] ");
		}
	    }
	}
	return sbuild.toString();
    }

    public String listPlayersFlagsRaw(String player, String text) {
	StringBuilder sbuild = new StringBuilder();

	sbuild.append("[\"\",");
	sbuild.append("{\"text\":\"" + text + "\"}");

	Set<Entry<String, Map<String, Boolean>>> set = playerFlags.entrySet();
	synchronized (set) {
	    Iterator<Entry<String, Map<String, Boolean>>> it = set.iterator();
	    boolean random = true;
	    while (it.hasNext()) {
		Entry<String, Map<String, Boolean>> nextEnt = it.next();
		String next = nextEnt.getKey();

		String perms = printPlayerFlags(nextEnt.getValue());
		if (next.length() == 36) {
		    String resolvedName = Residence.getInstance().getPlayerName(next);
		    if (resolvedName != null) {
			try {
			    UUID uuid = UUID.fromString(next);
			    this.cachedPlayerNameUUIDs.put(uuid, resolvedName);
			} catch (Exception e) {
			}
			next = resolvedName;
		    } else if (this.cachedPlayerNameUUIDs.containsKey(next))
			next = this.cachedPlayerNameUUIDs.get(next);
		}

		if (next.equalsIgnoreCase(Residence.getInstance().getServerLandname()))
		    continue;

		if (!perms.equals("none")) {
		    sbuild.append(",");

		    if (random) {
			random = false;
			if (player.equals(next))
			    next = "&4" + next + "&r";
			else
			    next = "&2" + next + "&r";
		    } else {
			random = true;
			if (player.equals(next))
			    next = "&4" + next + "&r";
			else
			    next = "&3" + next + "&r";
		    }
		    sbuild.append(ConvertToRaw(next, perms));
		}
	    }
	}

	sbuild.append("]");
	return ChatColor.translateAlternateColorCodes('&', sbuild.toString());
    }

    public String listOtherPlayersFlagsRaw(String text, String player) {
//	player = player.toLowerCase();
	String uuids = Residence.getInstance().getPlayerUUIDString(player);
	StringBuilder sbuild = new StringBuilder();

	sbuild.append("[\"\",");
	sbuild.append("{\"text\":\"" + text + "\"}");

	Set<Entry<String, Map<String, Boolean>>> set = playerFlags.entrySet();
	synchronized (set) {
	    Iterator<Entry<String, Map<String, Boolean>>> it = set.iterator();
	    boolean random = true;
	    while (it.hasNext()) {
		Entry<String, Map<String, Boolean>> nextEnt = it.next();
		String next = nextEnt.getKey();
		if (!Residence.getInstance().getConfigManager().isOfflineMode() && !next.equals(player) && !next.equals(uuids) || Residence.getInstance().getConfigManager().isOfflineMode() && !next
		    .equals(player)) {
		    String perms = printPlayerFlags(nextEnt.getValue());
		    if (next.length() == 36) {
			String resolvedName = Residence.getInstance().getPlayerName(next);
			if (resolvedName != null) {
			    try {
				UUID uuid = UUID.fromString(next);
				this.cachedPlayerNameUUIDs.put(uuid, resolvedName);
			    } catch (Exception e) {
			    }
			    next = resolvedName;
			} else if (this.cachedPlayerNameUUIDs.containsKey(next))
			    next = this.cachedPlayerNameUUIDs.get(next);
		    }
		    if (!perms.equals("none")) {
			sbuild.append(",");

			if (random) {
			    random = false;
			    next = "&2" + next + "&r";
			} else {
			    random = true;
			    next = "&3" + next + "&r";
			}
			sbuild.append(ConvertToRaw(next, perms));
		    }
		}
	    }
	}

	sbuild.append("]");
	return ChatColor.translateAlternateColorCodes('&', sbuild.toString());
    }

    protected String ConvertToRaw(String playerName, String perms) {
	if (perms.contains(" ")) {
	    String[] splited = perms.split(" ");
	    int i = 0;
	    perms = "";
	    for (String one : splited) {
		i++;
		perms += one + " ";
		if (i >= 5) {
		    i = 0;
		    perms += "\n";
		}
	    }
	}
	return "{\"text\":\"[" + playerName + "]\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + perms + "\"}]}}}";
    }

    public String listGroupFlags() {
	StringBuilder sbuild = new StringBuilder();
	Set<String> set = groupFlags.keySet();
	synchronized (set) {
	    Iterator<String> it = set.iterator();
	    while (it.hasNext()) {
		String next = it.next();
		String perms = listGroupFlags(next);
		if (!perms.equals("none")) {
		    sbuild
			.append(next)
			.append("[")
			.append(ChatColor.DARK_AQUA)
			.append(perms)
			.append(ChatColor.RED)
			.append("] ");
		}
	    }
	}
	return sbuild.toString();
    }

    public String listGroupFlags(String group) {
	group = group.toLowerCase();
	if (groupFlags.containsKey(group)) {
	    StringBuilder sbuild = new StringBuilder();
	    Map<String, Boolean> get = groupFlags.get(group);
	    Set<Entry<String, Boolean>> set = get.entrySet();
	    synchronized (get) {
		Iterator<Entry<String, Boolean>> it = set.iterator();
		while (it.hasNext()) {
		    Entry<String, Boolean> next = it.next();
		    if (next.getValue()) {
			sbuild.append("&2").append("+").append(next.getKey());
			if (it.hasNext()) {
			    sbuild.append(" ");
			}
		    } else {
			sbuild.append("&3").append("-").append(next.getKey());
			if (it.hasNext()) {
			    sbuild.append(" ");
			}
		    }
		}
	    }
	    if (sbuild.length() == 0) {
		groupFlags.remove(group);
		sbuild.append("none");
	    }
	    return ChatColor.translateAlternateColorCodes('&', sbuild.toString());
	}
	return "none";
    }

    public void clearFlags() {
	groupFlags.clear();
	playerFlags.clear();
	cuboidFlags.clear();
    }

    public void printFlags(Player player) {
	Residence.getInstance().msg(player, lm.General_ResidenceFlags, listFlags());
	Residence.getInstance().msg(player, lm.General_PlayersFlags, listPlayerFlags(player.getName()));
	Residence.getInstance().msg(player, lm.General_GroupFlags, listGroupFlags());
	Residence.getInstance().msg(player, lm.General_OthersFlags, listOtherPlayersFlags(player.getName()));
    }

    public void copyUserPermissions(String fromUser, String toUser) {
	Map<String, Boolean> get = this.getPlayerFlags(fromUser, false);
	if (get != null) {
	    Map<String, Boolean> targ = this.getPlayerFlags(toUser, true);
	    for (Entry<String, Boolean> entry : get.entrySet()) {
		targ.put(entry.getKey(), entry.getValue());
	    }
	}
    }

    @Deprecated //Seemed to be a duplicate function of removeAllPlayerFlags()... deprecating
    public void clearPlayersFlags(String user) {
	this.removeAllPlayerFlags(user);
    }

    public void setParent(FlagPermissions p) {
	parent = p;
    }

    public FlagPermissions getParent() {
	return parent;
    }

}
