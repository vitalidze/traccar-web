/*
 * Copyright 2015 Vitaly Litvak (vitavaque@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.web.i18n.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.json.client.*;

import java.util.HashMap;
import java.util.Map;

public class DynamicTranslator {
    private static final Map<String, JSONObject> STRINGS = new HashMap<>();

    private final String resource;

    public DynamicTranslator(String resource) {
        this.resource = resource;
    }

    public String string(String key, Object... args) {
        JSONValue value = getStrings().get(key);
        JSONString string = value == null ? null : value.isString();
        return string == null ? na(key) : format(string.stringValue(), args);
    }

    public String stringWithSelect(String key, Object select, Object... args) {
        JSONValue value = getStrings().get(key);
        JSONObject object = value == null ? null : value.isObject();
        if (object == null) {
            return na(key);
        }

        String selectKey = select instanceof Enum ? ((Enum) select).name()
                : select == null
                    ? "null" : select.toString();
        value = object.get(selectKey);
        if (value == null) {
            // default
            value = object.get("null");
        }
        JSONString string = value == null ? null : value.isString();
        Object[] allArgs = new Object[args.length + 1];
        System.arraycopy(args, 0, allArgs, 1, args.length);
        allArgs[0] = select;

        return string == null ? na(key + "[" + selectKey + "]") : format(string.stringValue(), allArgs);
    }

    public String[] stringArray(String key, Object... args) {
        JSONValue value = getStrings().get(key);
        JSONArray array = value == null ? null : value.isArray();
        if (array == null) {
            return new String[0];
        }
        String[] result = new String[array.size()];
        for (int i = 0; i < array.size(); i++) {
            result[i] = array.get(i).isString().stringValue();
        }
        return result;
    }

    public int integer(String key, Object... args) {
        JSONValue value = getStrings().get(key);
        JSONNumber number = value == null ? null : value.isNumber();
        return number == null ? Integer.MIN_VALUE : (int) number.doubleValue();
    }

    private String na(String key) {
        return "@@@@ " + key + " @@@@";
    }

    public String format(String format, Object... arguments) {
        // A very simple implementation of format
        int i = 0;
        while (i < arguments.length) {
            String delimiter = "{" + i + "}";
            while (format.contains(delimiter)) {
                format = format.replace(delimiter, String.valueOf(arguments[i]));
            }
            i++;
        }
        return format;
    }

    private JSONObject getStrings() {
        JSONObject strings = STRINGS.get(resource);
        if (strings == null) {
            String str = lookup(GWT.getModuleBaseForStaticFiles() + resource + "/" + LocaleInfo.getCurrentLocale().getLocaleName() + ".json");
            strings = JSONParser.parseStrict(str).isObject();
            STRINGS.put(resource, strings);
        }
        return strings;
    }

    private static native String lookup(String url) /*-{
        if (window.XMLHttpRequest) {
            AJAX = new XMLHttpRequest();
        } else {
            AJAX = new ActiveXObject("Microsoft.XMLHTTP");
        }
        if (AJAX) {
            AJAX.open("GET", url, false);
            AJAX.send(null);
            return AJAX.responseText;
        } else {
            return null;
        }
    }-*/;
}
