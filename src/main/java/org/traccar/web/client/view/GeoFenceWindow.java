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
package org.traccar.web.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ColorPalette;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.event.CloseEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.*;
import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.Style;
import org.gwtopenmaps.openlayers.client.StyleMap;
import org.gwtopenmaps.openlayers.client.control.DrawFeature;
import org.gwtopenmaps.openlayers.client.control.DrawFeatureOptions;
import org.gwtopenmaps.openlayers.client.control.ModifyFeature;
import org.gwtopenmaps.openlayers.client.control.ModifyFeatureOptions;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.geometry.Geometry;
import org.gwtopenmaps.openlayers.client.handler.*;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.traccar.web.client.GeoFenceDrawing;
import org.traccar.web.client.model.EnumKeyProvider;
import org.traccar.web.client.model.GeoFenceProperties;
import org.traccar.web.shared.model.*;

import java.util.Arrays;

public class GeoFenceWindow implements Editor<GeoFence> {

    private static GeoFenceDialogUiBinder uiBinder = GWT.create(GeoFenceDialogUiBinder.class);

    interface GeoFenceDialogUiBinder extends UiBinder<Widget, GeoFenceWindow> {
    }

    private GeoFenceDriver driver = GWT.create(GeoFenceDriver.class);

    interface GeoFenceDriver extends SimpleBeanEditorDriver<GeoFence, GeoFenceWindow> {
    }

    public interface GeoFenceHandler {
        public void onSave(GeoFence geoFence);
        public void onClear();
        public void onCancel();
        public GeoFenceDrawing repaint(GeoFence geoFence);
    }

    private final GeoFenceHandler geoFenceHandler;
    private final Map map;
    private final Vector geoFenceLayer;

    @UiField
    Window window;

    @UiField
    TextField name;

    @UiField
    TextArea description;

    @UiField(provided = true)
    ComboBox<GeoFenceType> type;

    @UiField(provided = true)
    NumberPropertyEditor<Float> floatPropertyEditor = new NumberPropertyEditor.FloatPropertyEditor();

    @UiField
    NumberField<Float> radius;

    @UiField
    ColorPalette color;

    Style lineStyle;
    DrawFeature drawLineFeatureControl;
    Style polygonStyle;
    DrawFeature drawPolygonFeatureControl;
    RegularPolygonHandlerOptions circleOptions;
    DrawFeature drawCircleFeatureControl;

    ModifyFeature modifyFeature;
    final GeoFence geoFence;
    GeoFenceDrawing geoFenceDrawing;

    public GeoFenceWindow(GeoFence geoFence, GeoFenceDrawing geoFenceDrawing, Map map, Vector geoFenceLayer, GeoFenceHandler geoFenceHandler) {
        this.geoFenceHandler = geoFenceHandler;
        this.map = map;
        this.geoFenceLayer = geoFenceLayer;
        this.geoFence = new GeoFence();

        if (geoFence != null) {
            this.geoFence.copyFrom(geoFence);
        }

        ListStore<GeoFenceType> geoFenceTypeStore = new ListStore<GeoFenceType>(
                new EnumKeyProvider<GeoFenceType>());
        geoFenceTypeStore.addAll(Arrays.asList(GeoFenceType.values()));
        type = new ComboBox<GeoFenceType>(
                geoFenceTypeStore, new GeoFenceProperties.GeoFenceTypeLabelProvider());

        type.setForceSelection(true);
        type.setTriggerAction(ComboBoxCell.TriggerAction.ALL);

        uiBinder.createAndBindUi(this);

        driver.initialize(this);
        driver.edit(this.geoFence);

        this.geoFenceDrawing = geoFenceDrawing;
        if (geoFenceDrawing == null) {
            draw();
        } else {
            edit();
        }
//        // Create a line
//        lineStyle = new Style(); //create a Style to use
//        lineStyle.setFillColor("white");
//        lineStyle.setPointRadius(5d);
//        lineStyle.setStrokeWidth(15d);
////        drawStyle.setStrokeWidth(5d / map.getResolution());
//        lineStyle.setStrokeOpacity(0.6);
//
//        //Create PathHanlderOptions using this StyleMap
//        PathHandlerOptions phOpt = new PathHandlerOptions();
//        phOpt.setStyleMap(new StyleMap(lineStyle));
//
//        //Create DrawFeatureOptions and set the PathHandlerOptions (that have the StyleMap, that have the Style we wish)
//        DrawFeatureOptions drawFeatureOptions = new DrawFeatureOptions();
//        drawFeatureOptions.setHandlerOptions(phOpt);
//
//        // Create the drawline control
//        drawLineFeatureControl = new DrawFeature(geoFenceLayer, new PathHandler(), drawFeatureOptions);
//        map.addControl(drawLineFeatureControl);
//
//        // Create circle
//        circleOptions = new RegularPolygonHandlerOptions();
//        circleOptions.setSides(40);
//        drawFeatureOptions = new DrawFeatureOptions();
//        drawFeatureOptions.setHandlerOptions(circleOptions);
//        drawCircleFeatureControl = new DrawFeature(geoFenceLayer, new RegularPolygonHandler(), drawFeatureOptions);
//        map.addControl(drawCircleFeatureControl);
//
//        // Create polygon
//        polygonStyle = new Style(); //create a Style to use
//        polygonStyle.setFillColor("white");
//        polygonStyle.setPointRadius(5d);
//        polygonStyle.setStrokeWidth(30d);
//        polygonStyle.setStrokeOpacity(0.6);
//        polygonStyle.setStrokeColor(geoFence.getColor());
//
//        //Create PathHanlderOptions using this StyleMap
//        phOpt = new PathHandlerOptions();
//        phOpt.setStyleMap(new StyleMap(polygonStyle));
//
//        //Create DrawFeatureOptions and set the PathHandlerOptions (that have the StyleMap, that have the Style we wish)
//        drawFeatureOptions = new DrawFeatureOptions();
//        drawFeatureOptions.setHandlerOptions(phOpt);
//
//        // Create the drawPolygon control
//        drawPolygonFeatureControl = new DrawFeature(geoFenceLayer, new PolygonHandler(), drawFeatureOptions);
//        map.addControl(drawPolygonFeatureControl);
//
//        // activate selected control
//        getControl(geoFence.getType()).activate();
//        setUpColor(geoFence.getColor());
    }

