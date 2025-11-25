package com.jcatena.travelbackend.trip;

import com.jcatena.travelbackend.trip.dto.TripRequest;
import com.jcatena.travelbackend.trip.dto.TripResponse;
import com.jcatena.travelbackend.trip.dto.TripSummaryResponse;
import com.jcatena.travelbackend.trip.dto.TripSettlementResponse;
import com.jcatena.travelbackend.trip.dto.TripUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    public TripResponse createTrip(@Valid @RequestBody TripRequest request) {
        return tripService.createTrip(request);
    }

    @GetMapping
    public List<TripResponse> getAllTrips() {
        return tripService.getAllTrips();
    }

    @GetMapping("/{id}")
    public TripResponse getTrip(@PathVariable Long id) {
        return tripService.getTripById(id);
    }

    @GetMapping("/{id}/summary")
    public TripSummaryResponse getTripSummary(@PathVariable Long id) {
        return tripService.getSummary(id);
    }

    @GetMapping("/{id}/settlement")
    public TripSettlementResponse getTripSettlement(@PathVariable Long id) {
        return tripService.getSettlement(id);
    }

    @DeleteMapping("/{id}")
    public void deleteTrip(@PathVariable Long id) {
        tripService.deleteTrip(id);
    }

    @PutMapping("/{id}")
    public TripResponse updateTrip(@PathVariable Long id,
                                   @RequestBody TripUpdateRequest request) {
        return tripService.updateTrip(id, request);
    }

    @GetMapping("/user/{userId}")
    public List<TripResponse> getTripsByUser(@PathVariable Long userId) {
        return tripService.getTripsByUser(userId);
    }



}
