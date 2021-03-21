package dev.azn9.mysteryWords.injector;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.FieldInfo;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Injector {

    private static final Map<Class<?>, Object> CLASSES = new HashMap<>();

    public void registerInjection(Object o) {
        System.out.println("Registered injection type " + o.getClass().getName());

        CLASSES.put(o.getClass(), o);
    }

    public void startInjection() {
        CLASSES.put(Injector.class, this);

        new ClassGraph().enableAllInfo().scan().getClassesWithFieldAnnotation(Inject.class.getName()).forEach(classInfo -> {
            for (FieldInfo fieldInfo : classInfo.getDeclaredFieldInfo()) {
                if (fieldInfo.hasAnnotation(Inject.class.getName())) {
                    Field field = fieldInfo.loadClassAndGetField();
                    field.setAccessible(true);

                    if (CLASSES.containsKey(field.getType())) {
                        System.out.println("Injecting in " + classInfo.getName() + " value " + field.getType().getName());

                        try {
                            field.set(null, field.getType().cast(CLASSES.get(field.getType())));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else
                        System.out.println("Injection for " + classInfo.getName() + " (" + field.getType().getName() + ") not found !");
                }
            }
        });
    }

    public void injectAtRuntime(Object o) {
        new ClassGraph().enableAllInfo().scan().getClassesWithFieldAnnotation(Inject.class.getName()).forEach(classInfo -> {
            for (FieldInfo fieldInfo : classInfo.getDeclaredFieldInfo()) {
                if (fieldInfo.hasAnnotation(Inject.class.getName())) {
                    Field field = fieldInfo.loadClassAndGetField();
                    field.setAccessible(true);

                    if (field.getType().isAssignableFrom(o.getClass())) {
                        try {
                            field.set(null, field.getType().cast(o));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

}