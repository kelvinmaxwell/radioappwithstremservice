package com.karimax.radioappwithstremservice;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chibde.visualizer.CircleBarVisualizerSmooth;
import com.google.android.material.snackbar.Snackbar;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements ConnectionReceiver.ReceiverListener {
    public NotificationUtils mNotificationUtils;
    private long pressedTime;
    AlertDialog.Builder builder;
    private AudioServiceBinder audioServiceBinder = null;
    private Handler audioProgressUpdateHandler = null;
    public String state="start";
    ImageButton startBackgroundAudio;
    CircleBarVisualizerSmooth imageView;
    private static String TAG = "PermissionDemo";
    private static final int RECORD_REQUEST_CODE = 101;
    final String audioFileUrl = "https://live.ecast.co.il/stream5/8000/azori";

    // Show played audio progress.
    private ProgressBar backgroundAudioProgress;
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

        mNotificationUtils = new NotificationUtils(this);








        startBackgroundAudio = findViewById(R.id.start_audio_in_background);
        setTitle("dev2qa.com - Play Audio Use Background Service");


        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied");
            setupPermissions();
        }
        else{
            clickplaybtn();

        }





        bindAudioService();
        // Bind background audio service when activity is created.


        backgroundAudioProgress = (ProgressBar)findViewById(R.id.playbtn);
        backgroundAudioProgress.setVisibility(View.GONE);


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
        Button pauseBackgroundAudio = (Button)findViewById(R.id.pause_audio_in_background);
        pauseBackgroundAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioServiceBinder.pauseAudio();
                Toast.makeText(getApplicationContext(), "Play web audio file is paused.", Toast.LENGTH_LONG).show();
            }
        });
        // Click this button to stop the media player in background service.
        Button stopBackgroundAudio = (Button)findViewById(R.id.stop_audio_in_background);
        stopBackgroundAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioServiceBinder.stopAudio(imageView);
//                backgroundAudioProgress.setVisibility(ProgressBar.INVISIBLE);
                state="start";
                Toast.makeText(getApplicationContext(), "Stop play web audio file.", Toast.LENGTH_LONG).show();
            }
        });


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


        imageView.setColor(ContextCompat.getColor(this, R.color.white));



// Set you media player to the visualizer.
        try{imageView.setPlayer(mediaPlayer.getAudioSessionId());




        }
        catch (NullPointerException e){
            Log.d("mic",e.toString());

        }

    }


    private void checkConnection() {

        // initialize intent filter
        IntentFilter intentFilter = new IntentFilter();

        // add action
        intentFilter.addAction("android.new.conn.CONNECTIVITY_CHANGE");

        // register receiver
        registerReceiver(new ConnectionReceiver(), intentFilter);

        // Initialize listener
        ConnectionReceiver.Listener = this;

        // Initialize connectivity manager
        ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Initialize network info
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        // get connection status
        boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();

        // display snack bar
        showSnackBar(isConnected);
    }

    private void showSnackBar(boolean isConnected) {

        // initialize color and message
        String message;
        int color;

        // check condition
        if (isConnected) {

            // when internet is connected
            // set message


        } else {

            // when internet
            // is disconnected
            // set message



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
        }


        // initialize snack bar

    }

    @Override
    public void onNetworkChange(boolean isConnected) {
        // display snack bar
        showSnackBar(isConnected);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // call method
        checkConnection();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // call method
        checkConnection();
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



    private void setupPermissions() {

        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied");

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(this);
                builder.setMessage("Permission to access the microphone is required for this app to record audio.")
                        .setTitle("Permission required");

                builder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int id) {
                                Log.i(TAG, "Clicked");
                                makeRequest();
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                makeRequest();
            }
        }}


    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                RECORD_REQUEST_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RECORD_REQUEST_CODE: {

                if (grantResults.length == 0
                        || grantResults[0] !=
                        PackageManager.PERMISSION_GRANTED) {

                    Log.i(TAG, "Permission has been denied by user");
                    finish();
                } else {



                    clickplaybtn();




                    Log.i(TAG, "Permission has been granted by user");
                }
            }
        }
    }

    public void clickplaybtn(){






        startBackgroundAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                checkConnection();
                startBackgroundAudio.setClickable(false);











                if(state.equalsIgnoreCase("start")||state.equalsIgnoreCase("stop")) {


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

                    imageView = findViewById(R.id.visualizer);


                    // Start audio in background service.
                    audioServiceBinder.startAudio(imageView,backgroundAudioProgress);


                    state="playing";
                    gone();
                    mutecalls();
                    notigications();
                    animator();



                    Glide.with(getApplicationContext()).load(R.drawable.stop_foreground).into(startBackgroundAudio);




                }
                else if(state.equalsIgnoreCase("playing")){
                    startBackgroundAudio.setClickable(true);
                    audioServiceBinder.stopAudio(imageView);
                    imageView=null;

                    Intent stopServiceIntent = new Intent(MainActivity.this, AudioService.class);
                    stopService(stopServiceIntent);

                    state="stop";
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
            audioServiceBinder.stopAudio(imageView);
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


                    audioServiceBinder.stopAudio(imageView);
                }
                if(state==TelephonyManager.CALL_STATE_OFFHOOK){

                    audioServiceBinder.stopAudio(imageView);
                }


                if(state==TelephonyManager.CALL_STATE_IDLE){

                }
            }
        };
        telephonyManager.listen(callStateListener,PhoneStateListener.LISTEN_CALL_STATE);

    }







    private void gone() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                backgroundAudioProgress.setVisibility(View.GONE);
            }
        });
    }



}

