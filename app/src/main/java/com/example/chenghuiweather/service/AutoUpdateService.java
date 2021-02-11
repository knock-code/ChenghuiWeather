package com.example.chenghuiweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chenghuiweather.activity.WeatherActivity;
import com.example.chenghuiweather.receiver.AutoUpdateReceiver;
import com.example.chenghuiweather.util.Utility;
import com.example.chenghuiweather.util.Weather;

public class AutoUpdateService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWeather();
            }
        }).start();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 60 * 1000;  //1分钟
        Long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent intent1 = new Intent(this, AutoUpdateReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this,0,intent1,0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新天气信息
     * */
    private void updateWeather() {
        Log.d("GGG", Thread.currentThread().getId() + "");
        SharedPreferences sp = getSharedPreferences("flag",0);
        String flag = sp.getString("county_code","");
        String address = "http://guolin.tech/api/weather?cityid=" + flag + "&key=bc0418b57b2d4918819d3974ac1285d9";
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(address,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Weather weather = Utility.handleWeatherResponse(s);
                        Intent intent = new Intent("package com.example.chenghuiweather.activity.FLAG");
                        intent.putExtra("weather",weather);
                        sendBroadcast(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        volleyError.printStackTrace();
                    }
                });
        queue.add(stringRequest);
    }
}
