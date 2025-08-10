package it.novaverse.commonutilities;

import it.novaverse.commonutilities.module.ModuleManager;
import it.novaverse.commonutilities.service.CommandService;
import it.novaverse.commonutilities.service.PluginService;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class CommonUtilities extends JavaPlugin {

    private PluginService service;
    private CommandService commands;
    private ModuleManager moduleManager;

    private File configFile;

    public void initialize() {
        try {
            ensureConfigCreation();
        } catch (IOException e) {
            getLogger().severe("Unable to create the default config file! " + e.getMessage());
        }

        final YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        service = new PluginService(this);
        commands = new CommandService();

        final ConfigurationSection section = config.isConfigurationSection("modules") ? config.getConfigurationSection(
                "modules") : config.createSection("modules");

        moduleManager = new ModuleManager(section, service, commands);
        moduleManager.loadInternalModules(() -> {
            try {
                config.save(configFile);
            } catch (IOException e) {
                getLogger().severe("Unable to save the default config file! " + e.getMessage());
            }
        });
    }

    private void ensureConfigCreation() throws IOException {
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            getDataFolder().mkdirs();
            configFile.createNewFile();
        }
    }

    @Override
    public void onEnable() {
        initialize();

        commands.register(this);
        moduleManager.enableModules();

        service.registerOutgoingPluginChannel("BungeeCord");
    }

    @Override
    public void onDisable() {
        moduleManager.disableModules();

        service.unregisterOutgoingPluginChannel("BungeeCord");
    }
}
