package me.lagbug.emailer.spigot.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import me.lagbug.emailer.spigot.Emailer;
import me.lagbug.emailer.spigot.common.utils.general.MySQL;
import me.lagbug.emailer.spigot.common.utils.util.CommonUtils;

public class PlayerDataCache {

	private static final Emailer plugin = Emailer.getPlugin(Emailer.class);

	private static final Map<String, String> verifiedData = new HashMap<>();

	public static void initiate() {
		if (!verifiedData.isEmpty()) {
			verifiedData.clear();
		}

		if (plugin.mysql) {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
				String verifiedTable = plugin.getConfigFile().getString("storage.mysql.tables.verified");

				try {
					if (MySQL.prepareStatement("SELECT * FROM " + verifiedTable + ";").executeQuery().next()) {
						ResultSet result = MySQL.executeQuery("SELECT * FROM " + verifiedTable + ";");

						while (result.next()) {
							verifiedData.put(result.getString("player_uuid"), result.getString("player_email"));
						}
					}
				} catch (SQLException ex) {
					CommonUtils.log("Exception thrown while trying to read player data from database.");
				}
			});
		} else {
			try {
				FileConfiguration verifiedFile = plugin.getVerifiedFile();

				for (String key : verifiedFile.getKeys(false)) {
					verifiedData.put(key, verifiedFile.getString(key + ".email"));
				}
			} catch (ConcurrentModificationException | NullPointerException ignored) {

			}
		}
	}

	public static void save(boolean async) {
		if (plugin.mysql) {
			if (async) {
				Bukkit.getScheduler().runTask(plugin, () -> {
					String verifiedTable = plugin.getConfigFile().getString("storage.mysql.tables.verified");

					try {
						if (MySQL.prepareStatement("SELECT * FROM " + verifiedTable + ";").executeQuery().next()) {
							ResultSet result = MySQL.executeQuery("SELECT * FROM " + verifiedTable + ";");

							while (result.next()) {
								String uuid = result.getString("player_uuid");
								if (!verifiedData.containsKey(uuid)) {
									MySQL.prepareStatement(
											"DELETE FROM " + verifiedTable + " WHERE player_uuid = '" + uuid + "';")
											.executeUpdate();
								}
							}
						}

						for (String key : verifiedData.keySet()) {
							if (!MySQL
									.prepareStatement(
											"SELECT * FROM " + verifiedTable + " WHERE player_uuid = '" + key + "';")
									.executeQuery().next()) {
								MySQL.prepareStatement(
										"INSERT INTO " + verifiedTable + " (player_uuid, player_email) VALUES('" + key
												+ "','" + verifiedData.get(key) + "');")
										.executeUpdate();
							}
						}
					} catch (SQLException ex) {
						CommonUtils.log("Exception thrown while trying to read player data from database.");
					}
				});
			} else {
				String verifiedTable = plugin.getConfigFile().getString("storage.mysql.tables.verified");

				try {
					if (MySQL.prepareStatement("SELECT * FROM " + verifiedTable + ";").executeQuery().next()) {
						ResultSet result = MySQL.executeQuery("SELECT * FROM " + verifiedTable + ";");

						while (result.next()) {
							String uuid = result.getString("player_uuid");
							if (!verifiedData.containsKey(uuid)) {
								MySQL.prepareStatement(
										"DELETE FROM " + verifiedTable + " WHERE player_uuid = '" + uuid + "';")
										.executeUpdate();
							}
						}
					}

					for (String key : verifiedData.keySet()) {
						if (!MySQL
								.prepareStatement(
										"SELECT * FROM " + verifiedTable + " WHERE player_uuid = '" + key + "';")
								.executeQuery().next()) {
							MySQL.prepareStatement(
									"INSERT INTO " + verifiedTable + " (player_uuid, player_email) VALUES('" + key
											+ "','" + verifiedData.get(key) + "');")
									.executeUpdate();
						}
					}
				} catch (SQLException ex) {
					CommonUtils.log("Exception thrown while trying to read player data from database.");
				}
			}

		} else {
			// Getting the required files
			try {
				FileConfiguration verifiedFile = plugin.getVerifiedFile();
				// Removing any un-verified players
				for (String key : verifiedFile.getKeys(false)) {
					if (!verifiedData.containsKey(key)) {
						verifiedFile.set(key, null);
					}
				}

				// Adding any new ones
				for (String key : verifiedData.keySet()) {
					String address = verifiedData.get(key);

					if (!verifiedFile.contains(key)) {
						verifiedFile.set(key + ".email", address);
					}
				}

				// Saving the files TODO
				plugin.saveFile("data/verified.yml");
			} catch (ConcurrentModificationException | NullPointerException ignored) {

			}
		}
	}

	public static Map<String, String> getVerifiedData() {
		return verifiedData;
	}
}
