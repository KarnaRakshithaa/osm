package com.lisom.docs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import ir.sana.osm.R;

public class DocBrowseActivity extends AppCompatActivity {

    private SimpleDateFormat sdf = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.doc_browse_layout);

        // to have the back arrow on top
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("LISOM: Documents");



        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.doc_browse_listview);
//        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // create dummy data
        List<DocItemModel> dummyData = DocDataCoordinator.getInstance().fetch_dummy_doc_data();
        RecycleViewDataAdapter adapter = new RecycleViewDataAdapter(dummyData);
        recyclerView.setAdapter(adapter);
        DocDataCoordinator.getInstance().setDataAdapter(adapter);

    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        return true;
    }

    private static class RecycleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView docItemNameView;
        TextView docItemCreatedAtView;

        ImageView docIcon;


        DocItemModel boundItemObject;
        Context context;


        public RecycleViewHolder(View itemView, Context ctx) {
            super(itemView);
            context = ctx;
            this.docItemNameView = (TextView) itemView.findViewById(R.id.lr_tvFileName);
            this.docItemCreatedAtView = (TextView) itemView.findViewById(R.id.lr_tvdate);
            this.docIcon = (ImageView) itemView.findViewById(R.id.lr_ivFileIcon);

        }

        public void bind(final DocItemModel item) {
            boundItemObject = item;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String pdf_url = "https://firebasestorage.googleapis.com/v0/b/manvijlabs.appspot.com/o/tallagappa%2FPET%20report_Mar%202023.pdf?alt=media&token=dacee5be-f894-4468-b237-9a302d8b58a5";
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pdf_url));
            context.startActivity(browserIntent);
        }

    }


    static class RecycleViewDataAdapter extends RecyclerView.Adapter<RecycleViewHolder> {

        private List<DocItemModel> docList;
        private int feedItemCount = 0;

        //        RecyclerViewItemClickListener feedItemClickListener;
        public RecycleViewDataAdapter(List<DocItemModel> data){
            setData(data);
        }
        @Override
        public RecycleViewHolder onCreateViewHolder(ViewGroup parent,
                                                                    int viewType) {
//            System.out.println("onCreateViewHolder");

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.doc_item_layout, parent, false);

//            feedItemClickListener = new RecyclerViewItemClickListener(parent.getContext());
//            view.setOnClickListener(feedItemClickListener);

            RecycleViewHolder myViewHolder = new RecycleViewHolder(view, parent.getContext());
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(final RecycleViewHolder holder, final int listPosition) {
            DocItemModel docItem = docList.get(listPosition);
            System.out.println("Doc recycleview onBindViewHolder listposition - "+listPosition);
            holder.docItemNameView.setText(docItem.getName());
            holder.docItemCreatedAtView.setText(docItem.getCreatedAt());

            if(docItem.isFolder()){
                holder.docIcon.setImageResource(R.drawable.icons8_folder);
            }
            else {
                switch (docItem.guessDocType()){
                    case PDF:
                        holder.docIcon.setImageResource(R.drawable.pdf_svgrepo_com);
                        break;
                    case WORD:
                        holder.docIcon.setImageResource(R.drawable.icons8_microsoft_word);
                        break;
                    case EXCEL:
                        holder.docIcon.setImageResource(R.drawable.icons8_microsoft_excel);
                        break;
                    default:
                        holder.docIcon.setImageResource(R.drawable.icons8_file);
                        break;

                }

            }


            holder.bind(docList.get(listPosition));

        }

        public void setData(List<DocItemModel> data){
            docList = data;
            if(data != null)
                feedItemCount = data.size();
        }

        @Override
        public int getItemCount() {
            System.out.println("getting item count = "+feedItemCount);
            return feedItemCount;
        }



    }
}