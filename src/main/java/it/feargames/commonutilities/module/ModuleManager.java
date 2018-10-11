package it.feargames.commonutilities.module;

import co.aikar.commands.BaseCommand;
import it.feargames.commonutilities.annotation.RegisterCommands;
import it.feargames.commonutilities.annotation.RegisterListeners;
import it.feargames.commonutilities.service.CommandService;
import it.feargames.commonutilities.service.PluginService;
import it.feargames.commonutilities.service.ProtocolServiceWrapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
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
    @NonNull
    private ProtocolServiceWrapper protocol;

    private final Map<String, Module> modules = new LinkedHashMap<>();

    public void loadInternalModules(final Runnable onDefaultSave) {
        Reflections moduleReflection = new Reflections(getClass().getPackage().getName() + ".implementation");
        Set<Class<? extends Module>> moduleClasses = moduleReflection.getSubTypesOf(Module.class);
        for (Class<? extends Module> moduleClass : moduleClasses) {
            if (moduleClass.isInterface() || Modifier.isAbstract(moduleClass.getModifiers())) {
                continue;
            }
            try {
                String moduleName = Character.toLowerCase(moduleClass.getSimpleName().charAt(0)) + moduleClass.getSimpleName().substring(1);
                ConfigurationSection moduleConfig = config.getConfigurationSection(moduleName);
                if (moduleConfig == null) {
                    moduleConfig = config.createSection(moduleName);
                }
                loadModule(moduleName, moduleClass, moduleConfig, onDefaultSave);
            } catch (Exception e) {
                log.log(Level.SEVERE, "Unable to load a module!", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void loadModule(@NonNull final String name, @NonNull final Class<? extends Module> clazz, final ConfigurationSection config, final Runnable onDefaultSave) {
        Module module;
        try {
            Constructor<? extends Module> constructor = clazz.getDeclaredConstructor();
            boolean accessible = constructor.isAccessible();
            constructor.setAccessible(true);
            module = constructor.newInstance();
            constructor.setAccessible(accessible);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            log.log(Level.SEVERE, "Unable to construct module " + name + "!", e);
            return;
        }
        module.injectConfig(config, onDefaultSave);
        module.onLoad(name, service, protocol);
        modules.put(name, module);
    }

    @SuppressWarnings("unchecked")
    public void enableModules() {
        for (Map.Entry<String, Module> entry : modules.entrySet()) {
            Module module = entry.getValue();
            if (!module.isEnabled()) {
                continue;
            }
            if (module.getClass().getAnnotation(RegisterListeners.class) != null) {
                service.registerListener((Listener) module);
            }
            if (module.getClass().getAnnotation(RegisterCommands.class) != null) {
                commands.registerCommand((BaseCommand) module);
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
