package pl.epsi.glWrapper.utils;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Utils {

    public static String readFile(InputStream in) throws IOException {
        if (in == null) throw new IllegalArgumentException("Input stream is null when retrieving file!");

        final StringBuilder sBuffer = new StringBuilder();
        final BufferedReader br = new BufferedReader(new InputStreamReader(in));
        final char[] buffer = new char[1024];

        int cnt;
        while ((cnt = br.read(buffer, 0, buffer.length)) > -1) {
            sBuffer.append(buffer, 0, cnt);
        }
        br.close();
        in.close();
        return sBuffer.toString();
    }

    public static InputStream asInputStream(File f) throws FileNotFoundException {
        return new FileInputStream(f);
    }

    public static <T> int getIndexForObject(ArrayList<T> array, T object) {
        int index = 0;
        for (T o : array) {
            if (o == object) return index;
            index++;
        }
        return -1;
    }

    public static int[] getArgbColors(int color) {
        int[] colors = new int[4];
        colors[0] = (color >> 24) & 255;
        colors[1] = (color >> 16) & 255;
        colors[2] = (color >> 8) & 255;
        colors[3] = color & 255;

        return colors;
    }

    public static float[] getArgbColors0To1(int color) {
        float[] colors = new float[4];
        colors[0] = ((color >> 24) & 255) / 255f;
        colors[1] = ((color >> 16) & 255) / 255f;
        colors[2] = ((color >> 8)  & 255) / 255f;
        colors[3] = (color         & 255) / 255f;

        return colors;
    }

    public static void printByteBuffer(ByteBuffer buf) {
        int pos = buf.position();
        int limit = buf.limit();

        System.out.print("[");
        for (int i = pos; i < limit; i++) {
            System.out.printf("%02X", buf.get(i));
            if (i < limit - 1) System.out.print(" ");
        }
        System.out.println("]");
    }

    public static <K, V> V getOrThrow(HashMap<K, V> map, K key, IllegalArgumentException err) {
        if (map.containsKey(key)) return map.get(key);
        throw err;
    }

}
