package com.example.chenghuiweather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

/**数据库类*/
public class WeatherDatabaseHelper extends SQLiteOpenHelper {
    public WeatherDatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table Province(id integer primary key autoincrement," +
                "province_name text," +
                "province_code text)");  //创建省Province表
        db.execSQL("create table City(id integer primary key autoincrement," +
                "city_name text," +
                "city_code text," +
                "province_id integer)");  //创建省City表
        db.execSQL("create table County(id integer primary key autoincrement," +
                "county_name text," +
                "county_code text," +
                "city_id integer)");  //创建省County表
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
