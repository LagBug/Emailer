package me.lagbug.emailer.spigot.api;

import org.bukkit.OfflinePlayer;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.lagbug.emailer.spigot.Emailer;
import me.lagbug.emailer.spigot.utils.Checker;

public class EmailerPlaceholders extends PlaceholderExpansion {

	@SuppressWarnings("unused")
	private final Emailer plugin;

	public EmailerPlaceholders(Emailer plugin) {
	        this.plugin = plugin;
	    }

	@Override
	public String getAuthor() {
		return "LagBug";
	}

	@Override
	public String getIdentifier() {
		return "player";
	}

	@Override
	public String getVersion() {
		return "1.0.0";
	}

	@Override
	public boolean persist() {
		return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
	}

	@Override
	public String onRequest(OfflinePlayer player, String params) {
		if (params.equalsIgnoreCase("player_email")) {
			return new Checker(player.getUniqueId()).getEmail();
		}

		return null; // Placeholder is unknown by the Expansion
	}

}
