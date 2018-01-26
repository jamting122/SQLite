package com.example.k.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

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

    public void deleteAll(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM BOOK");
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
            item.status = cursor.getInt(6);
            items.add(item);
            adapter.notifyDataSetChanged();
            cursor.moveToNext();
        }
        db.close();
    }

    //데이터를 보내기위한 box작업
    public void Boxing (){
        //데이터 베이스의 모든 정보를 보내기위해 어레이리스트에 담기
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM SAVE", null);
        while(cursor.moveToNext()){
            StringBuilder stringBuilder = new StringBuilder("");
            if(cursor.isLast()){stringBuilder.append("final~");}
            else{stringBuilder.append("send~");}
            Log.i("Check", "Boxing: "+stringBuilder.toString());
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
            ((MainActivity) MainActivity.mContext).sendMessage(stringBuilder.toString());
        }
        db.execSQL("DELETE FROM SAVE");
        db.close();
    }

    //받은 데이터를 처리하기 위한 Unboxing 작업
    public void unBoxing (ArrayList<Item> items) {
        SQLiteDatabase db = getWritableDatabase();
        for(int i =0;i<items.size();i++){
            if(items.get(i).status==2){
                db.execSQL("INSERT INTO BOOK VALUES (null, '" + items.get(i).date + "', '" + items.get(i).title + "', '" + items.get(i).content + "', '" + items.get(i).author + "', '" + items.get(i).uri + "');");
            }
            else if(items.get(i).status==1){
                db.execSQL("UPDATE BOOK SET uri = '" + items.get(i).uri + "' WHERE title = '" + items.get(i).title + "';");
            }
            else {
                db.execSQL("DELETE FROM BOOK WHERE title='" + items.get(i).title + "';");
            }
        }
        //받아온 데이터 처리 후 반대쪽에다가 처리할 데이터 넘겨주기
        Cursor cursor = db.rawQuery("SELECT * FROM SAVE", null);
        while(cursor.moveToNext()){
            StringBuilder stringBuilder = new StringBuilder("");
            if(cursor.isLast()){stringBuilder.append("finish~");}
            else{stringBuilder.append("send~");}
            Log.i("Check", "Boxing: "+stringBuilder.toString());
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
            ((MainActivity) MainActivity.mContext).sendMessage(stringBuilder.toString());
        }
        db.execSQL("DELETE FROM SAVE");
        db.close();

        return;
    }

    public void reBoxing (ArrayList<Item> items){
        SQLiteDatabase db = getWritableDatabase();
        for(int i =0;i<items.size();i++){
            if(items.get(i).status==2){
                db.execSQL("INSERT INTO BOOK VALUES (null, '" + items.get(i).date + "', '" + items.get(i).title + "', '" + items.get(i).content + "', '" + items.get(i).author + "', '" + items.get(i).uri + "');");
            }
            else if(items.get(i).status==1){
                db.execSQL("UPDATE BOOK SET uri = '" + items.get(i).uri + "' WHERE title = '" + items.get(i).title + "';");
            }
            else {
                db.execSQL("DELETE FROM BOOK WHERE title='" + items.get(i).title + "';");
            }
        }
        db.close();
        return;
    }
}
