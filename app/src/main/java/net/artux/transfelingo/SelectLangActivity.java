package net.artux.transfelingo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class SelectLangActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ListView listView;
    EditText editText;
    Toolbar mActionBarToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sellect_lang);

        listView = findViewById(R.id.list_lang);
        setLangs(listView);
        listView.setOnItemClickListener(this);

        mActionBarToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mActionBarToolbar);
        getSupportActionBar().setTitle(getIntent().getStringExtra("title"));

        editText = findViewById(R.id.editText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapterList.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //TODO: setLocale
        /*TranslateHelper.getYandexTranslateAPI().listLangs(TranslateHelper.apiTranslateKey,"ru").enqueue(new Callback<Lang>() {
            @Override
            public void onResponse(Call<Lang> call, Response<Lang> response) {
                arrayAdapter = new ArrayAdapter<>(SelectLangActivity.this,android.R.layout.simple_list_item_1,response.body().getDirs());
                listView.setAdapter(arrayAdapter);
            }

            @Override
            public void onFailure(Call<Lang> call, Throwable t) {

            }
        });
        */
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if ( editText!= null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            }
        }

        String langName = (String) adapterList.getItem(position);
        position = adapter.getPosition(langName);
        Log.d("mtag",String.valueOf(position));

        Intent response = new Intent();
        response.putExtra("selectedLangId",position);
        try {
            response.putExtra("selectedLangCode", jsonArray.getString(position));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        response.putExtra("selectedLangName",(String) adapter.getItem(position));
        setResult(RESULT_OK,response);
        finish();
    }

    JSONArray jsonArray = null;
    ArrayAdapter adapter = null;
    ArrayAdapter adapterList = null;

    void setLangs(ListView listView){


        if (adapter == null) {

            GetLanguages gl = new GetLanguages(getApplicationContext()); // получение языков
            gl.execute();
            String[] s = null;
            JSONObject jsonObject = null;
            try {
                jsonObject = gl.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }


            try {

                if (jsonObject != null) {
                    jsonArray = jsonObject.names();
                }
                int l = jsonArray.length();
                s = new String[l];
                for (int i = 0; i < l; i++) {
                    if (jsonObject != null) {
                        s[i] = jsonObject.getString(jsonArray.getString(i));
                        // конвертация в массив
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


            adapter = new ArrayAdapter<>(SelectLangActivity.this, android.R.layout.simple_list_item_1, s);
            adapterList = new ArrayAdapter<>(SelectLangActivity.this, android.R.layout.simple_list_item_1, s);
        }

        listView.setAdapter(adapterList);

    }


}
