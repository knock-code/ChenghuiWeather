package com.example.chenghuiweather.model;
/**省类*/
public class Province {
    private int id;
    private String provinceName;
    private String provinceCode;

    public int getId() {
        return id;
    }

    public Province setId(int id) {
        this.id = id;
        return this;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public Province setProvinceName(String provinceName) {
        this.provinceName = provinceName;
        return this;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public Province setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
        return this;
    }
}
