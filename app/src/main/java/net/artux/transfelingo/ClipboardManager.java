package net.artux.transfelingo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import net.artux.transfelingo.Activities.MainActivity;
import net.artux.transfelingo.Models.ModelAnswer;
import net.artux.transfelingo.utills.TranslateHelper;

/**
 * Created by Максим on 02.11.2017.
 * Translingo 2017.
 */
public class ClipboardManager extends Service {
    private android.content.ClipboardManager mCM;
    NotificationManager notificationManager;
    int mStartMode;
    String  lastT;
    HistoryDbHelper mDbHelper;
    TranslateHelper translateHelper;

    public static final String ACTION_DO = "ACTION_DO";
    public static final String ACTION_CLOSE = "ACTION_CLOSE";

    public final static String BROADCAST_ACTION = "com.devilsoftware.transfelingo.quickt";

    static SharedPreferences preferences;
    BroadcastReceiver br;
    ModelAnswer answer;

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if(intent!=null)
            if(intent.hasExtra("stop")){
                stopSelf(startId);
                stopSelf();
                mCM = null;
                Log.d("QuickT","STOP");
            }else {
                mDbHelper = new HistoryDbHelper(this);
                Log.d("QuickT", "ONSTART");
                checkAction(intent.getAction());

                br = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String textTo = intent.getStringExtra("textTo");
                        String textFrom = intent.getStringExtra("textFrom");
                        String way = intent.getStringExtra("way");
                        answer = new ModelAnswer(textFrom, textTo, way, 0);

                        makeAnswer(answer, preferences.getBoolean("quickt", false));
                    }
                };
                IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
                registerReceiver(br, intFilt);

                mCM = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                mCM.addPrimaryClipChangedListener(new android.content.ClipboardManager.OnPrimaryClipChangedListener() {

                    @Override
                    public void onPrimaryClipChanged() {
                        if(mCM!=null){  String newClip = mCM.getText().toString();
                        if (!newClip.equals("") & !newClip.equals(lastT)) {
                            lastT = newClip;
                            if (!preferences.getBoolean("quickt", false)) {

                                NotificationCompat.Action actionDone = generateAction(R.drawable.ic_action_done, "Да", ACTION_DO);
                                NotificationCompat.Action actionClose = generateAction(R.drawable.ic_action_close, "Нет", ACTION_CLOSE);

                                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                                        .setTicker("Transfelingo")
                                        .setContentTitle("Перевести?")
                                        .setContentText("Вы уверены, что хотите перевести \"" + newClip + "\"" + MainActivity.getWay() + "?")
                                        .setSmallIcon(R.mipmap.ic_launcher)
                                        .addAction(actionClose)
                                        .addAction(actionDone);


                                Notification notif = new NotificationCompat.BigTextStyle(builder)
                                        .bigText("Вы уверены, что хотите перевести \"" + newClip + "\"" + MainActivity.getWay() + "?")
                                        .build();

                                notif.flags |= Notification.FLAG_AUTO_CANCEL;
                                notificationManager.notify(1, notif);
                            }else{

                                TranslateHelper.generalTranslateText(lastT,MainActivity.getWay(),null,getApplicationContext(),BROADCAST_ACTION);

                        }}

                    }
                    }
                });


            }
        return mStartMode;
    }

    void makeAnswer(ModelAnswer modelAnswer, boolean toast){

        if(toast) {
            Toast.makeText(getApplicationContext(),modelAnswer.saveTextTo,Toast.LENGTH_SHORT).show();
        }else {
            Log.d("TAG", "TAG");
            NotificationCompat.Builder builder;
            builder = new NotificationCompat.Builder(getApplicationContext())
                    .setTicker("Transfelingo")
                    .setContentTitle("Уже переведено")
                    .setContentText(lastT + " - " + modelAnswer.saveTextTo)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .addAction(generateAction(R.drawable.ic_action_done, "Okay", ACTION_CLOSE));


            Notification notif = null;
            notif = new NotificationCompat.BigTextStyle(builder)
                    .bigText(lastT + " - " + modelAnswer.saveTextTo)
                    .build();

            notificationManager.notify(1, notif);
        }
    }

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction ) {

        Intent intent = new Intent( getApplicationContext(), ClipboardManager.class );
        intent.setAction( intentAction );
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);

        return new NotificationCompat.Action.Builder( icon, title, pendingIntent ).build();
    }

    void checkAction(String action){
        if(action!=null) {
            Log.d("TAG", action);
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (action.equals(ACTION_DO)) {
                TranslateHelper.generalTranslateText(lastT,MainActivity.getWay(),null,getApplicationContext(),BROADCAST_ACTION);
            } else if (action.equals(ACTION_CLOSE)) {
                notificationManager.cancelAll();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        notificationManager = null;
        mDbHelper = null;
        mCM = null;

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
}