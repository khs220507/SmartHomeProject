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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
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
        // Get the latest 10 sensor data
        List<SensorData> latest10Data = sensorDataService.getLatest10Data();

        if (latest10Data.isEmpty()) {
            // Handle case when no data is available
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No sensor data available");
        }

        // Call Python server to get PMV data
        String pythonServerUrl = "http://localhost:5000/latest10PMVData";
        ResponseEntity<PMVData[]> responseEntity = new RestTemplate().getForEntity(pythonServerUrl, PMVData[].class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            PMVData[] pmvDataArray = responseEntity.getBody();
            List<PMVData> pmvDataList = Arrays.asList(pmvDataArray);

            // Get weather data
            ResponseEntity<Object> weatherResponse = getWeatherData();
            System.out.println(weatherResponse);

            if (weatherResponse.getStatusCode() == HttpStatus.OK) {
                // Combine sensor data, PMV data, and weather data into a single response object
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("latestData", latest10Data);
                responseData.put("pmvData", pmvDataList);
                responseData.put("weatherData", weatherResponse.getBody());

                return ResponseEntity.status(HttpStatus.OK).body(responseData);
            } else {
                System.err.println("Error occurred while fetching weather data: " + weatherResponse.getBody());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while fetching weather data");
            }
        } else {
            System.err.println("Error occurred while fetching PMV data: " + responseEntity.getBody());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while fetching PMV data");
        }
    }

    private ResponseEntity<Object> getWeatherData() {

        String apiKey = "V0gKSXsI%2FUBNHMNqU76jwCi62UriYk3PhhRDO4DJPq44oLyB596SjWXsndjSNbPejC3bff9iwwe2HPTrbt6wyg%3D%3D";
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmm");

        String base_date = now.format(dateFormatter);



        LocalDateTime baseTime = now.withHour(now.getHour() - 1).withMinute(0).withSecond(0);


        String base_time = baseTime.format(timeFormatter);

        System.out.println(base_time);
        System.out.println(base_date);

        // Coordinates for Seoul
        String nx = "37";
        String ny = "127";


        String url = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst"
                + "?serviceKey=" + apiKey
                + "&pageNo=1"
                + "&numOfRows=1000"
                + "&dataType=JSON"
                + "&base_date=" + base_date
                + "&base_time=" + base_time
                + "&nx=" + nx
                + "&ny=" + ny;

        try {
            // Send API request to fetch weather data
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Object> responseEntity = restTemplate.getForEntity(url, Object.class);
            System.out.println("테스트 1 : " + responseEntity);
            Object responseBody = responseEntity.getBody();
            System.out.println("테스트 2 : " + responseBody);
            return responseEntity;
        } catch (HttpClientErrorException e) {
            // Handle client errors (4xx status codes)
            System.err.println("Client error while fetching weather data: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Client error occurred while fetching weather data");
        } catch (HttpServerErrorException e) {
            // Handle server errors (5xx status codes)
            System.err.println("Server error while fetching weather data: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error occurred while fetching weather data");
        } catch (RestClientException e) {
            // Handle general errors
            System.err.println("Error occurred while fetching weather data: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while fetching weather data");
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
