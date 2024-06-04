package com.khs.myiot;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
    SensorData findTopByOrderByIdDesc();
    Optional<SensorData> findFirstByOrderByIdAsc();
    List<SensorData> findTop10ByOrderByIdDesc();

}