package com.tollplazas.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tollplazas.backend.dto.TollRequest;
import com.tollplazas.backend.dto.TollResponse;
import com.tollplazas.backend.services.Implementation.TollPlazaServiceImplementation;

import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping("/api/v1")
@RequiredArgsConstructor 
public class TollPlazaController {

    private final TollPlazaServiceImplementation tollPlazaService;

    @PostMapping("/toll-plaza")
    public ResponseEntity<TollResponse> tollPlazaRequest(@RequestBody TollRequest tollRequest){

         return ResponseEntity.status(HttpStatus.OK).body(tollPlazaService.findBetween(tollRequest));
    }

}
