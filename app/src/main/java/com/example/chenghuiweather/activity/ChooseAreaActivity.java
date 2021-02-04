package com.example.chenghuiweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chenghuiweather.R;
import com.example.chenghuiweather.model.City;
import com.example.chenghuiweather.model.County;
import com.example.chenghuiweather.model.Province;
import com.example.chenghuiweather.model.ToolDatabase;
import com.example.chenghuiweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

public class ChooseAreaActivity extends Activity {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ToolDatabase toolDatabase;
    private List<String> dataList = new ArrayList<>();
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    /**
     * 省列表
     * */
    private List<Province> provinceList;
    /**
     * 市列表
     * */
    private List<City> cityList;
    /**
     * 县列表
     * */
    private List<County> countyList;
    /**
     * 选中的省份
     * */
    private Province selectedProvince;
    /**
     * 选中的市
     * */
    private City selectedCity;
    /**
     * 当前选中的级别
     * */
    private int currentLevel;
    /**
     * 是否从WeatherActivity中跳转过来
     * */
    private boolean isFromWeatherActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences("flag",0);
        String flag = sp.getString("county_code","");
        isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity",false);
        if (!(flag.equals("")) && !isFromWeatherActivity){
            Intent intent = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
            intent.putExtra("county_code",flag);
            startActivity(intent);
            finish();
            return;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        listView = findViewById(R.id.list_view);
        titleText = findViewById(R.id.title_text);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        toolDatabase = ToolDatabase.getInstance(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    String countyCode = countyList.get(position).getCountyCode();
                    Intent intent = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
                    intent.putExtra("county_code",countyCode);
                    startActivity(intent);
                    editor = sp.edit();
                    editor.putString("county_code",countyCode);
                    editor.commit();
                    finish();
                }
            }
        });
        queryProvinces();  //加载省级数据
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有去服务器上查询
     * */
    private void queryProvinces() {
        provinceList = toolDatabase.loadProvince();
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryFromService(null,"province");
        }
    }

    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没有去服务器上查询
     * */
    private void queryCities() {
        cityList = toolDatabase.loadCities(selectedProvince.getId());
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        } else {
            queryFromService(selectedProvince.getProvinceCode(),"city");
        }
    }

    /**
     * 查询选中市内所有的县，优先从数据库中查询，如果没有到服务器上查询
     * */
    private void queryCounties() {
        countyList = toolDatabase.loadCounties(selectedCity.getId());
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        } else {
            queryFromService(selectedCity.getCityCode(),"county");
        }
    }

    /**根据传入的代号和类型从服务器上查询省市县数据*/
    private void queryFromService(final String code, final String type) {
        String address = null;
        if (code != null) {
            if (type.equals("city")) {
                address = "http://guolin.tech/api/china/" + code;
            } else if (type.equals("county")){
                address = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode() + "/" + code;
            }
        } else {
            address = "http://guolin.tech/api/china";
        }
        showProgressDialog();
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(address,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        boolean result = false;
                        if ("province".equals(type)){
                            result = Utility.handleProvincesResponse(toolDatabase,s);
                        } else if ("city".equals(type)){
                            result = Utility.handleCitiesResponse(toolDatabase,s,selectedProvince.getId());
                        } else if ("county".equals(type)){
                            result = Utility.handleCountiesResponse(toolDatabase,s,selectedCity.getId());
                        }
                        if (result) {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            } else if ("city".equals(type)){
                                queryCities();
                            } else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
        queue.add(stringRequest);
    }

    /**
     * 显示进度对话框
     * */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载……");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     * */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    /**
     * 捕获Back键，根据当前的级别来判断，此时应该返回市列表、省列表、还是直接退出
     * */
    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_COUNTY) {
            queryCities();
        } else if (currentLevel == LEVEL_CITY) {
            queryProvinces();
        } else {
            if (isFromWeatherActivity) {
                Intent intent = new Intent(this,WeatherActivity.class);
                intent.putExtra("county_code",sp.getString("county_code",""));
                startActivity(intent);
            }
            finish();
        }
    }
}
