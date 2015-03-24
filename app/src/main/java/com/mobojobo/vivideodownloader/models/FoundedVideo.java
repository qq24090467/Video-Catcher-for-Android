package com.mobojobo.vivideodownloader.models;

/**
 * Created by pc on 23.03.2015.
 */
public class FoundedVideo {
    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    String link;
    String title;
    public FoundedVideo(String link,String title){
        this.setLink(link);
        this.setTitle(title);
    }

}
