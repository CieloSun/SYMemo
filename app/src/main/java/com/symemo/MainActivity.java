package com.symemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

import java.text.SimpleDateFormat;

public class MainActivity extends Activity {
    public static String dbName = "BookStore.db";
    public FloatingActionButton fab;
    public FloatingActionButton editFab;
    public FloatingActionButton saveFab;
    public TextView contentText;
    public EditText contentEdit;
    public TextView contentTime;
    private MyDatabaseHelper dbHelper;
    SQLiteDatabase db;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //此两行防错
        dbHelper=new MyDatabaseHelper(this,dbName,null,1);
        db=dbHelper.getWritableDatabase();//
        contentEdit=(EditText)findViewById(R.id.content_edit);
        contentText=(TextView)findViewById(R.id.content_text);
        contentTime=(TextView)findViewById(R.id.content_time);
        //以下部分为直接进入MainActivity时，读取最后一条笔记。
        Cursor cursor=db.query("Book",null,null,null,null,null,null);
        if(!cursor.moveToFirst()){
            contentText.setText(R.string.welcome_content);
            contentTime.setText(R.string.welcome_time);
            //saveToDatabase();
        }else{
            cursor.moveToLast();
            contentText.setText(cursor.getString(cursor.getColumnIndex("content")));
            contentTime.setText(cursor.getString(cursor.getColumnIndex("time")));
        }
        cursor.close();
        fab= (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbHelper.close();
                Intent intent=new Intent(MainActivity.this,SimpleActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        editFab=(FloatingActionButton)findViewById(R.id.fab_edit);
        editFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentEdit.setText(contentText.getText());
                AlertDialog.Builder dialog=new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("Pay Attention");
                dialog.setMessage("Do you want to write a new one or just edit the old one?");
                dialog.setCancelable(true);
                dialog.setPositiveButton("New", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        contentEdit.setText("");
                    }
                });
                dialog.setNegativeButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.delete("Book", "time=?", new String[]{contentTime.getText().toString()});
                    }
                });
                dialog.show();
                contentText.setVisibility(View.GONE);
                contentTime.setVisibility(View.GONE);
                contentEdit.setVisibility(View.VISIBLE);
            }
        });

        saveFab=(FloatingActionButton)findViewById(R.id.fab_save);
        saveFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(contentEdit.getVisibility()==View.VISIBLE&&contentEdit.getText().toString().length()!=0){
                    SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    contentEdit.setVisibility(View.GONE);
                    contentText.setVisibility(View.VISIBLE);
                    contentTime.setVisibility(View.VISIBLE);
                    contentText.setText(contentEdit.getText());
                    contentTime.setText(dateFormat.format(new Date()));
                    saveToDatabase();
                }
                else {
                    Toast.makeText(MainActivity.this,"Cannot save.",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    public void saveToDatabase(){
        ContentValues values=new ContentValues();
        values.put("content", contentText.getText().toString());
        values.put("time", contentTime.getText().toString());
        db.insert("Book", null, values);
    }
    @Override
    public void onStart() {
        super.onStart();
        dbHelper = new MyDatabaseHelper(this,dbName,null,1);
        db = dbHelper.getWritableDatabase();

    }
    @Override
    public void onStop(){
        dbHelper.close();
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        switch (requestCode){
            case 1:
                if(resultCode==RESULT_OK){
                    contentText.setText(data.getStringExtra("content"));
                    contentTime.setText(data.getStringExtra("time"));
                }
                break;
            default:
        }
    }
    @Override
    public void onBackPressed(){
        if(contentEdit.getVisibility()==View.VISIBLE){
            AlertDialog.Builder dialog=new AlertDialog.Builder(MainActivity.this);
            dialog.setTitle("Warning");
            dialog.setMessage("You haven't save the content,do you want to save?");
            dialog.setCancelable(false);
            dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            dialog.show();
        }else {
            finish();
        }
    }
}
