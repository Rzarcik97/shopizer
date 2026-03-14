package com.salesmanager.shop.model.content;

import java.util.ArrayList;
import java.util.List;

/**
 * Folder containing content
 * images and other files
 *
 * @author carlsamson
 */
public class ContentFolder {

    List<Content> content = new ArrayList<Content>();
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<Content> getContent() {
        return content;
    }

    public void setContent(List<Content> content) {
        this.content = content;
    }

}
