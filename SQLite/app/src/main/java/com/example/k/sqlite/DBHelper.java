package com.example.k.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by k on 2018-01-12.
 */

public class DBHelper extends SQLiteOpenHelper{

    //Constructor DBHelper, It's accept DB name and vesion
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate (SQLiteDatabase db){
        /* create new table
        * name is BOOK, automaticly add _id
        * table structor
        * _id, title, date, content, author, uri, status*/
        db.execSQL("CREATE TABLE BOOK (_id INTEGER PRIMARY KEY AUTOINCREMENT, date TEXT, title TEXT, content TEXT, author TEXT, uri TEXT);");
        db.execSQL("CREATE TABLE SAVE (_id INTEGER PRIMARY KEY AUTOINCREMENT, date TEXT, title TEXT, content TEXT, author TEXT, uri TEXT, status INTEGER);");
    }

    @Override
    public void onUpgrade (SQLiteDatabase db,int oldVersion,int newVersion){
        //new verson update
        db.execSQL("DROP TABLE IF EXISTS BOOK");
        db.execSQL("DROP TABLE IF EXISTS SAVE");
        onCreate(db);
    }

    //insert data in DB
    public void insert(String date, String title, String content, String author, String uri){
        //Open DB can to write
        SQLiteDatabase db = getWritableDatabase();
        //add row
        db.execSQL("INSERT INTO BOOK VALUES (null, '" + date + "', '" + title + "', '" + content + "', '" + author + "', '" + uri + "');");
        db.execSQL("INSERT INTO SAVE VALUES (null, '" + date + "', '" + title + "', '" + content + "', '" + author + "', '" + uri + "', '" + 2 + "');");
        db.close();
    }

    //delete data in DB
    public void delete(String date, String title, String content, String author, String uri) {
        //Open to Db can delete
        SQLiteDatabase db = getWritableDatabase();
        //delete row same title
        db.execSQL("DELETE FROM BOOK WHERE title='" + title + "';");
        db.execSQL("INSERT INTO SAVE VALUES (null, '" + date + "', '" + title + "', '" + content + "', '" + author + "', '" + uri + "', '" + 0 + "');");
        db.close();
    }

    public void update (String date, String title, String content, String author, String uri){
        //Open to DB can update
        SQLiteDatabase db = getWritableDatabase();
        //update row same title
        db.execSQL("UPDATE BOOK SET uri = '" + uri + "' WHERE title = '" + title + "';");
        db.execSQL("INSERT INTO SAVE VALUES (null, '" + date + "', '" + title + "', '" + content + "', '" + author + "', '" + uri + "', '" + 1 + "');");
        db.close();
    }

    //read data and connect listviewadapter
    public void getResult(ArrayList<Item> items, ListViewAdapter adapter) {
        // Open to DB can read
        SQLiteDatabase db = getReadableDatabase();
        // 행단위로 DB출력 (커서사용)
        Cursor cursor = db.rawQuery("SELECT * FROM BOOK", null);
        cursor.moveToFirst();
        adapter.clear();
        while (!cursor.isAfterLast()) {
            //DB에서 빼놓을 공간 확보
            Item item = new Item();
            item._id = cursor.getString(0);
            item.date = cursor.getString(1);
            item.title = cursor.getString(2);
            item.content = cursor.getString(3);
            item.author = cursor.getString(4);
            item.uri = cursor.getString(5);
            items.add(item);
            adapter.notifyDataSetChanged();
            cursor.moveToNext();
        }
        db.close();
    }

