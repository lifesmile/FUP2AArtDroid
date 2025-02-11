package com.faceunity.pta_art.constant;

import android.content.Context;
import android.util.Log;

import com.faceunity.pta_art.ui.seekbar.ColorPickGradient;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tujh on 2018/11/6.
 */
public abstract class ColorConstant {
    private static final String TAG = ColorConstant.class.getSimpleName();

    public static double[][] skin_color;
    public static double[][] lip_color;
    public static double[][] iris_color;
    public static double[][] hair_color;
    public static double[][] beard_color;
    public static double[][] glass_frame_color;
    public static double[][] glass_color;
    public static double[][] hat_color;

    public static void init(Context context) {
        try {
            InputStream is = context.getAssets().open(FilePathFactory.jsonColor());
            byte[] itemData = new byte[is.available()];
            is.read(itemData);
            is.close();
            String json = new String(itemData);
            JSONObject jsonObject = new JSONObject(json);

            skin_color = parseJson(jsonObject, "skin_color");
            lip_color = parseJson(jsonObject, "lip_color");
            iris_color = parseJson(jsonObject, "iris_color");
            hair_color = parseJson(jsonObject, "hair_color");
            beard_color = parseJson(jsonObject, "beard_color");
            glass_frame_color = parseJson(jsonObject, "glass_frame_color");
            glass_color = parseJson(jsonObject, "glass_color");
            hat_color = parseJson(jsonObject, "hat_color");

            ColorPickGradient.init(skin_color);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static double[][] parseJson(JSONObject jsonObject, String key) throws Exception {
        JSONObject object = jsonObject.getJSONObject(key);
        List<double[]> colors = new ArrayList<>();
        for (int i = 1; object.has(String.valueOf(i)); i++) {
            JSONObject color = object.getJSONObject(String.valueOf(i));
            int r = color.getInt("r");
            int g = color.getInt("g");
            int b = color.getInt("b");
            if (color.has("intensity")) {
                int intensity = color.getInt("intensity");
                colors.add(new double[]{r, g, b, intensity});
            } else {
                colors.add(new double[]{r, g, b});
            }
        }
        double[][] doubles = new double[colors.size()][];
        colors.toArray(doubles);
//        Log.e(TAG, key + Arrays.toString(doubles));
        return doubles;
    }

    public static double[] getColor(double[][] colors, double value) {
        int index = (int) value;
        if (index >= colors.length - 1) return colors[colors.length - 1];
        if (index < 0) return colors[0];
        double v = value - index;
        double[] c = Arrays.copyOf(colors[index], colors[index].length);
        c[0] = colors[index][0] + v * (colors[index + 1][0] - colors[index][0]);
        c[1] = colors[index][1] + v * (colors[index + 1][1] - colors[index][1]);
        c[2] = colors[index][2] + v * (colors[index + 1][2] - colors[index][2]);
        return c;
    }

    /**
     * 取到某点的颜色值
     *
     * @return
     */
    public static double[] getRadioColor(double radio) {
        Log.i("ssss", Arrays.toString(ColorPickGradient.getColor(radio)));
        return ColorPickGradient.getColor(radio);
    }
}
