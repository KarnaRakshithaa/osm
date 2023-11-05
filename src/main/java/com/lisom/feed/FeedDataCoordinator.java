package com.lisom.feed;

import java.util.ArrayList;
import java.util.List;

public class FeedDataCoordinator {

    private static final FeedDataCoordinator _pvtInstance = new FeedDataCoordinator();

    List<RSSFeedItemModel> feedRecordList;

    RSSFeedActivity.RecycleViewDataAdapter dataAdapter;


    public static FeedDataCoordinator getInstance(){
        return _pvtInstance;
    }

    public void setDataAdapter(RSSFeedActivity.RecycleViewDataAdapter dataAdapterInstance){
        dataAdapter = dataAdapterInstance;
    }

    private FeedDataCoordinator(){
        feedRecordList = new ArrayList<>(8);
    }

    public List<RSSFeedItemModel> fetch_dummy_feed_data(){
        int data_size = 5;
        feedRecordList.clear();
//        feedRecordList = new ArrayList<>(8);
//        for(int i=0;i<data_size;i++){
//            feedRecordList.add(create_dummy_feed_item(i));
//        }
        feedRecordList.add(create_dummy_issue_item(0));
        feedRecordList.add(create_dummy_rfi_item(0));
        feedRecordList.add(create_dummy_design_chg_item(0));
        feedRecordList.add(create_dummy_info_item(0));
        feedRecordList.add(create_dummy_conflict_item(0));
        feedRecordList.add(create_dummy_feed_item(0));

        return feedRecordList;
    }

    private RSSFeedItemModel create_dummy_feed_item(int loopIndex){
        String snippet = "" +
                "62.1 km and 63.7 km - 400KV pylons of KPTCL will have to be shifted as they infringe on the proposed alignment";
        RSSFeedItemModel tmp = new RSSFeedItemModel(
                ""+loopIndex,
                RSSFeedItemModel.FeedItemType.ISSUE,
                "lat=0.0,lon=0.1",
                "400KV pylons will have to be shifted",
                snippet+". Apparently, lorem ipsum is a big deal = "+loopIndex,
                "https://www.google.com/search?q=kptcl",
                "2023-09-06 12:45 PM ");
        return tmp;
    }

    private RSSFeedItemModel create_dummy_issue_item(int loopIndex){
        String snippet = "" +
                "Sirsi (66 km), Mundgod (116.20 km) stations - Traction Substation (TSS) to be planned";
        RSSFeedItemModel tmp = new RSSFeedItemModel(
                ""+loopIndex,
                RSSFeedItemModel.FeedItemType.ISSUE,
                "lat=0.0,lon=0.2",
                "Sirsi+Mundgod Traction stations to be planned",
                snippet,
                "https://www.google.com/search?q=Sirsi",
                "2023-09-01 11:15 AM ");
        return tmp;
    }

    private RSSFeedItemModel create_dummy_rfi_item(int loopIndex){
        String snippet = "" +
                "Gottagadi (87.65 km) station - Sectioning Post (SP) to be planned";
        RSSFeedItemModel tmp = new RSSFeedItemModel(
                ""+loopIndex,
                RSSFeedItemModel.FeedItemType.REQUESTFORINFO,
                "lat=0.0,lon=0.3",
                "Gottagadi Sectioning Post to be planned ",
                snippet,
                "https://www.google.com/search?q=SectioningPost",
                "2023-09-02 3.55 PM ");
        return tmp;
    }

    private RSSFeedItemModel create_dummy_info_item(int loopIndex){
        String snippet = "" +
                "Preliminary Engineering and Traffic (PET) Survey for construction " +
                "of new BG railway line from Talguppa to Hubballi via Siddapur, " +
                "Sirsi, Mundgod and Tadas";
        RSSFeedItemModel tmp = new RSSFeedItemModel(
                ""+loopIndex,
                RSSFeedItemModel.FeedItemType.NOTIFICATION,
                "lat=0.0,lon=0.4",
                "PET Survey Result",
                snippet,
                "https://www.google.com/search?q=PETSurvey",
                "2023-09-04 8:00 AM ");
        return tmp;
    }

    private RSSFeedItemModel create_dummy_design_chg_item(int loopIndex){
        String snippet = "" +
                "155 km - Black Cotton Soil";
        RSSFeedItemModel tmp = new RSSFeedItemModel(
                ""+loopIndex,
                RSSFeedItemModel.FeedItemType.NOTIFICATION,
                "lat=0.0,lon=0.5",
                "Black Cotton Soil ",
                snippet,
                "https://www.google.com/search?q=blackcottonsoil",
                "2023-09-05 02:15 PM ");
        return tmp;
    }

    private RSSFeedItemModel create_dummy_conflict_item(int loopIndex){
        String snippet = "126.59 - 130.5 km alignment passes through eco-sensitive zone of the Attiveri BIrd Sanctuary in the villages of Hungunnd, Nelliharavi, and Astakatti in Mundgod taluk of Uttara Kannnada district" +
                "Station location conflict with local authorities";
        RSSFeedItemModel tmp = new RSSFeedItemModel(
                ""+loopIndex,
                RSSFeedItemModel.FeedItemType.ISSUE,
                "lat=0.0,lon=0.6",
                "Attiveri Bird Sanctuary Issue",
                snippet,
                "https://www.google.com/search?q=Attiveri",
                "2023-09-09 11:24 AM ");
        return tmp;
    }



    public void add_new_draft_entry(RSSFeedItemModel newItem){
        feedRecordList.add(0, newItem); // add at the top of the feed
        if(dataAdapter != null){
            dataAdapter.setData(feedRecordList);
            dataAdapter.notifyDataSetChanged();
        }



    }

}
