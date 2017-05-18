/*
 * Copyright 2017 Godwin peter .O (godwin@peter.com.ng)
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

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ImagePreLoader {
    private final Element loadingArea;
    private Set<ImageLoader> imageLoaders;
    private final Listener listener;

    public interface Listener {
        void imagesLoaded();
    }

    private static class ImageLoader {
        final ImageElement image = DOM.createImg().cast();
        final ImageResource resource;
        final String url;

        ImageLoader(Element loadingArea, ImageResource resource) {
            init(loadingArea);
            this.url = null;
            this.resource = resource;
        }

        ImageLoader(Element loadingArea, String url) {
            init(loadingArea);
            this.url = url;
            this.resource = null;
        }

        private void init(Element loadingArea) {
            Event.sinkEvents(image, Event.ONLOAD | Event.ONERROR);
            loadingArea.appendChild(image);
        }

        void start() {
            if (url == null) {
                image.setSrc(resource.getSafeUri().asString());
            } else {
                image.setSrc(url);
            }
        }

        public boolean imageEquals(ImageElement image) {
            return this.image == image;
        }
    }

    public ImagePreLoader(Collection<ImageResource> imagesToLoad, Listener listener) {
        this(listener);
        // init image loaders
        imageLoaders = new HashSet<>(imagesToLoad.size());
        for (ImageResource resource : imagesToLoad) {
            imageLoaders.add(new ImageLoader(loadingArea, resource));
        }
    }

    public ImagePreLoader(Set<String> imagesToLoad, Listener listener) {
        this(listener);
        // init image loaders
        imageLoaders = new HashSet<>(imagesToLoad.size());
        for (String url : imagesToLoad) {
            imageLoaders.add(new ImageLoader(loadingArea, url));
        }
    }

    private ImagePreLoader(Listener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        this.listener = listener;
        // init 'loading area'
        loadingArea = DOM.createDiv();
        loadingArea.getStyle().setProperty("visibility", "hidden");
        loadingArea.getStyle().setProperty("position", "absolute");
        loadingArea.getStyle().setProperty("width", "1px");
        loadingArea.getStyle().setProperty("height", "1px");
        loadingArea.getStyle().setProperty("overflow", "hidden");
    }

    public void load() {
        // init elements
        Document.get().getBody().appendChild(loadingArea);
        // set up listener
        Event.setEventListener(loadingArea, new EventListener() {
            public void onBrowserEvent(Event event) {
                boolean success;
                if (Event.ONLOAD == event.getTypeInt()) {
                    success = true;
                } else if (Event.ONERROR == event.getTypeInt()) {
                    success = false;
                } else {
                    return;
                }

                if (!ImageElement.is(event.getCurrentEventTarget()))
                    return;

                ImageElement image = ImageElement.as(Element.as(event.getCurrentEventTarget()));

                for (Iterator<ImageLoader> it = imageLoaders.iterator(); it.hasNext(); ) {
                    if (it.next().imageEquals(image)) {
                        it.remove();
                    }
                }

                if (imageLoaders.isEmpty()) {
                    Document.get().getBody().removeChild(loadingArea);
                    listener.imagesLoaded();
                }
            }
        });
        // start loading
        for (ImageLoader loader : new HashSet<>(imageLoaders)) {
            loader.start();
        }
    }
}
