package it.novaverse.commonutilities.service;

import it.novaverse.commonutilities.CommonUtilities;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

@NoArgsConstructor
public class CommandService {

    @SuppressWarnings("deprecated")
    public void register(@NonNull CommonUtilities plugin) {
        LegacyPaperCommandManager<CommandSender> commandManager = LegacyPaperCommandManager.createNative(
                plugin,
                ExecutionCoordinator.asyncCoordinator()
        );

        if (commandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            commandManager.registerBrigadier();
        } else if (commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            commandManager.registerAsynchronousCompletions();
        }

        AnnotationParser<CommandSender> commandSenderAnnotationParser = new AnnotationParser<>(
                commandManager,
                CommandSender.class
        );

        try {
            commandSenderAnnotationParser.parseContainers(plugin.getClass().getClassLoader());
        } catch (Exception e) {
            plugin.getLogger().severe("There was an error while parsing the command containers: " + e.getMessage());
        }
    }
}
