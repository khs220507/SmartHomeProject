package com.khs.myiot;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.sql.Timestamp;

@Service
public class SensorDataService {

    private final SensorDataRepository repository;

    @Autowired
    public SensorDataService(SensorDataRepository repository) {
        this.repository = repository;
    }

    public void saveSensorData(float temperature, float humidity) {
        long count = repository.count();

        if (count >= 1000) {
            // 만약 저장된 데이터가 이미 1000개라면, 가장 오래된 데이터를 삭제
            SensorData oldestData = repository.findFirstByOrderByIdAsc().orElse(null);
            if (oldestData != null) {
                repository.delete(oldestData);
            }
        }

        SensorData data = new SensorData();

        data.setTemperature(temperature);
        data.setHumidity(humidity);
        LocalDateTime nowInKorea = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime nowInKoreaWithoutNanos = nowInKorea.truncatedTo(ChronoUnit.SECONDS);
        Timestamp timestamp = Timestamp.valueOf(nowInKoreaWithoutNanos);
        timestamp.setNanos(0);  // Explicitly remove nanoseconds
        data.setTime(timestamp);
        repository.save(data);
    }


    public List<SensorData> getLatest10Data() {
        return repository.findTop10ByOrderByIdDesc();
    }
    // 추가적인 메서드 정의 가능
}