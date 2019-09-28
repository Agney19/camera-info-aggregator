package com.agney.agneyweb.controller;

import com.agney.agneyweb.dto.CameraAggregateDataOutDto;
import com.agney.agneyweb.service.CameraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CameraController {
    @Autowired
    CameraService cameraService;

    /** Получить агрегированную информацию по камерам */
    @GetMapping(value = "camera/info/aggregate")
    public List<CameraAggregateDataOutDto> getInfoAggregate() {
        return cameraService.getInfoAggregate();
    }
}
