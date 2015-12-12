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
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

import java.util.HashMap;
import java.util.Map;

public class StringTranslator {
    private static final Map<String, JSONObject> STRINGS = new HashMap<>();

    private final String resource;

    public StringTranslator(String resource) {
        this.resource = resource;
    }

    public String translate(String key, Object... args) {
        return format(getString(key), args);
    }

    public String translateSelect(String key, Object select, Object... args) {
        return format(getString(key), args);
    }

    private String getString(String key) {
        JSONValue value = getStrings().get(key);
        if (value != null) {
            JSONString string = value.isString();
            if (string != null) {
                return string.stringValue();
            }
        }
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
