package it.feargames.commonutilities;

import it.feargames.commonutilities.module.ModuleManager;
import it.feargames.commonutilities.service.CommandService;
import it.feargames.commonutilities.service.PluginService;
import it.feargames.commonutilities.service.ProtocolServiceWrapper;
import org.bukkit.plugin.java.JavaPlugin;

public final class CommonUtilities extends JavaPlugin {

    private PluginService service;
    private CommandService commands;
    private ProtocolServiceWrapper protocol;
    private ModuleManager moduleManager;

    @Override
    public void onLoad() {
        saveDefaultConfig();

        service = new PluginService(this);
        commands = new CommandService();
        protocol = new ProtocolServiceWrapper(this, service);
        moduleManager = new ModuleManager(getConfig().createSection("modules"), service, commands, protocol);
        moduleManager.loadInternalModules(this::saveConfig);
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
