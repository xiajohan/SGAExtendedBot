package com.reztek.modules.GuardianControl;

import java.awt.Color;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.reztek.modules.GuardianControl.BungieHashDefines.StepsHashReturn;
import com.reztek.secret.GlobalDefs;
import com.reztek.utils.BotUtils;

public class Guardian {
	
	public static final String PLATFORM_XB = "1";
	public static final String PLATFORM_PS = "2";
	public static final String PLATFORM_ALL = "All";
	
	private static final String BUNGIE_API_KEY = GlobalDefs.BUNGIE_API_KEY;
	private static final String BUNGIE_BASE_URL = "https://www.bungie.net/Platform/Destiny";
	private static final String BUNGIE_SEARCH_URL = "/SearchDestinyPlayer/";
	private static final String BUNGIE_ACCOUNT_URL = "/Account/";
	private static final String BUNGIE_BASE_IMAGES = "https://www.bungie.net";
	private static final String GUARDIAN_API_BASE_URL = "https://api.guardian.gg";
	private static final String GUARDIAN_API_ELO = "/elo/";
	private static final String GUARDIAN_API_FIRETEAM = "/fireteam/14/";
	private static final String DTR_API_BASE_URL = "https://api.destinytrialsreport.com";
	private static final String DTR_API_PLAYER = "/player/";
	private static final String DTR_API_THISWEEKWEPS = "/lastWeapons/";
	private static final String DTR_API_WEPSTATS = "/weaponStats/";
	
	private static final int WEAPON_PRIMARY = 1;
	private static final int WEAPON_SPECIAL = 2;
	private static final int WEAPON_HEAVY = 3;
	
	public class GuardianWeaponStats {
		private String p_WepName = null;
		private String p_WepKills = null;
		private String p_WepHeadshots = null;
		private String p_WepIcon = null;
		private DamageTypeReturn p_dtr = null;
		private ArrayList<GuardianWeaponPerk> p_PerkList = new ArrayList<GuardianWeaponPerk>();
		public String getWeaponName() { return p_WepName; }
		public String getWeaponKills(){return p_WepKills; }
		public String getWeaponHeadshots() { return p_WepHeadshots; }
		public String getWepIcon() { return p_WepIcon; }
		public final ArrayList <GuardianWeaponPerk> getWepPerks() { return p_PerkList; }
		public String getHeadshotPercentage() {
			DecimalFormat df = new DecimalFormat();
			df.setMaximumFractionDigits(2); 
			float hsPerc = Float.valueOf(p_WepHeadshots) / Float.valueOf(p_WepKills);
			hsPerc *= 100;
			return df.format(hsPerc) + "%";
		}
		public DamageTypeReturn getDamageType() {
			return p_dtr;
		}
	}
	
	public class DamageTypeReturn {
		private String _dtId;
		private String _dtName;
		private Color _dtColor;
		private String _dtIcon;
		public String getId() { return _dtId; }
		public String getName() { return _dtName; }
		public Color getColor() { return _dtColor; }
		public String getDamageIcon() { return _dtIcon; }
		private DamageTypeReturn (String id, String name, Color color, String icon) {
			_dtId = id;
			_dtName = name;
			_dtColor = color;
			_dtIcon = icon;
		}
	}
	
	public class GuardianWeaponPerk {
		private String p_PerkName = null;
		private String p_PerkDesc = null;
		private String p_PerkIcon = null;
		public String getPerkName() { return p_PerkName; }
		public String getPerkDesc() { return p_PerkDesc; }
		public String getPerkIcon() { return p_PerkIcon; }
		public GuardianWeaponPerk(String perkName, String perkDesc, String perkIcon) {
			p_PerkName = perkName;
			p_PerkDesc = perkDesc;
			p_PerkIcon = perkIcon;
		}
	}
	
