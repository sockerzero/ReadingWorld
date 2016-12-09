package com.dnd.readingworld.Model;

import java.io.Serializable;

/**
 * Created by Asus on 11/21/2016.
 */

public class ContentWord implements Serializable {

    private String wordContent;
    private String wordType;
    private String linkImage;

    public ContentWord() {
    }

    public ContentWord(String wordContent, String wordType, String linkImage) {
        this.wordContent = wordContent;
        this.wordType = wordType;
        this.linkImage = linkImage;
    }

    public String getWordContent() {
        return wordContent;
    }

    public void setWordContent(String wordContent) {
        this.wordContent = wordContent;
    }

    public String getWordType() {
        return wordType;
    }

    public void setWordType(String wordType) {
        this.wordType = wordType;
    }

    public String getLinkImage() {
        return linkImage;
    }

    public void setLinkImage(String linkImage) {
        this.linkImage = linkImage;
    }
}
