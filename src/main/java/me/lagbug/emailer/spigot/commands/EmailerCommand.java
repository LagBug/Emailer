package me.lagbug.emailer.spigot.commands;

import org.bukkit.command.CommandSender;

import me.lagbug.emailer.spigot.Emailer;
import me.lagbug.emailer.spigot.commands.subcommands.ForceVerify;
import me.lagbug.emailer.spigot.commands.subcommands.Reload;
import me.lagbug.emailer.spigot.commands.subcommands.Search;
import me.lagbug.emailer.spigot.commands.subcommands.Send;
import me.lagbug.emailer.spigot.common.commands.SpigotCommand;
import me.lagbug.emailer.spigot.utils.Permissions;

public class EmailerCommand extends SpigotCommand {

	@SuppressWarnings("unused")
	private final Emailer plugin = Emailer.getPlugin(Emailer.class);
	
	public EmailerCommand() {
		super(Permissions.USE, 1, new Send(), new Reload(), new ForceVerify(), new Search());
		super.setUsage("emailer");
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		
	}
}