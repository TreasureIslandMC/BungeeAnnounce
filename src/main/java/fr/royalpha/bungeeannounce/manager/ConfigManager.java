package fr.royalpha.bungeeannounce.manager;

import fr.royalpha.bungeeannounce.BungeeAnnouncePlugin;
import fr.royalpha.bungeeannounce.task.ScheduledAnnouncement;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Royalpha
 */
public class ConfigManager {
    private final BungeeAnnouncePlugin plugin;
    private Configuration config;

    public ConfigManager(BungeeAnnouncePlugin plugin) {
        this.plugin = plugin;
        if (!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdirs();
        File file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.exists()) {
            getLogger().info("Thanks for using BungeeAnnounce by Royalpha. Don't forget to review it !");
            getLogger().info("We are a team of developers and we would really appreciate if you could follow our twitter page where we post news about our plugins <3 https://twitter.com/AsyncDevTeam");
            getLogger().info("Generating configuration file ...");
            try (InputStream in = plugin.getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                new ExceptionManager(e).register(plugin, true);
                getLogger().warning("Error when generating configuration file !");
            } finally {
                getLogger().info("Configuration file was generated with success !");
            }
        }
        try {
            this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            new ExceptionManager(e).register(plugin, true);
        }

        Field.init(this.config);
    }


    public List<ScheduledAnnouncement> loadScheduledAnnouncement() {
        List<ScheduledAnnouncement> output = new ArrayList<>();
        int i = 0;

        Configuration schedulerSection = this.config.getSection("scheduler");
        for (String taskName : schedulerSection.getKeys()) {
            try {
                String type = schedulerSection.getString(taskName + ".type", "");

                AnnouncementManager announcement = AnnouncementManager.getAnnouncement(type);
                if (announcement == null) {
                    getLogger().log(Level.WARNING, "Error when loading announcement \"%s\", the field 'type' wasn't recognized.", taskName);
                    continue;
                }

                String message = schedulerSection.getString(taskName + ".message", "<No message was set for this announcement>");
                List<String> servers = schedulerSection.getStringList(taskName + ".servers");
                String permission = schedulerSection.getString(taskName + ".permission", "");
                int delay = schedulerSection.getInt(taskName + ".delay", 5);
                int interval = schedulerSection.getInt(taskName + ".interval", 10);

                if (interval < 0) {
                    getLogger().info("The scheduled announcement \"" + taskName + "\" has a negative interval. So it was frozen. In other words, the only way to broadcast it is to use the command: /forceBroadcast " + taskName);
                } else {
                    output.add(new ScheduledAnnouncement(this.plugin, announcement, message, servers, permission, delay, interval));
                }
                i++;

            } catch (Exception ex) {
                getLogger().warning("Error when loading announcement \"" + taskName + "\" in config.yml");
                new ExceptionManager(ex).register(this.plugin, true);
            }
        }
        if (i > 0)
            getLogger().log(Level.INFO, i + " scheduled announcement" + (i > 1 ? "s" : "") + " " + (i > 1 ? "were" : "was") + " correctly loaded.");
        return output;
    }

    public Logger getLogger() {
        return this.plugin.getLogger();
    }

    public enum Field {
        REGISTER_LOGS("enable-announcement-logs", Boolean.class, false),
        ANNOUNCE_PREFIX("announce-prefix", String.class, "");

        private final String configField;
        private final Class<?> type;
        private final Object def;
        private Object value;

        Field(String configField, Class<?> type, Object def) {
            this.configField = configField;
            this.type = type;
            this.def = def;
        }

        public String getField() {
            return this.configField;
        }

        public Class<?> getType() {
            return this.type;
        }

        public boolean getBoolean() {
            return (boolean) this.value;
        }

        public String getString() {
            return (String) this.value;
        }

        public Object getDefault() {
            return this.def;
        }

        private void setValue(Object obj) {
            this.value = obj;
        }

        public static void init(Configuration config) {
            for (Field field : values()) {
                if (field.getType() == String.class) {
                    field.setValue(ChatColor.translateAlternateColorCodes('&', config.getString(field.getField(), (String) field.getDefault())));
                } else if (field.getType() == Boolean.class) {
                    field.setValue(config.getBoolean(field.getField(), (boolean) field.getDefault()));
                }
            }
        }
    }

    public Configuration getConfig() {
        return this.config;
    }
}
