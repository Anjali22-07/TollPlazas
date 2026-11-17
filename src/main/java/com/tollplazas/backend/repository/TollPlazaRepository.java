package com.tollplazas.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tollplazas.backend.entity.TollPlaza;

public interface TollPlazaRepository extends JpaRepository<TollPlaza, Long> {

}
