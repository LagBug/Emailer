package me.lagbug.emailer.spigot;

import java.sql.SQLException;
import java.util.*;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.lagbug.emailer.global.EmailAddress;
import me.lagbug.emailer.global.EmailTemplate;
import me.lagbug.emailer.global.enums.EmailType;
import me.lagbug.emailer.spigot.api.EmailerPlaceholders;
import me.lagbug.emailer.spigot.commands.EmailerCommand;
import me.lagbug.emailer.spigot.commands.SetEmailCommand;
import me.lagbug.emailer.spigot.commands.UnsubscribeCommand;
import me.lagbug.emailer.spigot.commands.VerifyCommand;
import me.lagbug.emailer.spigot.common.utils.general.Metrics;
import me.lagbug.emailer.spigot.common.utils.general.MySQL;
import me.lagbug.emailer.spigot.common.utils.general.UpdateChecker;
import me.lagbug.emailer.spigot.common.utils.general.UpdateChecker.UpdateResult;
import me.lagbug.emailer.spigot.common.utils.util.CommonUtils;
import me.lagbug.emailer.spigot.common.utils.util.FileUtils;
import me.lagbug.emailer.spigot.events.AsyncPlayerChat;
import me.lagbug.emailer.spigot.utils.PlayerDataCache;

public class Emailer extends JavaPlugin {

	private final Map<OfflinePlayer, String> pending = new HashMap<>();
	private final Map<String, EmailTemplate> templates = new HashMap<>();
	private final Map<Player, String> editing = new HashMap<>();
	private final Map<Player, OfflinePlayer> linking = new HashMap<>();
	
	private final FileUtils fileUtils = new FileUtils();
	private FileConfiguration configFile, langFile, templatesFile;

	private final Properties properties = new Properties();
	private Session session;
	private EmailAddress email;
	private final String user = "%%__USER__%%";
	
	public UpdateResult updateResult;
	public boolean mysql;

	@Override
	public void onEnable() {
		// We register our commands
		getCommand("emailer").setExecutor(new EmailerCommand());
		getCommand("setemail").setExecutor(new SetEmailCommand());
		getCommand("auth").setExecutor(new VerifyCommand());
		getCommand("unsubscribe").setExecutor(new UnsubscribeCommand());

		// We register our events as well
		Bukkit.getPluginManager().registerEvents(new AsyncPlayerChat(), this);
		
		// We initiate everything that's required
		initiate();

		CommonUtils.forceLog(
				getDescription().getName() + " v" + getDescription().getVersion() + " has been enabled successfully",
				"Plugin licensed to [https://www.spigotmc.org/members/" + user + "/]");
		// We register the bStats Metrics
		new Metrics(this);

		// If the update checker is enabled, we schedule it
		if (configFile.getBoolean("updateChecker")) {
			new UpdateChecker(this, 66184).schedule(600);
		}
	}

	@Override
	public void onDisable() {
		// We save any unsaved data
		PlayerDataCache.save(false);
		
		// If MySQL is enabled, we close the connection
		if (mysql) {
			try {
				MySQL.getConnection().close();
			} catch (SQLException e) {
				CommonUtils.log("Could not close the MySQL connection");
			}
		}

		CommonUtils.forceLog(
				getDescription().getName() + " v" + getDescription().getVersion() + " has been disabled successfully");
	}

	public void initiate() {
		// We clear the current templates and pending verifications, in case of a plugin reload
		if (!templates.isEmpty() || !pending.isEmpty()) {
			templates.clear();
			pending.clear();
		}

		// We initiate the files needed for the plugin to run
		fileUtils.initiate(this, "config.yml", "templates.yml", "lang/en_US.yml", "guis/home.yml", "guis/recipient.yml", "guis/player_manager.yml", "guis/templates.yml", "data/verified.yml");

		// Assign each file to a variable
		configFile = getFile("config.yml");
		templatesFile = getFile("templates.yml");
		langFile = fileUtils.getFile("lang/" + configFile.getString("languageFile") + ".yml");

		// Setting some global booleans
		mysql = configFile.getString("storage.type").equals("MYSQL");

		CommonUtils.initiate(this, configFile.getBoolean("debug"));

		// If MySQL is enabled we try to connect asynchronously
		if (mysql) {
			Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
				// We initiate a new MySQL object with the login information
				MySQL.initiate(configFile.getString("storage.mysql.host"),
						configFile.getString("storage.mysql.database"),
						Collections.singletonList(configFile.getString("storage.mysql.tables.verified")),
						configFile.getString("storage.mysql.username"), configFile.getString("storage.mysql.password"),
						configFile.getString("storage.mysql.statement"), configFile.getInt("storage.mysql.port"));

				// If the connection was not successful we automatically which to FLAT file
				// support
				if (!MySQL.connect()) {
					mysql = false;
					CommonUtils.forceLog("Automatically switched to FLAT file support");
				}

				PlayerDataCache.initiate();
			});
		} else {
			PlayerDataCache.initiate();
		}
		
		//Register the PAPI extension if the plugin is enabled
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new EmailerPlaceholders(this).register();
        }

		// We register all of our email templates
		templatesFile.getConfigurationSection("emails").getKeys(false).forEach(key -> templates.put(key,
				new EmailTemplate(key,
						templatesFile.getString("emails." + key + ".subject"),
						EmailType.valueOf(templatesFile.getString("emails." + key + ".type")),
						templatesFile.getString("emails." + key + ".html"),
						templatesFile.getStringList("emails." + key + ".text"))));

		registerTasks();
		
		// Set the properties for email sending
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", getConfigFile().getString("server.host"));
		properties.put("mail.smtp.port", getConfigFile().getInt("server.port"));

		// Set the email address by this specific user
		email = new EmailAddress(getConfigFile().getString("email.address"));

		// And create a new session with the specified email & password
		setSession(Session.getInstance(properties, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(email.getAddress(), getConfigFile().getString("email.password"));
			}
		}));
	}

	private void registerTasks() {
		// Register any tasks required
		Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> PlayerDataCache.save(true), 20 * 5, 20L * 60 * getConfigFile().getInt("saveDataEvery"));
	}
	
	public FileConfiguration getConfigFile() {
		return configFile;
	}

	public FileConfiguration getLangFile() {
		return langFile;
	}

	public FileConfiguration getVerifiedFile() {
		return fileUtils.getFile("data/verified.yml");
	}

	public FileConfiguration getTemplatesFile() {
		return fileUtils.getFile("templates.yml");
	}
	
	public FileConfiguration getTemplatesGuiFile() {
		return fileUtils.getFile("guis/templates.yml");
	}

	public YamlConfiguration getFile(String path) {
		return fileUtils.getFile(path);
	}

	public String getMessage(String path) {
		return langFile.contains(path)
				? ChatColor.translateAlternateColorCodes('&',
						langFile.getString(path).replace("%prefix%", configFile.getString("prefix")))
				: "Error: The specified path (lang/../" + path + ") could not be found.";
	}

	public Map<OfflinePlayer, String> getPending() {
		return pending;
	}

	public Map<Player, String> getEditing() {
		return editing;
	}
	
	public Map<Player, OfflinePlayer> getLinking() {
		return linking;
	}
	
	public Map<String, EmailTemplate> getEmailTemplates() {
		return templates;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public EmailAddress getEmail() {
		return email;
	}

	public void saveFile(String path) {
		fileUtils.saveFile(path);
	}

	public void reloadFiles() {
		fileUtils.initiate(this);
	}
}