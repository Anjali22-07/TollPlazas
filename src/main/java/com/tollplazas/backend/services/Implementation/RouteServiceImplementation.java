package com.tollplazas.backend.services.Implementation;

import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.tollplazas.backend.services.RouteServices;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;

@Service 
public class RouteServiceImplementation implements RouteServices{

    private final RestClient restClient;       //communicates with the external API.

    public RouteServiceImplementation(@Value("${ors.api.key}") String apiKey){
             this.restClient= RestClient.builder()
                             .baseUrl("https://api.openrouteservice.org")
                            .defaultHeader("Authorization",apiKey)      //attaches your API key to requests.
                            .build();
                              
    }

    @Override
    public JsonNode getRoute(double sourceLongitude, double sourceLatitude, double destinationLongitude,
            double destinationLatitude) {

                Map<String, Object> requestBody=  Map.of(
                    "coordinates", 
                      List.of(List.of(sourceLongitude, sourceLatitude),
                              List.of(destinationLongitude, destinationLatitude)
                    )
                );


                return restClient.post()
                      .uri("/v2/directions/driving-car/geojson")
                      .body(requestBody)
                      .retrieve()
                      .body(JsonNode.class);

           }

    

}
