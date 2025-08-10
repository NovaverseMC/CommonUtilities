package it.novaverse.commonutilities.module;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import it.novaverse.commonutilities.CommonUtilities;
import it.novaverse.commonutilities.annotation.RegisterListeners;
import it.novaverse.commonutilities.module.implementation.command.CommonCommand;
import it.novaverse.commonutilities.service.CommandService;
import it.novaverse.commonutilities.service.PluginService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

@Log
@RequiredArgsConstructor
public class ModuleManager {

    @NonNull
    private ConfigurationSection config;
    @NonNull
    private PluginService service;
    @NonNull
    private CommandService commands;

    private final Map<String, Module> modules = new LinkedHashMap<>();

    @SuppressWarnings("unchecked")
    public void loadInternalModules(final Runnable onDefaultSave) {
        String pkg = CommonUtilities.class.getPackage().getName();

        try (
                ScanResult scanResult = new ClassGraph()
                        .enableAllInfo()
                        .acceptPackages(pkg)
                        .scan()
        ) {
            Class<? extends Module>[] classes = scanResult.getClassesImplementing(Module.class)
                    .loadClasses()
                    .toArray(Class[]::new);
            for (Class<? extends Module> clazz : classes) {
                if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) continue;

                String moduleName = clazz.getSimpleName();
                moduleName = moduleName.substring(0, 1).toLowerCase() + moduleName.substring(1);
                ConfigurationSection moduleSection = config.getConfigurationSection(moduleName);
                if (moduleSection == null) moduleSection = config.createSection(moduleName);

                loadModule(moduleName, clazz, moduleSection, onDefaultSave);
            }
        }
    }

    public void loadModule(@NonNull final String name, @NonNull final Class<? extends Module> clazz, final ConfigurationSection config, final Runnable onDefaultSave) {
        log.log(Level.INFO, "Loading Module: {0}", name);
        Module module;
        try {
            Constructor<? extends Module> constructor = clazz.getDeclaredConstructor();
            boolean accessible = constructor.isAccessible();
            constructor.setAccessible(true);
            module = constructor.newInstance();
            constructor.setAccessible(accessible);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException |
                 InvocationTargetException e) {
            log.log(Level.SEVERE, "Unable to construct module " + name + "!", e);
            return;
        }
        module.injectConfig(config, onDefaultSave);
        module.onLoad(name, service);
        modules.put(name, module);
    }

    public void enableModules() {
        for (Map.Entry<String, Module> entry : modules.entrySet()) {
            Module module = entry.getValue();
            if (!module.isEnabled()) {
                continue;
            }
            log.info("Enabling module " + entry.getKey() + "...");
            if (module.getClass().getAnnotation(RegisterListeners.class) != null) {
                service.registerListener((Listener) module);
            }
            if (module instanceof CommonCommand) {
                commands.registerCommand((CommonCommand) module);
            }

            module.onEnable();
            log.info("Module " + entry.getKey() + " enabled!");
        }
    }

    public void disableModules() {
        modules.forEach((key, module) -> {
            if (module.isEnabled()) {
                if (module.getClass().getAnnotation(RegisterListeners.class) != null) {
                    service.unregisterListener((Listener) module);
                }
                module.onDisable();
                log.info("Module " + key + " disabled!");
            }
            module.onUnload();
        });
    }

}
