package com.tollplazas.backend.dto;

public record RouteRequest(
     double sourceLongitude,
     double sourceLatitude,
     double destinationLatitude,
     double destinationLongitude
){

}
