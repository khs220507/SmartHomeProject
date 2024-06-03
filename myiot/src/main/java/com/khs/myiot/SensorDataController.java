package com.khs.myiot;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
public class SensorDataController {

    private final SensorDataService sensorDataService;

    @Autowired
    public SensorDataController(SensorDataService sensorDataService) {
        this.sensorDataService = sensorDataService;
    }

    @PostMapping("/data")
    public ResponseEntity<String> handleData(@RequestBody SensorDataRequest request) {
        try {
            float temperature = request.getTemperature();
            float humidity = request.getHumidity();

            // 받은 데이터를 JPA를 통해 데이터베이스에 저장
            sensorDataService.saveSensorData(temperature, humidity);

            // 성공적으로 데이터를 저장했음을 응답
            return ResponseEntity.ok("Data saved successfully");
        } catch (Exception e) {
            // 예외 발생 시 에러 응답
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while saving data");
        }
    }
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/latestData")
    public ResponseEntity<Object> latestData() {
        // Get the latest sensor data
        SensorData latestData = sensorDataService.getLatestData();

        if (latestData == null) {
            // Handle case when no data is available
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No sensor data available");
        }

        // Call Python server to get PMV data
        String pythonServerUrl = "http://localhost:5000/latestPMVData";
        ResponseEntity<PMVData> responseEntity = new RestTemplate().getForEntity(pythonServerUrl, PMVData.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            PMVData pmvData = responseEntity.getBody();

            // Create a response object containing sensor data and PMV, temperature, humidity
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("latestData", latestData);
            responseData.put("pmv", pmvData.getPmv());
            responseData.put("temperature", pmvData.getTemperature());
            responseData.put("humidity", pmvData.getHumidity());

            System.out.println("Latest Data: " + responseData.get("latestData"));
            System.out.println("PMV: " + responseData.get("pmv"));
            System.out.println("Temperature: " + responseData.get("temperature"));
            System.out.println("Humidity: " + responseData.get("humidity"));

            return ResponseEntity.status(HttpStatus.OK).body(responseData);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while fetching data");
        }
    }
    @PostMapping("/receivePMV")
    public ResponseEntity<String> receivePMV(@RequestBody PMVData pmvData) {
        float pmv = pmvData.getPmv();
        // Process the received PMV value as needed
        System.out.println("Received PMV from Python: " + pmv);
        return ResponseEntity.status(HttpStatus.OK).body("PMV data received successfully");
    }




}
