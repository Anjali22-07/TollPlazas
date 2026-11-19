package com.tollplazas.backend.services.Implementation;


import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import com.tollplazas.backend.exceptions.BadRequestException;
import com.tollplazas.backend.exceptions.ExternalServiceException;
import com.tollplazas.backend.services.PincodeServices;

import tools.jackson.databind.JsonNode;

@Service
public class PincodeServicesImplementation implements PincodeServices {

     private final RestClient restClient;

    public PincodeServicesImplementation(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("https://nominatim.openstreetmap.org")
                .defaultHeader("User-Agent", "TollPlazasAssignment/1.0")
                .build();
    }

    @Override
    @Cacheable("pincodes")
    public double[] getCoordinates(String pincode) {
        try {
            JsonNode result = restClient.get()
                    .uri(uri -> uri.path("/search")
                            .queryParam("postalcode", pincode)
                            .queryParam("country", "India")
                            .queryParam("format", "json")
                            .queryParam("limit", 1)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

           if (result == null || result.isEmpty()) {
                throw new BadRequestException("Invalid source or destination pincode");
            }

            double lat = result.get(0).path("lat").asDouble();   // Nominatim sends these as strings
            double lon = result.get(0).path("lon").asDouble();
            return new double[]{lon, lat};
        } catch (RestClientException e) {
              throw new ExternalServiceException("Pincode lookup service is unavailable", e);
         }
    }
}