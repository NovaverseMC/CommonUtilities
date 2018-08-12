package it.feargames.commonutilities.util;

import lombok.experimental.UtilityClass;
import lombok.extern.java.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@UtilityClass
@Log
public class ReflectionUtils {

    private static final Method getStackTraceElementMethod;

    static {
        try {
            getStackTraceElementMethod = Throwable.class.getDeclaredMethod("getStackTraceElement", int.class);
            getStackTraceElementMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            log.severe("Unable to setup ReflectionUtils!");
            throw new RuntimeException(e);
        }
    }

    /**
     * Faster alternative to Throwable.getStackTrace()[element],
     * it doesn't generate the full stack trace.
     *
     * @param index the index of the stacktrace element.
     */
    public static StackTraceElement getStackTraceElement(int index) {
        try {
            return (StackTraceElement) getStackTraceElementMethod.invoke(new Throwable(), index);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