    //데이터를 한개씩 보내고, 받았다는 신호가 들어오면 다음 데이터 보내기
    public void sendBoxing (String string){
        Log.i("check", "sendBoxing: "+string);
        if(string.equals("start")){}
        else{
            SQLiteDatabase db = getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM SAVE",null);
            cursor.moveToNext();
            db.execSQL("DELETE FROM SAVE WHERE title='" + cursor.getString(2) + "';");
            db.close();
        }
        StringBuilder stringBuilder = new StringBuilder("");
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM SAVE", null);
        cursor.moveToNext();
        if (cursor.getCount() == 1) {
            stringBuilder.append("final~");
            stringBuilder.append(cursor.getString(1));
            stringBuilder.append("~");
            stringBuilder.append(cursor.getString(2));
            stringBuilder.append("~");
            stringBuilder.append(cursor.getString(3));
            stringBuilder.append("~");
            stringBuilder.append(cursor.getString(4));
            stringBuilder.append("~");
            stringBuilder.append(cursor.getString(5));
            stringBuilder.append("~");
            stringBuilder.append(cursor.getString(6));
            stringBuilder.append("~");
            db.execSQL("DELETE FROM SAVE");
        } else if (cursor.getCount() == 0) {
            return;
        } else {
            stringBuilder.append("send~");
            stringBuilder.append(cursor.getString(1));
            stringBuilder.append("~");
            stringBuilder.append(cursor.getString(2));
            stringBuilder.append("~");
            stringBuilder.append(cursor.getString(3));
            stringBuilder.append("~");
            stringBuilder.append(cursor.getString(4));
            stringBuilder.append("~");
            stringBuilder.append(cursor.getString(5));
            stringBuilder.append("~");
            stringBuilder.append(cursor.getString(6));
            stringBuilder.append("~");
        }
        Log.i("check", "sendBoxing: "+stringBuilder.toString());
        ((MainActivity) MainActivity.mContext).sendMessage(stringBuilder.toString());
        db.close();
    }

    //데이터를 받았을 때, 받은것을 처리하고 다시 보내주어서 처리하기
    public void checkBoxing (String string) {
        Log.i("check", "checkBoxing: "+string);
        SQLiteDatabase db = getReadableDatabase();
        string = string.toString();
        String[] strings = string.split("~");
        if(strings[0].equals("sendaudio")){
            try{
                File file = File.createTempFile(strings[1],"mp3");
                file.deleteOnExit();
                String uri = file.getAbsolutePath();
                Log.i("check", "나오긴함?: "+uri);
                byte[] buffer = strings[2].getBytes();
                FileOutputStream buf = new FileOutputStream(file);
                buf.write(buffer);
                buf.close();
                db.execSQL("UPDATE BOOK SET uri = '" + uri + "' WHERE title = '" + strings[1] + "';");
            } catch (IOException e){}
            db.close();
            ((MainActivity) MainActivity.mContext).sendMessage("check~");
            return;
        }
        if(strings[0].equals("send")){
            if(strings[6].equals("2")){
                db.execSQL("INSERT INTO BOOK VALUES (null, '" + strings[1] + "', '" + strings[2] + "', '" + strings[3] + "', '" + strings[4] + "', '" + strings[5] + "');");
                ((MainActivity) MainActivity.mContext).sendMessage("audio~"+ strings[5] + "~" + strings[2]);
                return;
            } else if (strings[6].equals("1")) {
                db.execSQL("UPDATE BOOK SET uri = '" + strings[5] + "' WHERE title = '" + strings[2] + "';");
            } else {
                db.execSQL("DELETE FROM BOOK WHERE title='" + strings[2] + "';");
            } ((MainActivity) MainActivity.mContext).sendMessage("check~");
        } else if (strings[0].equals("check")) {
            sendBoxing("keep");
        } else if (strings[0].equals("audio")){
            sendAudio(strings[1],strings[2]);
        } else {
            if(strings[6].equals("2")){
                db.execSQL("INSERT INTO BOOK VALUES (null, '" + strings[1] + "', '" + strings[2] + "', '" + strings[3] + "', '" + strings[4] + "', '" + strings[5] + "');");
                ((MainActivity) MainActivity.mContext).sendMessage("audio~"+ strings[5] + "~" + strings[2]);
            } else if (strings[6].equals("1")) {
                db.execSQL("UPDATE BOOK SET uri = '" + strings[5] + "' WHERE title = '" + strings[2] + "';");
            } else {
                db.execSQL("DELETE FROM BOOK WHERE title='" + strings[2] + "';");
            } sendBoxing("start");
        }
        db.close();
    }

    //오디오 파일 보내기
    public void sendAudio (String path,String title){
        Log.i("check", "sendAudio: " + path + "AND" + title);
        try {
            File audio = new File(path);
            int size = (int) audio.length();
            byte[] bytes = new byte[size];
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(audio));
            buf.read(bytes,0,bytes.length);
            buf.close();
            StringBuilder stringBuilder = new StringBuilder("");
            stringBuilder.append("sendaudio~");
            stringBuilder.append(title);
            stringBuilder.append("~");
            stringBuilder.append(bytes);
            stringBuilder.append("~");
            ((MainActivity) MainActivity.mContext).sendMessage(stringBuilder.toString());
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
