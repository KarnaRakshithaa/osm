package com.lisom.feed;

public class RSSFeedItemModel {

    public enum FeedItemType {
        ISSUE,
        REQUESTFORINFO,
        NOTIFICATION
    }
    String feedId;
    FeedItemType feedItemType;
    String geoReference;
    String title;
    String description;

    String urlReference;
    String publishDateAsStr;

    public RSSFeedItemModel(String feedId, FeedItemType feedItemType,
                            String geoReference, String title,
                            String description,
                            String url,String publishDateAsStr) {
        this.feedId = feedId;
        this.feedItemType = feedItemType;
        this.geoReference = geoReference;
        this.title = title;
        this.description = description;
        this.urlReference = url;
        this.publishDateAsStr = publishDateAsStr;
    }

    public String getFeedId() {
        return feedId;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    public FeedItemType getFeedItemType() {
        return feedItemType;
    }

//    public void setFeedItemType(String feedItemType) {
//        this.feedItemType = feedItemType;
//    }
    public void setFeedItemType(FeedItemType feedItemType) {
        this.feedItemType = feedItemType;
    }

    public String getGeoReference() {
        return geoReference;
    }

    public void setGeoReference(String geoReference) {
        this.geoReference = geoReference;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublishDateAsStr() {
        return publishDateAsStr;
    }

    public void setPublishDateAsStr(String publishDateAsStr) {
        this.publishDateAsStr = publishDateAsStr;
    }

    public String getUrlReference() {
        return urlReference;
    }

    public void setUrlReference(String urlReference) {
        this.urlReference = urlReference;
    }
}
