package it.feargames.commonutilities;

import it.feargames.commonutilities.module.ModuleManager;
import it.feargames.commonutilities.service.CommandService;
import it.feargames.commonutilities.service.PluginService;
import it.feargames.commonutilities.service.ProtocolServiceWrapper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class CommonUtilities extends JavaPlugin {

    private PluginService service;
    private CommandService commands;
    private ProtocolServiceWrapper protocol;
    private ModuleManager moduleManager;

    private File configFile;

    @Override
    public void onLoad() {
        try {
            ensureConfigCreation();
        } catch (IOException e) {
            getLogger().severe("Unable to create the default config file! " + e.getMessage());
        }

        final YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        service = new PluginService(this);
        commands = new CommandService();
        protocol = new ProtocolServiceWrapper(this, service);
        
        final ConfigurationSection section = config.isConfigurationSection("modules") ?
                config.getConfigurationSection("modules") : config.createSection("modules");

        moduleManager = new ModuleManager(section, service, commands, protocol);
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
        protocol.initialize();
        commands.register(this);
        moduleManager.enableModules();
    }

    @Override
    public void onDisable() {
        moduleManager.disableModules();
        protocol.cleanup();
    }
}
