package com.devilsoftware.transfelingo;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

/**
 * Created by Maksim on 26.03.2017.
 */

public class GetLanguages extends AsyncTask<Void,JSONObject,JSONObject> {

    Context context;

    GetLanguages(Context context) {
        this.context = context;
    }

    @Override
    protected JSONObject doInBackground(Void... voids) {
        String res = null;
        JSONObject jsonObject = null;
        if(PreferenceManager.getDefaultSharedPreferences(context).getString("res",null)==null) {
            DefaultHttpClient hc = new DefaultHttpClient();
            ResponseHandler response = new BasicResponseHandler();
            HttpGet http = new HttpGet("https://translate.yandex.net/api/v1.5/tr.json/getLangs?ui="+ Locale.getDefault().getLanguage()+"&key=trnsl.1.1.20170314T174431Z.2acfc79d00e25cc3.61022e10aa1ba186af396e522a23f67b546c069c");
            try {
                res = (String) hc.execute(http, response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("res",res).apply();
        }else{
            res = PreferenceManager.getDefaultSharedPreferences(context).getString("res",null);
        }

        try {
            JSONObject js = new JSONObject(res);
            jsonObject = js.getJSONObject("langs");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // если не понятно что здесь написанно, то смотрите поток DictTaskLangs, тоже самое тут, только получение других языков
        return jsonObject;
    }
}
