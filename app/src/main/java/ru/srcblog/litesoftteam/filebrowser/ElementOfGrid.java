package ru.srcblog.litesoftteam.filebrowser;

import android.graphics.drawable.Drawable;

/**
 * Created by javavirys on 01.05.2017.
 * jv ava
 */

public class ElementOfGrid {

    public String title;
    public boolean isDir;

    public ElementOfGrid() {
        title = null;
        isDir = false;
    }

    public ElementOfGrid(String title,boolean isDir)
    {
        this.title = title;
        this.isDir = isDir;
    }

}
