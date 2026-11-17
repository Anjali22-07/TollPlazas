package com.tollplazas.backend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tollplazas.backend.dto.RouteRequest;
import com.tollplazas.backend.repository.TollPlazaRepository;
import com.tollplazas.backend.services.Implementation.RouteServiceImplementation;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;

@RestController 
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor 
public class RouteController {

    private final RouteServiceImplementation routeServices;


    @PostMapping 
    public JsonNode getRoute(@RequestBody RouteRequest requestRoute){

        return routeServices.getRoute(
           requestRoute.sourceLongitude(),
      requestRoute.sourceLatitude(),
      requestRoute.destinationLongitude(),
      requestRoute.destinationLatitude()
        );
    }

}
