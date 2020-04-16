package com.devilsoftware.transfelingo.API;

import com.devilsoftware.transfelingo.Models.GeneralResponse;
import com.devilsoftware.transfelingo.Models.Lang;
import com.devilsoftware.transfelingo.Models.Langs;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by maxim on 10.02.18.
 */

public interface YandexTranslateAPI {

    // API Переводчика

    @GET("api/v1.5/tr.json/getLangs")
    Call<Lang> listLangs(@Query("key") String apiKey, @Query("ui") String ui);

    @GET("api/v1.5/tr.json/detect")
    Call<String> detectLang(@Query("key") String apiKey, @Query("text") String text, @Query("hint") String hint);

    @GET("api/v1.5/tr.json/translate")
    Call<GeneralResponse> translateText(@Query("key") String apiKey, @Query("text") String text, @Query("lang") String way);

}
