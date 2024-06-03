package com.khs.myiot;



import jakarta.persistence.Entity;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private float temperature;
    private float humidity;
    @Column(name="timestamp", columnDefinition="TIMESTAMP(0)")
    private Timestamp time;



    public float getHumidity() {
        return humidity;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "SensorData{" +
                "id=" + id +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", time=" + time +
                '}';
    }
}