package com.example.dodgegame;

import static java.lang.Math.abs;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MotionEvent;
import android.widget.TextView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity implements SensorEventListener {
    //Code from this program has been used from Beginning Android Games
    //Review SurfaceView, Canvas, continue

    GameSurface gameSurface;
    MediaPlayer mediaPlayer;
    public int scoreDodge = 0;
    public String timerVal;
    //public String speedVal = "Slow";
    public float goofygoober;
    //boolean speedToggle = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        gameSurface = new GameSurface(this);
        setContentView(gameSurface);
        gameSurface.setKeepScreenOn(true);

        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        int length = mediaPlayer.getCurrentPosition();
        mediaPlayer.start();

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this,accelerometerSensor,SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onStop() {
        super.onStop();
        gameSurface.pause();
    }

    @Override
    protected void onPause(){
        super.onPause();
        gameSurface.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        gameSurface.resume();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
//        float[] g = new float[3];
//        g = sensorEvent.values.clone();
//        double norm_Of_g = Math.sqrt(g[0] * g[0] + g[1] * g[1] + g[2] * g[2]);
//        g[0] = (float) (g[0] / norm_Of_g);
//        g[1] = (float) (g[1] / norm_Of_g);
//        g[2] = (float) (g[2] / norm_Of_g);
//
//        int inclination = (int) Math.round(Math.toDegrees(Math.acos(g[2])));
//
//        if (inclination > 25 || inclination < 155){
//            int rotation = (int) Math.round(Math.toDegrees(Math.atan2(g[0], g[1])));
//            goofygoober = rotation;
//        }

        goofygoober = sensorEvent.values[0];
        Log.d("hi", String.valueOf(goofygoober));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    //----------------------------GameSurface Below This Line--------------------------
    public class GameSurface extends SurfaceView implements Runnable {
        //https://developer.android.com/reference/android/view/SurfaceView

        Thread gameThread;
        SurfaceHolder holder;
        volatile boolean running = false;
        //volatile boolean touched = false;

        Bitmap enemyCar;
        Bitmap playerFine;
        Bitmap playerFineBak;
        Bitmap playerInjured;

        Paint paintProperty;

        int screenWidth;
        int screenHeight;
        //int x_direction;
        int y_direction;
        int carSpeedRand = new Random().nextInt((25 - 15) + 1) + 15;

        public GameSurface(Context context) {
            super(context);
            holder=getHolder();
            enemyCar = BitmapFactory.decodeResource(getResources(),R.drawable.enemy_car);
            enemyCar = Bitmap.createScaledBitmap(enemyCar,(int)(enemyCar.getWidth()*0.32), (int)(enemyCar.getHeight()*0.32), true);

            playerFine = BitmapFactory.decodeResource(getResources(),R.drawable.player_fine);
            playerFine = Bitmap.createScaledBitmap(playerFine,(int)(playerFine.getWidth()*0.2), (int)(playerFine.getHeight()*0.2), true);
            playerFineBak = BitmapFactory.decodeResource(getResources(),R.drawable.player_fine);
            playerFineBak = Bitmap.createScaledBitmap(playerFineBak,(int)(playerFineBak.getWidth()*0.2), (int)(playerFineBak.getHeight()*0.2), true);

            playerInjured = BitmapFactory.decodeResource(getResources(),R.drawable.player_injured);
            playerInjured = Bitmap.createScaledBitmap(playerInjured,(int)(playerInjured.getWidth()*0.2), (int)(playerInjured.getHeight()*0.2), true);

            y_direction = 0 - enemyCar.getHeight();
            Display screenDisplay = getWindowManager().getDefaultDisplay();
            Point sizeOfScreen = new Point();
            screenDisplay.getSize(sizeOfScreen);
            screenWidth=sizeOfScreen.x;
            screenHeight=sizeOfScreen.y;

            paintProperty= new Paint();

            new CountDownTimer(60000, 1000) {
                public void onTick(long millisUntilFinished) {
                    timerVal = String.valueOf(millisUntilFinished / 1000);
                    invalidate(); // Force the View to redraw
                }
                public void onFinish() {
                    timerVal = "0";
                    pause();
                }
            }.start();

        }

//        @Override
//        public boolean onTouchEvent(MotionEvent event) {
//            speedToggle = !speedToggle;
//            return speedToggle;
//        }

        @Override
        public void run() {

            boolean loopFlag1 = false;
            boolean loopFlag2 = false;
            MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.explosion);
            int min = 0;
            int max = screenWidth - enemyCar.getWidth();
            int enemy_car_pos_random = new Random().nextInt((max - min) + 1) + min;
            int player_x_pos = (screenWidth - playerFine.getWidth()) / 2;
            int enemy_car_x_pos;
            boolean deathFlag = false;

            while (running == true){

//                int speedMax = 0;
//                int speedMin = 0;
//                if (speedToggle = false) {
//                    speedVal = "Slow";
//                    speedMax = 25;
//                    speedMin = 15;
//                }
//                else {
//                    speedVal = "Fast";
//                    speedMax = 45;
//                    speedMin = 35;
//                }
//                carSpeedRand = new Random().nextInt((speedMax - speedMin) + 1) + speedMin;

                if (holder.getSurface().isValid() == false)
                    continue;
                // https://developer.android.com/reference/android/graphics/Canvas
                Canvas canvas= holder.lockCanvas();
                //make textview programmatically
                //Atari 2600 - Street Racer game

                y_direction+=carSpeedRand;
                if(y_direction >= screenHeight + enemyCar.getHeight()){
                    if (deathFlag != true){
                        scoreDodge++;
                    }
                    else if (deathFlag = true){
                        if (scoreDodge >= 1){
                            scoreDodge--;
                        }
                        else{
                            scoreDodge = 0;
                        }
                    }
                    enemy_car_pos_random = new Random().nextInt((max - min) + 1) + min;
                    y_direction = 0 - enemyCar.getHeight();
                    deathFlag = false;
                    playerFine = playerFineBak;
                }
                canvas.drawRGB(92,102,113);
                canvas.drawBitmap(enemyCar,enemy_car_pos_random,y_direction,null);

                enemy_car_x_pos = enemy_car_pos_random + enemyCar.getWidth();
                if(player_x_pos + playerFine.getWidth() <= screenWidth && player_x_pos >= 0){
                    if(goofygoober < -1){
                        player_x_pos = (int) (player_x_pos + (8 + (abs(goofygoober)*2)));
                    }
                    else if(goofygoober > 1){
                        player_x_pos = (int) (player_x_pos - (8 + (abs(goofygoober)*2)));
                    }
                }
                else if (player_x_pos + playerFine.getWidth() >= screenWidth){
                    player_x_pos = screenWidth - playerFine.getWidth();
                    if(goofygoober < -1){
                        player_x_pos = (int) (player_x_pos + (8 + (abs(goofygoober)*2)));
                    }
                }
                else if (player_x_pos <= 0){
                    player_x_pos = 0;
                    if(goofygoober > 1){
                        player_x_pos = (int) (player_x_pos - (8 + (abs(goofygoober)*2)));
                    }
                }
                canvas.drawBitmap(playerFine, player_x_pos, screenHeight - 500, null);
                if ((player_x_pos >= enemy_car_pos_random && player_x_pos <= enemy_car_x_pos) || (player_x_pos + playerFine.getWidth() >= enemy_car_pos_random && player_x_pos + playerFine.getWidth() <= enemy_car_x_pos)){
                    if ((y_direction + enemyCar.getHeight() >= screenHeight - 500) && y_direction < (screenHeight - 500) + playerFine.getHeight() ) {
                        playerFine = playerInjured;
                        deathFlag = true;
                        mp.start();
                    }
                }

                Paint textPaint = new Paint();
                textPaint.setColor(Color.WHITE);
                textPaint.setTextSize(70);
                canvas.drawText("Time: " + timerVal, 20,70, textPaint);
                canvas.drawText("Score: " + scoreDodge, 20,150, textPaint);
//                canvas.drawText("Speed: " + speedVal, 20,230, textPaint);
                if(Integer.parseInt(timerVal) == 0){
                    textPaint.setTextSize(140);
                    canvas.drawText("Game Over!", 20,300, textPaint);
                }

                holder.unlockCanvasAndPost(canvas);
            }
        }

        public void resume(){
            running = true;
            gameThread=new Thread(this);
            gameThread.start();
        }

        public void pause() {
            boolean retry = true;
            running = false;
            while (retry) {
                try {
                    gameThread.join();
//                    if(Integer.parseInt(timerVal) > 0){
//                        y_direction = 0 - enemyCar.getHeight();
//                        retry = false;
//                    }
                } catch (InterruptedException e) {
                }
            }
        }

    }//GameSurface
}//Activity