package com.lisom.feed;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import ir.sana.osm.R;

public class NewFeedItemActivity extends AppCompatActivity {

    private RSSFeedItemModel.FeedItemType selectedEntryType;

    SimpleDateFormat sdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_feed_item);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("LISOM: New Feed Entry Form");


//        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));

    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        return true;
    }

    public void submitFeedEntryAction(View view) {
        createNewEntryLocal();
    }

    public void saveAsDraftFeedEntryAction(View view) {
        createNewEntryLocal();
    }

    private void createNewEntryLocal(){
        //Decide what happens when the user clicks the submit button
        String title = ((EditText)findViewById(R.id.new_entry_title)).getText().toString();
        String desc = ((EditText)findViewById(R.id.new_entry_desc)).getText().toString();
        String geoRef = ((EditText)findViewById(R.id.new_entry_geo_reference)).getText().toString();
        String urlRef = ((EditText)findViewById(R.id.new_entry_url_ref)).getText().toString();

        RSSFeedItemModel newItem =  composeNewItemObject(title, desc, geoRef, urlRef);

        FeedDataCoordinator.getInstance().add_new_draft_entry(newItem);

        super.onBackPressed();
    }

    public void onNewEntryTypeRBClicked(View view){
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.new_entry_issue_type:
                if (checked)
                    selectedEntryType = RSSFeedItemModel.FeedItemType.ISSUE;
                    break;
            case R.id.new_entry_rfi_type:
                if (checked)
                    selectedEntryType = RSSFeedItemModel.FeedItemType.REQUESTFORINFO;
                    break;
            case R.id.new_entry_notif_type:
                if (checked)
                    selectedEntryType = RSSFeedItemModel.FeedItemType.NOTIFICATION;
                    break;
        }
    }

    public RSSFeedItemModel composeNewItemObject(String title,
                                                 String description,
                                                 String geoRef,
                                                 String urlRef){
        String feedId = "some_feed_id";
        RSSFeedItemModel tmp = new RSSFeedItemModel(
                feedId,
                selectedEntryType,
                geoRef,
                title,
                description,
                urlRef,
                getCurrentDateTSAsString());
        return tmp;
    }

    private String getCurrentDateTSAsString(){
        // Input
        Date date = new Date(System.currentTimeMillis());
        String text = sdf.format(date);
        return text;
    }
}