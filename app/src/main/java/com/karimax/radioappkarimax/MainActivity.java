package com.karimax.radioappkarimax;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import android.Manifest;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.karimax.radioappkarimax.R;

public class MainActivity extends AppCompatActivity {
    public NotificationUtils mNotificationUtils;
    private long pressedTime;
    public String value;
    public String hand="nottested";
    public String restrted="false";
    AlertDialog.Builder builder;
    private AudioServiceBinder audioServiceBinder = null;
    private Handler audioProgressUpdateHandler = null;
    public String state="start",connected="null";
     public  Bundle extras;
    public Handler handler = new Handler();
    public Runnable runnable;
    public ImageView showjump;

    ImageButton startBackgroundAudio;

    private static String TAG = "PermissionDemo";
    private static final int RECORD_REQUEST_CODE = 101;
    final String audioFileUrl = "https://live.ecast.co.il/stream/69fm/azori";

    // Show played audio progress.

    private TextView audioFileUrlTextView;
    // This service connection object is the bridge between activity and background service.
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            // Cast and assign background service's onBind method returned iBander object.
            audioServiceBinder = (AudioServiceBinder) iBinder;
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        showjump=findViewById(R.id.imageView2);



        showjump.setVisibility(View.GONE);

        Glide.with(getApplicationContext()).load(R.drawable.mypick).into( showjump);

        NetworkStateManager.getInstance().getNetworkConnectivityStatus()
                .observe(this, activeNetworkStateObserver);

        extras = getIntent().getExtras();


        mNotificationUtils = new NotificationUtils(this);










        startBackgroundAudio = findViewById(R.id.start_audio_in_background);
        setTitle("dev2qa.com - Play Audio Use Background Service");





            clickplaybtn();








        bindAudioService();
        // Bind background audio service when activity is created.






        // Get audio file url textview.
        audioFileUrlTextView = (TextView)findViewById(R.id.audio_file_url_text_view);
        if(audioFileUrlTextView != null)
        {
            // Show web audio file url in the text view.
            audioFileUrlTextView.setText("Audio File Url. \r\n" + audioFileUrl);
        }

//        backgroundAudioProgress.setVisibility(ProgressBar.VISIBLE);
        // Click this button to start play audio in a background service.




        // Click this button to pause the audio played in background service.
//        Button pauseBackgroundAudio = (Button)findViewById(R.id.pause_audio_in_background);
//        pauseBackgroundAudio.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                audioServiceBinder.pauseAudio();
//                Toast.makeText(getApplicationContext(), "Play web audio file is paused.", Toast.LENGTH_LONG).show();
//            }
//        });
        // Click this button to stop the media player in background service.
//        Button stopBackgroundAudio = (Button)findViewById(R.id.stop_audio_in_background);
//        stopBackgroundAudio.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                audioServiceBinder.stopAudio(imageView);
////                backgroundAudioProgress.setVisibility(ProgressBar.INVISIBLE);
//                state="start";
//                Toast.makeText(getApplicationContext(), "Stop play web audio file.", Toast.LENGTH_LONG).show();
//            }
//        });


