package com.tollplazas.backend.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.tollplazas.backend.dto.*;
import com.tollplazas.backend.exceptions.BadRequestException;
import com.tollplazas.backend.services.TollPlazaServices;


@WebMvcTest(TollPlazaController.class)
class TollControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean TollPlazaServices tollPlazaServices;

    private static final String URL = "/api/v1/toll-plaza";

    @Test
    void validRequest_returns200WithRouteAndPlazas() throws Exception {
        when(tollPlazaServices.findBetween(any())).thenReturn(new TollResponse(
                new RouteDto("110001", "560001", 2057.1),
                List.of(new TollPlazaDto("Kiranj Toll Plaza", 28.18, 77.17, 66.12))));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourcePincode\":\"110001\",\"destinationPincode\":\"560001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route.distanceInKm").value(2057.1))
                .andExpect(jsonPath("$.tollPlazas[0].name").value("Kiranj Toll Plaza"))
                .andExpect(jsonPath("$.tollPlazas[0].distanceFromSource").value(66.12));
    }

    @Test
    void samePincode_returns400WithMessage() throws Exception {
        when(tollPlazaServices.findBetween(any()))
                .thenThrow(new BadRequestException("Source and destination pincodes cannot be the same"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourcePincode\":\"110001\",\"destinationPincode\":\"110001\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Source and destination pincodes cannot be the same"));
    }

    @Test
    void malformedPincode_returns400() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourcePincode\":\"abc\",\"destinationPincode\":\"560001\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid source or destination pincode"));
    }

    @Test
    void missingField_returns400() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourcePincode\":\"110001\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid source or destination pincode"));
    }

    @Test
    void emptyBody_returns400() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Request body is missing or malformed"));
    }
}