	public static class PlatformCodeFromNicknameData {
		protected String _nickName = null;
		protected String _platform = Guardian.PLATFORM_ALL;
		protected boolean _usesTag = false;
		public String getNickname() { return _nickName; }
		public String getPlatform() { return _platform; }
		public boolean usesTag() { return _usesTag; }
	}
	
	// -- BUNGIE
	private String p_id = null;
	private String p_name = null;
	private String p_platform = null;
	private String p_characterIdLastPlayed = null;
	private String p_grimoireScore = null;
	private String p_characterLastPlayedSubclassHash = null;
	private String p_currentEmblemPath = null;
	private GuardianWeaponStats p_currentPrimaryWep = new GuardianWeaponStats();
	private GuardianWeaponStats p_currentSpecialWep = new GuardianWeaponStats();
	private GuardianWeaponStats p_currentHeavyWep = new GuardianWeaponStats();
	
	// -- Guardian GG
	private String p_rumbleELO = null;
	private String p_rumbleRank = null;
	private String p_trialsELO = null;
	private String p_trialsRank = null;
	private String p_lighthouseCount = null;
	
	// -- Destiny Trials Report
	private String p_thisWeekTrialsFlawless = null;
	private String p_thisWeekTrialsMatches = null;
	private String p_thisWeekTrialsLosses = null;
	private String p_thisWeekTrialsKills = null;
	private String p_thisWeekTrialsDeaths = null;
	private float  p_thisWeekTrialsKD = 0;
	private String p_thisYearTrialsMatches = null;
	private String p_thisYearTrialsKills = null;
	private String p_thisYearTrialsDeaths = null;
	private float  p_thisYearTrialsKD = 0;
	private ArrayList<GuardianWeaponStats> p_thisMapWepStats = new ArrayList<GuardianWeaponStats>();
	private ArrayList<GuardianWeaponStats> p_thisWeekMapWepStats = new ArrayList<GuardianWeaponStats>();
	
	private Guardian() {
		
	}
	
	public static Guardian guardianFromNickname(String guardianName) {
		PlatformCodeFromNicknameData pc = platformCodeFromNickname(guardianName);
		return guardianFromName(pc.getNickname(), pc.getPlatform());
	}
	
	public static Guardian guardianFromName(String guardianName, String platform) {
		Guardian g = new Guardian();
		if (!g.findGuardianIdOnBungie(guardianName,platform)) return null;
		return Guardian.guardianFromMembershipId(g.getId(), g.getName(), g.getPlatform(), g);
	}
	
	public static Guardian guardianFromMembershipId(String membershipId, String name, String platform) {
		Guardian g = new Guardian();
		return guardianFromMembershipId(membershipId, name, platform, g);
	}
	
	protected static Guardian guardianFromMembershipId(String membershipId, String name, String platform, Guardian g) {		
		g.p_id = membershipId;
		g.p_name = name;
		g.p_platform = platform;
		
		g.getGuardianGG();
		g.getGuardianDTR();
		
		return g;
	}
	
	public static String platformCodeFromCommand(String command) {
		String platform = Guardian.PLATFORM_ALL;
		for (String platCode : command.split("-")) {
			switch (platCode) {
				case "ps":
					platform = Guardian.PLATFORM_PS;
					break;
				case "xb":
					platform = Guardian.PLATFORM_XB;
					break;
				default:
					platform = Guardian.PLATFORM_ALL;
					break;
			}
		}
		return platform;
	}
	
	public static PlatformCodeFromNicknameData platformCodeFromNickname(String nickname) {
		PlatformCodeFromNicknameData ret = new PlatformCodeFromNicknameData();
		ret._nickName = nickname;
		
		String[] nickSep = nickname.split("]");
		if (nickSep.length > 1) {
			// a Tag proceeds their name try and get platform
			ret._usesTag = true;
			String[] nickCheckSlash = nickSep[0].split("\\/");
			String tag = null;
			if (nickCheckSlash.length > 1) {
				// 2 tags in there name seperated by / - just use first one
				tag = nickCheckSlash[0].substring(1);
			} else {
				tag = nickSep[0].substring(1);
			}
			if (tag.equalsIgnoreCase("PS4")) ret._platform = Guardian.PLATFORM_PS;
			if (tag.equalsIgnoreCase("XB1")) ret._platform = Guardian.PLATFORM_XB;
			ret._nickName = nickSep[1].trim();
		}
		
		return ret;
	}
	
