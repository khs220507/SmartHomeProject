package com.khs.myiot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PMVController {

    @Autowired
    private PMVService pmvService;

    @PostMapping("/pmvData")
    public ResponseEntity<String> receivePMVData(@RequestBody PMVData pmvData) {
        pmvService.processPMVData(pmvData.getPmv());
        return ResponseEntity.status(HttpStatus.OK).body("PMV data received successfully");
    }
}