package com.example.k.sqlite;

/**
 * Created by k on 2018-01-12.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class ListViewAdapter extends BaseAdapter {

    //보관장소
    private ArrayList<Item> listViewItemList = null;
    LayoutInflater inflater = null;

    public ListViewAdapter(ArrayList<Item> item,Context ctx){
        this.listViewItemList = item;
        this.inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return listViewItemList.size();
    }

    @Override
    public Item getItem(int position) {
        return listViewItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //보여주는 부분
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        //convertView가 null일 때, 커스텀 레이아웃 얻어오기
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.item_view, parent, false);
        }

        //현재 포지션에 레이아웃 및 해당 값 넣어주기
        TextView _id = (TextView) convertView.findViewById(R.id._id);
        TextView date = (TextView) convertView.findViewById(R.id.date);
        TextView title= (TextView) convertView.findViewById(R.id.title);
        TextView content = (TextView) convertView.findViewById(R.id.content);
        TextView author = (TextView) convertView.findViewById(R.id.author);
        TextView uri = (TextView) convertView.findViewById(R.id.uri);
        TextView status = (TextView) convertView.findViewById(R.id.status);

        Item i = listViewItemList.get(position);
        _id.setText(i._id);
        author.setText(i.author);
        title.setText(i.title);
        content.setText(i.content);
        date.setText(i.date);
        uri.setText(i.uri);
        status.setText(""+i.status);

        return convertView;

    }

    //listviewitemlist에 있는 모든 데이터 삭제
    public void clear(){listViewItemList.clear();}


}
