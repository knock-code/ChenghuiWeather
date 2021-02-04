package com.example.chenghuiweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chenghuiweather.R;
import com.example.chenghuiweather.util.Utility;
import com.example.chenghuiweather.util.Weather;

public class WeatherActivity extends Activity implements View.OnClickListener {
    private LinearLayout weatherInfoLayout;
    /**
     * 用于显示城市名
     * */
    private TextView cityNameText;
    /**
     * 用于显示发布时间
     * */
    private TextView publishText;
    /**
     * 用于显示天气描述信息
     * */
    private TextView weatherDespText;
    /**
     * 用于显示气温1
     * */
    private TextView temp1Text;
    /**
     * 用于显示气温2
     * */
    private TextView temp2Text;
    /**
     * 用于显示当前日期
     * */
    private TextView currentDateText;
    /**
     * 切换城市按钮
     * */
    private Button switchCity;
    /**
     * 更新天气按钮
     * */
    private Button refreshWeather;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        //初始化各控件
        weatherInfoLayout = findViewById(R.id.weather_info_layout);
        cityNameText = findViewById(R.id.city_name);
        publishText = findViewById(R.id.publish_text);
        weatherDespText = findViewById(R.id.weather_desp);
        temp1Text = findViewById(R.id.temp1);
        temp2Text = findViewById(R.id.temp2);
        currentDateText = findViewById(R.id.current_date);
        switchCity = findViewById(R.id.switch_city);
        refreshWeather = findViewById(R.id.refresh_weather);
        switchCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);
        String countyCode = getIntent().getStringExtra("county_code");
        if (!TextUtils.isEmpty(countyCode)){
            //有县级代号就去查询天气
            publishText.setText("同步中……");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        }
    }

    /**
     * 查询县级代号对应的天气代号
     * */
    private void queryWeatherCode(String countyCode) {
        String address = "http://guolin.tech/api/weather?cityid=" + countyCode + "&key=bc0418b57b2d4918819d3974ac1285d9";
        queryFromService(address);
    }

    /**
     * 根据传入的地址和类型去向服务器查询天气信息
     * */
    private void queryFromService(final String address){
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(address,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //处理器返回天气的信息
                        Weather weather = Utility.handleWeatherResponse(s);
                        showWeather(weather);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        publishText.setText("同步失败");
                    }
                });
        queue.add(stringRequest);
    }

    private void showWeather(Weather weather) {
        cityNameText.setText(weather.getHeWeather().get(0).getBasic().getLocation()); //城市名
        temp1Text.setText(weather.getHeWeather().get(0).getNow().getTmp()); //最低温
        temp2Text.setText(weather.getHeWeather().get(0).getNow().getVis()); //最高温
        weatherDespText.setText(weather.getHeWeather().get(0).getNow().getCond_txt()); //天气描述信息
        publishText.setText("今天" + weather.getHeWeather().get(0).getUpdate().getUtc() + "发布"); //发布时间
        currentDateText.setText(weather.getHeWeather().get(0).getUpdate().getLoc()); //当前日期
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
    }

    /**
     * Home和刷新点击事件
     * */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_city:{
                Intent intent = new Intent(this,ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity",true);
                startActivity(intent);
                finish();
                break;
            }
            case R.id.refresh_weather:{
                publishText.setText("同步中……");
                String countyCode = getIntent().getStringExtra("county_code");
                weatherInfoLayout.setVisibility(View.INVISIBLE);
                cityNameText.setVisibility(View.INVISIBLE);
                queryWeatherCode(countyCode);
                break;
            }
            default:
                break;
        }
    }
}
