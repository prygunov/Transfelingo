package net.artux.transfelingo.utills;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import net.artux.transfelingo.API.YandexDictionaryAPI;
import net.artux.transfelingo.API.YandexTranslateAPI;
import net.artux.transfelingo.Activities.MainActivity;
import net.artux.transfelingo.DictTaskLangs;
import net.artux.transfelingo.Models.DictionaryResponses.Def;
import net.artux.transfelingo.Models.DictionaryResponses.DictResponse;
import net.artux.transfelingo.Models.DictionaryResponses.Ex;
import net.artux.transfelingo.Models.DictionaryResponses.Tr;
import net.artux.transfelingo.Models.GeneralResponse;
import net.artux.transfelingo.Models.ModelAnswer;
import net.artux.transfelingo.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Максим on 02.11.2017.
 * Translingo 2017.
 */

public class TranslateHelper extends Application {

    static JSONArray jsDictLangs;

    private static YandexTranslateAPI yandexTranslateAPI;
    private static YandexDictionaryAPI yandexDictionaryAPI;

    static String apiTranslateKey = "trnsl.1.1.20170314T174431Z.2acfc79d00e25cc3.61022e10aa1ba186af396e522a23f67b546c069c";
    static String apiDictionaryKey = "dict.1.1.20170401T154458Z.34d99bf3b4b5fd4a.cc07d646a0decff667e36d0018d4dbf0dc9a36fc";


