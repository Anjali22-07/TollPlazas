package com.tollplazas.backend.services.Implementation;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.tollplazas.backend.exceptions.BadRequestException;
import com.tollplazas.backend.exceptions.ExternalServiceException;
import com.tollplazas.backend.services.RouteServices;
import org.springframework.cache.annotation.Cacheable;
import tools.jackson.databind.JsonNode;

@Service 
public class RouteServiceImplementation implements RouteServices{

    private final RestClient restClient;       //communicates with the external API.

        public RouteServiceImplementation(RestClient.Builder builder,
                                        @Value("${ors.api.key}") String apiKey) {
            this.restClient = builder
                    .baseUrl("https://api.openrouteservice.org")
                    .defaultHeader("Authorization", apiKey)
                    .build();
        }

    @Override
    @Cacheable("routes")
    public JsonNode getRoute(double sourceLongitude, double sourceLatitude, double destinationLongitude,
            double destinationLatitude) {

                Map<String, Object> requestBody=  Map.of(
                    "coordinates", 
                      List.of(List.of(sourceLongitude, sourceLatitude),
                              List.of(destinationLongitude, destinationLatitude)
                    )
                );

             try{
                return restClient.post()
                      .uri("/v2/directions/driving-car/geojson")
                      .body(requestBody)
                      .retrieve()
                      .body(JsonNode.class);
             }catch (RestClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                throw new BadRequestException("No route could be found between the given pincodes");
            }
               throw new ExternalServiceException("Routing service is unavailable", e);
            } catch (RestClientException e) {
                throw new ExternalServiceException("Routing service is unavailable", e);
            }   
    }

       
        
}