    public void show() {
        window.show();
    }

    public void hide() {
        window.hide();
    }

    @UiHandler("saveButton")
    public void onSaveClicked(SelectEvent event) {
        window.hide();
        removeControls();
        geoFenceHandler.onSave(flush());
    }

    @UiHandler("clearButton")
    public void onClearClicked(SelectEvent event) {
        if (getActiveControl() != null) {
            getActiveControl().cancel();
        }
        geoFenceHandler.onClear();
    }

    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        removeControls();
        geoFenceHandler.onCancel();
        window.hide();
    }

    @UiHandler("type")
    public void onTypeChanged(SelectionEvent<GeoFenceType> event) {
        getControl(type.getValue()).cancel();
        getControl(type.getValue()).deactivate();
        getControl(event.getSelectedItem()).activate();
    }

    @UiHandler("color")
    public void onColorChanged(ValueChangeEvent<String> event) {
        repaint();
    }

    @UiHandler("radius")
    public void onRadiusChanged(ValueChangeEvent<Float> event) {
        repaint();
    }

    private void repaint() {
        modifyFeature.deactivate();
        geoFenceDrawing = geoFenceHandler.repaint(flush());
        edit();
    }

    private GeoFence flush() {
        GeoFence updated = driver.flush();
        Geometry geometry = geoFenceDrawing.getShape().getGeometry();
        switch (type.getCurrentValue()) {
            case CIRCLE:
                LonLat center = geometry.getBounds().getCenterLonLat();
                center.transform(map.getProjection(), "EPSG:4326");
                updated.points(new GeoFence.LonLat(center.lon(), center.lat()));
                break;
        }
        return updated;
    }

    private void setUpColor(String color) {
        switch (type.getCurrentValue()) {
            case POLYGON:
                polygonStyle.setFillColor("#" + color);
                ((PathHandler) drawPolygonFeatureControl.getHandler()).setStyle(polygonStyle);
                break;
            case LINE:
                lineStyle.setStrokeColor("#" + color);
                ((PathHandler) drawLineFeatureControl.getHandler()).setStyle(lineStyle);
                break;
            case CIRCLE:
                // TODO
//                circleOptions.set
                break;
        }
    }

    private void startDrawing(DrawFeature control) {
        DrawFeatureOptions drawFeatureOptions = new DrawFeatureOptions();
        drawFeatureOptions.onFeatureAdded(new DrawFeature.FeatureAddedListener() {
            @Override
            public void onFeatureAdded(VectorFeature vectorFeature) {
                // TODO
            }
        });
    }

    private DrawFeature getActiveControl() {
       return getControl(type.getValue());
    }

    private DrawFeature getControl(GeoFenceType type) {
        switch (type) {
            case LINE:
                return drawLineFeatureControl;
            case CIRCLE:
                return drawCircleFeatureControl;
            case POLYGON:
                return drawPolygonFeatureControl;
        }
        return null;
    }

    private void removeControls() {
        if (getActiveControl() != null) {
            getActiveControl().deactivate();
            getActiveControl().cancel();

            map.removeControl(drawCircleFeatureControl);
            map.removeControl(drawLineFeatureControl);
            map.removeControl(drawPolygonFeatureControl);
        }

        if (modifyFeature != null) {
            modifyFeature.deactivate();
            map.removeControl(modifyFeature);
        }
    }

    private void draw() {

    }

    private void edit() {
        if (modifyFeature == null) {
            // add editing feature
            ModifyFeatureOptions options = new ModifyFeatureOptions();
            options.setClickout(false);
            options.setStandalone(true);
            options.setToggle(false);
            modifyFeature = new ModifyFeature(geoFenceLayer, options);
            map.addControl(modifyFeature);
        }

        modifyFeature.activate();
        if (type.getValue() == GeoFenceType.CIRCLE) {
            modifyFeature.setMode(ModifyFeature.DRAG);
        } else if (type.getValue() == GeoFenceType.LINE) {
            modifyFeature.setMode(ModifyFeature.RESHAPE);
        } else if (type.getValue() == GeoFenceType.POLYGON) {
            modifyFeature.setMode(ModifyFeature.DRAG | ModifyFeature.RESHAPE);
        }
        modifyFeature.selectFeature(geoFenceDrawing.getShape());
    }
}
