package com.tollplazas.backend.config;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.tollplazas.backend.entity.TollPlaza;
import com.tollplazas.backend.repository.TollPlazaRepository;

import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor 
public class TollPlazacsvimporter implements CommandLineRunner{

    private final TollPlazaRepository tollplazaRepo;

    @Override
    public void run(String... args) throws Exception {
        if(tollplazaRepo.count()>0){
            System.out.println("Toll Plaza data already exists");
            return;
        }

        ClassPathResource resource =
                new ClassPathResource("data/toll_plaza_india.csv"); //this class access files stored inside your application's classpath

        List<TollPlaza> tollPlazas = new ArrayList<>();

        try (Reader reader = new InputStreamReader(
                resource.getInputStream(), StandardCharsets.UTF_8);

             CSVParser parser = CSVParser.parse(
                     reader,
                     CSVFormat.DEFAULT.builder()
                             .setHeader()
                             .setSkipHeaderRecord(true)
                             .get())) {

            for (CSVRecord record : parser) {

                TollPlaza tollPlaza = new TollPlaza();

                tollPlaza.setLongitude(
                        Double.parseDouble(record.get("longitude")));

                tollPlaza.setLatitude(
                        Double.parseDouble(record.get("latitude")));

                tollPlaza.setTollName(record.get("toll_name"));

                tollPlaza.setGeoState(record.get("geo_state"));

                tollPlazas.add(tollPlaza);
            }
        }

        tollplazaRepo.saveAll(tollPlazas);

        System.out.println(
                tollPlazas.size() + " toll plazas imported successfully.");
    }


}
