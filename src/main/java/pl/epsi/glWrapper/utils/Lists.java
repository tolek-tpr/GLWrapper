package pl.epsi.glWrapper.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Lists {

    @SafeVarargs
    public static <T>List<T> addToList(List<T> list, T... objects) {
        Collections.addAll(list, objects);
        return list;
    }

    @SafeVarargs
    public static <T> ArrayList<T> asArrayList(T... objects) {
        ArrayList<T> a = new ArrayList<>();
        Collections.addAll(a, objects);
        return a;
    }

    public static Object[] getArrayFromGlNumberType(GlNumberType type, int size) {
        return (Object[]) Array.newInstance(type.getClazz(), size);
    }

    public static Object toPrimitiveArray(ArrayList<Object> list, GlNumberType type) {
        int size = list.size();

        switch (type) {
            case FLOAT:
                float[] floatArr = new float[size];
                for (int i = 0; i < size; i++) {
                    floatArr[i] = ((Number) list.get(i)).floatValue();
                }
                return floatArr;

            case INT:
                int[] intArr = new int[size];
                for (int i = 0; i < size; i++) {
                    intArr[i] = ((Number) list.get(i)).intValue();
                }
                return intArr;

            /*case DOUBLE:
                double[] doubleArr = new double[size];
                for (int i = 0; i < size; i++) {
                    doubleArr[i] = ((Number) list.get(i)).doubleValue();
                }
                return doubleArr;

            case SHORT:
                short[] shortArr = new short[size];
                for (int i = 0; i < size; i++) {
                    shortArr[i] = ((Number) list.get(i)).shortValue();
                }
                return shortArr;

            case BYTE:
                byte[] byteArr = new byte[size];
                for (int i = 0; i < size; i++) {
                    byteArr[i] = ((Number) list.get(i)).byteValue();
                }
                return byteArr;

            case LONG:
                long[] longArr = new long[size];
                for (int i = 0; i < size; i++) {
                    longArr[i] = ((Number) list.get(i)).longValue();
                }
                return longArr;*/

            default:
                throw new IllegalArgumentException("Unsupported data type: " + type);
        }
    }

}
