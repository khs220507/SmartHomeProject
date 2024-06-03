package com.khs.myiot;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.time.LocalDateTime;


@Entity
public class PMVData {

    @Id
    @GeneratedValue
    private Long id;

    private float pmv;
    private float temperature; // 추가: 온도
    private float humidity; // 추가: 습도
    private LocalDateTime timestamp;
    // getters and setters

    public float getPmv() {
        return pmv;
    }

    public void setPmv(float pmv) {
        this.pmv = pmv;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getHumidity() {
        return humidity;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}