package com.tollplazas.backend.dto;

import java.util.List;

public record TollResponse(RouteDto route, List<TollPlazaDto> tollPlazas) {

}
