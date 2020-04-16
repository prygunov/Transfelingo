package net.artux.transfelingo.API;

import net.artux.transfelingo.Models.GeneralResponse;
import net.artux.transfelingo.Models.Lang;

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