	public ArrayList<HashMap<String,String>> getTrialsFireteamMembershipId() {
		ArrayList<HashMap<String,String>> fireteam =  new ArrayList<HashMap<String,String>>();
		JSONArray ob = new JSONObject("{\"GGArray\":" + BotUtils.getJSONString(GUARDIAN_API_BASE_URL + GUARDIAN_API_FIRETEAM + getId(), null) + "}").getJSONArray("GGArray");
		
		for (int x = 0; x < ob.length(); ++x) {
			JSONObject itObj = ob.getJSONObject(x);
			if (itObj.getString("membershipId").equals(getId())) continue;
			HashMap<String,String> hm = new HashMap<String,String>();
			hm.put("membershipId", itObj.getString("membershipId"));
			hm.put("name", itObj.getString("name"));
			hm.put("platform",String.valueOf(itObj.getInt("membershipType")));
			fireteam.add(hm);
		}
		return fireteam;
	}
	
	private boolean getGuardianDTR() {
		JSONObject ob = new JSONObject("{\"DTRArray\":" + BotUtils.getJSONString(DTR_API_BASE_URL + DTR_API_PLAYER + p_id,null) + "}").getJSONArray("DTRArray").getJSONObject(0);
		JSONObject flYearArray = null;
		
		try {
			flYearArray = ob.getJSONObject("flawless").getJSONObject("years");
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return false;
		}
		
		String[] years = JSONObject.getNames(flYearArray);
		int flawlessCount  = 0;
		
		for (String year : years) {
			flawlessCount += flYearArray.optJSONObject(year).getInt("count");
		}
		
		p_lighthouseCount = String.valueOf(flawlessCount);
		
		JSONObject thisWeekOb = ob.getJSONArray("thisWeek").getJSONObject(0);
		
		p_thisWeekTrialsMatches =  String.valueOf(thisWeekOb.getInt("matches"));
		p_thisWeekTrialsFlawless =  String.valueOf(thisWeekOb.getInt("flawless"));
		p_thisWeekTrialsLosses = String.valueOf(thisWeekOb.getInt("losses"));
		p_thisWeekTrialsKills = String.valueOf(thisWeekOb.getInt("kills"));
		p_thisWeekTrialsDeaths = String.valueOf(thisWeekOb.getInt("deaths"));
		p_thisWeekTrialsKD = (Float.valueOf(p_thisWeekTrialsKills) / Float.valueOf(p_thisWeekTrialsDeaths));
		
		p_thisYearTrialsKills = String.valueOf(ob.getInt("kills"));
		p_thisYearTrialsDeaths =  String.valueOf(ob.getInt("deaths"));
		p_thisYearTrialsMatches = String.valueOf(ob.getInt("match_count"));
		p_thisYearTrialsKD = (Float.valueOf(p_thisYearTrialsKills) / Float.valueOf(p_thisYearTrialsDeaths));
		
		JSONArray thisMapWeps = ob.getJSONArray("thisMapWeapons");
		for (int x = 0; x < thisMapWeps.length(); ++x) {
			JSONObject wep = thisMapWeps.getJSONObject(x);
			GuardianWeaponStats gws = new GuardianWeaponStats();
			gws.p_WepName = wep.getString("itemTypeName");
			gws.p_WepKills = String.valueOf(wep.getInt("sum_kills"));
			gws.p_WepHeadshots = String.valueOf(wep.getInt("sum_headshots"));
			p_thisMapWepStats.add(gws);
		}
		
		JSONArray thisWeekMapWeps = new JSONObject("{\"DTRArray\":" + BotUtils.getJSONString(DTR_API_BASE_URL + DTR_API_THISWEEKWEPS + p_characterIdLastPlayed,null) + "}").getJSONArray("DTRArray");
		for (int x = 0; x < thisWeekMapWeps.length(); ++x) {
			JSONObject wep = thisWeekMapWeps.getJSONObject(x);
			GuardianWeaponStats gws = new GuardianWeaponStats();
			gws.p_WepName = wep.getString("itemTypeName");
			gws.p_WepKills = String.valueOf(wep.getInt("sum_kills"));
			gws.p_WepHeadshots = String.valueOf(wep.getInt("sum_headshots"));
			p_thisWeekMapWepStats.add(gws);
		}
		
		return true;
	}
	
