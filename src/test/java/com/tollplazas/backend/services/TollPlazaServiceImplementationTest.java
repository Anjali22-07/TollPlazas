package com.tollplazas.backend.services;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.tollplazas.backend.dto.TollPlazaDto;
import com.tollplazas.backend.dto.TollRequest;
import com.tollplazas.backend.dto.TollResponse;
import com.tollplazas.backend.entity.TollPlaza;
import com.tollplazas.backend.exceptions.BadRequestException;
import com.tollplazas.backend.repository.TollPlazaRepository;
import com.tollplazas.backend.services.Implementation.TollPlazaServiceImplementation;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class TollPlazaServiceImplementationTest {

    @Mock PincodeServices pincodeServices;
    @Mock RouteServices routeServices;
    @Mock TollPlazaRepository tollPlazaRepository;
    @InjectMocks TollPlazaServiceImplementation service;

    // a straight north-to-south route along longitude 77.0, from lat 28 to lat 26 (~222 km)
    private static final String ROUTE_JSON = """
        {"features":[{
           "properties":{"summary":{"distance":222000.0}},
           "geometry":{"coordinates":[[77.0,28.0],[77.0,27.5],[77.0,27.0],[77.0,26.5],[77.0,26.0]]}
        }]}""";

    private JsonNode route() {
        return JsonMapper.builder().build().readTree(ROUTE_JSON);
    }

    private TollPlaza plaza(String name, double lat, double lon) {
        TollPlaza p = mock(TollPlaza.class);
        lenient().when(p.getTollName()).thenReturn(name);
        lenient().when(p.getLatitude()).thenReturn(lat);
        lenient().when(p.getLongitude()).thenReturn(lon);
        return p;
    }

    private void stubRoute() {
        when(pincodeServices.getCoordinates("110001")).thenReturn(new double[]{77.0, 28.0});
        when(pincodeServices.getCoordinates("560001")).thenReturn(new double[]{77.0, 26.0});
        when(routeServices.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(route());
    }

    private void stubPlazas(TollPlaza... plazas) {
    when(tollPlazaRepository.findAll()).thenReturn(List.of(plazas));
   }

    @Test
    void invalidPincode_throwsBadRequest() {
        BadRequestException e = assertThrows(BadRequestException.class,
                () -> service.findBetween(new TollRequest("abc", "560001")));
        assertEquals("Invalid source or destination pincode", e.getMessage());
        verifyNoInteractions(pincodeServices, routeServices);
    }

    @Test
    void samePincode_throwsBadRequest() {
        BadRequestException e = assertThrows(BadRequestException.class,
                () -> service.findBetween(new TollRequest("110001", "110001")));
        assertEquals("Source and destination pincodes cannot be the same", e.getMessage());
    }

    @Test
    void noPlazasOnRoute_returnsEmptyList() {
        stubRoute();
        stubPlazas(plaza("Far Away", 20.0, 80.0));
        TollResponse response = service.findBetween(new TollRequest("110001", "560001"));

        assertTrue(response.tollPlazas().isEmpty());
        assertEquals(222.0, response.route().distanceInKm(), 0.01);
    }

    @Test
    void plazaOnRoute_isReturned_andOffRoutePlazaIsExcluded() {
        stubRoute();
         stubPlazas(plaza("On Route", 27.0, 77.0), plaza("Off Route", 27.0, 78.5));


        List<TollPlazaDto> result = service.findBetween(new TollRequest("110001", "560001")).tollPlazas();

        assertEquals(1, result.size());
        assertEquals("On Route", result.get(0).name());
        assertEquals(111.0, result.get(0).distanceFromSource(), 2.0);   // ~half of the 222 km route
    }

    @Test
    void plazas_areSortedByDistanceAlongRoute() {
        stubRoute();
       stubPlazas(plaza("Second", 26.5, 77.0), plaza("First", 27.5, 77.0));
        List<TollPlazaDto> result = service.findBetween(new TollRequest("110001", "560001")).tollPlazas();

        assertEquals(List.of("First", "Second"), result.stream().map(TollPlazaDto::name).toList());
    }

    @Test
    void duplicatePlazaRows_areReturnedOnce() {
        stubRoute();
        stubPlazas(plaza("Same Plaza", 27.0, 77.0), plaza("Same Plaza", 27.0, 77.0));
        assertEquals(1, service.findBetween(new TollRequest("110001", "560001")).tollPlazas().size());
    }
}
