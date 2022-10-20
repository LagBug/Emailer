package me.lagbug.emailer.spigot.common.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.lagbug.emailer.spigot.Emailer;
import me.lagbug.emailer.spigot.guis.HomeInventory;
import me.lagbug.emailer.spigot.utils.Permissions;

public abstract class SpigotCommand implements CommandExecutor {

	private final Emailer plugin = Emailer.getPlugin(Emailer.class);
	
	private final String permission;
	private String noPermissions;
	private String usage;
	private final int reqArgs;
	private final List<SubCommand> subCommands = new ArrayList<>();

	public SpigotCommand(String permission, int reqArgs, SubCommand... subCommands) {
		this.permission = permission;
		this.reqArgs = reqArgs - 1;

		this.subCommands.addAll(Arrays.asList(subCommands));
	}

	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
		if (!sender.hasPermission(permission)) {
			sender.sendMessage(plugin.getMessage("errors.noPermissions"));
			return false;
		}

		if (reqArgs <= -1) {
			onCommand(sender, args);
			return false;
		}

		if (args.length <= reqArgs) {
			if (sender instanceof Player && permission.equals(Permissions.USE)) {
				new HomeInventory().openInventory((Player) sender);
				return false;
			}
			
			try {
				sender.sendMessage(plugin.getMessage("errors.wrongUsage").replace("%usage%", "/" + usage + " <" + getSubCommandNames() + ">"));
			} catch (NullPointerException ex) {
				sender.sendMessage(plugin.getMessage("errors.wrongUsage").replace("%usage%", usage));
			}
			return false;
		}

		boolean found = false;

		try {
			for (SubCommand subCommand : this.subCommands) {
				if (subCommand.getNames().contains(args[0].toLowerCase())) {
					found = true;

					if (!sender.hasPermission(subCommand.getPermission()) || !sender.hasPermission(permission)) {
						sender.sendMessage(noPermissions);
						return false;
					}

					subCommand.sender = sender;
					subCommand.onCommand(sender, args);
					return false;
				}
			}

			if (!found) {
				if (sender instanceof Player && permission.equals(Permissions.USE)) {
					new HomeInventory().openInventory((Player) sender);	
				} else {
					sender.sendMessage(plugin.getMessage("errors.wrongUsage").replace("%usage%",
							"/" + usage + " <" + getSubCommandNames() + ">"));
				}
			}
		} catch (NullPointerException ex) {
			onCommand(sender, args);

		}

		return false;
	}

	public abstract void onCommand(CommandSender sender, String[] args);

	protected String getSubCommandNames() {
		StringBuilder result = new StringBuilder();
		for (SubCommand cmd : subCommands) {
			result.append("/").append(cmd.getNames().get(0));
		}

		return result.substring(1, result.length());
	}

	protected void setUsage(String usage) {
		this.usage = usage;
	}
}