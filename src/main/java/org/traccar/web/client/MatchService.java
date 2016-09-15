/*
 * Copyright 2016 Vitaly Litvak (vitavaque@gmail.com)
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
package org.traccar.web.client;

public abstract class MatchService {
    protected final String url;

    public MatchService(String url) {
        this.url = url;
    }

    public abstract void load(Track track, Callback callback);

    public interface Callback {
        void onSuccess(Track track);
        void onError(int code, String text);
    }

    static native String formatLonLat(double lonLat) /*-{
        return lonLat.toFixed(6);
    }-*/;
}
