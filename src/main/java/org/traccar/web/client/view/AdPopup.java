/*
 * Copyright 2014 Vitaly Litvak (vitavaque@gmail.com)
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
package org.traccar.web.client.view;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

public class AdPopup extends PopupPanel implements ResizeHandler {
    final static int WIDTH = 420;
    final static int HEIGHT = 90;

    public AdPopup() {
        super(false, false);

        VerticalPanel container = new VerticalPanel();
        container.setWidth("100%");
        container.setHeight("100%");
        setWidget(container);
        setStyleName("adPopup");

        HorizontalPanel header = new HorizontalPanel();
        header.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        header.setStyleName("adHeader");
        Anchor close = new Anchor("X");
        close.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                hide();
            }
        });
        header.add(close);
        container.add(header);

        Panel content = new HTMLPanel("Your ad contents go here");
        content.setStyleName("adContent");
        container.add(content);

        Window.addResizeHandler(this);
    }

    @Override
    public void onResize(ResizeEvent resizeEvent) {
        if (isVisible()) {
            position();
        }
    }

    public void position() {
        setPopupPosition((Window.getClientWidth() - WIDTH) / 2, Window.getClientHeight() - HEIGHT - 5);
    }
}
