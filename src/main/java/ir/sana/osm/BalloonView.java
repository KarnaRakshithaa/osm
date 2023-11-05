package ir.sana.osm;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;

public class BalloonView extends BasicInfoWindow {

    BalloonViewClient lisomWebClient;

    public BalloonView(int layoutResId, MapView mapView) {
        super(layoutResId, mapView);
        lisomWebClient = new BalloonViewClient();

    }

    @Override
    public void onOpen(Object item) {
        Marker markerItem = (Marker)item;
        String customHtml = markerItem.getSnippet();
        System.out.println("Bubbleview on open - "+ customHtml);




        AlertDialog.Builder alert = new AlertDialog.Builder(getMapView().getContext());
        alert.setTitle(markerItem.getTitle());

        WebView wv = new WebView(getMapView().getContext());
//        wv.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
//        wv.getSettings().setAllowContentAccess(true);

        try {
            String base64 = null;
            base64 = android.util.Base64.encodeToString(customHtml.getBytes("UTF-8"),
                    android.util.Base64.DEFAULT);

            wv.loadData(base64, "text/html; charset=utf-8", "base64");
//            wv.loadData(getBase64HtmlInIframe(base64),"text/html; charset=utf-8", "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

//        wv.loadData(customHtml, "text/html", "UTF-8");

        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                view.loadUrl(url);

                return true;
            }
        });



        alert.setView(wv);
        alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        alert.show();


//        super.onOpen(item);



    }


    private String getBase64HtmlInIframe(String base64){
        String basicContent = "" +
                "<html>\n" +
                "<body>\n" +
                "\t<h1>I serve ads!</h1>\n" +
                "\t<iframe src=\"data:text/html;base64,"+base64+"\"></iframe>\n" +
                "</body>\n" +
                "</html>";

        return basicContent;


    }




    @Override
    public void onClose() {

    }
}
