package com.reztek.modules.GuardianControl;

import org.json.JSONObject;

import com.reztek.utils.BotUtils;

public abstract class BungieHashDefines {
	static class BaseHashReturn {
		protected String _hash;
		protected String _name;
		protected String _icon;
		public String getName() {
			return _name;
		}
		public String getIcon() {
			return _icon;
		}
		public String getHash() {
			return _hash;
		}
	}
	static class StepsHashReturn extends BaseHashReturn {
		protected String _description;
		public String getDescription() {
			return _description;
		}
	}
	
	private static JSONObject SubclassHashDefinitions = null;
	public static BaseHashReturn GetSubclassForHash(String Hash) {
		BaseHashReturn hr = new BaseHashReturn();
		if (SubclassHashDefinitions == null) {
			SubclassHashDefinitions = new BotUtils.JsonConverter("DestinySubclassDefinition.json", BungieHashDefines.class).getJsonObject();
		}
		hr._hash = Hash;
		hr._name = SubclassHashDefinitions.getJSONObject(Hash).getString("name");
		hr._icon = SubclassHashDefinitions.getJSONObject(Hash).getString("icon");
		return hr;
	}
	
	private static JSONObject WeaponHashDefinitions = null;
	public static BaseHashReturn GetWeaponForHash(String Hash) {
		BaseHashReturn hr = new BaseHashReturn();
		if (WeaponHashDefinitions == null) {
			WeaponHashDefinitions = new BotUtils.JsonConverter("DestinyWeaponDefinition.json", BungieHashDefines.class).getJsonObject();
		}
		hr._hash = Hash;
		hr._name = WeaponHashDefinitions.getJSONObject(Hash).getString("name");
		hr._icon = WeaponHashDefinitions.getJSONObject(Hash).getString("icon");
		return hr;
	}
	
	private static JSONObject StepsHashDefinitions = null;
	public static StepsHashReturn GetStepForHash(String Hash) {
		StepsHashReturn sr = new StepsHashReturn();
		if (StepsHashDefinitions == null) {
			StepsHashDefinitions = new BotUtils.JsonConverter("DestinyStepsDefinition.json", BungieHashDefines.class).getJsonObject();
		}
		sr._hash = Hash;
		sr._name = StepsHashDefinitions.getJSONObject(Hash).getString("n");
		sr._icon = StepsHashDefinitions.getJSONObject(Hash).getString("i");
		sr._description = StepsHashDefinitions.getJSONObject(Hash).getString("d");
		return sr;
	}
	
	private static JSONObject TalentGridDefinitions = null;
	public static String getStepHashForTalentGridNode(String talentGridHash, int node, int stepIndex) {
		if (TalentGridDefinitions == null) {
			TalentGridDefinitions = new BotUtils.JsonConverter("DestinyTalentGridDefinition.json", BungieHashDefines.class).getJsonObject();
		}
		return String.valueOf(TalentGridDefinitions.getJSONArray(talentGridHash).getJSONObject(node).getJSONArray("s").getBigInteger(stepIndex));
	}
}
