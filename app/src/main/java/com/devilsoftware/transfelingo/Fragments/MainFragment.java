package com.devilsoftware.transfelingo.Fragments;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.devilsoftware.transfelingo.Activities.MainActivity;
import com.devilsoftware.transfelingo.HistoryAdapter;
import com.devilsoftware.transfelingo.HistoryDbHelper;
import com.devilsoftware.transfelingo.ItemHistory;
import com.devilsoftware.transfelingo.Models.ModelAnswer;
import com.devilsoftware.transfelingo.R;
import com.devilsoftware.transfelingo.SelectLangActivity;
import com.devilsoftware.transfelingo.SwipeDetector;
import com.devilsoftware.transfelingo.utills.BackPressedListener;
import com.devilsoftware.transfelingo.utills.TranslateHelper;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import ru.yandex.speechkit.Emotion;
import ru.yandex.speechkit.Error;
import ru.yandex.speechkit.Language;
import ru.yandex.speechkit.OnlineModel;
import ru.yandex.speechkit.OnlineRecognizer;
import ru.yandex.speechkit.OnlineVocalizer;
import ru.yandex.speechkit.Recognition;
import ru.yandex.speechkit.Recognizer;
import ru.yandex.speechkit.RecognizerListener;
import ru.yandex.speechkit.Synthesis;
import ru.yandex.speechkit.Track;
import ru.yandex.speechkit.Vocalizer;
import ru.yandex.speechkit.VocalizerListener;
import ru.yandex.speechkit.Voice;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.INPUT_METHOD_SERVICE;

public class MainFragment extends Fragment implements View.OnClickListener, BackPressedListener {

    View mainView;

    LinearLayout.LayoutParams paramsList, paramsMain, paramNavBar;
    ListView listView;
    EditText inputText;
    View outputView;

    Context context;

    ModelAnswer lastAnswer;

    ArrayAdapter adapter;

    ImageView wayImage;
    ImageView voiceImage;
    ImageView vocalaizeImage;

    HistoryAdapter historyAdapter;
    List<ItemHistory> itemHistories = new ArrayList<>();

    static SharedPreferences preferences;

    static JSONArray jsonArray; // здесь будем держать языки

    String last; // последний ответ

    //далее - мои потоки, в которых происходит перевод.
    OnlineRecognizer recognizer;
    OnlineVocalizer vocalizer;

    boolean focusClosed = true;

    ViewGroup navBar, languageBar, mainContent;



    SQLiteDatabase db;
    HistoryDbHelper mDbHelper;

    ImageView copyImage;
    ImageView moreImage;

    TextView from, to;

    public final static String BROADCAST_ACTION = "com.devilsoftware.transfelingo.mainfragment";
    BroadcastReceiver br;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        mainView = inflater.inflate(R.layout.fragment_main, null);

