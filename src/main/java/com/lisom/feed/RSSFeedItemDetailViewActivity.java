package com.lisom.feed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

import ir.sana.osm.R;

public class RSSFeedItemDetailViewActivity extends AppCompatActivity {

    WebView webView;
    String urlReference;
    String geoReference;

    private static final String TAG = "FeedItemViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rss_feed_item_detail_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("LISOM: Feed Detail");

        Intent in = getIntent();
        urlReference = in.getStringExtra("url_reference");
        geoReference = in.getStringExtra("geo_reference");
        if (TextUtils.isEmpty(urlReference)) {
            Toast.makeText(getApplicationContext(), "URL not found", Toast.LENGTH_SHORT).show();
            finish();
        }
        webView = findViewById(R.id.webView);
        initWebView();
        String petReportLink = "gs://manvijlabs.appspot.com/tallagappa/PET report_Mar 2023.pdf";
//        resolveItemUrl(petReportLink);
//        webView.loadUrl(petReportLink);
        loadItemURLInWebView(urlReference);

    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        return true;
    }

    private void resolveItemUrl(final String gsLocationUrl){
        StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(gsLocationUrl);

        gsReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                String resolvedURL = uri.toString();
                Log.d(TAG, " Feed item location resolved url => " + resolvedURL);
                loadItemURLInWebView(resolvedURL);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.d(TAG, "Could not get download url for "+gsLocationUrl, exception);
                Toast.makeText(getApplicationContext(),
                        "Failed to load item detail view - "+exception.getMessage(),
                        Toast.LENGTH_LONG).show();

            }
        });
    }

    private void initWebView() {
        webView.setWebViewClient(new FeedItemWebViewClient(getApplicationContext()));
        webView.clearCache(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
//        webView.getSettings().setPluginsEnabled(true);
        webView.setHorizontalScrollBarEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                webView.loadUrl(url);
                return true;
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                invalidateOptionsMenu();
                Toast.makeText(getApplicationContext(), "Loading error code "+error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error Code - "+error);

            }
        });
        webView.clearCache(false);
        webView.clearHistory();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setHorizontalScrollBarEnabled(true);
    }

    private class FeedItemWebViewClient extends WebViewClient {
        Context context;
        public FeedItemWebViewClient(Context context) {
            super();
            this.context = context;
        }
        public boolean shouldOverrideUrlLoading(
                WebView view, String url) {
            return true;
        }
    }

    private void loadItemURLInWebView(String resolvedURL){
//        String page_url = "https://www.rites.com/Railways";
        webView.loadUrl(resolvedURL);
    }
}