package com.lisom.feed;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import ir.sana.osm.OsmActivity;
import ir.sana.osm.R;

public class RSSFeedActivity extends AppCompatActivity {

    private static String TAG = "RSSFeedActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.rss_feed_layout);

        // to have the back arrow on top
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("LISOM: RSS Feed");

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.feed_recycler_view);
//        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // create dummy data
        List<RSSFeedItemModel> dummyData = FeedDataCoordinator.getInstance().fetch_dummy_feed_data();
        RecycleViewDataAdapter adapter = new RecycleViewDataAdapter(dummyData);
        recyclerView.setAdapter(adapter);
        FeedDataCoordinator.getInstance().setDataAdapter(adapter);


        FloatingActionButton fab = findViewById(R.id.rss_feed_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "You can create your own feed entry later", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent newEntryIntent = new Intent(getApplicationContext(), NewFeedItemActivity.class);
                startActivity(newEntryIntent);
            }
        });

//        registerForContextMenu(recyclerView);

    }

//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo);
//        // inflate menu
//        MenuInflater inflater = getActivity().getMenuInflater();
//        inflater.inflate(R.menu.my_context_menu, menu);
//    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;

    }

//    public void finishAndGoBackToMap(){
//        finish();
//        Intent intent = new Intent(this, OsmActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        startActivity(intent);
//
//    }




    @Override
    public boolean onSupportNavigateUp() {
        finish();
        super.onBackPressed();
        return true;
    }


    private static class RecycleViewHolder extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener, View.OnClickListener{

        TextView feedItemTypeView;
        TextView feedItemTitleView;
        TextView feedItemDescView;
        TextView feedItemPubDateView;

        RSSFeedItemModel boundItemObject;
        Context context;

        private MenuItem.OnMenuItemClickListener onContextMenuClickListener;


        public RecycleViewHolder(View itemView, Context ctx) {
            super(itemView);
            context = ctx;
            this.feedItemTypeView = (TextView) itemView.findViewById(R.id.feed_item_type);
            this.feedItemTitleView = (TextView) itemView.findViewById(R.id.feed_item_title);
            this.feedItemDescView = (TextView) itemView.findViewById(R.id.feed_item_description);
            this.feedItemPubDateView = (TextView) itemView.findViewById(R.id.feed_item_pub_date);

            itemView.setOnCreateContextMenuListener(this);

            onContextMenuClickListener = new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    System.out.println("context menu item clicked "+item.getTitle()+", id is "+item.getItemId()+", item is "+ boundItemObject.getTitle());
                    switch (item.getItemId()) {
                        case 101:
                            Log.d(TAG, "Go To MAP");
                            gotoMapLocation();
                            break;

                        case 102:
                            Log.d(TAG, "Go To Wiki Page");
                            gotoWikiPage();
                            break;
                    }
                    return true;
                }
            };



        }

        public void bind(final RSSFeedItemModel item) {
            boundItemObject = item;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

            menu.setHeaderTitle("Go To...");
            MenuItem goToMapOption = menu.add(0, 101, 1, "Location on Map");//groupId, itemId, order, title
            goToMapOption.setOnMenuItemClickListener(onContextMenuClickListener);

            MenuItem goToWikiOption = menu.add(0, 102, 2, "Wiki/Details Page");
            goToWikiOption.setOnMenuItemClickListener(onContextMenuClickListener);

        }

