package com.devilsoftware.transfelingo.Activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.devilsoftware.transfelingo.ClipboardManager;
import com.devilsoftware.transfelingo.Fragments.HistoryFragment;
import com.devilsoftware.transfelingo.Fragments.MainFragment;
import com.devilsoftware.transfelingo.Fragments.PreferencesFragment;
import com.devilsoftware.transfelingo.HistoryDbHelper;
import com.devilsoftware.transfelingo.Models.ModelAnswer;
import com.devilsoftware.transfelingo.R;
import com.devilsoftware.transfelingo.SelectLangActivity;
import com.devilsoftware.transfelingo.utills.BackPressedListener;

import ru.yandex.speechkit.SpeechKit;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener{

    ModelAnswer lastAnswer;

    static SharedPreferences preferences;

    public static String fromid;
    public static String toid; // id языков

    public static boolean reverse;
    FragmentTransaction fragmentTransaction;
    MainFragment mainFragment = new MainFragment();
    PreferencesFragment preferencesFragment = new PreferencesFragment();
    HistoryFragment historyFragment = new HistoryFragment();


    SQLiteDatabase db;
    HistoryDbHelper mDbHelper;

    public final static String BROADCAST_ACTION = "com.devilsoftware.transfelingo";
    BroadcastReceiver br;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        try {
            SpeechKit.getInstance().init(getApplicationContext(), "6bfa5347-8460-4b8d-bc1e-f3d0caababf4");
            SpeechKit.getInstance().setUuid("4A4528194A4528194A4528194A452819");
        } catch (SpeechKit.LibraryInitializationException e) {
            e.printStackTrace();
        }

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
        registerReceiver(br, intFilt);

        mDbHelper = new HistoryDbHelper(this);
        db = mDbHelper.getWritableDatabase();


        fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container,mainFragment);
        fragmentTransaction.commit();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(preferences.getInt("first",0)==0){
            Intent intent = new Intent(getApplicationContext(), SelectLangActivity.class);
            intent.putExtra("title", getString(R.string.fromwhat));
            startActivityForResult(intent, 0);
        } else {
            recoverLangs();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        switch (v.getId()) {
            case R.id.action_more:
                menu.add(0,0,0, R.string.send);
                menu.add(0,1,1, R.string.fullsc);
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {


        if(lastAnswer!=null) {
            if (item.getItemId() == 0) {
                String shareBody = lastAnswer.textTo;
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Отправить через.."));
            } else {
                startActivity(new Intent(this, FullScreenActivity.class).putExtra("text", lastAnswer.textTo));
            }
        }
        return super.onContextItemSelected(item);
    }

    public static String getWay(){
        String way;
        if(!reverse){
            way = toid + "-" + fromid;
        }else{
            way = fromid + "-" + toid;
        }
        return way;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }else{

        }

        BackPressedListener backPressedListener = mainFragment;

        if (backPressedListener != null) {
            backPressedListener.onBackPressed();
        } else {
            super.onBackPressed();
        }


    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(br);
        super.onDestroy();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    void updateQucikT(){
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Menu menu = navigationView.getMenu();
        MenuItem quickT = menu.findItem(R.id.nav_start_quickT);

        if(isMyServiceRunning(ClipboardManager.class)) {
            quickT.setTitle(getString(R.string.quickstop));
        }else{
            quickT.setTitle(getString(R.string.quickstart));
        }

    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        fragmentTransaction = getFragmentManager().beginTransaction();

        int id = item.getItemId();
        Intent intent;
        if(id==R.id.nav_translate){
            fragmentTransaction.replace(R.id.container,mainFragment);
        }else if(id == R.id.nav_gallery){
            fragmentTransaction.replace(R.id.container,historyFragment);
        }else if(id == R.id.nav_settings){
            fragmentTransaction.replace(R.id.container,preferencesFragment);
        }else if(id == R.id.nav_start_quickT){
            intent = new Intent(this,ClipboardManager.class);
            if(!isMyServiceRunning(ClipboardManager.class)){
                startService(intent);
                Toast.makeText(getApplicationContext(), R.string.now,Toast.LENGTH_LONG).show();
                updateQucikT();
            }else{
                intent.putExtra("stop",0);
                startService(intent);
                NavigationView navigationView = findViewById(R.id.nav_view);
                navigationView.setNavigationItemSelectedListener(this);
                Menu menu = navigationView.getMenu();
                MenuItem quickT = menu.findItem(R.id.nav_start_quickT);
                quickT.setTitle(getString(R.string.quickstart));
            }
        }

        fragmentTransaction.addToBackStack("backStack");
        fragmentTransaction.commit();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                InputMethodManager inputMethodManager = (InputMethodManager)  MainActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(), 0);
            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(RESULT_OK == resultCode){
            if(requestCode==0){

                fromid = data.getStringExtra("selectedLangCode");
                preferences.edit()
                        .putString("selectedLangName1",data.getStringExtra("selectedLangName"))
                        .putString("selectedLangCode1",data.getStringExtra("selectedLangCode"))
                        .apply();

                if(preferences.getInt("first",0)==0){
                    Intent intent = new Intent(this, SelectLangActivity.class);
                    intent.putExtra("title", getString(R.string.towhat));
                    startActivityForResult(intent, 1);
                    preferences.edit().putInt("first",1).apply();
                }

            }
            if(requestCode==1){

                toid = data.getStringExtra("selectedLangCode");
                preferences.edit()
                        .putString("selectedLangName2",data.getStringExtra("selectedLangName"))
                        .putString("selectedLangCode2",data.getStringExtra("selectedLangCode"))
                        .apply();
            }
            recoverLangs();
        }
    }

    void recoverLangs(){
        fromid = preferences.getString("selectedLangCode1", null);
        toid = preferences.getString("selectedLangCode2", null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.selectFrom:

                Intent intent = new Intent(MainActivity.this, SelectLangActivity.class);
                startActivityForResult(intent,0);

                break;
            case R.id.selectTo:

                intent = new Intent(MainActivity.this, SelectLangActivity.class);
                startActivityForResult(intent,1);

                break;
        }
    }
}
