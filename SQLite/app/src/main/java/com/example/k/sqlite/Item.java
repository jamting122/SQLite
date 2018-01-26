package com.example.k.sqlite;

/**
 * Created by k on 2018-01-12.
 */

public class Item {

    String _id;
    String date;
    String title;
    String content;
    String author;
    String uri;
    int status;
    /*
    * Data 상태를 알려줌
    *  0 = 받은 데이터
    *  1 = 수정된 데이터
    *  2 = 새롭게 생성한 데이터
    *  1,2 -> 동기화시 보내주는 데이터
    *  0 -> 동기화시 그냥 넘기는 데이터
    *  */

    public void Item(){};


    public void set_id(String _id) {
        this._id = _id;
    }

    public String get_id() {
        return _id;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
