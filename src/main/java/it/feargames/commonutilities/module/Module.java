package it.feargames.commonutilities.module;

import it.feargames.commonutilities.config.ConfigValueInjectable;
import it.feargames.commonutilities.service.PluginService;
import it.feargames.commonutilities.service.ProtocolServiceWrapper;

public interface Module extends ConfigValueInjectable {

    /**
     * Called when the module is loaded, even if disabled in the config file
     *
     * @param name     the module name
     * @param service  the plugin service
     * @param protocol the protocol service
     */
    default void onLoad(String name, PluginService service, ProtocolServiceWrapper protocol) {
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
