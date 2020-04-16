package com.devilsoftware.transfelingo;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Максим on 28.10.2017.
 */

public class HistoryAdapter extends ArrayAdapter<ItemHistory> {

    private final Activity context;
    public List<ItemHistory> items;
    HistoryDbHelper historyDbHelper;

    public HistoryAdapter(Activity context,  List<ItemHistory> items, HistoryDbHelper historyDbHelper) {
        super(context,R.layout.history_item,items);
        this.context = context;
        this.items = items;
        this.historyDbHelper = historyDbHelper;
    }

    private class ViewHolder{
        TextView textWay;
        TextView text;
        ImageView imageView;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ItemHistory itemHistory = items.get(position);
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.history_item, null, true);
        }
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.textWay = rowView.findViewById(R.id.way);
            viewHolder.text = rowView.findViewById(R.id.text);
            viewHolder.imageView = rowView.findViewById(R.id.star);


            viewHolder.text.setText(itemHistory.textFrom + " - " + itemHistory.textTo);
            viewHolder.textWay.setText(itemHistory.direction);
            if(itemHistory.choice==0)
                viewHolder.imageView.setImageResource(R.drawable.ic_action_star_bolder);
            else
                viewHolder.imageView.setImageResource(R.drawable.ic_action_star);
            viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(itemHistory.choice==1){
                        viewHolder.imageView.setImageResource(R.drawable.ic_action_star_bolder);
                        itemHistory.choice = 0;
                        ContentValues cv = new ContentValues();
                        SQLiteDatabase dbs = historyDbHelper.getWritableDatabase();
                        cv.put("choice",0);
                        dbs.update("tablehistory",cv,"id = ?", new String[]{Integer.toString(itemHistory.id)});
                        itemHistory.choice = 0;
                        dbs.close();
                    }else{
                        viewHolder.imageView.setImageResource(R.drawable.ic_action_star);
                        itemHistory.choice = 1;

                        ContentValues cv = new ContentValues();
                        SQLiteDatabase dbs = historyDbHelper.getWritableDatabase();

                        cv.put("choice",1);
                        dbs.update("tablehistory",cv,"id = ?", new String[]{Integer.toString(itemHistory.id)});

                        itemHistory.choice = 1;
                        dbs.close();
                    }
                }
            });
        return rowView;
    }

}
