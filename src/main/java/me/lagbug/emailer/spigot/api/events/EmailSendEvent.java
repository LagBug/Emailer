package me.lagbug.emailer.spigot.api.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.lagbug.emailer.spigot.utils.Email;

public class EmailSendEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();
	private boolean isCancelled;
	private final Email email;
	
	public EmailSendEvent(Email email) {
		this.email = email;
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
	
	public Email getEmail() {
		return this.email;
	}
}
