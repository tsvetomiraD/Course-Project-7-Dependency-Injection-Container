package dic;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Container {
    Map<String, Object> classes = new HashMap<>();

    public Object getInstance(String key) {
        return null;
    }

    public <T> T getInstance(Class<T> c) throws Exception {
        if (classes.containsKey(c.getName())) {
            return (T) classes.get(c.getName());
        }
        Object cl = c.getDeclaredConstructor().newInstance();
        Field[] fields = c.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            f.set(classes.get(f.getType().getName()), cl);
        }
        classes.put(c.getName(), cl);
        return (T) cl;
    }

    public void decorateInstance(Object o) {
        return;
    }

    public void registerInstance(String key, Object instance) {
        classes.put(key, instance);
    }

    public void registerImplementation(Class c, Class subClass) throws Exception {
        if (classes.containsKey(c.getName())) {
            return;
        }
        Object cl = c.getDeclaredConstructor().newInstance();
        Field[] fields = c.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            f.set(getInstance(f.getType()), cl);
        }
        classes.put(c.getName(), cl);
    }

    public void registerImplementation(Class c) throws Exception {
        if (classes.containsKey(c.getName())) {
            return;
        }
        Object cl = c.getDeclaredConstructor().newInstance();
        Field[] fields = c.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            f.set(getInstance(f.getType()), cl);
        }
        classes.put(c.getName(), cl);
    }

    public void registerInstance(Object instance) {
        classes.put("name", instance);
    }
}
