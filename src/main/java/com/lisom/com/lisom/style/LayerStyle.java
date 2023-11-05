package com.lisom.com.lisom.style;

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import ir.sana.osm.R;

public class LayerStyle {

    // K = layer name, V = LayerStyle object
    private static HashMap<String, LayerStyle> styleHashMap;

    // K = iconByName, V = Drawable
    private static HashMap<String, Drawable> iconName2DrawableMap;

    String layerName;
    PointFeatureStyle pointStyle;
    LineFeatureStyle lineStyle;
    PolygonFeatureStyle polygonStyle;

    public LayerStyle(String layerName,
                      PointFeatureStyle ptStyle,
                      LineFeatureStyle lnStyle,
                      PolygonFeatureStyle pnStyle
                      ){
        this.layerName = layerName;
        this.pointStyle = ptStyle;
        this.lineStyle = lnStyle;
        this.polygonStyle = pnStyle;
    }

    public PointFeatureStyle getPointStyle(){
        return pointStyle;
    }

    public LineFeatureStyle getLineStyle(){
        return lineStyle;
    }

    public PolygonFeatureStyle getPolygonStyle(){
        return polygonStyle;
    }


    public static void loadStyles(Context ctx){

        loadIconDrawablesMap(ctx);

        if(styleHashMap != null)
            styleHashMap.clear();
        styleHashMap = null;


        styleHashMap = new HashMap<>(16);

        // TODO - undo these comments after actual implementation
        // String jsonString = loadJSONFromAsset(ctx);
        // Object jsonObj = parseJSONIntoJavaObject(jsonString);
        // buildStyleMap(jsonObj);
        buildHardcodedStyleMap(null);
    }

    public static LayerStyle getStyleForLayer(String layerName){
        if(styleHashMap == null)
            throw new RuntimeException("LayerStyle Map has not been loaded ");
        LayerStyle layerStyle = styleHashMap.get(layerName);
        return layerStyle;
    }

    private static void loadIconDrawablesMap(Context ctx){

        if(iconName2DrawableMap != null){
            iconName2DrawableMap.clear();
            iconName2DrawableMap = null;
        }

        iconName2DrawableMap = new HashMap<>(16);



        iconName2DrawableMap.put("rlwys_station",
                ctx.getDrawable(R.drawable.rlwys_station));

        iconName2DrawableMap.put("rlwys_roadunderbridge",
                ctx.getDrawable(R.drawable.rlwys_roadunderbridge));

        iconName2DrawableMap.put("rlwys_roadoverbridge",
                ctx.getDrawable(R.drawable.rlwys_roadoverbridge));

        iconName2DrawableMap.put("rlwys_minorbridge",
                ctx.getDrawable(R.drawable.rlwys_minorbridge));

        iconName2DrawableMap.put("rlwys_majorbridge",
                ctx.getDrawable(R.drawable.rlwys_majorbridge));

        iconName2DrawableMap.put("rlwys_kmpost",
                null);

        iconName2DrawableMap.put("rlwys_point",
                ctx.getDrawable(R.drawable.rlwys_point));

        iconName2DrawableMap.put("rlwys_benchmark",
                ctx.getDrawable(R.drawable.rlwys_benchmark));

        iconName2DrawableMap.put("baseline_push_pin_24",
                ctx.getDrawable(R.drawable.baseline_push_pin_24));


    }

    private String loadJSONFromAsset(Context ctx) {
        String json;
        try {
            InputStream is = ctx.getAssets().open("layer_icons.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }




    private static Object parseJSONIntoJavaObject(String style_json_content){
        //TODO - write actual json parsing logic
        return null;

    }

    private static void buildStyleMap(Object jsonObj){
        //TODO - write actual implementation
    }

    private static void buildHardcodedStyleMap(Object jsonObj){
        styleHashMap.put("Alignment", null);

        LayerStyle stnLayerStyle = new LayerStyle("Stations",
                new PointFeatureStyle("rlwys_station"),
                null, null);
        styleHashMap.put("Stations", stnLayerStyle);

        LayerStyle rubLayerStyle = new LayerStyle("RUB",
                new PointFeatureStyle("rlwys_roadunderbridge"),
                null, null);
        styleHashMap.put("RUB", rubLayerStyle);


        LayerStyle robLayerStyle = new LayerStyle("ROB",
                new PointFeatureStyle("rlwys_roadoverbridge"),
                null, null);
        styleHashMap.put("ROB", robLayerStyle);


        LayerStyle minorBridgeLayerStyle = new LayerStyle("Minorbridges",
                new PointFeatureStyle("rlwys_minorbridge"),
                null, null);
        styleHashMap.put("Minorbridges", minorBridgeLayerStyle);


        LayerStyle majorBridgeLayerStyle = new LayerStyle("Majorbridges",
                new PointFeatureStyle("rlwys_majorbridge"),
                null, null);
        styleHashMap.put("Majorbridges", majorBridgeLayerStyle);

        LayerStyle kmPostLayerStyle = new LayerStyle("KMPost",
                new PointFeatureStyle(),
                null, null);
        styleHashMap.put("KMPost", kmPostLayerStyle);


        styleHashMap.put("Curves", null);


        LayerStyle gradPostLayerStyle = new LayerStyle("Gradient Post",
                new PointFeatureStyle("rlwys_point"),
                null, null);
        styleHashMap.put("Gradient Post", gradPostLayerStyle);


        styleHashMap.put("Mj_Br catchment", null);
        styleHashMap.put("MjBr_longeststream", null);


        LayerStyle benchmarksLayerStyle = new LayerStyle("Survey benchmarks",
                new PointFeatureStyle("rlwys_benchmark"),
                null, null);
        styleHashMap.put("Survey benchmarks", benchmarksLayerStyle);

        styleHashMap.put("Villages", null);
        styleHashMap.put("Powerlines", null);
    }



    public static class PointFeatureStyle {
        private String iconName;
        private boolean textIconFlag;



        public PointFeatureStyle(String iconName){
            this.iconName = iconName;
            textIconFlag = false;
        }

        public PointFeatureStyle(){
            this.iconName = null;
            textIconFlag = true;
        }

        public boolean hasTextIcon(){
            return textIconFlag;
        }

        public Drawable getIconObject(){
            if(textIconFlag)
                return null;
            else {
                Drawable iconDrawable = iconName2DrawableMap.get(iconName);
                if(iconDrawable != null)
                    return iconDrawable;
                else {
                    // default to pushpin icon
                    Drawable pushpinDrawable = iconName2DrawableMap.get("baseline_push_pin_24");
                    return pushpinDrawable;
                }

            }


        }

    }

    public static class LineFeatureStyle {
        private String lineStyle; // solid-line, dotted-line, custom-line1, custom-line2,...
        private int lineColor;
        private int lineWidth;
    }

    public static class PolygonFeatureStyle {

        private String lineStyle; // solid-line, dotted-line, custom-line1, custom-line2,...
        private int lineColor;
        private int lineWidth;

    }

}
