package dic;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;

class Container {
    private final Set<Class<?>> PRIMITIVE_TYPES = Set.of(byte.class, short.class, int.class, long.class, float.class, double.class, char.class, Character.class, String.class, boolean.class, Integer.class);
    Map<String, Object> classes = new HashMap<>();
    Map<Class<?>, Class<?>> impls = new HashMap<>();
    List<Class<?>> startedClass = new ArrayList<>();
    List<Class<?>> addedProxyLazy = new ArrayList<>();
    Properties properties;

    public Container(Properties properties) {
        this.properties = properties;
    }

    public Object getInstance(String key) {
        return classes.get(key);
    }

    public <T> T getInstance(Class<T> c) throws Exception {
        if (classes.containsKey(c.getName())) {
            return (T) classes.get(c.getName());
        }
        Lazy lazy = c.getDeclaredAnnotation(Lazy.class);
        if (lazy != null) {
            if (!addedProxyLazy.contains(c)) {
                addedProxyLazy.add(c);
                return (T) new LazyClass(this);
            }
        }

        Class<?> classImpl = null;
        if (c.isInterface()) {
            if (!impls.containsKey(c)) {
                Default defaultClass = c.getDeclaredAnnotation(Default.class);
                if (defaultClass != null) {
                    classImpl = defaultClass.cl();
                    impls.put(c, classImpl);
                }
            }

            if (classImpl == null) {
                throw new RegistryException("Interface is not implemented");
            }
        }
        classImpl = classImpl != null ? classImpl : c;
        if (classes.containsKey(classImpl.getName())) {
            return (T) classes.get(classImpl.getName());
        }
        if (startedClass.size() == 0) {
            startedClass.add(c);
        }
        Object res = instanceClass(classImpl);
        classes.put(classImpl.getName(), res);
        return (T) res;
    }

    private Object instanceClass(Class<?> classImpl) throws Exception {
        Constructor<?> con = getConstructor(classImpl);
        Object inst = null;
        if (con != null) {
            Object[] params = getParams(con);
            inst = con.newInstance(params);
        } else {
            inst = classImpl.getDeclaredConstructor().newInstance();
        }
        instanceFields(inst);

        if (Initializer.class.isAssignableFrom(classImpl)) {
            ((Initializer) inst).init();
        }
        return inst;
    }

    private Object[] getParams(Constructor<?> con) throws Exception {
        Object[] params = null;
        Class<?>[] paramsTypes = con.getParameterTypes();

        if (paramsTypes.length != 0) {
            params = new Object[paramsTypes.length];
        }

        for (int i = 0; i < paramsTypes.length; i++) {
            Class<?> paramType = paramsTypes[i];
            Object paramInst = getInstance(paramType);
            params[i] = paramInst;
        }
        return params;
    }

    private Constructor<?> getConstructor(Class<?> classImpl) {
        Constructor<?> con = null;
        for (Constructor c : classImpl.getDeclaredConstructors()) {
            Inject annotation = c.getDeclaredAnnotation(Inject.class);
            if (annotation == null) {
                continue;
            }
            c.setAccessible(true);
            con = c;
            break;
        }
        return con;
    }

    private void instanceFields(Object cl) throws Exception {
        Class<?> claasImpl = cl.getClass();
        for (Field f : claasImpl.getDeclaredFields()) {
            Inject annotation = f.getAnnotation(Inject.class);
            if (annotation == null) {
                continue;
            }
            f.setAccessible(true);
            Named name = f.getAnnotation(Named.class);
            Object o = null;

            if (PRIMITIVE_TYPES.contains(f.getType())) {
                o = name != null ? properties.get(name) : properties.get(f.getName());
            }
            Class<?> checkClass = startedClass.size() != 0 ? startedClass.get(0) : null;
            if (f.getType().equals(checkClass)) {
                throw new RegistryException("Circular dependency");
            }
            if (o == null) {
                if (name != null) {
                    o = getInstance(f.getName());
                } else {
                    o = getInstance(f.getType());
                }
            }
            f.set(cl, o);
        }
    }

    public void decorateInstance(Object o) throws Exception {
        instanceFields(o);
    }

    public void registerInstance(String key, Object instance) {
        classes.put(key, instance);
    }

    public void registerImplementation(Class c, Class subClass) throws Exception {
        impls.put(c, subClass);
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
        classes.put(instance.getClass().getName(), instance);
    }
}

class LazyClass implements InvocationHandler {
    Container container;
    LazyClass(Container con) {
        this.container = con;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        container.getInstance(method.getClass());
        return method;
    }
}
