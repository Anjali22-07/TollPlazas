package com.tollplazas.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TollRequest(
    @NotBlank(message = "Invalid source or destination pincode")
    @Pattern(regexp = "[1-9][0-9]{5}", message = "Invalid source or destination pincode") 
    String sourcePincode, 
    @NotBlank(message = "Invalid source or destination pincode")
    @Pattern (regexp = "[1-9][0-9]{5}", message = "Invalid source or destination pincode")
    String destinationPincode) {

}
