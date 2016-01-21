package com.symemo;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by 63289 on 2015/12/21.
 */
public class BookItem {
    public int id;
    public String content;
    public String time;
    public BookItem(int i,String c,String t){
        id=i;
        content=c;
        time=t;
    }
    public int getId(){return id;}
    public String getContent(){return content;}
    public String getTime(){return time;}
}
