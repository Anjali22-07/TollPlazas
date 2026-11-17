package com.tollplazas.backend.services;

import tools.jackson.databind.JsonNode;

public interface RouteServices {

    public JsonNode getRoute(
        double sourceLongitude,
        double sourceLatitude,
        double  destinationLongitude,
        double  destinationLatitude
    );

}
