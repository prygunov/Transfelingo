package com.devilsoftware.transfelingo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Максим on 29.10.2017.
 * Translingo 2017.
 */

public class HistoryDbHelper extends SQLiteOpenHelper {

    public HistoryDbHelper(Context context) {
        super(context,"historydb", null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table tablehistory ( "
                +"id integer primary key autoincrement,"
                +"textfrom text,"
                +"textto text,"
                +"choice integer,"
                +"way text"+");"); //создание таблицы, если её нет.
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


}
