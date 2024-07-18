package it.novaverse.commonutilities.service;

import it.novaverse.commonutilities.CommonUtilities;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

@NoArgsConstructor
public class CommandService {

    @SuppressWarnings("deprecated")
    public void register(@NonNull CommonUtilities plugin) {
        LegacyPaperCommandManager<CommandSender> paperCommandManager = LegacyPaperCommandManager.createNative(
                plugin,
                ExecutionCoordinator.asyncCoordinator()
        );

        AnnotationParser<CommandSender> commandSenderAnnotationParser = new AnnotationParser<>(
                paperCommandManager,
                CommandSender.class
        );

        try {
            commandSenderAnnotationParser.parseContainers(plugin.getClass().getClassLoader());
        } catch (Exception e) {
            plugin.getLogger().severe("There was an error while parsing the command containers: " + e.getMessage());
        }
    }

    public void registerParamterInjector() {
        // TODO: Implement parameter injector
    }
    
    public void registerArgumentSupplier() {
        // TODO: Implement argument supplier
    }

}
