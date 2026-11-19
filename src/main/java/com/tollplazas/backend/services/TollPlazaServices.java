package com.tollplazas.backend.services;

import com.tollplazas.backend.dto.TollRequest;
import com.tollplazas.backend.dto.TollResponse;

public interface TollPlazaServices {

    public TollResponse findBetween(TollRequest tollRequest);

}
