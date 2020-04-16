package com.devilsoftware.transfelingo.Fragments;

import android.app.Fragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.devilsoftware.transfelingo.HistoryAdapter;
import com.devilsoftware.transfelingo.HistoryDbHelper;
import com.devilsoftware.transfelingo.ItemHistory;
import com.devilsoftware.transfelingo.R;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    View mainView;

    HistoryAdapter historyAdapter;
    List<ItemHistory> itemHistories = new ArrayList<>();

    HistoryDbHelper mDbHelper;
    SQLiteDatabase db;
    ListView listView;

    TextView textView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        mainView = inflater.inflate(R.layout.fragment_choice, null);


        return init();
    }

    View init() {
        mDbHelper = new HistoryDbHelper(getActivity());
        db = mDbHelper.getWritableDatabase();

        listView = mainView.findViewById(R.id.choice);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                                                @Override

                                                public boolean onItemLongClick(final AdapterView<?> adapterView, final View view, final int poz, long l) {
                                                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                                                    alert.setMessage(R.string.delitem);
                                                    alert.setTitle(R.string.del);
                                                    alert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            db = mDbHelper.getWritableDatabase();
                                                            db.delete("tablehistory", "id = ?", new String[]{Integer.toString(itemHistories.get(poz).id)});
                                                            db.close();
                                                            historyAdapter.remove(itemHistories.get(poz));
                                                            // удаление элемента и последующее обновление списков
                                                        }
                                                    });
                                                    alert.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {

                                                        }
                                                    });
                                                    alert.create().show();
                                                    return false;
                                                }
                                            }
        );

        textView = mainView.findViewById(R.id.texthint);
        // do the same for other MenuItems

        // add NavigationItemSelectedListener to check the navigation clicks

        updateList();

        return mainView;
    }


    public void updateList() {

        db = mDbHelper.getReadableDatabase();View mainView;
        itemHistories.clear();

        Cursor cursor = db.query("tablehistory", null, null, null, null, null, null);

        if (cursor.moveToFirst()) {

            int id = cursor.getColumnIndex("id");
            int way = cursor.getColumnIndex("way");
            int textf = cursor.getColumnIndex("textfrom");
            int textt = cursor.getColumnIndex("textto");
            int choice = cursor.getColumnIndex("choice");

            do {
                // заполняем список из sql, если это список избранного, то только избранные записи, в противном слуае только история
                Log.d("ROWS","id = "+cursor.getInt(id) + " way = " + cursor.getString(way) + " textf = " +cursor.getString(textf)+" textto = "+cursor.getString(textt) + " choice = "+ cursor.getString(choice) );
                if(cursor.getInt(choice)==1) {
                    ItemHistory item = new ItemHistory(cursor.getString(textf),
                            cursor.getString(textt), cursor.getString(way),
                            cursor.getInt(choice), cursor.getInt(id));
                    itemHistories.add(0, item);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        if(historyAdapter==null)
            historyAdapter = new HistoryAdapter(getActivity(),itemHistories,mDbHelper);
        else
            historyAdapter.items = itemHistories;

        historyAdapter.notifyDataSetChanged();

        if(historyAdapter.items.size()==0){
            textView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.INVISIBLE);
        }else{
            textView.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
            listView.setAdapter(historyAdapter);
        }


    }

}
