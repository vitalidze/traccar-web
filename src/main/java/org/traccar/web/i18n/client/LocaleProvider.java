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

public class LocaleProvider {
    private final String localeQueryParam;
    private final String localeCookie;

    public LocaleProvider(String localeQueryParam, String localeCookie) {
        this.localeQueryParam = localeQueryParam;
        this.localeCookie = localeCookie;
    }

    public String getLocale() {
        String locale = getLocaleByQueryParam(localeQueryParam);
        if (locale == null) {
            locale = getLocaleByCookie(localeCookie);
        }
        if (locale == null) {
            locale = "default"; // fallback
        }
        return locale;
    }

    private static native String getLocaleByQueryParam(String localeQueryParam) /*-{
        var queryParam = location.search;
        var qpStart = queryParam.indexOf(localeQueryParam);
        if (qpStart >= 0) {
            var end = queryParam.indexOf("&", qpStart);
            if (end < 0) {
                end = queryParam.length;
            }
            return queryParam.substring(qpStart + localeQueryParam.length + 1, end);
        }
        return null;
    }-*/;

    private static native String getLocaleByCookie(String localeCookie) /*-{
        var cookies = $doc.cookie;
        var idx = cookies.indexOf(localeCookie + "=");
        if (idx >= 0) {
            var end = cookies.indexOf(';', idx);
            if (end < 0) {
                end = cookies.length;
            }
            return cookies.substring(idx + localeCookie.length + 1, end);
        }
        return null;
    }-*/;
}
