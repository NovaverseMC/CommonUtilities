package it.feargames.commonutilities.service;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandManager;
import it.feargames.commonutilities.CommonUtilities;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor
public class CommandService {

    private BukkitCommandManager commandManager = null;

    @SuppressWarnings("deprecated")
    public void register(@NonNull CommonUtilities plugin) {
        commandManager = new BukkitCommandManager(plugin);
        commandManager.enableUnstableAPI("help");
    }

    public void registerCommand(BaseCommand command) {
        commandManager.registerCommand(command);
    }

    public void unregisterCommand(BaseCommand command) {
        commandManager.unregisterCommand(command);
    }

    public void unregisterCommands() {
        commandManager.unregisterCommands();
    }

}
