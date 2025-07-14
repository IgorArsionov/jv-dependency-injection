package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private static final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class: " + clazz.getName()
                    + "  is not marked with @Component and cannot be injected");
        }
        Object classInstance = null;
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field: declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                classInstance = createNewInstance(clazz);

                try {
                    field.setAccessible(true);
                    field.set(classInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (classInstance == null) {
            classInstance = createNewInstance(clazz);
        }
        return classInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (NoSuchMethodException | InvocationTargetException
                 | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImpl = new HashMap();
        interfaceImpl.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImpl.put(ProductService.class, ProductServiceImpl.class);
        interfaceImpl.put(ProductParser.class, ProductParserImpl.class);

        if (interfaceClazz.isInterface()) {
            return interfaceImpl.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
