package ir.sana.osm;

import android.webkit.WebView;
import android.webkit.WebViewClient;

public class BalloonViewClient extends WebViewClient {

    @Override
    public boolean shouldOverrideUrlLoading(WebView webView, String url) {
        return true;
    }

}
