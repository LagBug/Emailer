package me.lagbug.emailer.spigot.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import me.lagbug.emailer.global.EmailAddress;
import me.lagbug.emailer.spigot.Emailer;
import me.lagbug.emailer.spigot.common.commands.SubCommand;
import me.lagbug.emailer.spigot.utils.Checker;
import me.lagbug.emailer.spigot.utils.Permissions;

public class ForceVerify extends SubCommand {

	private final Emailer plugin = Emailer.getPlugin(Emailer.class);

	public ForceVerify() {
		super("forceverify:verify:f:v", Permissions.FORCE_VERIFY);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onCommand(CommandSender sender, String[] args) {
		// If the usage is wrong we let the player know and return here
		if (args.length <= 2) {
			sender.sendMessage(
					plugin.getMessage("errors.wrongUsage").replace("%usage%", "/emailer forceVerify <player> <email>"));
			return;
		}

		// We get the player to be verified and the email
		OfflinePlayer toVerify = Bukkit.getOfflinePlayer(args[1]);
		EmailAddress address = new EmailAddress(args[2]);

		// We initiate a Checker object
		Checker checker = new Checker(toVerify.getUniqueId(), address);

		// If the player is already verified we let them know and return here
		if (checker.isVerified()) {
			sender.sendMessage(plugin.getMessage("commands.verify.already").replace("%player%", sender.getName()));
			return;
		}

		// Otherwise we force verify them and send a success message
		checker.forceVerify();
		sender.sendMessage(plugin.getMessage("commands.verify.force").replace("%address%", address.getAddress())
				.replace("%player%", sender.getName()));
	}

}