package me.lagbug.emailer.spigot.guis;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import me.lagbug.emailer.spigot.Emailer;
import me.lagbug.emailer.spigot.common.builders.CustomInventory;
import me.lagbug.emailer.spigot.common.utils.communication.Title;
import me.lagbug.emailer.spigot.utils.PlaceholderManager;
import me.lagbug.emailer.spigot.utils.PlayerDataCache;

public class HomeInventory extends CustomInventory {

	private static final Emailer plugin = Emailer.getPlugin(Emailer.class);
	
	public HomeInventory() {
		super(plugin.getFile("guis/home.yml"));
	}

	@Override
	public void onClick(Player player, String action, ItemStack item, int slot, ClickType click) {
		switch (action) {
		case "SEND_EMAIL":
			//We destroy the current GUI
			destroy();
			//And then open the new GUI
			new TemplateInventory().openInventory(player);
			break;
		case "GOTO_PLAYER_MANAGER":
			// We destroy the current GUI
			destroy();
			
			player.closeInventory();
			plugin.getLinking().put(player, player);
			//Send the searching title to notify the player
			Title.sendForever(player, "&bPlayer Search", "&7Enter a player name to continue");
			break;
		case "RELOAD_PLUGIN":
			// We reload the files & data
			PlayerDataCache.save(false);
			plugin.reloadFiles();
			plugin.initiate();
			
			// And then let the player know it was successful
			player.sendMessage(plugin.getMessage("commands.reload.success"));
			break;
		default:
			break;
		}
	}

	@Override
	public void onUpdate(Player player) {
		PlaceholderManager.get().forEach(key -> replace(key.getKey(), key.getValue()));
	}

	@Override
	public void onClose(Player player) {
		//Empty as the GUI will auto-destroy itself.
	}
}
