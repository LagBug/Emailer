package me.lagbug.emailer.spigot.commands.subcommands;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.lagbug.emailer.spigot.Emailer;
import me.lagbug.emailer.spigot.common.commands.SubCommand;
import me.lagbug.emailer.spigot.utils.Permissions;
import me.lagbug.emailer.spigot.utils.PlayerDataCache;

public class Search extends SubCommand {

	private final Emailer plugin = Emailer.getPlugin(Emailer.class);

	public Search() {
		super("search:check", Permissions.SEARCH);
	}

	@Override
	public void onCommand(CommandSender player, String[] args) {

		if (args.length <= 1) {
			player.sendMessage(plugin.getMessage("errors.wrongUsage").replace("%usage%", "/emailer search <player/email>"));
			return;
		}

		Player p = Bukkit.getPlayer(args[1]);
		Map<String, String> verified = PlayerDataCache.getVerifiedData();

		if (p == null) {
			boolean found = false;
			OfflinePlayer match = null;

			for (String email : verified.values()) {
				if (email.equalsIgnoreCase(args[1])) {
					for (String uuid : verified.keySet()) {
						if (verified.get(uuid).equals(email)) {
							match = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
							found = true;
							break;		
						}
					}
				}
			}

			if (found) {
				player.sendMessage(plugin.getMessage("commands.search.found").replace("%input%", args[1]).replace("%output%",
						match.getName()));
				return;
			}

			player.sendMessage(plugin.getMessage("commands.search.notFound").replace("%input%", args[1]));
			return;
		}

		if (verified.containsKey(p.getUniqueId().toString())) {
			player.sendMessage(plugin.getMessage("commands.search.found").replace("%input%", args[1]).replace("%output%",
					verified.get(p.getUniqueId().toString())));
			return;
		}

		player.sendMessage(plugin.getMessage("commands.search.notFound").replace("%input%", p.getName()));
	}

}