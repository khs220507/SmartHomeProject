package com.khs.myiot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PMVService {

    public void processPMVData(float pmv) {
        System.out.println("Received PMV: " + pmv);
        // Add your logic here to handle the received PMV data
    }
}