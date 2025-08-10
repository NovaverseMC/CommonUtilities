package it.novaverse.commonutilities.module;

import it.novaverse.commonutilities.config.ConfigValueInjectable;
import it.novaverse.commonutilities.service.PluginService;

public interface Module extends ConfigValueInjectable {

    /**
     * Called when the module is loaded, even if disabled in the config file
     *
     * @param name     the module name
     * @param service  the plugin service
     */
    default void onLoad(String name, PluginService service) {
    }

    /**
     * Called when the module is enabled
     */
    default void onEnable() {
    }

    /**
     * Called when the module is disabled
     */
    default void onDisable() {
    }

    /**
     * Called when the module is unloaded, even if disabled in the config file
     */
    default void onUnload() {
    }

    /**
     * Returns if the module is considered as enabled
     *
     * @return true if the module is considered enabled
     */
    boolean isEnabled();

}
