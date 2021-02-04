package com.example.chenghuiweather.model;
/**市类*/
public class City {
    private int id;
    private String cityName;
    private String cityCode;
    private int provinceId;

    public int getId() {
        return id;
    }

    public City setId(int id) {
        this.id = id;
        return this;
    }

    public String getCityName() {
        return cityName;
    }

    public City setCityName(String cityName) {
        this.cityName = cityName;
        return this;
    }

    public String getCityCode() {
        return cityCode;
    }

    public City setCityCode(String cityCode) {
        this.cityCode = cityCode;
        return this;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public City setProvinceId(int provinceId) {
        this.provinceId = provinceId;
        return this;
    }
}
