package com.lisom.docs;

import com.lisom.feed.RSSFeedActivity;
import com.lisom.feed.RSSFeedItemModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class DocDataCoordinator {

    private static final DocDataCoordinator _pvtInstance = new DocDataCoordinator();

    List<DocItemModel> docList;

    DocBrowseActivity.RecycleViewDataAdapter dataAdapter;

    SimpleDateFormat sdf = null;

    public static DocDataCoordinator getInstance(){
        return _pvtInstance;
    }

    public void setDataAdapter(DocBrowseActivity.RecycleViewDataAdapter dataAdapterInstance){
        dataAdapter = dataAdapterInstance;
    }

    private DocDataCoordinator(){

        docList = new ArrayList<>(8);
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));

    }

    public List<DocItemModel> fetch_dummy_doc_data(){
        int folder_data_size = 5;
        docList.clear();
        docList = new ArrayList<>(16);


//        for(int i=0;i<folder_data_size;i++){
//            docList.add(create_dummy_folder_items(i));
//        }

        docList.add(createDummyFolderByName("Codes"));
        docList.add(createDummyFolderByName("Feasibility"));
        docList.add(createDummyFolderByName("DPR"));
        docList.add(createDummyFolderByName("Drawings"));
        docList.add(createDummyFolderByName("Contractor Reports"));
        docList.add(createDummyFolderByName("O & M"));


//        int file_data_size = 5;
//
//        for(int j=0;j<file_data_size;j++){
//            docList.add(create_dummy_file_items(j));
//        }

        docList.add(createDummyFilesByName("PET Survey.pdf"));
        docList.add(createDummyFilesByName("Acquisition Plan.docx"));
        docList.add(createDummyFilesByName("Alignment Gradients.xlsx"));

        return docList;
    }

    private DocItemModel create_dummy_folder_items(int loopIndex){
        DocItemModel tmp = new DocItemModel();
        tmp.setDocId("folder_#_"+loopIndex);
        tmp.setName("Folder-"+loopIndex);
        tmp.setFolder(true);
        tmp.setParentFolderPath("/");
        tmp.setCreatedAt("2023-09-01 11:15 AM ");
        return tmp;
    }

    private DocItemModel create_dummy_file_items(int loopIndex){
        DocItemModel tmp = new DocItemModel();
        tmp.setDocId("file_doc_#_"+loopIndex);
        tmp.setName("File-"+loopIndex);
        tmp.setFolder(false);
        tmp.setParentFolderPath("/");
        tmp.setCreatedAt("2023-09-15 11:15 AM ");
        return tmp;
    }

    private DocItemModel createDummyFolderByName(String folderName){
        DocItemModel tmp = new DocItemModel();
        tmp.setDocId("folder_#_"+folderName);
        tmp.setName(folderName);
        tmp.setFolder(true);
        tmp.setParentFolderPath("/");
        tmp.setCreatedAt(getCurrentDateTSAsString());
        return tmp;
    }

    private DocItemModel createDummyFilesByName(String fileName){
        DocItemModel tmp = new DocItemModel();
        tmp.setDocId("file_#_"+fileName);
        tmp.setName(fileName);
        tmp.setFolder(false);
        tmp.setParentFolderPath("/");
        tmp.setCreatedAt(getCurrentDateTSAsString());
        return tmp;
    }


    private String getCurrentDateTSAsString(){
        // Input
        Date date = new Date(System.currentTimeMillis());
        String text = sdf.format(date);
        return text;
    }



}
