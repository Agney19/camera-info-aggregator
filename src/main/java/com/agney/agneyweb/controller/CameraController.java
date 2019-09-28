package com.agney.agneyweb.controller;

import com.agney.agneyweb.dto.CameraAggregateDataDto;
import com.agney.agneyweb.service.CameraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CameraController {
    @Autowired
    CameraService cameraService;

    @GetMapping(value = "camera/aggregate")
    public List<CameraAggregateDataDto> getInfoAggregate() {
        return cameraService.getInfoAggregate();
    }
}
