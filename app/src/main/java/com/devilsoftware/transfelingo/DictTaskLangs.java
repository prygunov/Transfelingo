package com.devilsoftware.transfelingo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

/**
 * Created by Maksim on 16.03.2017.
 */

public class DictTaskLangs extends AsyncTask<Void,JSONArray,JSONArray> {

    private final String APIToken = "dict.1.1.20170401T154458Z.34d99bf3b4b5fd4a.cc07d646a0decff667e36d0018d4dbf0dc9a36fc";

    Context context;

    public DictTaskLangs(Context context){
        this.context = context;
    }

    @Override
    protected JSONArray doInBackground(Void... params) {
        JSONArray jsonArray = null;
        String langs = null;
       if(PreferenceManager.getDefaultSharedPreferences(context).getString("langsdict",null)==null) {
            DefaultHttpClient hc = new DefaultHttpClient();
            ResponseHandler response = new BasicResponseHandler();
            HttpGet http;
            http = new HttpGet("https://dictionary.yandex.net/api/v1/dicservice.json/getLangs?key=" + APIToken); // получение поддреживаемых словрем языков
            try {
                langs = (String) hc.execute(http, response);
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                editor.putString("langsdict", langs);
                editor.apply(); // сохранение, чтобы потом снова не запрашивать
            } catch (IOException e) {
                e.printStackTrace();
            }

            /*
            По-моему очень удобно, не стоит постоянно запрашивать языки, а если произошло добавление какого-либо нового языка,
             у приложения можно просто сбросить насторойки и этот язык появится
             */
        }else{
            langs = PreferenceManager.getDefaultSharedPreferences(context).getString("langsdict",null);//если уже есть то просто возращаем раннее полученный ответ
        }
        try {
            jsonArray = new JSONArray(langs);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }
}


