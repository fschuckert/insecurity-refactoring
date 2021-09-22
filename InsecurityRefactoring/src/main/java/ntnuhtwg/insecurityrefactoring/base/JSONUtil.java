/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base;

import java.util.LinkedList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author blubbomat
 */
public class JSONUtil {

    public static <E> List<E> getListSave(JSONObject json, String listName, Class<E> clazz) {
        List<E> retval = new LinkedList<>();

        if (json.containsKey(listName)) {
            for (Object obj : getObjectSave(json, listName, JSONArray.class)) {
                if (clazz.isInstance(obj)) {
                    retval.add(clazz.cast(obj));
                }
            }
        }

        return retval;
    }

    public static <E> E getObjectSave(JSONObject json, String attribute, Class<E> clazz) {
        return getObjectSave(json, attribute, clazz, null);
    }

    public static <E> E getObjectSave(JSONObject json, String attribute, Class<E> clazz, E defaultValue) {
        if (json.containsKey(attribute)) {
            Object obj = json.get(attribute);
            if (clazz.isInstance(obj)) {
                return clazz.cast(obj);
            }
        }

        return defaultValue;
    }

    public static JSONArray toJSONArray(List<String> codeLines) {
        JSONArray retval = new JSONArray();
        for (String str : codeLines) {
            retval.add(str);
        }
        return retval;
    }
    
    public static JSONArray toJSONArray(String[] codeLines) {
        JSONArray retval = new JSONArray();
        for (String str : codeLines) {
            retval.add(str);
        }
        return retval;
    }
}