        TelephonyManager telephonyManager =
                (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

        PhoneStateListener callStateListener = new PhoneStateListener() {
            public void onCallStateChanged(int state, String incomingNumber)
            {
                if(state==TelephonyManager.CALL_STATE_RINGING){

                }
                if(state==TelephonyManager.CALL_STATE_OFFHOOK){

                }

                if(state==TelephonyManager.CALL_STATE_IDLE){

                }
            }
        };


        if (Build.VERSION.SDK_INT >= 31)
        {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)
                telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
        else // no permission needed
              telephonyManager.listen(callStateListener,PhoneStateListener.LISTEN_CALL_STATE);










}
    // Bind background service with caller activity. Then this activity can use
    // background service's AudioServiceBinder instance to invoke related methods.
    private void bindAudioService()
    {
        if(audioServiceBinder == null) {
            Intent intent = new Intent(MainActivity.this, AudioService.class);
            // Below code will invoke serviceConnection's onServiceConnected method.

            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        }
    }
    // Unbound background audio service with caller activity.
    private void unBoundAudioService()
    {
        if(audioServiceBinder != null) {
            unbindService(serviceConnection);
        }
    }
    @Override
    protected void onDestroy() {
        // Unbound background audio service when activity is destroyed.
        unBoundAudioService();
        super.onDestroy();
    }
    // Create audio player progressbar updater.
    // This updater is used to update progressbar to reflect audio play process.
    private void createAudioProgressbarUpdater()
    {
        /* Initialize audio progress handler. */
        if(audioProgressUpdateHandler==null) {
            audioProgressUpdateHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    // The update process message is sent from AudioServiceBinder class's thread object.
                    if (msg.what == audioServiceBinder.UPDATE_AUDIO_PROGRESS_BAR) {
                        if( audioServiceBinder != null) {
                            // Calculate the percentage.
                            int currProgress =audioServiceBinder.getAudioProgress();
                            // Update progressbar. Make the value 10 times to show more clear UI change.
               Log.d("percentage",String.valueOf(currProgress));
//
//                            backgroundAudioProgress.setProgress(currProgress*10);
                        }
                    }
                }
            };
        }
    }



    public void animator(){

        AudioServiceBinder md=new AudioServiceBinder();
         MediaPlayer  mediaPlayer= md.retutnmdinstance();






// Set you media player to the visualizer.
        try{





        }
        catch (NullPointerException e){
            Log.d("mic",e.toString());

        }

    }




    private void showSnackBar(boolean isConnected) {

        // initialize color and message
        String message;
        int color;

        // check condition
        if (isConnected) {

            connected="true";

            // when internet is connected
            // set message


        } else {


            Glide.with(getApplicationContext()).load(R.drawable.play_foreground).into(startBackgroundAudio);

            message = "Not Connected to Internet";

            // set text color
            color = Color.RED;

            Snackbar snackbar = Snackbar.make(findViewById(R.id.start_audio_in_background), message, Snackbar.LENGTH_LONG);

            // initialize view
            View view = snackbar.getView();

            // Assign variable
            TextView textView = view.findViewById(R.id.snackbar_text);

            // set text color
            textView.setTextColor(color);

            // show snack bar
            snackbar.show();

            // when internet
            // is disconnected
            // set message

            audioServiceBinder.stopAudio2();















            state="stop";
//                    imageView.setVisibility(View.GONE);



        }


        // initialize snack bar

    }



    public void notigications(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationUtils = new NotificationUtils(this);
        }



        String title = "רדיו אזורי";
        String author = "שידור חי";




        if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(author)) {
            Notification.Builder nb = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                nb = mNotificationUtils.
                        getAndroidChannelNotification(title, author);
            }
            nb.setSmallIcon(R.drawable.play_foreground);



            mNotificationUtils.getManager().notify(101, nb.build());




        }

    }








    public void clickplaybtn(){






        startBackgroundAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {




                startBackgroundAudio.setClickable(false);










                if(state.equalsIgnoreCase("start")||state.equalsIgnoreCase("stop")) {





                        //The key argument here must match that used in the other activity



                    // Set web audio file url


                    audioServiceBinder.setAudioFileUrl(audioFileUrl);

                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    startBackgroundAudio.setClickable(true);


                                }
                            },
                            3500
                    );
                    // Web audio is a stream audio.

                    // Set application context.
                    audioServiceBinder.setContext(getApplicationContext());
                    // Initialize audio progress bar updater Handler object.

                    createAudioProgressbarUpdater();
                    audioServiceBinder.setAudioProgressUpdateHandler(audioProgressUpdateHandler);

                    // Start audio in background service.





                    createAudioProgressbarUpdater();




                    // Start audio in background service.
                    audioServiceBinder.startAudio();


                    state="playing";

                    showjump.setVisibility(View.VISIBLE);

//                    gone();
                    mutecalls();
                    notigications();
                    animator();



                    Glide.with(getApplicationContext()).load(R.drawable.stop_foreground).into(startBackgroundAudio);




                }
                else if(state.equalsIgnoreCase("playing")){



                    startBackgroundAudio.setClickable(true);
                    audioServiceBinder.stopAudio();


                    Intent stopServiceIntent = new Intent(MainActivity.this, AudioService.class);
                    stopService(stopServiceIntent);

                    state="stop";
                    showjump.setVisibility(View.GONE);




//                    imageView.setVisibility(View.GONE);



                    Glide.with(getApplicationContext()).load(R.drawable.play_foreground).into(startBackgroundAudio);



                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBackPressed() {



        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();

            Intent stopServiceIntent = new Intent(MainActivity.this, AudioService.class);
            stopService(stopServiceIntent);
            finish();
        } else {

            Glide.with(getApplicationContext()).load(R.drawable.play_foreground).into(startBackgroundAudio);
            audioServiceBinder.stopAudio();
            mNotificationUtils = new NotificationUtils(this);
            mNotificationUtils.cancelNotification();
            state="start";
            Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        pressedTime = System.currentTimeMillis();


    }


    public void mutecalls(){


        TelephonyManager telephonyManager =
                (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

        PhoneStateListener callStateListener = new PhoneStateListener() {
            public void onCallStateChanged(int state, String incomingNumber)
            {
                if(state==TelephonyManager.CALL_STATE_RINGING){


                    audioServiceBinder.stopAudio();
                }
                if(state==TelephonyManager.CALL_STATE_OFFHOOK){

                    audioServiceBinder.stopAudio();
                }


                if(state==TelephonyManager.CALL_STATE_IDLE){

                }
            }
        };

        if (Build.VERSION.SDK_INT >= 31)
        {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)
                telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
        else // no permission needed
            telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);

    }







    private void gone() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                backgroundAudioProgress.setVisibility(View.GONE);
            }
        });
    }


    private final Observer<Boolean> activeNetworkStateObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(Boolean isConnected) {
           showSnackBar(isConnected);
        }
    };



}

