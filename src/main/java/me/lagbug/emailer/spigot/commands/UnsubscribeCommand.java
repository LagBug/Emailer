package me.lagbug.emailer.spigot.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.lagbug.emailer.spigot.Emailer;
import me.lagbug.emailer.spigot.common.commands.SpigotCommand;
import me.lagbug.emailer.spigot.common.commands.SubCommand;
import me.lagbug.emailer.spigot.common.utils.util.ActionUtil;
import me.lagbug.emailer.spigot.common.utils.util.CommonUtils;
import me.lagbug.emailer.spigot.utils.Checker;
import me.lagbug.emailer.spigot.utils.Permissions;

public class UnsubscribeCommand extends SpigotCommand {

	private final Emailer plugin = Emailer.getPlugin(Emailer.class);
	
	public UnsubscribeCommand() {
		super(Permissions.UNSUBSCRIBE, 0, (SubCommand) null);
		super.setUsage("/unsubscribe");
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		// If the sender is not a player we return
		if (!(sender instanceof Player)) {
			sender.sendMessage(plugin.getMessage("errors.onlyPlayers"));
			return;
		}

		Player player = (Player) sender;
		Checker checker = new Checker(player.getUniqueId());
		
		// If the player is not verified we return
		if (!checker.isVerified()) {
			player.sendMessage(plugin.getMessage("commands.verify.notVerified").replace("%player%", player.getName()));
			return;
		}

		// Otherwise we un-verify the player
		checker.unVerify();
		// Let them know they were successfully un-subscribed 
		player.sendMessage(plugin.getMessage("commands.unsubscribe.success"));
		CommonUtils.log(player.getName() + " has been unsubscribed from the emails");
		// And run the actions specified in the config
		ActionUtil.execute(player, plugin.getConfigFile().getStringList("onUnsubscribe"));
	}

}