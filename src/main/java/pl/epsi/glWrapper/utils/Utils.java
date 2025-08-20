package pl.epsi.glWrapper.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Utils {

    public static String readFile(InputStream in) throws IOException {
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

}
