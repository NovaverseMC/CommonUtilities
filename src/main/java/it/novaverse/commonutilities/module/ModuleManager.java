package it.novaverse.commonutilities.module;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import it.novaverse.commonutilities.annotation.RegisterListeners;
import it.novaverse.commonutilities.service.CommandService;
import it.novaverse.commonutilities.service.PluginService;
import it.novaverse.commonutilities.service.ProtocolServiceWrapper;
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
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;

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
        String pkg = getClass().getPackage().getName() + ".implementation";

        try (ScanResult scanResult = new ClassGraph().verbose().enableAllInfo().acceptPackages(pkg).scan()) {
            for (ClassInfo routeClassInfo : scanResult.getClassesImplementing("Module")) {
                if (routeClassInfo.isInterface() || routeClassInfo.isAbstract()) continue;

                String moduleName = routeClassInfo.getSimpleName();
                moduleName = moduleName.substring(0, 1).toLowerCase() + moduleName.substring(1);
                ConfigurationSection moduleSection = config.getConfigurationSection(moduleName);
                if (moduleSection == null) moduleSection = config.createSection(moduleName);

                loadModule(moduleName, routeClassInfo.loadClass(Module.class), moduleSection, onDefaultSave);
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
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException |
                 InvocationTargetException e) {
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