        Log.d("sasd", "fdfde");

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String textTo = intent.getStringExtra("textTo");
                String textFrom = intent.getStringExtra("textFrom");
                String way = intent.getStringExtra("way");
                lastAnswer = new ModelAnswer(textFrom, textTo, way, 0);
            }
        };

        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        getActivity().registerReceiver(br, intFilt);

        mDbHelper = new HistoryDbHelper(getActivity());
        db = mDbHelper.getWritableDatabase();

        return init();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(br);
    }

    public void updateList() {

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
                Log.d("ROWS", "id = " + cursor.getInt(id) + " way = " + cursor.getString(way) + " textf = " + cursor.getString(textf) + " textto = " + cursor.getString(textt) + " choice = " + cursor.getString(choice));
                ItemHistory item = new ItemHistory(cursor.getString(textf), cursor.getString(textt), cursor.getString(way), cursor.getInt(choice), cursor.getInt(id));
                itemHistories.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (historyAdapter == null)
            historyAdapter = new HistoryAdapter(getActivity(), itemHistories, mDbHelper);
        else
            historyAdapter.items = itemHistories;

        listView.setAdapter(historyAdapter);
    }

    @SuppressLint("ClickableViewAccessibility")
    View init() {
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mainContent = mainView.findViewById(R.id.main_content);
        mDbHelper = new HistoryDbHelper(getActivity());
        listView = mainView.findViewById(R.id.list_history);
        paramsList = (LinearLayout.LayoutParams) listView.getLayoutParams();
        paramsMain = (LinearLayout.LayoutParams) mainContent.getLayoutParams();
        paramNavBar = (LinearLayout.LayoutParams) mainContent.findViewById(R.id.nav_bar).getLayoutParams();
        wayImage = mainView.findViewById(R.id.wayImage);

        updateList();

        historyAdapter.setNotifyOnChange(true);

        inputText = mainView.findViewById(R.id.inputText);
        outputView = mainView.findViewById(R.id.outputView);

        from = mainView.findViewById(R.id.selectFrom);
        to = mainView.findViewById(R.id.selectTo);

        /* TextView serviceLink = (TextView) findViewById(R.id.serviceLink);
        serviceLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://translate.yandex.ru/"));
                startActivity(intent);
            }
        });*/


        wayImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wayImage.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.rotation180));

                if (MainActivity.reverse) {
                    MainActivity.reverse = false;
                    wayImage.setRotation(180);
                } else {
                    MainActivity.reverse = true;
                    wayImage.setRotation(0);
                }

                preferences.edit().putBoolean("reverse", MainActivity.reverse).apply();
            }
        });

        MainActivity.reverse = preferences.getBoolean("reverse", false);
        if (MainActivity.reverse) {
            wayImage.setRotation(0);
        } else {
            wayImage.setRotation(180);
        }

        copyImage = mainView.findViewById(R.id.action_copy);


        copyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text", lastAnswer.textTo);
                assert clipboardManager != null;
                clipboardManager.setPrimaryClip(clip);
                Toast.makeText(getActivity(), R.string.text_copied, Toast.LENGTH_SHORT).show();
            }
        });

        moreImage = mainView.findViewById(R.id.action_more);
        registerForContextMenu(moreImage);
        moreImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreImage.showContextMenu();
            }
        });


        vocalizer = new OnlineVocalizer.Builder(Language.RUSSIAN, new VocalizerListener() {
            @Override
            public void onSynthesisDone(@NonNull Vocalizer vocalizer) {

            }

            @Override
            public void onPartialSynthesis(@NonNull Vocalizer vocalizer, @NonNull Synthesis synthesis) {

            }

            @Override
            public void onPlayingBegin(@NonNull Vocalizer vocalizer) {

            }

            @Override
            public void onPlayingDone(@NonNull Vocalizer vocalizer) {

            }

            @Override
            public void onVocalizerError(@NonNull Vocalizer vocalizer, @NonNull Error error) {

            }
        })
                .setEmotion(Emotion.GOOD)
                .setVoice(Voice.ERMIL)
                .build(); // 1
        vocalizer.prepare(); // 2


        vocalaizeImage = mainView.findViewById(R.id.vocalaize);
        vocalaizeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                vocalizer.synthesize(lastAnswer.saveTextTo, Vocalizer.TextSynthesizingMode.APPEND);

            }
        });

        voiceImage = mainView.findViewById(R.id.voiceBtn);

        voiceImage.performLongClick();

        final OnlineRecognizer recognizer = new OnlineRecognizer.Builder(getVoiceLang(), OnlineModel.QUERIES, new RecognizerListener() {
            @Override
            public void onRecordingBegin(@NonNull Recognizer recognizer) {

                Toast.makeText(getActivity(),"Speak now",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSpeechDetected(@NonNull Recognizer recognizer) {

            }

            @Override
            public void onSpeechEnds(@NonNull Recognizer recognizer) {

            }

            @Override
            public void onRecordingDone(@NonNull Recognizer recognizer) {

            }

            @Override
            public void onPowerUpdated(@NonNull Recognizer recognizer, float v) {

            }

            @Override
            public void onPartialResults(@NonNull Recognizer recognizer, @NonNull Recognition recognition, boolean b) {

                inputText.setText(recognition.getBestResultText());
            }

            @Override
            public void onRecognitionDone(@NonNull Recognizer recognizer) {
                saveToHistory(lastAnswer,mDbHelper);
            }

            @Override
            public void onRecognizerError(@NonNull Recognizer recognizer, @NonNull Error error) {

            }

            @Override
            public void onMusicResults(@NonNull Recognizer recognizer, @NonNull Track track) {

            }
        })
                .setDisableAntimat(false)
                .setEnablePunctuation(true)
                .build(); // 1
        recognizer.prepare(); // 2
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.RECORD_AUDIO)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        0);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        voiceImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Permission is not granted
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                            Manifest.permission.RECORD_AUDIO)) {
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                    } else {
                        // No explanation needed; request the permission
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.RECORD_AUDIO},
                                0);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                } else {
                    recognizer.startRecording();
                }


                return false;
            }
        });



        listView.setOnScrollListener(onScrollListener());
        navBar = mainView.findViewById(R.id.nav_bar);
        languageBar = mainView.findViewById(R.id.langs);
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
                                                            db.delete("tablehistory","id = ?",new String[]{Integer.toString(itemHistories.get(poz).id)});
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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });


        inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged ( final CharSequence s, int start, int before,
                                        int count){

            }
            @Override
            public void afterTextChanged ( final Editable s){
                if(s.length()>0) {
                    TranslateHelper.translateText(inputText.getText().toString(), MainActivity.getWay(),
                            (LinearLayout) mainView.findViewById(R.id.content), listView, getActivity(), BROADCAST_ACTION);
                }
            }
        });

        inputText.setFocusableInTouchMode(true);


        if(preferences.getBoolean("swipeInput",false))
            new SwipeDetector(inputText).setOnSwipeListener(new SwipeDetector.onSwipeEvent() {
                @Override
                public void SwipeEventDetected(View v, SwipeDetector.SwipeTypeEnum swipeType) {
                    switch (v.getId()){
                        case R.id.inputText:
                            if(swipeType==SwipeDetector.SwipeTypeEnum.LEFT_TO_RIGHT||swipeType== SwipeDetector.SwipeTypeEnum.RIGHT_TO_LEFT){
                                inputText.setText("");
                            }
                            break;
                        case R.id.outputView:
                            if(swipeType== SwipeDetector.SwipeTypeEnum.TOP_TO_BOTTOM){
                                mainView.findViewById(R.id.resetFocus).requestFocus();
                            }else if(swipeType == SwipeDetector.SwipeTypeEnum.BOTTOM_TO_TOP){
                                inputText.requestFocus();

                                ((InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE))
                                        .showSoftInput(inputText, InputMethodManager.SHOW_IMPLICIT);
                            }
                            break;
                    }
                }
            });

        from.setOnClickListener(this);
        to.setOnClickListener(this);


        inputText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                focusClosed = false;
                if(hasFocus){
                    ValueAnimator valueAnimator = ValueAnimator.ofFloat(5f,0);
                    valueAnimator.setDuration(250);
                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            paramsMain.weight = (float) animation.getAnimatedValue();
                            mainContent.setLayoutParams(paramsMain);
                            if((float) animation.getAnimatedValue() == 0) focusClosed = true;
                        }
                    });
                    valueAnimator.start();

                    InputMethodManager inputMethodManager =
                            (InputMethodManager)getActivity().getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.toggleSoftInputFromWindow(
                            inputText.getApplicationWindowToken(),
                            InputMethodManager.SHOW_FORCED, 0);

                    mainView.findViewById(R.id.nav_bar).setVisibility(View.GONE);
                } else {
                    ValueAnimator valueAnimator = ValueAnimator.ofFloat(0,5f);
                    valueAnimator.setDuration(250);
                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            paramsMain.weight = (float) animation.getAnimatedValue();
                            mainContent.setLayoutParams(paramsMain);
                            if((float) animation.getAnimatedValue() == 5f)
                                focusClosed = true;
                        }
                    });
                    valueAnimator.start();
                    mainView.findViewById(R.id.nav_bar).setVisibility(View.VISIBLE);
                    InputMethodManager mgr = (InputMethodManager)
                            getActivity().getSystemService(INPUT_METHOD_SERVICE);
                    assert mgr != null;
                    mgr.hideSoftInputFromWindow(inputText.getWindowToken(), 0);
                }
            }
        });

        recoverLangs();

        return mainView;
    }

    Language getVoiceLang(){

        String lang = preferences.getString("langv","ENGLISH");

        Language response = Language.ENGLISH;
        switch (lang){
            case "RUSSIAN":
                response = Language.RUSSIAN;
                break;
            case "ENGLISH":
                response = Language.ENGLISH;
                break;
            case "TURKISH":
                response = Language.TURKISH;
                break;
            case "UKRAINIAN":
                response = Language.UKRAINIAN;
                break;
        }

        return response;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(RESULT_OK == resultCode){
            if(requestCode==0){

                from.setText(data.getStringExtra("selectedLangName"));
                MainActivity.fromid = data.getStringExtra("selectedLangCode");
                preferences.edit()
                        .putString("selectedLangName1",data.getStringExtra("selectedLangName"))
                        .putString("selectedLangCode1",data.getStringExtra("selectedLangCode"))
                        .apply();

                if(preferences.getInt("first",0)==0){
                    Intent intent = new Intent(getActivity(), SelectLangActivity.class);
                    intent.putExtra("title", getString(R.string.towhat));
                    startActivityForResult(intent, 1);
                    preferences.edit().putInt("first",1).apply();
                }

            }
            if(requestCode==1){

                to.setText(data.getStringExtra("selectedLangName"));
                MainActivity.toid = data.getStringExtra("selectedLangCode");
                preferences.edit()
                        .putString("selectedLangName2",data.getStringExtra("selectedLangName"))
                        .putString("selectedLangCode2",data.getStringExtra("selectedLangCode"))
                        .apply();
            }
            recoverLangs();
        }
    }

    void recoverLangs(){
        from.setText(preferences.getString("selectedLangName1", "null"));
        MainActivity.fromid = preferences.getString("selectedLangCode1", null);

        to.setText(preferences.getString("selectedLangName2", "null"));
        MainActivity.toid = preferences.getString("selectedLangCode2", null);
    }


    public AbsListView.OnScrollListener onScrollListener() {
        return new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {


            }

            boolean doneAnim=true;
            ValueAnimator valueAnimatorUp = new ValueAnimator();
            ValueAnimator valueAnimatorDown = new ValueAnimator();
            int durationAnim = 250;

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                if (focusClosed && view.getChildAt(view.getChildCount()-1)!=null)
                    if (doneAnim && view.getLastVisiblePosition() != -1) {
                        if (!valueAnimatorUp.isRunning() || !valueAnimatorDown.isRunning()) {
                            doneAnim = false;
                            if (view.getLastVisiblePosition() == view.getAdapter().getCount() - 1 &&
                                    view.getChildAt(view.getChildCount() - 1).getBottom() <= view.getHeight()) {
                                if (paramsList.weight == 0 && !valueAnimatorUp.isRunning()) {
                                    valueAnimatorUp = ValueAnimator.ofFloat(0, 5f);
                                    valueAnimatorUp.setDuration(durationAnim);
                                    valueAnimatorUp.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                        @Override
                                        public void onAnimationUpdate(ValueAnimator animation) {
                                            paramsList.weight = (float) animation.getAnimatedValue();
                                            listView.setLayoutParams(paramsList);
                                            if ((float) animation.getAnimatedValue() == 5f)
                                                doneAnim = true;
                                        }

                                    });
                                    valueAnimatorUp.start();
                                }
                                doneAnim = true;
                            } else {
                                int allVisibleItems = view.getLastVisiblePosition() - view.getFirstVisiblePosition();
                                if (paramsList.weight == 5 && !valueAnimatorDown.isRunning() && allVisibleItems + 2 <= totalItemCount - view.getLastVisiblePosition()) {
                                    valueAnimatorDown = ValueAnimator.ofFloat(5f, 0);
                                    valueAnimatorDown.setDuration(durationAnim);
                                    valueAnimatorDown.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                        @Override
                                        public void onAnimationUpdate(ValueAnimator animation) {

                                            paramsList.weight = (float) animation.getAnimatedValue();
                                            listView.setLayoutParams(paramsList);
                                            if ((float) animation.getAnimatedValue() == 0)
                                                doneAnim = true;
                                        }
                                    });
                                    valueAnimatorDown.start();
                                    doneAnim = true;
                                }
                                doneAnim = true;
                            }

                        }

                    }
            }

        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.selectFrom:

                Intent intent = new Intent(getActivity(), SelectLangActivity.class);
                startActivityForResult(intent,0);

                break;
            case R.id.selectTo:

                intent = new Intent(getActivity(), SelectLangActivity.class);
                startActivityForResult(intent,1);

                break;
        }
    }

    public static boolean checkIsDataAlreadyInDBorNot(String TableName,
                                                      String dbfield, String fieldValue, SQLiteDatabase db) {
        String Query = "SELECT * FROM " + TableName + " WHERE " + dbfield + " = " + "?";
        Cursor cursor = db.rawQuery(Query, new String[]{fieldValue});
        if(cursor.getCount() <= 0){
            cursor.close();
            db.close();
            return false;
        }
        cursor.close();
        db.close();
        return true;
    }


    static void saveToHistory(ModelAnswer modelAnswer, HistoryDbHelper dbHelper){

        ContentValues cv = new ContentValues();
        SQLiteDatabase dbs = dbHelper.getWritableDatabase();

        if(modelAnswer!=null) {
            cv.put("textfrom", modelAnswer.textFrom);
            cv.put("textto", modelAnswer.textTo);
            cv.put("way", modelAnswer.way);
            cv.put("choice", modelAnswer.choice);
            dbs.insert("tablehistory", null, cv);
        }
        dbs.close();

    }

    @Override
    public void onBackPressed() {
        if(inputText.hasFocus()){
            InputMethodManager mgr = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(inputText.getWindowToken(), 0);
            inputText.clearFocus();
            listView.requestFocus();
            if(lastAnswer!=null)
                if(!checkIsDataAlreadyInDBorNot("tablehistory","textto", lastAnswer.textTo, mDbHelper.getReadableDatabase()))
                    saveToHistory(lastAnswer, mDbHelper);
        }
        updateList();
    }
}
