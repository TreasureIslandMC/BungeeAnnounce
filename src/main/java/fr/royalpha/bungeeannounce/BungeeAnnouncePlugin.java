package fr.royalpha.bungeeannounce;

import co.aikar.commands.BungeeCommandManager;
import fr.royalpha.bungeeannounce.command.BungeeAnnounceCommand;
import fr.royalpha.bungeeannounce.handler.Logger;
import fr.royalpha.bungeeannounce.manager.AnnouncementManager;
import fr.royalpha.bungeeannounce.manager.ConfigManager;
import fr.royalpha.bungeeannounce.manager.MsgManager;
import fr.royalpha.bungeeannounce.task.ScheduledAnnouncement;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.bstats.bungeecord.Metrics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Royalpha
 */
public class BungeeAnnouncePlugin extends Plugin implements Listener {
	
	private static BungeeAnnouncePlugin instance;
	private Logger logSystem;

	private ConfigManager configManager;
	private List<ScheduledAnnouncement> scheduledAnnouncement;
	private MsgManager msgManager;
	
	public BungeeAnnouncePlugin() {
		this.scheduledAnnouncement = new ArrayList<>();
	}

	public MsgManager getMsgManager() {
		return msgManager;
	}

	@Override
	public void onEnable() {
		instance = this;
		this.msgManager = new MsgManager();

		loadConfigFile();
		initializeLogSystem();
		loadConfigContent();
		this.msgManager.loadIgnoredPlayers(this);
		registerCommands();
		registerListeners();

		initializeMetrics();
	}

	@Override
	public void onDisable() {
		final Configuration ignored = configManager.getIgnored();
		ignored.set("ignored",prepareForSaving());

		try {
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(ignored, new File(getDataFolder(), "ignored.yml"));
		} catch (IOException e){
			getSLF4JLogger().error(e.getMessage());
		}
	}

	private Map<String,List<String>> prepareForSaving() {
		Map<String,List<String>> map = new HashMap<>();
		for(Map.Entry<UUID,List<UUID>> entry:msgManager.getPlayerIgnoreCache().entrySet()) {
			map.put(entry.getKey().toString(),entry.getValue().stream().map(UUID::toString).toList());
		}

		return map;
	}

	/*
	 * To do better, the load method should be executed in the onEnable to avoid repeating lines of code. However, since it is only used for the BAReload command, I decided to separate it from the onEnable.
	 */
	public void load() {
		this.configManager = new ConfigManager(this);
		logSystem = new Logger(this);
		
		this.scheduledAnnouncement = this.configManager.loadScheduledAnnouncement();
	}
	private void loadConfigFile() {
		this.configManager = new ConfigManager(this);
	}
	private void initializeLogSystem() {
		logSystem = new Logger(this);
	}
	private void loadConfigContent() {
		this.scheduledAnnouncement = this.configManager.loadScheduledAnnouncement();
	}

	private void registerCommands(){
		PluginManager pM = getProxy().getPluginManager();
		BungeeCommandManager bungeeCommandManager = new BungeeCommandManager(this);
		for (AnnouncementManager aM : AnnouncementManager.values())
			pM.registerCommand(this, aM.getCommandClass());
		bungeeCommandManager.registerCommand(new BungeeAnnounceCommand(this));
	}

	private void registerListeners() {
		getProxy().getPluginManager().registerListener(this, this);
	}

	private void initializeMetrics() {
		Metrics metrics = new Metrics(this, 8662);
	}

	public List<ScheduledAnnouncement> getScheduledAnnouncement() {
		return this.scheduledAnnouncement;
	}
	
	public ConfigManager getConfigManager() {
		return this.configManager;
	}
	
	public static ProxyServer getProxyServer() {
		return instance.getProxy();
	}
	
	public Logger getLoggerSystem() {
		return logSystem;
	}

	public static BungeeAnnouncePlugin instance() {
		return instance;
	}

}
