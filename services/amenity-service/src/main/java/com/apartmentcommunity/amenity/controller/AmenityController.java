package com.apartmentcommunity.amenity.controller;

import com.apartmentcommunity.amenity.model.Amenity;
import com.apartmentcommunity.amenity.service.AmenityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/amenities")
public class AmenityController {
    private final AmenityService amenityService;

    @Autowired
    public AmenityController(AmenityService amenityService) {
        this.amenityService = amenityService;
    }

    @GetMapping
    public ResponseEntity<List<Amenity>> getAllAmenities() {
        List<Amenity> amenities = amenityService.getAllAmenities();
        return ResponseEntity.ok(amenities);
    }

    @PostMapping
    public ResponseEntity<Amenity> createAmenity(@RequestBody Amenity amenity) {
        Amenity created = amenityService.createAmenity(amenity);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
