/*
  https://gist.github.com/mfmendiola/bb8397162df9f76681325ab9f705748b
  ArrayUtil exposes a set of helper methods for working with
  ReadableArray (by React Native), Object[], and JSONArray.
 */

package com.doochik.RNAppMetrica;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class ArrayUtil {

    public static Object[] toArray(JSONArray jsonArray) throws JSONException {
        Object[] array = new Object[jsonArray.length()];

        for (int i = 0; i < jsonArray.length(); i++) {
            Object value = jsonArray.get(i);

            if (value instanceof JSONObject) {
                value = MapUtil.toMap((JSONObject) value);
            }
            if (value instanceof JSONArray) {
                value = ArrayUtil.toArray((JSONArray) value);
            }

            array[i] = value;
        }

        return array;
    }

    public static Object[] toArray(ReadableArray readableArray) {
        Object[] array = new Object[readableArray.size()];

        for (int i = 0; i < readableArray.size(); i++) {
            ReadableType type = readableArray.getType(i);

            switch (type) {
                case Null:
                    array[i] = null;
                    break;
                case Boolean:
                    array[i] = readableArray.getBoolean(i);
                    break;
                case Number:
                    array[i] = readableArray.getDouble(i);
                    break;
                case String:
                    array[i] = readableArray.getString(i);
                    break;
                case Map:
                    array[i] = MapUtil.toMap(readableArray.getMap(i));
                    break;
                case Array:
                    array[i] = ArrayUtil.toArray(readableArray.getArray(i));
                    break;
            }
        }

        return array;
    }
}