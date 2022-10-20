package me.lagbug.emailer.spigot.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.OfflinePlayer;

import me.lagbug.emailer.spigot.Emailer;
import me.lagbug.emailer.spigot.common.utils.general.UpdateChecker.UpdateResult;

public class PlaceholderManager {

	private static final Emailer plugin = Emailer.getPlugin(Emailer.class);
	private static final List<Placeholder> placeholders = new ArrayList<>();

	public static List<Placeholder> get() {
		if (!placeholders.isEmpty()) {
			placeholders.clear();
		}

		// Plugin
		add("%plugin_author%", plugin.getDescription().getAuthors().get(0));
		add("%plugin_version%", plugin.getDescription().getVersion());
		add("%version_status%", plugin.updateResult == UpdateResult.FOUND ? "Outdated" : "Latest");
		return placeholders;
	}
	
	public static List<Placeholder> get(OfflinePlayer player) {
		get();

		// Plugin
		Checker checker = new Checker(player.getUniqueId());
		add("%player_linked%", checker.isVerified());
		add("%player_email%", checker.getEmail());
		return placeholders;
	}

	public static String formatTime(int secs) {
		int remainder = secs % 86400;

		int days = secs / 86400;
		int hours = remainder / 3600;
		int minutes = (remainder / 60) - (hours * 60);
		int seconds = (remainder % 3600) - (minutes * 60);

		if (days > 0) {
			return days + "d " + hours + "h " + minutes + "m " + seconds + "s";
		} else if (hours > 0) {
			return hours + "h " + minutes + "m " + seconds + "s";
		} else if (minutes > 0) {
			return minutes + "m " + seconds + "s";
		} else {
			return seconds + "s";
		}
	}

	private static void add(String key, String value) {
		placeholders.add(new Placeholder(key, value));
	}
	
	private static void add(String key, Object value) {
		placeholders.add(new Placeholder(key, value.toString()));
	}

	public static class Placeholder {
		private String key;
		private String value;

		public Placeholder(String key, String value) {
			setKey(key);
			setValue(value);
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}
}