	private boolean getGuardianGG() {
		try {
			JSONArray ob = new JSONObject("{\"GGArray\":" + BotUtils.getJSONString(GUARDIAN_API_BASE_URL + GUARDIAN_API_ELO + p_id, null) + "}").getJSONArray("GGArray");
			for (int x = 0; x < ob.length(); ++x) {
				JSONObject itObj = ob.getJSONObject(x);
				if (itObj.getInt("mode") == 13) { // rumble
					p_rumbleELO = String.valueOf(itObj.getBigDecimal("elo").setScale(0, BigDecimal.ROUND_HALF_EVEN));
					p_rumbleRank = (itObj.getInt("rank") > 0 ? String.valueOf(itObj.getInt("rank")) : "N/A");
				} else if (itObj.getInt("mode") == 14) { // trials
					p_trialsELO = String.valueOf(itObj.getBigDecimal("elo").setScale(0, BigDecimal.ROUND_HALF_EVEN));
					p_trialsRank = (itObj.getInt("rank") > 0 ? String.valueOf(itObj.getInt("rank")) : "N/A");
				}
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
			return false;
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private boolean findGuardianIdOnBungie(String guardianName, String platform) {
		HashMap<String,String> props = new HashMap<String,String>();
		props.put("X-API-Key", BUNGIE_API_KEY);
		
		try {
			JSONObject ob = new JSONObject (BotUtils.getJSONString(BUNGIE_BASE_URL + BUNGIE_SEARCH_URL + platform + "/" + guardianName + "/", props));
			p_id = ob.getJSONArray("Response").getJSONObject(0).getString("membershipId");
			p_name = ob.getJSONArray("Response").getJSONObject(0).getString("displayName");
			p_platform = String.valueOf(ob.getJSONArray("Response").getJSONObject(0).getInt("membershipType"));
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return false;
		}
		
		try {
			JSONObject ob = new JSONObject(BotUtils.getJSONString(BUNGIE_BASE_URL + "/" + p_platform + BUNGIE_ACCOUNT_URL + p_id + "/", props));
			p_grimoireScore = String.valueOf(ob.getJSONObject("Response").getJSONObject("data").getInt("grimoireScore"));
			p_characterIdLastPlayed = ob.getJSONObject("Response").getJSONObject("data").getJSONArray("characters").getJSONObject(0).getJSONObject("characterBase").getString("characterId");
			p_characterLastPlayedSubclassHash = String.valueOf(ob.getJSONObject("Response").getJSONObject("data").getJSONArray("characters").getJSONObject(0).getJSONObject("characterBase").getJSONObject("peerView").getJSONArray("equipment").getJSONObject(0).getBigInteger("itemHash"));
			p_currentEmblemPath = ob.getJSONObject("Response").getJSONObject("data").getJSONArray("characters").getJSONObject(0).getString("emblemPath");
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return false;
		}
		
		p_currentPrimaryWep = currentWeapon(WEAPON_PRIMARY);
		p_currentSpecialWep = currentWeapon(WEAPON_SPECIAL);
		p_currentHeavyWep   = currentWeapon(WEAPON_HEAVY);

		return true;
	}
	
	private GuardianWeaponStats currentWeapon(int weapon) {
		GuardianWeaponStats gw = new GuardianWeaponStats();
		
		HashMap<String,String> props = new HashMap<String,String>();
		props.put("X-API-Key", BUNGIE_API_KEY);
		
		try {
			JSONObject ob = new JSONObject(BotUtils.getJSONString(BUNGIE_BASE_URL + "/" + p_platform + BUNGIE_ACCOUNT_URL + p_id + "/Character/" + p_characterIdLastPlayed + "/Inventory/", props));
			
			//primary info
			String pItemHash = String.valueOf(ob.getJSONObject("Response").getJSONObject("data").getJSONObject("buckets").getJSONArray("Equippable").getJSONObject(weapon).getJSONArray("items").getJSONObject(0).getBigInteger("itemHash"));
			String pTalentGridHash = String.valueOf(ob.getJSONObject("Response").getJSONObject("data").getJSONObject("buckets").getJSONArray("Equippable").getJSONObject(weapon).getJSONArray("items").getJSONObject(0).getBigInteger("talentGridHash"));
			JSONArray pNodes = ob.getJSONObject("Response").getJSONObject("data").getJSONObject("buckets").getJSONArray("Equippable").getJSONObject(weapon).getJSONArray("items").getJSONObject(0).getJSONArray("nodes");
			String pDT = String.valueOf(ob.getJSONObject("Response").getJSONObject("data").getJSONObject("buckets").getJSONArray("Equippable").getJSONObject(weapon).getJSONArray("items").getJSONObject(0).getInt("damageType"));
			switch (pDT) {
			case "2":
				gw.p_dtr = new DamageTypeReturn(pDT, BungieHashDefines.GetStepForHash("2688431654").getName(), Color.CYAN, BUNGIE_BASE_IMAGES + BungieHashDefines.GetStepForHash("2688431654").getIcon());
				break;
			case "3":
				gw.p_dtr = new DamageTypeReturn(pDT, BungieHashDefines.GetStepForHash("1975859941").getName(), new Color(0xFF, 0x90, 0x00), BUNGIE_BASE_IMAGES + BungieHashDefines.GetStepForHash("1975859941").getIcon());
				break;
			case "4":
				gw.p_dtr = new DamageTypeReturn(pDT, BungieHashDefines.GetStepForHash("472357138").getName(), new Color(0x90, 0x00, 0xFF), BUNGIE_BASE_IMAGES + BungieHashDefines.GetStepForHash("472357138").getIcon());
				break;
			case "1":
			default:
				gw.p_dtr = new DamageTypeReturn(pDT, BungieHashDefines.GetStepForHash("643689081").getName(), Color.GRAY, BUNGIE_BASE_IMAGES + BungieHashDefines.GetStepForHash("643689081").getIcon());
			}
			gw.p_WepName = BungieHashDefines.GetWeaponForHash(pItemHash).getName();
			gw.p_WepIcon = BUNGIE_BASE_IMAGES + BungieHashDefines.GetWeaponForHash(pItemHash).getIcon();
			
			for (int x = 0; x < pNodes.length(); ++x) {
				if (pNodes.getJSONObject(x).getBoolean("isActivated") && !pNodes.getJSONObject(x).getBoolean("hidden")) {
					StepsHashReturn shr = BungieHashDefines.GetStepForHash(BungieHashDefines.getStepHashForTalentGridNode(pTalentGridHash, x, pNodes.getJSONObject(x).getInt("stepIndex")));
					// Add Ignores here
					if (shr.getHash().equals("1270552711")) continue;
					if (shr.getHash().equals("643689081")) continue;
					if (shr.getHash().equals("472357138")) continue;
					if (shr.getHash().equals("1975859941")) continue;
					if (shr.getHash().equals("2688431654")) continue;
					gw.p_PerkList.add(new GuardianWeaponPerk(shr.getName(), shr.getDescription(), BUNGIE_BASE_IMAGES + shr.getIcon()));
				}
			}
			
			// get Stats for weapon
			try {
				JSONObject ws = new JSONObject("{\"DTRArray\":" + BotUtils.getJSONString(DTR_API_BASE_URL + DTR_API_WEPSTATS + p_id + "/" + pItemHash,null) + "}").getJSONArray("DTRArray").getJSONObject(0);
				gw.p_WepKills = ws.getString("kills");
				gw.p_WepHeadshots = ws.getString("headshots");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return gw;
	}
	
	// --------- Getters
	
	public String getId() {
		return p_id;
	}
	
	public String getName() {
		return p_name;
	}
	
	public String getRumbleELO() {
		return p_rumbleELO;
	}
	
	public String getGrimoireScore() {
		return p_grimoireScore;
	}
	
	public String getLastPlayedCharacterId() {
		return p_characterIdLastPlayed;
	}
	
	public String getTrialsELO() {
		return p_trialsELO;
	}
	
	public String getRumbleRank() {
		return p_rumbleRank;
	}
	
	public String getTrialsRank() {
		return p_trialsRank;
	}
	
	public String getLighthouseCount() {
		return p_lighthouseCount;
	}
	
	public String getPlatform() {
		return p_platform;
	}
	
	public String getThisWeekTrialsFlawless() {
		return p_thisWeekTrialsFlawless;
	}
	
	public String getThisWeekTrialsDeaths() {
		return p_thisWeekTrialsDeaths;
	}
	
	public String getThisWeekTrialsKills() {
		return p_thisWeekTrialsKills;
	}
	
	public String getThisWeekTrialsMatches() {
		return p_thisWeekTrialsMatches;
	}
	
	public String getThisWeekTrialsLosses() {
		return p_thisWeekTrialsLosses;
	}
	
	public String getThisWeekTrialsKD () {
		if (Double.isNaN(p_thisWeekTrialsKD)) return "N/A";
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2); 
		return df.format(p_thisWeekTrialsKD);
	}
	
	public String getThisYearTrialsKills() {
		return p_thisYearTrialsKills;
	}
	
	public String getThisYearTrialsDeaths() {
		return p_thisYearTrialsDeaths;
	}
	
	public String getThisYearTrialsKD () {
		if (Double.isNaN(p_thisYearTrialsKD)) return "N/A";
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		return df.format(p_thisYearTrialsKD);
	}
	
	public String getThisYearTrialsMatches() {
		return p_thisYearTrialsMatches;
	}
	
	public final ArrayList<GuardianWeaponStats> getThisWeekMapWeaponStats() {
		return p_thisWeekMapWepStats;
	}
	
	public final ArrayList<GuardianWeaponStats> getThisMapWeaponStats() {
		return p_thisMapWepStats;
	}
	
	public String getCharacterLastPlayedSubclassHash() {
		return p_characterLastPlayedSubclassHash;
	}
	
	public String getCharacterLastPlayedSubclass() {
		return BungieHashDefines.GetSubclassForHash(p_characterLastPlayedSubclassHash).getName();
	}
	
	public String getCharacterLaspPlayedSubclassIcon() {
		return BUNGIE_BASE_IMAGES + BungieHashDefines.GetSubclassForHash(p_characterLastPlayedSubclassHash).getIcon();
	}
	
	public String getCharacterLastPlayedEmblem() {
		return BUNGIE_BASE_IMAGES + p_currentEmblemPath;
	}
	
	public final GuardianWeaponStats getCurrentPrimaryWep() {
		return p_currentPrimaryWep;
	}
	
	public final GuardianWeaponStats getCurrentSpecialWep() {
		return p_currentSpecialWep;
	}
	
	public final GuardianWeaponStats getCurrentHeavyWep() {
		return p_currentHeavyWep;
	}
}
