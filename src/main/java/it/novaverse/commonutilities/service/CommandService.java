package it.novaverse.commonutilities.service;

import it.novaverse.commonutilities.CommonUtilities;
import it.novaverse.commonutilities.module.implementation.command.CommonCommand;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

@NoArgsConstructor
public class CommandService {

    private LegacyPaperCommandManager<CommandSender> commandManager;
    private AnnotationParser<CommandSender> annotationParser;

    @SuppressWarnings("deprecated")
    public void register(@NonNull CommonUtilities plugin) {
        commandManager = LegacyPaperCommandManager.createNative(
                plugin,
                ExecutionCoordinator.asyncCoordinator()
        );

        if (commandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            commandManager.registerBrigadier();
        } else if (commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            commandManager.registerAsynchronousCompletions();
        }

        annotationParser = new AnnotationParser<>(
                commandManager,
                CommandSender.class
        );
    }

    public void registerCommand(CommonCommand holder) {
        annotationParser.parse(holder);
    }
}
