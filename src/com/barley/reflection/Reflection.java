package com.barley.reflection;

import com.barley.memory.Storage;
import com.barley.runtime.*;
import com.barley.utils.Arguments;
import com.barley.utils.BarleyException;
import com.barley.utils.CallStack;
import com.barley.utils.Function;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class Reflection {
    private static final BarleyValue NULL = new NullValue();

    public static void initConstants() {
    }

    public void inject() {
        initConstants();
        Table.define("null", NULL);
        Table.define("boolean.class", new ClassValue(boolean.class));
        Table.define("boolean[].class", new ClassValue(boolean[].class));
        Table.define("boolean[][].class", new ClassValue(boolean[][].class));
        Table.define("byte.class", new ClassValue(byte.class));
        Table.define("byte[].class", new ClassValue(byte[].class));
        Table.define("byte[][].class", new ClassValue(byte[][].class));
        Table.define("short.class", new ClassValue(short.class));
        Table.define("short[].class", new ClassValue(short[].class));
        Table.define("short[][].class", new ClassValue(short[][].class));
        Table.define("char.class", new ClassValue(char.class));
        Table.define("char[].class", new ClassValue(char[].class));
        Table.define("char[][].class", new ClassValue(char[][].class));
        Table.define("int.class", new ClassValue(int.class));
        Table.define("int[].class", new ClassValue(int[].class));
        Table.define("int[][].class", new ClassValue(int[][].class));
        Table.define("long.class", new ClassValue(long.class));
        Table.define("long[].class", new ClassValue(long[].class));
        Table.define("long[][].class", new ClassValue(long[][].class));
        Table.define("float.class", new ClassValue(float.class));
        Table.define("float[].class", new ClassValue(float[].class));
        Table.define("float[][].class", new ClassValue(float[][].class));
        Table.define("double.class", new ClassValue(double.class));
        Table.define("double[].class", new ClassValue(double[].class));
        Table.define("double[][].class", new ClassValue(double[][].class));
        Table.define("String.class", new ClassValue(String.class));
        Table.define("String[].class", new ClassValue(String[].class));
        Table.define("String[][].class", new ClassValue(String[][].class));
        Table.define("Object.class", new ClassValue(Object.class));
        Table.define("Object[].class", new ClassValue(Object[].class));
        Table.define("Object[][].class", new ClassValue(Object[][].class));

        HashMap<String, Function> reflection = new HashMap<>();
        reflection.put("is_null", this::isNull);
        reflection.put("class", this::newClass);
        reflection.put("toObject", this::toObject);
        reflection.put("toValue", this::toValue);
        reflection.put("extract", args -> {
            Arguments.check(2, args.length);
            ClassValue cl = ((ClassValue) args[0]);
            return cl.injection.get(args[1].toString());
        });
        reflection.put("object", args -> {
            Arguments.check(2, args.length);
            ObjectValue cl = ((ObjectValue) args[0]);
            return cl.injection.get(args[1].toString());
        });
        reflection.put("instance", args -> {
            Arguments.checkAtLeast(1, args.length);
            ClassValue cl = ((ClassValue) args[0]);
            return cl.newInstance(List.of(args).subList(1, args.length).toArray(new BarleyValue[]{}));
        });
        reflection.put("call", args -> {
            Arguments.checkAtLeast(2, args.length);
            ObjectValue cl = ((ObjectValue) args[0]);
            //return ((Function) cl.injection.get(args[1].toString())).execute(List.of(args).subList(2, args.length).toArray(new BarleyValue[]{}));
            //return ((BarleyFunction) getValue(cl.object.getClass(), cl.object, args[1].toString())).execute(List.of(args).subList(2, args.length).toArray(new BarleyValue[]{}));
            return ((BarleyFunction) getValue(cl.object.getClass(), cl.object, args[1].toString())).execute(List.of(args).subList(2, args.length).toArray(new BarleyValue[]{}));
        });
        reflection.put("object_to_string",args -> {
            Arguments.check(1, args.length);
            ObjectValue cl = ((ObjectValue) args[0]);
            return new BarleyString(cl.object.toString());
        });
        reflection.put("static", args -> {
            Arguments.check(2,args.length);
            ClassValue val = ((ClassValue) args[0]);
            System.out.println(val.injection);
            return val.injection.get(args[1].toString());
        });
        reflection.put("null", args -> new NullValue());

        Modules.put("reflection", reflection);
    }

    //<editor-fold defaultstate="collapsed" desc="Values">
    private static class NullValue implements BarleyValue {

        @Override
        public BigInteger asInteger() {
            return BigInteger.ONE;
        }

        @Override
        public BigDecimal asFloat() {
            return BigDecimal.ONE;
        }

        @Override
        public Object raw() {
            return null;
        }

        @Override
        public String toString() {
            return "null";
        }
    }

    private static class ClassValue implements BarleyValue {

        public HashMap<String, BarleyValue> injection;

        public static BarleyValue classOrNull(Class<?> clazz) {
            if (clazz == null) return NULL;
            return new ClassValue(clazz);
        }

        private final Class<?> clazz;

        public ClassValue(Class<?> clazz) {
            this.clazz = clazz;
            this.injection = new HashMap<>();
            init(clazz);
        }

        public BarleyValue set(String key, BarleyValue value) {
            return injection.put(key, value);
        }

        private void init(Class<?> clazz) {
            set("isAnnotation", BarleyNumber.fromBoolean(clazz.isAnnotation()));
            set("isAnonymousClass", BarleyNumber.fromBoolean(clazz.isAnonymousClass()));
            set("isArray", BarleyNumber.fromBoolean(clazz.isArray()));
            set("isEnum", BarleyNumber.fromBoolean(clazz.isEnum()));
            set("isInterface", BarleyNumber.fromBoolean(clazz.isInterface()));
            set("isLocalClass", BarleyNumber.fromBoolean(clazz.isLocalClass()));
            set("isMemberClass", BarleyNumber.fromBoolean(clazz.isMemberClass()));
            set("isPrimitive", BarleyNumber.fromBoolean(clazz.isPrimitive()));
            set("isSynthetic", BarleyNumber.fromBoolean(clazz.isSynthetic()));

            set("modifiers", new BarleyNumber(clazz.getModifiers()));

            set("canonicalName", new BarleyString(clazz.getCanonicalName()));
            set("name", new BarleyString(clazz.getName()));
            set("simpleName", new BarleyString(clazz.getSimpleName()));
            set("typeName", new BarleyString(clazz.getTypeName()));
            set("genericString", new BarleyString(clazz.toGenericString()));

            set("getComponentType", new BarleyFunction(v -> classOrNull(clazz.getComponentType()) ));
            set("getDeclaringClass", new BarleyFunction(v -> classOrNull(clazz.getDeclaringClass()) ));
            set("getEnclosingClass", new BarleyFunction(v -> classOrNull(clazz.getEnclosingClass()) ));
            set("getSuperclass", new BarleyFunction(v -> new ClassValue(clazz.getSuperclass()) ));

            set("getClasses", new BarleyFunction(v -> array(clazz.getClasses()) ));
            set("getDeclaredClasses", new BarleyFunction(v -> array(clazz.getDeclaredClasses()) ));
            set("getInterfaces", new BarleyFunction(v -> array(clazz.getInterfaces()) ));

            set("asSubclass", new BarleyFunction(this::asSubclass));
            set("isAssignableFrom", new BarleyFunction(this::isAssignableFrom));
            set("new", new BarleyFunction(this::newInstance));
            set("cast", new BarleyFunction(this::cast));

            for (Method method : clazz.getDeclaredMethods()) {
                try {
                    method.setAccessible(true);
                } catch (InaccessibleObjectException ignored) {

                }
                if (injection.containsKey(method.getName())) continue;
                set(method.getName(), new BarleyFunction(args -> {
                    try {
                        Object[] vals = valuesToObjects(args);
                        System.out.println(Arrays.toString(method.getParameterTypes()));
                        Arrays.asList(vals).forEach(a -> System.out.println("type: " + a.getClass()));
                        System.out.println(Arrays.toString(vals));
                        return objectToValue(method.invoke(clazz, vals));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    return new BarleyAtom("error");
                }));
            }
        }

        private BarleyValue asSubclass(BarleyValue[] args) {
            Arguments.check(1, args.length);
            return new ClassValue(clazz.asSubclass( ((ClassValue)args[0]).clazz ));
        }

        private BarleyValue isAssignableFrom(BarleyValue[] args) {
            Arguments.check(1, args.length);
            return BarleyNumber.fromBoolean(clazz.isAssignableFrom( ((ClassValue)args[0]).clazz ));
        }

        public BarleyValue newInstance(BarleyValue[] args) {
            return findConstructorAndInstantiate(args, clazz.getDeclaredConstructors());
        }

        private BarleyValue cast(BarleyValue[] args) {
            Arguments.check(1, args.length);
            return objectToValue(clazz, clazz.cast(((ObjectValue)args[0]).object));
        }

        @Override
        public String toString() {
            return "#ClassValue" + injection;
        }

        @Override
        public BigInteger asInteger() {
            return BigInteger.ONE;
        }

        @Override
        public BigDecimal asFloat() {
            return BigDecimal.ONE;
        }

        @Override
        public Object raw() {
            return injection;
        }
    }

    public static class ObjectValue implements BarleyValue {

        public HashMap<String, BarleyValue> injection;

        public static BarleyValue objectOrNull(Object object) {
            if (object == null) return NULL;
            return new ObjectValue(object);
        }

        public BarleyValue set(String key, BarleyValue value) {
            return injection.put(key, value);
        }

        public final Object object;

        static {
        }

        public ObjectValue(Object object) {
            this.injection = new HashMap<>();
            this.object = object; Storage.segment(this);
        }

        public BarleyValue get(BarleyValue key) {
            return getValue(object.getClass(), object, key.toString());
        }

        @Override
        public String toString() {
            return "#ObjectValue" + injection;
        }

        @Override
        public BigInteger asInteger() {
            return BigInteger.ONE;
        }

        @Override
        public BigDecimal asFloat() {
            return BigDecimal.ONE;
        }

        @Override
        public Object raw() {
            return object;
        }
    }
//</editor-fold>

    private BarleyValue isNull(BarleyValue[] args) {
        Arguments.checkAtLeast(1, args.length);
        for (BarleyValue arg : args) {
            if (arg.toString().equals("null")) return new BarleyNumber(1);
        }
        return new BarleyNumber(1);
    }

    private BarleyValue newClass(BarleyValue[] args) {
        Arguments.check(1, args.length);

        final String className = args[0].toString();
        try {
            return new ClassValue(Class.forName(className));
        } catch (ClassNotFoundException ce) {
            throw new RuntimeException("Class " + className + " not found.", ce);
        }
    }

    private BarleyValue toObject(BarleyValue[] args) {
        Arguments.check(1, args.length);
        if (args[0] == NULL) return NULL;
        return new ObjectValue(Objects.requireNonNull(valueToObject(args[0])));
    }

    private BarleyValue toValue(BarleyValue[] args) {
        Arguments.check(1, args.length);
        if (args[0] instanceof ObjectValue) {
            return objectToValue( ((ObjectValue) args[0]).object );
        }
        return NULL;
    }


    //<editor-fold defaultstate="collapsed" desc="Helpers">
    private static BarleyValue getValue(Class<?> clazz, Object object, String key) {
        // Trying to get field
        try {
            final Field field = clazz.getField(key);
            return new BarleyFunction(b -> {
                try {
                    field.setAccessible(true);
                    return objectToValue(field.getType(), field.get(object));
                } catch (IllegalAccessException | InaccessibleObjectException e) {
                    return new BarleyAtom("reflection_error");
                }
            });
        } catch (NoSuchFieldException | SecurityException |
                IllegalArgumentException ex) {
            // ignore and go to the next step
        }

        // Trying to invoke method
        try {
            final Method[] allMethods = clazz.getMethods();
            final List<Method> methods = new ArrayList<>();
            for (Method method : allMethods) {
                if (method.getName().equals(key)) {
                    methods.add(method);
                }
            }
            if (methods.isEmpty()) {
                return new BarleyFunction(a -> new BarleyAtom("empty"));
            }
            return new BarleyFunction(methodsToFunction(object, methods));
        } catch (SecurityException ex) {
            // ignore and go to the next step
        }

        return NULL;
    }

    private static BarleyValue findConstructorAndInstantiate(BarleyValue[] args, Constructor<?>[] ctors) {
        for (Constructor<?> ctor : ctors) {
            ctor.setAccessible(true);
            if (ctor.getParameterCount() != args.length) continue;
            if (!isMatch(args, ctor.getParameterTypes())) continue;
            try {
                final Object result = ctor.newInstance(valuesToObjects(args));
                return new ObjectValue(result);
            } catch (InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InaccessibleObjectException | InvocationTargetException ex) {
                // skip
            }
        }

        Object[] as =  valuesToObjects(args);
        Class<?>[] types = new Class[as.length];
        for (int i = 0; i < as.length; i++) {
            types[i] = as[i].getClass();
        }

        throw new BarleyException("BadReflection", "Can't find constructor for length " + args.length + " and for constructors " + Arrays.toString(ctors) + ". when args: " + Arrays.toString(args) + "\n    when types of args is: " + Arrays.toString(types));
    }

    private static Function methodsToFunction(Object object, List<Method> methods) {
        return (args) -> {
            for (Method method : methods) {
                try {
                    method.setAccessible(true);
                } catch (IllegalArgumentException ex) {
                    // skip
                }
                if (method.getParameterCount() != args.length) continue;
                if (!isMatch(args, method.getParameterTypes())) continue;
                try {
                    method.setAccessible(true);
                    final Object result = method.invoke(object, valuesToObjects(args));
                    if (method.getReturnType() != void.class) {
                        return objectToValue(result);
                    }
                    return new BarleyNumber(1);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    System.err.println(ex.getCause());
                    BarleyException x = (BarleyException) ex.getCause();
                    System.out.printf("**  reflection error: %s\n", x.getText());
                    int count = CallStack.getCalls().size();
                    if (count == 0) System.exit(1);
                    System.out.println(String.format("\nCall stack was:"));
                    for (CallStack.CallInfo info : CallStack.getCalls()) {
                        System.out.println("    " + count + ". " + info);
                        count--;
                    }
                    System.exit(1);
                }
            }
            final String className = (object == null ? "null" : object.getClass().getName());
            System.out.println(Arrays.toString(object.getClass().getDeclaredMethods()));
            throw new BarleyException("BadReflection", "Method" + "  with " + args.length + " arguments"
                    + " not found or non accessible in " + className);
        };
    }

    private static boolean isMatch(BarleyValue[] args, Class<?>[] types) {
        for (int i = 0; i < args.length; i++) {
            final BarleyValue arg = args[i];
            final Class<?> clazz = types[i];

            if (arg == NULL) continue;

            final Class<?> unboxed = unboxed(clazz);
            boolean assignable = unboxed != null;
            final Object object = valueToObject(arg);
            assignable = assignable && (object != null);
            if (assignable && unboxed.isArray() && object.getClass().isArray()) {
                final Class<?> uComponentType = unboxed.getComponentType();
                final Class<?> oComponentType = object.getClass().getComponentType();
                assignable = assignable && (uComponentType != null);
                assignable = assignable && (oComponentType != null);
                assignable = assignable && (uComponentType.isAssignableFrom(oComponentType));
            } else {
                assignable = assignable && (unboxed.isAssignableFrom(object.getClass()));
            }
            if (assignable) continue;

            return false;
        }
        return true;
    }

    private static Class<?> unboxed(Class<?> clazz) {
        if (clazz == null) return null;
        if (clazz.isPrimitive()) {
            if (int.class == clazz) return Integer.class;
            if (boolean.class == clazz) return Boolean.class;
            if (double.class == clazz) return Double.class;
            if (float.class == clazz) return Float.class;
            if (long.class == clazz) return Long.class;
            if (byte.class == clazz) return Byte.class;
            if (char.class == clazz) return Character.class;
            if (short.class == clazz) return Short.class;
            if (void.class == clazz) return Void.class;
        }
        return clazz;
    }

    private static BarleyList array(Class<?>[] classes) {
        final BarleyList result = new BarleyList(classes.length);
        for (int i = 0; i < classes.length; i++) {
            result.set(i, ClassValue.classOrNull(classes[i]));
        }
        return result;
    }

    private static BarleyValue objectToValue(Object o) {
        if (o == null) return NULL;
        return objectToValue(o.getClass(), o);
    }

    private static BarleyValue objectToValue(Class<?> clazz, Object o) {
        if (o == null || o == NULL) return NULL;
        if (clazz.isPrimitive()) {
            if (int.class.isAssignableFrom(clazz))
                return BarleyNumber.of((int) o);
            if (boolean.class.isAssignableFrom(clazz))
                return BarleyNumber.fromBoolean((boolean) o);
            if (double.class.isAssignableFrom(clazz))
                return BarleyNumber.of((double) o);
            if (float.class.isAssignableFrom(clazz))
                return BarleyNumber.of((float) o);
            if (long.class.isAssignableFrom(clazz))
                return BarleyNumber.of((long) o);
            if (byte.class.isAssignableFrom(clazz))
                return BarleyNumber.of((byte) o);
            if (char.class.isAssignableFrom(clazz))
                return BarleyNumber.of((char) o);
            if (short.class.isAssignableFrom(clazz))
                return BarleyNumber.of((short) o);
        }
        if (Number.class.isAssignableFrom(clazz)) {
            return BarleyNumber.of((Number) o);
        }
        if (String.class.isAssignableFrom(clazz)) {
            return new BarleyString((String) o);
        }
        if (CharSequence.class.isAssignableFrom(clazz)) {
            return new BarleyString( ((CharSequence) o).toString() );
        }
        if (o instanceof BarleyNumber) {
            return (BarleyValue) o;
        }
        if (clazz.isArray()) {
            return arrayToValue(clazz, o);
        }

        final Class<?> componentType = clazz.getComponentType();
        if (componentType != null) {
            return objectToValue(componentType, o);
        }
        return new ObjectValue(o);
    }

    private static BarleyValue arrayToValue(Class<?> clazz, Object o) {
        final int length = Array.getLength(o);
        final BarleyList result = new BarleyList(length);
        final Class<?> componentType = clazz.getComponentType();
        int i = 0;
        if (boolean.class.isAssignableFrom(componentType)) {
            for (boolean element : (boolean[]) o) {
                result.set(i++, BarleyNumber.fromBoolean(element));
            }
        } else if (byte.class.isAssignableFrom(componentType)) {
            for (byte element : (byte[]) o) {
                result.set(i++, BarleyNumber.of(element));
            }
        } else if (char.class.isAssignableFrom(componentType)) {
            for (char element : (char[]) o) {
                result.set(i++, BarleyNumber.of(element));
            }
        } else if (double.class.isAssignableFrom(componentType)) {
            for (double element : (double[]) o) {
                result.set(i++, BarleyNumber.of(element));
            }
        } else if (float.class.isAssignableFrom(componentType)) {
            for (float element : (float[]) o) {
                result.set(i++, BarleyNumber.of(element));
            }
        } else if (int.class.isAssignableFrom(componentType)) {
            for (int element : (int[]) o) {
                result.set(i++, BarleyNumber.of(element));
            }
        } else if (long.class.isAssignableFrom(componentType)) {
            for (long element : (long[]) o) {
                result.set(i++, BarleyNumber.of(element));
            }
        } else if (short.class.isAssignableFrom(componentType)) {
            for (short element : (short[]) o) {
                result.set(i++, BarleyNumber.of(element));
            }
        } else {
            for (Object element : (Object[]) o) {
                result.set(i++, objectToValue(element));
            }
        }
        return result;
    }

    private static Object[] valuesToObjects(BarleyValue[] args) {
        Object[] result = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            result[i] = valueToObject(args[i]);
        }
        return result;
    }

    private static Object valueToObject(BarleyValue value) {
        if (value == NULL) return null;
        if (value instanceof BarleyNumber n) {
            return n.raw();
        } else if (value instanceof BarleyString s) {
            return s.toString();
        } else if (value instanceof BarleyList l) {
            return arrayToObject(l);
        } else if (value instanceof BarleyReference r) {
            return r.getRef();
        } else if (value instanceof BarleyAtom a) {
            return a.toString();
        }
        if (value instanceof ObjectValue) {
            return ((ObjectValue) value).object;
        }
        if (value instanceof ClassValue) {
            return ((ClassValue) value).clazz;
        }
        return value.raw();
    }

    private static Object arrayToObject(BarleyList value) {
        final int size = value.getList().size();
        final Object[] array = new Object[size];
        if (size == 0) {
            return array;
        }

        Class<?> elementsType = null;
        for (int i = 0; i < size; i++) {
            array[i] = valueToObject(value.getList().get(i));
            if (i == 0) {
                elementsType = array[0].getClass();
            } else {
                elementsType = mostCommonType(elementsType, array[i].getClass());
            }
        }

        if (elementsType.equals(Object[].class)) {
            return array;
        }
        return typedArray(array, size, elementsType);
    }

    private static <T, U> T[] typedArray(U[] elements, int newLength, Class<?> elementsType) {
        @SuppressWarnings("unchecked")
        T[] copy = (T[]) Array.newInstance(elementsType, newLength);
        System.arraycopy(elements, 0, copy, 0, Math.min(elements.length, newLength));
        return copy;
    }

    private static Class<?> mostCommonType(Class<?> c1, Class<?> c2) {
        if (c1.equals(c2)) {
            return c1;
        } else if (c1.isAssignableFrom(c2)) {
            return c1;
        } else if (c2.isAssignableFrom(c1)) {
            return c2;
        }
        final Class<?> s1 = c1.getSuperclass();
        final Class<?> s2 = c2.getSuperclass();
        if (s1 == null && s2 == null) {
            final List<Class<?>> upperTypes = Arrays.asList(
                    Object.class, void.class, boolean.class, char.class,
                    byte.class, short.class, int.class, long.class,
                    float.class, double.class);
            for (Class<?> type : upperTypes) {
                if (c1.equals(type) && c2.equals(type)) {
                    return s1;
                }
            }
            return Object.class;
        } else if (s1 == null || s2 == null) {
            if (c1.equals(c2)) {
                return c1;
            }
            if (c1.isInterface() && c1.isAssignableFrom(c2)) {
                return c1;
            }
            if (c2.isInterface() && c2.isAssignableFrom(c1)) {
                return c2;
            }
        }

        if (s1 != null) {
            return mostCommonType(s1, c2);
        } else {
            return mostCommonType(c1, s2);
        }
    }
}
