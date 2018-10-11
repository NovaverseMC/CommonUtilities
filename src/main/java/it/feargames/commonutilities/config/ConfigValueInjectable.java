package it.feargames.commonutilities.config;

import it.feargames.commonutilities.annotation.ConfigValue;
import lombok.NonNull;
import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public interface ConfigValueInjectable {

    @SuppressWarnings("unchecked")
    default void injectConfig(@NonNull final ConfigurationSection configuration, final Runnable onDefaultSave) {
        AtomicBoolean updated = new AtomicBoolean(false);
        Arrays.asList(getClass().getDeclaredFields()).forEach(field -> {
            ConfigValue annotation = field.getAnnotation(ConfigValue.class);
            if (annotation == null) {
                return;
            }
            ConfigValue.ValueType type = annotation.type() == ConfigValue.ValueType.AUTO ?
                    ConfigValue.ValueType.getType(field) : annotation.type();
            if (type == null) {
                onUnknownType(field, annotation);
                return; // Skip unknown types
            }
            String path = annotation.path().isEmpty() ? field.getName() : annotation.path();
            Object value = type.getDataType().get(configuration, path, null);

            // Default value
            if (value == null) {
                try {
                    value = FieldUtils.readField(field, this, true);
                    configuration.set(path, value);
                    if (annotation.comment().length > 0) {
                        // TODO: comment
                    }
                    updated.set(true);
                } catch (IllegalStateException | IllegalAccessException e) {
                    onDefaultSetException(e);
                }
            }

            try {
                FieldUtils.writeField(field, this, value, true);
            } catch (IllegalAccessException e) {
                onFieldWriteException(e);
            }
        });

        if (updated.get() && onDefaultSave != null) {
            onDefaultSave.run();
        }
    }

    default void onUnknownType(final Field field, final ConfigValue configValue) {
        throw new RuntimeException("Unknown field type " + field.getType().getSimpleName());
    }

    default void onDefaultSetException(final Exception e) {
        throw new RuntimeException(e);
    }

    default void onFieldWriteException(final Exception e) {
        throw new RuntimeException(e);
    }

}
