package com.devilsoftware.transfelingo.API;

import com.devilsoftware.transfelingo.Models.DictionaryResponses.DictResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by maxim on 10.02.18.
 */

public interface YandexDictionaryAPI {

    // API Словаря

    @GET("api/v1/dicservice.json/getLangs")
    Call<List<String>> listLangs(@Query("key") String apiKey,@Query("ui") String ui );

    @GET("api/v1/dicservice.json/lookup")
    Call<DictResponse> lookUp(@Query("key") String apiKey, @Query("text") String text, @Query("lang") String lang, @Query("ui") String ui, @Query("flags") int flags);

}
