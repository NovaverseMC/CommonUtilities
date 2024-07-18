package it.novaverse.commonutilities.annotation;

import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.bukkit.util.NumberConversions.*;

/**
 * A modified version of the CfgValue annotation class from https://github.com/JarvisCraft/EZ-Cfg
 * Original author: JarvisCraft
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigValue {

    /**
     * The relative path of the config value
     *
     * @return the path of the value
     */
    String path() default "";

    /**
     * Type of the stored value. By default it's automatically taken from the variable to store data in.
     *
     * @return the type of the value
     */
    ValueType type() default ValueType.AUTO;

    /**
     * The list of strings that will be used as comments for the config value
     *
     * @return the array of strings used as comment
     */
    String[] comment() default {};

    @SuppressWarnings("unused")
    enum ValueType {
        AUTO(null),
        // Base types
        BOOLEAN(new ConfigDataBoolean(), boolean.class, Boolean.class),
        BYTE(new ConfigDataByte(), byte.class, Byte.class),
        SHORT(new ConfigDataShort(), short.class, Short.class),
        INT(new ConfigDataInt(), int.class, Integer.class),
        LONG(new ConfigDataLong(), long.class, Long.class),
        FLOAT(new ConfigDataFloat(), float.class, Float.class),
        DOUBLE(new ConfigDataDouble(), double.class, Double.class),
        CHAR(new ConfigDataChar(), char.class, Character.class),
        STRING(new ConfigDataString(), String.class),
        // Collections
        LIST(new ConfigDataList(), TypeFamily.LIST),
        BOOLEAN_LIST(new ConfigDataListBoolean(), TypeFamily.LIST, boolean.class, Boolean.class),
        BYTE_LIST(new ConfigDataListByte(), TypeFamily.LIST, byte.class, Byte.class),
        SHORT_LIST(new ConfigDataListShort(), TypeFamily.LIST, short.class, Short.class),
        INT_LIST(new ConfigDataListInt(), TypeFamily.LIST, int.class, Integer.class),
        LONG_LIST(new ConfigDataListLong(), TypeFamily.LIST, long.class, Long.class),
        FLOAT_LIST(new ConfigDataListFloat(), TypeFamily.LIST, float.class, Float.class),
        DOUBLE_LIST(new ConfigDataListDouble(), TypeFamily.LIST, double.class, Double.class),
        CHAR_LIST(new ConfigDataListChar(), TypeFamily.LIST, char.class, Character.class),
        STRING_LIST(new ConfigDataListString(), TypeFamily.LIST, String.class),
        MAP_LIST(new ConfigDataListMap(), TypeFamily.LIST, Map.class),
        STRING_MAP(new ConfigDataMapString(), TypeFamily.MAP, String.class),
        INT_MAP(new ConfigDataMapInteger(), TypeFamily.MAP, int.class, Integer.class),
        DOUBLE_MAP(new ConfigDataMapDouble(), TypeFamily.MAP, double.class, Double.class),
        // Special types
        VECTOR(new ConfigDataVector(), Vector.class),
        OFFLINE_PLAYER(new ConfigDataOfflinePlayer(), OfflinePlayer.class),
        ITEM_STACK(new ConfigDataItemStack(), ItemStack.class),
        COLOR(new ConfigDataColor(), Color.class);

        /**
         * Method to get the value of the field
         */
        @Getter
        private final ConfigData dataType;
        @Getter
        private final Class<?>[] typeClasses;
        @Getter
        private final TypeFamily family;

        ValueType(final ConfigData dataType, Class... typeClasses) {
            this(dataType, TypeFamily.PRIMITIVE, typeClasses);
        }

        ValueType(final ConfigData dataType, TypeFamily family, Class... typeClasses) {
            this.dataType = dataType;
            this.family = family;
            this.typeClasses = typeClasses;
        }

        @SuppressWarnings("Duplicates")
        public static ValueType getType(final Field field) {
            if (List.class.isAssignableFrom(field.getType())) {
                Type typeArgument = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                if (typeArgument instanceof ParameterizedType) {
                    if (Map.class.isAssignableFrom((Class<?>) ((ParameterizedType) typeArgument).getRawType())) {
                        return MAP_LIST;
                    }
                } else for (ValueType type : values()) {
                    if (!type.getFamily().equals(TypeFamily.LIST)) {
                        continue;
                    }
                    for (Class<?> typeClass : type.typeClasses) {
                        if (typeClass.isAssignableFrom((Class<?>) typeArgument)) {
                            return type;
                        }
                    }
                }
            } else if (Map.class.isAssignableFrom(field.getType())) {
                Type typeArgument = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1];
                for (ValueType type : values()) {
                    if (!type.getFamily().equals(TypeFamily.MAP)) {
                        continue;
                    }
                    for (Class<?> typeClass : type.typeClasses) {
                        if (typeClass.isAssignableFrom((Class<?>) typeArgument)) {
                            return type;
                        }
                    }
                }
            } else {
                for (ValueType type : values()) {
                    if (!type.getFamily().equals(TypeFamily.PRIMITIVE)) {
                        continue;
                    }
                    for (Class<?> typeClass : type.typeClasses) {
                        if (typeClass.isAssignableFrom(field.getType())) {
                            return type;
                        }
                    }
                }
            }
            return null;
        }

        public enum TypeFamily {
            PRIMITIVE,
            LIST,
            MAP
        }

        /**
         * Abstract Wrapper for all dataType required to work with various config data types.
         *
         * @param <T> data type
         */
        public abstract static class ConfigData<T> {

            public void set(final ConfigurationSection configuration, final String path, final T value) {
                configuration.set(path, value);
            }

            public boolean isSet(final ConfigurationSection configuration, final String path) {
                return configuration.isSet(path);
            }

            public abstract T get(ConfigurationSection configuration, String path);

            public T get(final ConfigurationSection configuration, final String path, final T def) {
                T value = get(configuration, path);
                return value == null ? def : value;
            }

            public abstract boolean isValid(ConfigurationSection configuration, String path);
        }

        ///////////////////////////////////////////////////////////////////////////
        // Base types
        ///////////////////////////////////////////////////////////////////////////

        private static class ConfigDataBoolean extends ConfigData<Boolean> {
            @Override
            public Boolean get(final ConfigurationSection configuration, final String path) {
                return configuration.getBoolean(path);
            }

            @Override
            public Boolean get(final ConfigurationSection configuration, final String path, final Boolean def) {
                Object value = configuration.get(path, def);
                if (value instanceof Boolean) {
                    return (Boolean) value;
                } else {
                    return def;
                }
            }


            @Override
            public boolean isValid(final ConfigurationSection configuration, final String path) {
                return configuration.isBoolean(path);
            }
        }

        private static class ConfigDataByte extends ConfigData<Byte> {
            @Override
            public Byte get(final ConfigurationSection configuration, final String path) {
                return (byte) configuration.getInt(path);
            }

            @Override
            public Byte get(final ConfigurationSection configuration, final String path, final Byte def) {
                Object value = configuration.get(path);
                if (value instanceof Number) {
                    return (byte) toInt(value);
                } else {
                    return def;
                }
            }

            @Override
            public boolean isValid(final ConfigurationSection configuration, final String path) {
                return configuration.isInt(path);
            }
        }

        private static class ConfigDataShort extends ConfigData<Short> {
            @Override
            public Short get(final ConfigurationSection configuration, final String path) {
                return (short) configuration.getInt(path);
            }

            @Override
            public Short get(final ConfigurationSection configuration, final String path, final Short def) {
                Object value = configuration.get(path);
                if (value instanceof Number) {
                    return (short) toInt(value);
                } else {
                    return def;
                }
            }

            @Override
            public boolean isValid(final ConfigurationSection configuration, final String path) {
                return configuration.isInt(path);
            }
        }

        private static class ConfigDataInt extends ConfigData<Integer> {
            @Override
            public Integer get(final ConfigurationSection configuration, final String path) {
                return configuration.getInt(path);
            }

            @Override
            public Integer get(final ConfigurationSection configuration, final String path, final Integer def) {
                Object value = configuration.get(path);
                if (value instanceof Number) {
                    return toInt(value);
                } else {
                    return def;
                }
            }

            @Override
            public boolean isValid(final ConfigurationSection configuration, final String path) {
                return configuration.isInt(path);
            }
        }

        private static class ConfigDataLong extends ConfigData<Long> {
            @Override
            public Long get(final ConfigurationSection configuration, final String path) {
                return configuration.getLong(path);
            }

            @Override
            public Long get(final ConfigurationSection configuration, final String path, final Long def) {
                Object value = configuration.get(path, def);
                if (value instanceof Number) {
                    return toLong(value);
                } else {
                    return def;
                }
            }

            @Override
            public boolean isValid(final ConfigurationSection configuration, final String path) {
                return configuration.isLong(path);
            }
        }

        private static class ConfigDataFloat extends ConfigData<Float> {
            @Override
            public Float get(final ConfigurationSection configuration, final String path) {
                return (float) configuration.getDouble(path);
            }

            @Override
            public Float get(final ConfigurationSection configuration, final String path, final Float def) {
                Object value = configuration.get(path);
                if (value instanceof Number) {
                    return (float) toDouble(value);
                } else {
                    return def;
                }
            }

            @Override
            public boolean isValid(final ConfigurationSection configuration, final String path) {
                return configuration.isDouble(path);
            }
        }

        private static class ConfigDataDouble extends ConfigData<Double> {
            @Override
            public Double get(final ConfigurationSection configuration, final String path) {
                return configuration.getDouble(path);
            }

            @Override
            public Double get(final ConfigurationSection configuration, final String path, final Double def) {
                Object value = configuration.get(path);
                if (value instanceof Number) {
                    return toDouble(value);
                } else {
                    return def;
                }
            }

            @Override
            public boolean isValid(final ConfigurationSection configuration, final String path) {
                return configuration.isDouble(path);
            }
        }

        private static class ConfigDataChar extends ConfigData<Character> {
            @Override
            public Character get(final ConfigurationSection configuration, final String path) {
                return configuration.getString(path).charAt(0);
            }

            @Override
            public Character get(final ConfigurationSection configuration, final String path, final Character def) {
                String value = configuration.getString(path);
                return value == null || value.isEmpty() ? def : value.charAt(0);
            }

            @Override
            public boolean isValid(final ConfigurationSection configuration, final String path) {
                return configuration.isInt(path);
            }
        }

        private static class ConfigDataString extends ConfigData<String> {
            @Override
            public String get(final ConfigurationSection configuration, final String path) {
                return configuration.getString(path);
            }

            @Override
            public String get(final ConfigurationSection configuration, final String path, final String def) {
                return configuration.getString(path, def);
            }

            @Override
            public boolean isValid(final ConfigurationSection configuration, final String path) {
                return configuration.isString(path);
            }
        }

        ///////////////////////////////////////////////////////////////////////////
        // Lists
        ///////////////////////////////////////////////////////////////////////////

        private abstract static class AbstractConfigDataList<T> extends ConfigData<List<T>> {
            @Override
            public boolean isValid(final ConfigurationSection configuration, final String path) {
                return configuration.isList(path);
            }
        }

        private static class ConfigDataList extends AbstractConfigDataList<Object> {
            @Override
            @SuppressWarnings("unchecked")
            public List<Object> get(final ConfigurationSection configuration, final String path) {
                return (List<Object>) configuration.getList(path);
            }

            @Override
            @SuppressWarnings("unchecked")
            public List<Object> get(ConfigurationSection configuration, String path, List<Object> def) {
                return configuration.getList(path) == null ? def : (List<Object>) configuration.getList(path);
            }
        }

        private static class ConfigDataListBoolean extends AbstractConfigDataList<Boolean> {
            @Override
            public List<Boolean> get(final ConfigurationSection configuration, final String path) {
                return configuration.getBooleanList(path);
            }

            @Override
            public List<Boolean> get(ConfigurationSection configuration, String path, List<Boolean> def) {
                return configuration.getList(path) == null ? def : configuration.getBooleanList(path);
            }
        }

        private static class ConfigDataListByte extends AbstractConfigDataList<Byte> {
            @Override
            public List<Byte> get(final ConfigurationSection configuration, final String path) {
                return configuration.getByteList(path);
            }

            @Override
            public List<Byte> get(ConfigurationSection configuration, String path, List<Byte> def) {
                return configuration.getList(path) == null ? def : configuration.getByteList(path);
            }
        }

        private static class ConfigDataListShort extends AbstractConfigDataList<Short> {
            @Override
            public List<Short> get(final ConfigurationSection configuration, final String path) {
                return configuration.getShortList(path);
            }

            @Override
            public List<Short> get(ConfigurationSection configuration, String path, List<Short> def) {
                return configuration.getList(path) == null ? def : configuration.getShortList(path);
            }
        }

        private static class ConfigDataListInt extends AbstractConfigDataList<Integer> {
            @Override
            public List<Integer> get(final ConfigurationSection configuration, final String path) {
                return configuration.getIntegerList(path);
            }

            @Override
            public List<Integer> get(ConfigurationSection configuration, String path, List<Integer> def) {
                return configuration.getList(path) == null ? def : configuration.getIntegerList(path);
            }
        }

        private static class ConfigDataListLong extends AbstractConfigDataList<Long> {
            @Override
            public List<Long> get(final ConfigurationSection configuration, final String path) {
                return configuration.getLongList(path);
            }

            @Override
            public List<Long> get(ConfigurationSection configuration, String path, List<Long> def) {
                return configuration.getList(path) == null ? def : configuration.getLongList(path);
            }
        }

        private static class ConfigDataListFloat extends AbstractConfigDataList<Float> {
            @Override
            public List<Float> get(final ConfigurationSection configuration, final String path) {
                return configuration.getFloatList(path);
            }

            @Override
            public List<Float> get(ConfigurationSection configuration, String path, List<Float> def) {
                return configuration.getList(path) == null ? def : configuration.getFloatList(path);
            }
        }

        private static class ConfigDataListDouble extends AbstractConfigDataList<Double> {
            @Override
            public List<Double> get(final ConfigurationSection configuration, final String path) {
                return configuration.getDoubleList(path);
            }

            @Override
            public List<Double> get(ConfigurationSection configuration, String path, List<Double> def) {
                return configuration.getList(path) == null ? def : configuration.getDoubleList(path);
            }
        }

        private static class ConfigDataListChar extends AbstractConfigDataList<Character> {
            @Override
            public List<Character> get(final ConfigurationSection configuration, final String path) {
                return configuration.getCharacterList(path);
            }

            @Override
            public List<Character> get(ConfigurationSection configuration, String path, List<Character> def) {
                return configuration.getList(path) == null ? def : configuration.getCharacterList(path);
            }
        }

        private static class ConfigDataListString extends AbstractConfigDataList<String> {
            @Override
            public List<String> get(final ConfigurationSection configuration, final String path) {
                return configuration.getStringList(path);
            }

            @Override
            public List<String> get(ConfigurationSection configuration, String path, List<String> def) {
                return configuration.getList(path) == null ? def : configuration.getStringList(path);
            }
        }

        private static class ConfigDataListMap extends AbstractConfigDataList<Map<?, ?>> {
            @Override
            public List<Map<?, ?>> get(final ConfigurationSection configuration, final String path) {
                return configuration.getMapList(path);
            }

            @Override
            public List<Map<?, ?>> get(ConfigurationSection configuration, String path, List<Map<?, ?>> def) {
                return configuration.getList(path) == null ? def : configuration.getMapList(path);
            }
        }

        ///////////////////////////////////////////////////////////////////////////
        // Maps
        ///////////////////////////////////////////////////////////////////////////

        private abstract static class AbstractConfigDataMap<K, V> extends ConfigData<Map<K, V>> {
            @Override
            public boolean isValid(final ConfigurationSection configuration, final String path) {
                return configuration.isConfigurationSection(path);
            }
        }

        private static class ConfigDataMapString extends AbstractConfigDataMap<String, String> {
            @Override
            @SuppressWarnings("unchecked")
            public Map<String, String> get(final ConfigurationSection configuration, final String path) {
                ConfigurationSection mapSection = configuration.getConfigurationSection(path);
                if (mapSection == null) {
                    return null;
                }
                Map<String, String> result = new LinkedHashMap<>();
                for (String key : mapSection.getKeys(false)) {
                    result.put(key, configuration.getString(key));
                }
                return result;
            }

            @Override
            @SuppressWarnings("unchecked")
            public Map<String, String> get(ConfigurationSection configuration, String path, Map<String, String> def) {
                Map<String, String> result = get(configuration, path);
                return result == null ? def : result;
            }
        }

        private static class ConfigDataMapInteger extends AbstractConfigDataMap<String, Integer> {
            @Override
            @SuppressWarnings("unchecked")
            public Map<String, Integer> get(final ConfigurationSection configuration, final String path) {
                ConfigurationSection mapSection = configuration.getConfigurationSection(path);
                if (mapSection == null) {
                    return null;
                }
                Map<String, Integer> result = new LinkedHashMap<>();
                for (String key : mapSection.getKeys(false)) {
                    result.put(key, configuration.getInt(key));
                }
                return result;
            }

            @Override
            @SuppressWarnings("unchecked")
            public Map<String, Integer> get(ConfigurationSection configuration, String path, Map<String, Integer> def) {
                Map<String, Integer> result = get(configuration, path);
                return result == null ? def : result;
            }
        }

        private static class ConfigDataMapDouble extends AbstractConfigDataMap<String, Double> {
            @Override
            @SuppressWarnings("unchecked")
            public Map<String, Double> get(final ConfigurationSection configuration, final String path) {
                ConfigurationSection mapSection = configuration.getConfigurationSection(path);
                if (mapSection == null) {
                    return null;
                }
                Map<String, Double> result = new LinkedHashMap<>();
                for (String key : mapSection.getKeys(false)) {
                    result.put(key, configuration.getDouble(key));
                }
                return result;
            }

            @Override
            @SuppressWarnings("unchecked")
            public Map<String, Double> get(ConfigurationSection configuration, String path, Map<String, Double> def) {
                Map<String, Double> result = get(configuration, path);
                return result == null ? def : result;
            }
        }

        // TODO: implement other types

        ///////////////////////////////////////////////////////////////////////////
        // Special types
        ///////////////////////////////////////////////////////////////////////////

        private static class ConfigDataVector extends ConfigData<Vector> {
            @Override
            public Vector get(final ConfigurationSection configuration, final String path) {
                return configuration.getVector(path);
            }

            @Override
            public boolean isValid(final ConfigurationSection configuration, final String path) {
                return configuration.isVector(path);
            }

            @Override
            public Vector get(final ConfigurationSection configuration, final String path, final Vector def) {
                return configuration.getVector(path, def);
            }
        }

        private static class ConfigDataOfflinePlayer extends ConfigData<OfflinePlayer> {
            @Override
            public OfflinePlayer get(final ConfigurationSection configuration, final String path) {
                return configuration.getOfflinePlayer(path);
            }

            @Override
            public boolean isValid(final ConfigurationSection configuration, final String path) {
                return configuration.isOfflinePlayer(path);
            }

            @Override
            public OfflinePlayer get(final ConfigurationSection configuration, final String path, final OfflinePlayer def) {
                return configuration.getOfflinePlayer(path, def);
            }
        }

        private static class ConfigDataItemStack extends ConfigData<ItemStack> {
            @Override
            public ItemStack get(final ConfigurationSection configuration, final String path) {
                return configuration.getItemStack(path);
            }

            @Override
            public ItemStack get(final ConfigurationSection configuration, final String path, final ItemStack def) {
                return configuration.getItemStack(path, def);
            }

            @Override
            public boolean isValid(final ConfigurationSection configuration, final String path) {
                return configuration.isItemStack(path);
            }
        }

        private static class ConfigDataColor extends ConfigData<Color> {
            @Override
            public Color get(final ConfigurationSection configuration, final String path) {
                return configuration.getColor(path);
            }

            @Override
            public Color get(final ConfigurationSection configuration, final String path, final Color def) {
                return configuration.getColor(path, def);
            }

            @Override
            public boolean isValid(final ConfigurationSection configuration, final String path) {
                return configuration.isColor(path);
            }
        }

        private static class ConfigDataOther extends ConfigData {
            @Override
            public Object get(final ConfigurationSection configuration, final String path) {
                return configuration.get(path);
            }

            @Override
            public boolean isValid(final ConfigurationSection configuration, final String path) {
                return configuration.isSet(path);
            }
        }
    }
}
