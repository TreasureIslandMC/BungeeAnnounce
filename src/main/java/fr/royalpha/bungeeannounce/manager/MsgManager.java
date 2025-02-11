package fr.royalpha.bungeeannounce.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import fr.royalpha.bungeeannounce.BungeeAnnouncePlugin;
import fr.royalpha.bungeeannounce.util.BAUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Royalpha
 */
public class MsgManager {
	private final Map<String, String> playerReplyCache;
	private final Map<UUID, Boolean> playerToggleCache;
	private final Map<UUID, List<UUID>> playerIgnoreCache;

	
	public MsgManager() {
		this.playerReplyCache = new HashMap<>();
		this.playerToggleCache = new HashMap<>();
		this.playerIgnoreCache = new HashMap<>();
	}

	public void loadIgnoredPlayers(final @NotNull BungeeAnnouncePlugin plugin) {
		final Configuration ignoredConfig = plugin.getConfigManager().getIgnored();
		final Configuration ignoredSection = ignoredConfig.getSection("ignored");
		for(String playerUuidString: ignoredSection.getKeys()) {
			final List<UUID> ignoredUuids = new ArrayList<>();
			for(String ignoredString: ignoredSection.getStringList(playerUuidString)) {
				ignoredUuids.add(UUID.fromString(ignoredString));
			}
			final UUID playerUuid = UUID.fromString(playerUuidString);
			playerIgnoreCache.put(playerUuid,ignoredUuids);
		}
	}

	public void loadToggledPlayers(final @NotNull BungeeAnnouncePlugin plugin) {
		final Configuration toggledConfig = plugin.getConfigManager().getToggled();
		final Configuration toggledSection = toggledConfig.getSection("toggled");
		for(String playerUuidString: toggledSection.getKeys()) {
			final UUID playerUuid = UUID.fromString(playerUuidString);
			playerToggleCache.put(playerUuid,toggledSection.getBoolean(playerUuidString));
		}
	}


	public void ignore(final @NotNull ProxiedPlayer player, final @NotNull ProxiedPlayer toIgnore) {
		final UUID playerUuid = player.getUniqueId();
		final UUID ignoredUuid = toIgnore.getUniqueId();
		playerIgnoreCache.putIfAbsent(playerUuid,new ArrayList<>());
		final List<UUID> ignoredUuids = playerIgnoreCache.get(playerUuid);

		if(ignoredUuids.contains(ignoredUuid)) {
			ignoredUuids.remove(ignoredUuid);
			player.sendMessage(new TextComponent(ConfigManager.Field.IGNORE_PLAYER_OFF.getString().replace("%PLAYER%", toIgnore.getName())));
			return;
		}

		ignoredUuids.add(ignoredUuid);
		player.sendMessage(new TextComponent(ConfigManager.Field.IGNORE_PLAYER_ON.getString().replace("%PLAYER%",toIgnore.getName())));
	}

	public void toggle(final @NotNull ProxiedPlayer player) {
		final UUID playerUuid = player.getUniqueId();

		playerToggleCache.putIfAbsent(playerUuid, true);

		final boolean toggleState = playerToggleCache.get(playerUuid);
		final boolean newState = !toggleState;
		playerToggleCache.put(playerUuid,newState);
		if(newState) {
			player.sendMessage(new TextComponent(ConfigManager.Field.TOGGLE_MESSAGE_ON.getString()));
		} else {
			player.sendMessage(new TextComponent(ConfigManager.Field.TOGGLE_MESSAGE_OFF.getString()));
		}
	}
	
	public void message(final ProxiedPlayer sender,final ProxiedPlayer receiver,final @NotNull String message) {
		final String fromFormat = BAUtils.translatePlaceholders(ConfigManager.Field.PM_SENT.getString(), sender, receiver, sender).replace("%MESSAGE%", message.trim());
		final String toFormat = BAUtils.translatePlaceholders(ConfigManager.Field.PM_RECEIVED.getString(), sender,receiver,sender).replace("%MESSAGE%", message.trim());
		sender.sendMessage(new TextComponent(fromFormat));
		receiver.sendMessage(new TextComponent(toFormat));

		if (!playerReplyCache.containsKey(receiver.getName()))
			sendReplyUsage(sender,receiver);
		if (hasReplier(receiver))
			playerReplyCache.remove(receiver.getName());
		playerReplyCache.put(receiver.getName(), sender.getName());
	}

	private void sendReplyUsage(final @NotNull ProxiedPlayer sender, final ProxiedPlayer receiver) {
		AnnouncementManager.sendToPlayer(AnnouncementManager.ACTION, null, receiver, ConfigManager.Field.REPLY_INFO.getString().replace("%SENDER%", sender.getName()), false);
	}
	
	public ProxiedPlayer getReplier(@NotNull ProxiedPlayer player) {
		return ProxyServer.getInstance().getPlayer(playerReplyCache.get(player.getName()));
	}
	
	public String getReplierName(@NotNull ProxiedPlayer player) {
		return playerReplyCache.get(player.getName());
	}
	
	public boolean hasReplier(@NotNull ProxiedPlayer player) {
		return playerReplyCache.containsKey(player.getName());
	}
	
	public boolean isReplierOnline(ProxiedPlayer player) {
		return (hasReplier(player) && getReplier(player) != null && getReplier(player).isConnected());
	}

	public boolean isIgnored(final @NotNull ProxiedPlayer sender, final ProxiedPlayer receiver) {
		try {
			return playerIgnoreCache.get(receiver.getUniqueId()).contains(sender.getUniqueId());
		} catch (NullPointerException e) {
			return false;
		}
	}

	public boolean isToggled(final @NotNull ProxiedPlayer player) {
		return playerToggleCache.computeIfAbsent(player.getUniqueId(),uuid -> true);
	}

	public Map<UUID, List<UUID>> getPlayerIgnoreCache() {
		return playerIgnoreCache;
	}

	public Map<UUID, Boolean> getPlayerToggleCache() {
		return playerToggleCache;
	}
}
