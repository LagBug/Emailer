package me.lagbug.emailer.spigot.commands;

import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.lagbug.emailer.global.EmailAddress;
import me.lagbug.emailer.spigot.Emailer;
import me.lagbug.emailer.spigot.common.commands.SpigotCommand;
import me.lagbug.emailer.spigot.common.commands.SubCommand;
import me.lagbug.emailer.spigot.common.utils.util.ActionUtil;
import me.lagbug.emailer.spigot.common.utils.util.CommonUtils;
import me.lagbug.emailer.spigot.utils.Checker;
import me.lagbug.emailer.spigot.utils.Email;
import me.lagbug.emailer.spigot.utils.Permissions;

public class SetEmailCommand extends SpigotCommand {

	private final Emailer plugin = Emailer.getPlugin(Emailer.class);

	public SetEmailCommand() {
		super(Permissions.SET_EMAIL, 1, (SubCommand) null);
		super.setUsage("/setemail <email>");
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		// If the sender is not a player we return
		if (!(sender instanceof Player)) {
			sender.sendMessage(plugin.getMessage("errors.onlyPlayers"));
			return;
		}

		Player player = (Player) sender;
		String emailAddress = args[0];

		// If they are already linked with an email we return here
		if (new Checker(player.getUniqueId()).isVerified()) {
			player.sendMessage(plugin.getMessage("errors.already").replace("%player%", player.getName()));
			return;
		}

		// If the specified String is not an actual email we return here
		if (!Pattern
				.compile(
						"^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")
				.matcher(emailAddress).matches()) {
			player.sendMessage(plugin.getMessage("errors.wrongEmail"));
			return;
		}
		
		for (String current : plugin.getConfigFile().getStringList("security.list")) {
			if (Pattern.compile(current).matcher(emailAddress).matches()) {
				player.sendMessage(plugin.getMessage("errors.blocked"));
				return;
			}
		}
		
		// We create a new instance of an Email
		Email email = new Email(plugin.getSession(), plugin.getEmail(), new EmailAddress(emailAddress));
		// Create a random 8-digit verification code
		String code = CommonUtils.randomString(8);

		// We send an email with the verification template 
		email.setTemplate(plugin.getEmailTemplates().get(plugin.getTemplatesFile().get("verificationTemplate")))
				.setPlayer(player).setCode(code).send();
		
		// And add the pending data
		plugin.getPending().put(player, code + ";;" + emailAddress);

		Checker checker = new Checker(player.getUniqueId());

		// If the player is already verified, we unverify them
		if (checker.isVerified()) {
			checker.unVerify();
		}

		player.sendMessage(plugin.getMessage("commands.pending.success").replace("%email_address%", emailAddress));
		ActionUtil.execute(player, plugin.getConfigFile().getStringList("onVerificationRequest"));
		CommonUtils.log("Sent an email to " + emailAddress + " in order to verify " + player.getName());
	}

}
