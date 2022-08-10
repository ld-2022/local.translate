package com.ld;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 反射工具类
 */
public class ReflexFactory {

    static Map<Class<?>,Map<String,Optional<Field>>> fieldMap = new HashMap<>();
    static Map<Class<?>,Map<String,Optional<Method>>> methodMap = new HashMap<>();


    public static Optional<Method> findMethod(Class<?> clzz,String name,Class<?>... parameterTypes){
        if (methodMap.containsKey(clzz) && methodMap.get(clzz).containsKey(name)){
            return methodMap.get(clzz).get(name);
        }
        Optional<Method> optional;
        try {
            Method method = clzz.getDeclaredMethod(name,parameterTypes);
            method.setAccessible(true);
            optional =  Optional.of(method);
        } catch (NoSuchMethodException e) {
            optional = Optional.empty();
        }
        Map<String, Optional<Method>> optionalMap = methodMap.computeIfAbsent(clzz, k -> new HashMap<>());
        optionalMap.put(name,optional);
        return optional;
    }

    /**
     * 查找字段
     *
     * @param clzz
     * @param name
     * @return
     */
    public static Optional<Field> findField(Class<?> clzz, String name) {
        if (fieldMap.containsKey(clzz) && fieldMap.get(clzz).containsKey(name)){
            return fieldMap.get(clzz).get(name);
        }
        Optional<Field> optional;
        try {
            Field field = clzz.getDeclaredField(name);
            field.setAccessible(true);
            optional =  Optional.of(field);
        } catch (NoSuchFieldException e) {
            optional = Optional.empty();
        }
        Map<String, Optional<Field>> optionalMap = fieldMap.computeIfAbsent(clzz, k -> new HashMap<>());
        optionalMap.put(name,optional);
        return optional;
    }

    public static void main(String[] args) {
        ReflexFactory.findField(ReflexFactory.class,"fieldMap");
        ReflexFactory.findField(ReflexFactory.class,"fieldMap");

        ReflexFactory.findMethod(ReflexFactory.class, "main", String[].class);
       ReflexFactory.findMethod(ReflexFactory.class, "main", String[].class);

    }
}
