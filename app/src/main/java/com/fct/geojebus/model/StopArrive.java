package com.fct.geojebus.model;

public class StopArrive {
    private String route_country;
    private String route_name;
    private int wait_time;
    private String position;
    private int position_num;
    private String vehicle_num;
    private String route_bis;

    public String getRouteCountry() {
        return route_country;
    }

    public void setRouteCountry(String route_country) {
        this.route_country = route_country;
    }

    public String getRouteName() {
        return route_name;
    }

    public void setRouteName(String route_name) {
        this.route_name = route_name;
    }

    public int getWaitTime() {
        return wait_time;
    }

    public void setWaitTime(int wait_time) {
        this.wait_time = wait_time;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getPositionNum() {
        return position_num;
    }

    public void setPositionNum(int position_num) {
        this.position_num = position_num;
    }

    public String getVehicleNum() {
        return vehicle_num;
    }

    public void setVehicleNum(String vehicle_num) {
        this.vehicle_num = vehicle_num;
    }

    public String getRouteBis() {
        return route_bis;
    }

    public void setRouteBis(String route_bis) {
        this.route_bis = route_bis;
    }

}
