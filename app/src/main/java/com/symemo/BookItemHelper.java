package com.symemo;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.ViewDebug;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by 63289 on 2015/12/21.
 */
public class BookItemHelper {
    private List<BookItem> myList;
    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public BookItemHelper(Activity activity) {
        myList = new ArrayList<>();
        dbHelper = new MyDatabaseHelper(activity, "BookStore.db", null, 1);
        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query("Book", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String content = cursor.getString(cursor.getColumnIndex("content"));
                String time = cursor.getString(cursor.getColumnIndex("time"));
                BookItem tempItem = new BookItem(id, content, time);
                myList.add(tempItem);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    public List<BookItem> getMyList() {
        return myList;
    }

    public void deleteItem(BookItem item) {
        db.delete("Book", "id=?", new String[]{Integer.toString(item.id)});
        myList.remove(item);
    }

    public void deleteItem(int id) {
        db.delete("Book", "id=?", new String[]{Integer.toString(id)});
        myList.remove(id);
    }

    public BookItem search(int id) {
        return myList.get(id);
    }
}