//        @Override
//        public void onViewRecycled(RecyclerView.ViewHolder holder) {
//            holder.itemView.setOnLongClickListener(null);
////            holder..onViewRecycled(holder);
//        }

        public void gotoWikiPage(){

            Intent in = new Intent(context, RSSFeedItemDetailViewActivity.class);
            String geoReference = boundItemObject.getGeoReference();
            String urlReference = boundItemObject.getUrlReference();
//            String page_url = ((TextView) v.findViewById(R.id.)).getText().toString().trim();

            String page_url = "https://www.rites.com/Railways";
            in.putExtra("url_reference", urlReference);
            in.putExtra("geo_reference", geoReference);
            System.out.println("RecyclerView Item on click, georeference "+ geoReference);
            context.startActivity(in);

//            String pdf_url = "https://firebasestorage.googleapis.com/v0/b/manvijlabs.appspot.com/o/tallagappa%2FPET%20report_Mar%202023.pdf?alt=media&token=dacee5be-f894-4468-b237-9a302d8b58a5";
//
//            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pdf_url));
//            context.startActivity(browserIntent);

        }

        public void gotoMapLocation(){
            ((RSSFeedActivity)context).finish();
            Intent intent = new Intent(context, OsmActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // FLAG_ACTIVITY_CLEAR_TOP // FLAG_ACTIVITY_REORDER_TO_FRONT
            context.startActivity(intent);

        }

        @Override
        public void onClick(View v) {
            gotoWikiPage();
        }



    }


     static class RecycleViewDataAdapter extends RecyclerView.Adapter<RecycleViewHolder> {

        private List<RSSFeedItemModel> feedRecordList;
        private int feedItemCount = 0;

//        RecyclerViewItemClickListener feedItemClickListener;
        public RecycleViewDataAdapter(List<RSSFeedItemModel> data){
            setData(data);
        }
        @Override
        public RecycleViewHolder onCreateViewHolder(ViewGroup parent,
                                                    int viewType) {
//            System.out.println("onCreateViewHolder");

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rss_feed_item_layout, parent, false);

//            feedItemClickListener = new RecyclerViewItemClickListener(parent.getContext());
//            view.setOnClickListener(feedItemClickListener);

            RecycleViewHolder myViewHolder = new RecycleViewHolder(view, parent.getContext());
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(final RecycleViewHolder holder, final int listPosition) {
            System.out.println("onBindViewHolder listposition - "+listPosition);
            holder.feedItemTypeView.setText(feedRecordList.get(listPosition).getFeedItemType().name());
            holder.feedItemTitleView.setText(feedRecordList.get(listPosition).getTitle());
            holder.feedItemDescView.setText(feedRecordList.get(listPosition).getDescription());
            holder.feedItemPubDateView.setText(feedRecordList.get(listPosition).getPublishDateAsStr());
            holder.bind(feedRecordList.get(listPosition));


            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    System.out.println("TODO - Check if context menu shows up");
                    // return false so system can create context menu
                    return false;
                }
            });

        }

        public void setData(List<RSSFeedItemModel> data){
            feedRecordList = data;
            if(data != null)
                feedItemCount = data.size();
        }

        @Override
        public int getItemCount() {
            System.out.println("getting item count = "+feedItemCount);
            return feedItemCount;
        }

    }

    public interface ItemClickListener {
        void onItemClick(View view, RSSFeedItemModel itemObject);
    }


//    private static class RecyclerViewItemClickListener implements View.OnClickListener {
//        private final Context context;
//        private RSSFeedItemModel modelObjectForSelectedItem;
//
//        private RecyclerViewItemClickListener(Context context) {
//            this.context = context;
//        }
//
//        @Override
//        public void onClick(View v) {
//            Intent in = new Intent(context, RSSFeedItemDetailViewActivity.class);
//            String geoReference = modelObjectForSelectedItem.getGeoReference();
////            String page_url = ((TextView) v.findViewById(R.id.)).getText().toString().trim();
//
//            String page_url = "https://www.rites.com/Railways";
//            in.putExtra("geo_reference_url", geoReference);
//            context.startActivity(in);
//
////            String pdf_url = "https://firebasestorage.googleapis.com/v0/b/manvijlabs.appspot.com/o/tallagappa%2FPET%20report_Mar%202023.pdf?alt=media&token=dacee5be-f894-4468-b237-9a302d8b58a5";
////
////            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pdf_url));
////            context.startActivity(browserIntent);
//        }
//
//        public void setSelectedItem(RSSFeedItemModel item){
//            modelObjectForSelectedItem = item;
//        }
//    }





}