package com.tollplazas.backend.services.Implementation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.tollplazas.backend.dto.RouteDto;
import com.tollplazas.backend.dto.TollPlazaDto;
import com.tollplazas.backend.dto.TollRequest;
import com.tollplazas.backend.dto.TollResponse;
import com.tollplazas.backend.entity.TollPlaza;
import com.tollplazas.backend.exceptions.BadRequestException;
import com.tollplazas.backend.repository.TollPlazaRepository;
import com.tollplazas.backend.services.PincodeServices;
import com.tollplazas.backend.services.RouteServices;
import com.tollplazas.backend.services.TollPlazaServices;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;

@Service 
@RequiredArgsConstructor 
public class TollPlazaServiceImplementation implements TollPlazaServices {

     private static final double MAX_DISTANCE_FROM_ROUTE_KM = 0.5;   // 500 metres


    private final PincodeServices pincodeServices;
    private final RouteServices routeServices;
    private final TollPlazaRepository tollplazaRepo;

    @Override
    public TollResponse findBetween(TollRequest tollRequest) {

        String source= tollRequest.sourcePincode();
        String destination= tollRequest.destinationPincode();

        // 0. validate input
        if (!isValid(source) || !isValid(destination)) {
            throw new BadRequestException("Invalid source or destination pincode");
        }
        if (source.equals(destination)) {
        throw new BadRequestException("Source and destination pincodes cannot be the same");
        }

        //first convert the pincode to coordinates
           double[] src = pincodeServices.getCoordinates(source);
        double[] dst = pincodeServices.getCoordinates(destination);


        //find the route from coordinates

        JsonNode feature= routeServices.getRoute(src[0],src[1],dst[0],dst[1])
                           .path("features").get(0);    
        double distanceInKm = feature.path("properties").path("summary").path("distance").asDouble() / 1000;
        JsonNode routePoints = feature.path("geometry").path("coordinates");   // each point is [lon, lat]


        //find TollPlaza close to the route + dist from the source 
        // running distance along the route: cumulativeKm[i] = km driven to reach point i
            int n = routePoints.size();
            double[] cumulativeKm = new double[n];
            for (int i = 1; i < n; i++) {
                 double prevLat = routePoints.get(i - 1).get(1).asDouble();
                double prevLon = routePoints.get(i - 1).get(0).asDouble();
                double lat = routePoints.get(i).get(1).asDouble();
                double lon = routePoints.get(i).get(0).asDouble();
                cumulativeKm[i] = cumulativeKm[i - 1] + distanceKm(prevLat, prevLon, lat, lon);
           }

        // scale so the total matches the distance ORS reported (removes small rounding differences)

        double total = cumulativeKm[n - 1];
        double scale = total > 0 ? distanceInKm / total : 1;
 
         Set<String> seen= new HashSet<>();
        List<TollPlazaDto> tollPlazas = new ArrayList<>();
        for (TollPlaza plaza : tollplazaRepo.findAll()) {

         // find the route point closest to this plaza
            int nearestIndex = -1;
            double nearestKm = Double.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                double d = distanceKm(routePoints.get(i).get(1).asDouble(), routePoints.get(i).get(0).asDouble(),
                                    plaza.getLatitude(), plaza.getLongitude());
                if (d < nearestKm) {
                    nearestKm = d;
                    nearestIndex = i;
                }
            }

         
            
            String key = plaza.getTollName() + "|" + plaza.getLatitude() + "|" + plaza.getLongitude();
 
     
    // keep the plaza only if it is within 500 m of the route
            if (nearestKm <= MAX_DISTANCE_FROM_ROUTE_KM && seen.add(key)) {
                double alongRouteKm = cumulativeKm[nearestIndex] * scale;
                tollPlazas.add(new TollPlazaDto(
                    plaza.getTollName(), plaza.getLatitude(), plaza.getLongitude(), round(alongRouteKm)));
            }
        }
    tollPlazas.sort(Comparator.comparingDouble(TollPlazaDto::distanceFromSource));

    // return the Json response
        return new TollResponse(new RouteDto(source, destination, round(distanceInKm)), tollPlazas);

    }

      private boolean isValid(String pincode) {
        return pincode != null && pincode.matches("[1-9][0-9]{5}");
    }
     
    // straight-line distance between two points (haversine formula)
    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusKm = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(dLon / 2), 2);
        return 2 * earthRadiusKm * Math.asin(Math.sqrt(a));
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