    @Override
    public void onCreate() {
        super.onCreate();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://translate.yandex.net/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        yandexTranslateAPI = retrofit.create(YandexTranslateAPI.class);

        Retrofit dict = new Retrofit.Builder()
                .baseUrl("https://dictionary.yandex.net/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        yandexDictionaryAPI = dict.create(YandexDictionaryAPI.class);

    }

    public static YandexTranslateAPI getYandexTranslateAPI(){
        return yandexTranslateAPI;
    }

    public static YandexDictionaryAPI getYandexDictionaryAPI() { return yandexDictionaryAPI; }

    static Call<DictResponse> dictResponseCall;
    static Call<GeneralResponse> generalResponseCall;
    static ModelAnswer modelAnswer;

    public static ModelAnswer translateText(final String text, final String way, final LinearLayout outputView,
                                            final ListView listView, final Activity context, final String ACTION){

        try {
            if(getLangDict(context)){

                if(dictResponseCall !=null) {
                    dictResponseCall.cancel();
                }
                if(generalResponseCall !=null) {
                    generalResponseCall.cancel();
                }

                dictResponseCall = yandexDictionaryAPI.lookUp(apiDictionaryKey, text, way, "ru",0);

                dictResponseCall.enqueue(new Callback<DictResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<DictResponse> call, @NonNull Response<DictResponse> response) {

                        DictResponse dictResponse = response.body();
                        if(response.code()==200) {
                            if (dictResponse != null) {
                                if (dictResponse.getDef().size() != 0) {

                                    ResponseCreator responseCreator = new ResponseCreator(outputView, context, listView);
                                    responseCreator.clear();

                                    String textTo = dictResponse.getDef().get(0).getTr().get(0).getText();
                                    modelAnswer = new ModelAnswer(text, textTo, way, 0);

                                    responseCreator
                                            .addMainTextView(textTo)
                                            .createTrList(dictResponse.getDef());
                                           // .addActionTextView("Реализовано с помощью сервиса «Яндекс.Словарь»", "https://tech.yandex.ru/dictionary/", context);
                                    Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
                                    intent.putExtra("textFrom", text);
                                    intent.putExtra("textTo", textTo);
                                    intent.putExtra("way", way);
                                    context.sendBroadcast(intent);

                                } else {
                                    // если перевод переводчика пустой, гоним через API Yandex Переводчика

                                    generalTranslateText(text, way, outputView, context, ACTION);
                                }

                            } else {
                                // если перевод переводчика пустой, гоним через API Yandex Переводчика

                                generalTranslateText(text, way, outputView, context,ACTION);
                            }
                        }else {
                            ResponseCreator responseCreator = new ResponseCreator(outputView, context, listView);
                            responseCreator.clear();
                            responseCreator.setErrorView(response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<DictResponse> call, Throwable t) {

                    }

                });

                return modelAnswer;
                // если направление перевода поддерживается словарем, то пробуем через словарь

            }else{

                generalTranslateText(text,way,outputView,context, ACTION);
                if (modelAnswer!=null)
                Log.wtf("wtf","Returned" +modelAnswer.saveTextTo);

                return modelAnswer;
                // если нет, то сразу гоним через API Yandex Переводчика
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return modelAnswer;
    }



    public static void generalTranslateText(final String text, final String way, final LinearLayout outputView, final Context context, final String ACTION){


        if(generalResponseCall!=null)
            generalResponseCall.cancel();

        generalResponseCall = yandexTranslateAPI.translateText(apiTranslateKey, text, way);

        generalResponseCall.enqueue(new Callback<GeneralResponse>() {
            @Override
            public void onResponse(Call<GeneralResponse> call, Response<GeneralResponse> response) {
                if(response.code()==200) {
                    if (response.isSuccessful() & response.body() != null) {
                        GeneralResponse generalResponse = response.body();
                        String textTo = generalResponse.getText().get(0);
                        modelAnswer = new ModelAnswer(text, textTo, way, 0);

                        if (outputView!=null) {
                            ResponseCreator responseCreator = new ResponseCreator(outputView, context, null);
                            responseCreator.clear().addMainTextView(textTo);
                        }
                        if(context!=null){
                            Intent intent = new Intent(ACTION);
                            intent.putExtra("textFrom", text);
                            intent.putExtra("textTo", textTo);
                            intent.putExtra("way", way);
                            context.sendBroadcast(intent);
                        }

                    }
                }else{
                    if(outputView!=null) {
                        ResponseCreator responseCreator = new ResponseCreator(outputView, context, null);
                        responseCreator.clear();
                        responseCreator.setErrorView(response.code());
                    }
                    try {
                        Log.e("ERROR",response.message() + " " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<GeneralResponse> call, Throwable t) {

            }
        });

    }

    static class ResponseCreator{

        LinearLayout mainLayout;
        Context context;
        ListView listView;

        ResponseCreator(View mainLayout, Context context, ListView listView){
            this.mainLayout = (LinearLayout) mainLayout;
            this.context = context;
            this.listView = listView;
        }

        ResponseCreator clear(){
            mainLayout.removeAllViews(); // TODO: так низя
            return this;
        }

        ResponseCreator addMainTextView(String text){

            TextView textView = new TextView(context);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(10,10,10,10);
            textView.setLayoutParams(layoutParams);
            textView.setTextSize(28f);
            textView.setText(text);
            mainLayout.addView(textView,0);

            return this;
        }

        ResponseCreator addActionTextView(String text, final String Url, final Activity activity){

            TextView textView = new TextView(context);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            textView.setLayoutParams(layoutParams);
            textView.setText(text);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Url));
                    activity.startActivity(intent);

                }
            });
            mainLayout.addView(textView);

            return this;
        }

        String getTs(Def def){
            if (def.getTs()!=null){
                return "[" + def.getTs() + "]";
            }else{
                return "";
            }
        }

        ResponseCreator createTrList(List<Def> defList){

            for(int i=defList.size(); i>0; i--){
                Def def = defList.get(i-1);

                String text = def.getText() + " - "
                        + getTs(def) + " "
                        + def.getPos();

                TextView textView = new TextView(context);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.setMargins(10,10,10,10);
                textView.setLayoutParams(layoutParams);
                textView.setTextSize(18f);
                textView.setText(text);
                mainLayout.addView(textView,1);

                for (int j=0; j<def.getTr().size(); j++){
                    Tr tr = def.getTr().get(j);

                    StringBuilder res = new StringBuilder(tr.getText());

                    TextView textView1 = new TextView(context);
                    layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    layoutParams.setMargins(20,0,0,0);
                    textView1.setLayoutParams(layoutParams);
                    textView1.setTextSize(16f);

                    if(tr.getSyn()!=null){
                        for (int h=0;h<tr.getSyn().size();h++){
                            res.append(", ").append(tr.getSyn().get(h).getText());
                        }
                    }
                    if(tr.getMean()!=null){
                        res.append(" - ").append(tr.getMean().get(0).getText());
                        for (int h=1;h<tr.getMean().size();h++){
                            res.append(", ")
                                    .append(tr.getMean().get(h).getText());
                        }
                    }

                    textView1.setText(res.toString());
                    mainLayout.addView(textView1,2);

                    if(tr.getEx()!=null){
                        for (int h=0;h<tr.getEx().size();h++){
                            Ex ex = tr.getEx().get(h);
                            TextView textView2 = new TextView(context);
                            layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                            layoutParams.setMargins(30,5,0,0);
                            textView2.setLayoutParams(layoutParams);
                            textView2.setText(ex.getText() + " - " + ex.getTr().get(0).getText());
                            textView2.setTextSize(17f);
                            mainLayout.addView(textView2);
                        }
                    }
                }
            }

            return this;
        }

        ResponseCreator setErrorView(int code){

            TextView textView = new TextView(context);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            textView.setLayoutParams(layoutParams);
            textView.setText(R.string.somewr);
            switch (code){
                case 401:
                    textView.setText(R.string.key_invalid);
                    break;
                case 402:
                    textView.setText(R.string.key_locked);
                    break;
                case 403:
                    textView.setText(R.string.limit);
                    break;
                case 404:
                    textView.setText(R.string.limit);
                    break;
                case 422:
                    textView.setText(R.string.textcntbetr);
                    break;
                case 413:
                    textView.setText(R.string.textsizeerror);
                    break;
                case 501:
                    textView.setText(R.string.dirisntsupp);
                    break;
            }

            mainLayout.addView(textView);

            return this;
        }

        ResponseCreator setErrorView(String mesg){

            TextView textView = new TextView(context);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            textView.setLayoutParams(layoutParams);
            textView.setText(mesg);
            mainLayout.addView(textView);

            return this;
        }

    }

    static boolean getLangDict(Context context) throws ExecutionException, InterruptedException, JSONException {
        if(jsDictLangs == null) {
            DictTaskLangs taskLangs = new DictTaskLangs(context);
            taskLangs.execute();
            jsDictLangs = taskLangs.get(); // получение поддерживаемых направлений языков
        }

        boolean availableDict = false;

        for(int i = 0;i<jsDictLangs.length();i++){
            if(jsDictLangs.get(i).equals(MainActivity.getWay())){
                availableDict = true; // если направление поддрживается, то возражаем true в переменную andwhat
            }
        }
        return availableDict; // если список поддерживается, возращаем true
    }

}
