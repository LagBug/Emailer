package me.lagbug.emailer.spigot.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerVerifyEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();
	private boolean isCancelled;
	private final Player player;
	
	public PlayerVerifyEvent(Player player) {
		this.player = player;
	}
	
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.isCancelled = cancelled;
		
	}
	
	public Player getPlayer() {
		return this.player;
	}
}
