package me.lagbug.emailer.spigot.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import me.lagbug.emailer.global.EmailAddress;
import me.lagbug.emailer.spigot.Emailer;
import me.lagbug.emailer.spigot.common.utils.communication.Title;
import me.lagbug.emailer.spigot.common.utils.util.CommonUtils;
import me.lagbug.emailer.spigot.guis.PlayerManagerInventory;
import me.lagbug.emailer.spigot.utils.Checker;
import me.lagbug.emailer.spigot.utils.Email;
import me.lagbug.emailer.spigot.utils.PlayerDataCache;

public class AsyncPlayerChat implements Listener {

	private final Emailer plugin = Emailer.getPlugin(Emailer.class);

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		Player player = e.getPlayer();
		String message = e.getMessage();

		Map<Player, OfflinePlayer> map = plugin.getLinking();

		if (map.containsKey(player)) {
			if (map.get(player) == null) {
				Map<String, String> verified = PlayerDataCache.getVerifiedData();
				List<EmailAddress> recipients = new ArrayList<>();

				OfflinePlayer p = Bukkit.getOfflinePlayer(message);
				if (!verified.containsKey(p.getUniqueId().toString())) {
					player.sendMessage(plugin.getMessage("commands.verify.notVerified").replace("%player%", p.getName()));
					return;
				}

				recipients.add(new EmailAddress(verified.get(p.getUniqueId().toString())));

				// We the the recipients array
				EmailAddress[] arr = new EmailAddress[recipients.size()];
				arr = recipients.toArray(arr);

				// We send the email
				new Email(plugin.getSession(), plugin.getEmail(), arr)
						.setTemplate(plugin.getEmailTemplates().get(plugin.getEditing().get(player))).send();

				// Notify the player
				player.sendMessage(plugin.getMessage("commands.email.sent").replace("%recipients%",
						Integer.toString(recipients.size())));
				// And log this in the console
				CommonUtils.log(
						"An email has been sent by " + player.getName() + " to " + recipients.size() + " recipients.");

				map.remove(player);
			} else {
				if (message.contains("@")) {
					// We get the player to be verified and the email
					OfflinePlayer toVerify = map.get(player);
					EmailAddress address = new EmailAddress(message);

					// We initiate a Checker object
					Checker checker = new Checker(toVerify.getUniqueId(), address);

					// Otherwise we force verify them and send a success message
					checker.forceVerify();
					player.sendMessage(plugin.getMessage("commands.verify.force")
							.replace("%address%", address.getAddress()).replace("%player%", toVerify.getName()));
					map.remove(player);
				} else {
					map.put(player, Bukkit.getOfflinePlayer(message));
					Bukkit.getScheduler().runTask(plugin, () -> new PlayerManagerInventory().openInventory(player));
				}

			}

			Title.cancel(player);
			e.setCancelled(true);
		}
	}
}