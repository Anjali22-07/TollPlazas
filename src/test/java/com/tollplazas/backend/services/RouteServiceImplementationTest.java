package com.tollplazas.backend.services;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.tollplazas.backend.exceptions.BadRequestException;
import com.tollplazas.backend.exceptions.ExternalServiceException;
import com.tollplazas.backend.services.Implementation.RouteServiceImplementation;

import tools.jackson.databind.JsonNode;

class RouteServiceImplementationTest {

    private static final String URL =
            "https://api.openrouteservice.org/v2/directions/driving-car/geojson";

    private MockRestServiceServer server;
    private RouteServiceImplementation service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        service = new RouteServiceImplementation(builder, "test-api-key");
    }

    @Test
    void validRoute_returnsJsonNode() {
        server.expect(requestTo(URL))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header("Authorization", "test-api-key"))
              .andExpect(jsonPath("$.coordinates[0][0]").value(77.21))
              .andExpect(jsonPath("$.coordinates[0][1]").value(28.63))
              .andExpect(jsonPath("$.coordinates[1][0]").value(72.87))
              .andExpect(jsonPath("$.coordinates[1][1]").value(19.07))
              .andRespond(withSuccess(
                      "{\"type\":\"FeatureCollection\",\"features\":[]}",
                      MediaType.APPLICATION_JSON));

        JsonNode result = service.getRoute(77.21, 28.63, 72.87, 19.07);

        assertEquals("FeatureCollection", result.get("type").asString());
        server.verify();
    }

    @Test
    void clientError_throwsBadRequest() {
        server.expect(requestTo(URL))
              .andRespond(withBadRequest());

        assertThrows(BadRequestException.class,
                () -> service.getRoute(77.21, 28.63, 72.87, 19.07));
    }

    @Test
    void notFound_throwsBadRequest() {
        server.expect(requestTo(URL))
              .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(BadRequestException.class,
                () -> service.getRoute(77.21, 28.63, 72.87, 19.07));
    }

    @Test
    void serverError_throwsExternalServiceException() {
        server.expect(requestTo(URL))
              .andRespond(withServerError());

        assertThrows(ExternalServiceException.class,
                () -> service.getRoute(77.21, 28.63, 72.87, 19.07));
    }

    @Test
    void networkFailure_throwsExternalServiceException() {
        server.expect(requestTo(URL))
              .andRespond(withException(new IOException("Connection refused")));

        assertThrows(ExternalServiceException.class,
                () -> service.getRoute(77.21, 28.63, 72.87, 19.07));
    }
}
