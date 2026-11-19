package com.tollplazas.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;
import com.tollplazas.backend.exceptions.BadRequestException;
import com.tollplazas.backend.exceptions.ExternalServiceException;
import com.tollplazas.backend.services.Implementation.PincodeServicesImplementation;


class PincodeServicesImplementationTest {

    private MockRestServiceServer server;
    private PincodeServicesImplementation service;

    @BeforeEach 
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        service = new PincodeServicesImplementation(builder);
    }

    @Test 
    void validPincode_returnsLonLat() {
        server.expect(requestTo(containsString("postalcode=110001")))
              .andRespond(withSuccess("[{\"lat\":\"28.63\",\"lon\":\"77.21\"}]", MediaType.APPLICATION_JSON));

        double[] result = service.getCoordinates("110001");

        assertEquals(77.21, result[0], 0.001);   // longitude first
        assertEquals(28.63, result[1], 0.001);
    }

    @Test
    void unknownPincode_throwsBadRequest() {
        server.expect(requestTo(containsString("postalcode=999999")))
              .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertThrows(BadRequestException.class, () -> service.getCoordinates("999999"));
    }

    @Test
    void serverError_throwsExternalServiceException() {
        server.expect(requestTo(containsString("postalcode=110001")))
              .andRespond(withServerError());

        assertThrows(ExternalServiceException.class, () -> service.getCoordinates("110001"));
    }
}
