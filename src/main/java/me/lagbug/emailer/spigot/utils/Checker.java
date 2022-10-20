package me.lagbug.emailer.spigot.utils;

import java.util.Map;
import java.util.UUID;

import me.lagbug.emailer.global.EmailAddress;

/**
 * This class contains useful methods to get if a player is verified or do
 * actions like verify/unverify the player.
 *
 * @version 1.0
 */
public class Checker {

	// The required information for the player
	private final String uuid;
	private final EmailAddress address;

	// The constructor using a UUID as a string
	public Checker(String uuid, EmailAddress address) {
		this.uuid = uuid;
		this.address = address;
	}
	
	// The constructor to set the UUID
	public Checker(UUID uuid, EmailAddress address) {
		this(uuid.toString(), address);
	}
	
	public Checker(UUID uuid) {
		this(uuid.toString(), null);
	}

	/*
	 * This method is used to get whether a player is verified/blacklisted or not.
	 * If MYSQL is enabled, it will retrieve that through the database. Otherwise,
	 * it'll use YAML files.
	 * 
	 * @return - if the player is verified
	 */
	public boolean isVerified() {
		if (uuid != null) {
			return getPlayerData().containsKey(uuid);	
		} else if (address != null){
			return getPlayerData().containsValue(address.toString());	
		} else {
			return false;
		}
	}

	public void unVerify() {
		if (uuid != null) {
			getPlayerData().remove(uuid);
		}
	}

	public void forceVerify() {
		getPlayerData().put(uuid, address.getAddress());
	}

	public String getEmail() {
		return isVerified() ? getPlayerData().get(uuid) : "N/A";
	}
	
	private Map<String, String> getPlayerData() {
		return PlayerDataCache.getVerifiedData();
	}
	
	public static class PlayerData {
		private final String username;
		private final EmailAddress address;

		public PlayerData(String username, EmailAddress address) {
			this.username = username;
			this.address = address;
		}

		public String getUsername() {
			return username;
		}

		public EmailAddress getAddress() {
			return address;
		}
	}
}
