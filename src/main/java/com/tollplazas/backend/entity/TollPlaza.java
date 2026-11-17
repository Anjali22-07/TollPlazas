package com.tollplazas.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table (name = "toll_plaza")
@Getter 
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
public class TollPlaza {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    private Double longitude;

    private Double latitude;

    @Column(name = "toll_name")
    private String tollName;

    @Column(name = "geo_state")
    private String geoState;
}
