package com.lisom.docs;

import android.util.Log;

public class DocItemModel {

    private static String TAG = "DocItemModel";


    public enum DocItemType {
        PDF,
        WORD,
        EXCEL,
        UNKNOWN;

        public static final DocItemType fromExtensions(String extension){
            String extUpperCase = extension.toUpperCase();
            if(extUpperCase.equals("XLSX") || extUpperCase.equals("XLS"))
                return EXCEL;
            else if(extUpperCase.equals("DOCX") || extUpperCase.equals("DOC"))
                return WORD;
            else if(extUpperCase.equals("PDF"))
                return PDF;
            else return UNKNOWN;

        }
    }

    private String docId;
    private String name;
    private boolean isFolder;
    private String parentFolderPath;
    private String createdAt;

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFolder(boolean folder) {
        isFolder = folder;
    }

    public String getParentFolderPath() {
        return parentFolderPath;
    }

    public void setParentFolderPath(String parentFolderPath) {
        this.parentFolderPath = parentFolderPath;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public DocItemType guessDocType(){

        try {
            String fileExtension = name.substring(name.lastIndexOf(".")+1);
            Log.i(TAG, "fileExtension detected "+fileExtension);
            return DocItemType.fromExtensions(fileExtension);
        }catch(Exception ex){
            Log.e(TAG, "Could not understand file type of "+ name, ex);
            return DocItemType.UNKNOWN;
        }
    }
}
