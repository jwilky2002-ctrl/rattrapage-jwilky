package ht.mbds.rattrapage.api.controleur;

import ht.mbds.rattrapage.api.dto.ReservationRequest;
import ht.mbds.rattrapage.api.dto.ReservationResponse;
import ht.mbds.rattrapage.api.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/api/evenements/{code}/reservations")
    public ResponseEntity<ReservationResponse> reserver(@PathVariable String code,
                                                        @Valid @RequestBody ReservationRequest requete) {
        ReservationResponse reservation = reservationService.reserver(code, requete);
        return ResponseEntity.created(URI.create("/api/reservations/" + reservation.id())).body(reservation);
    }

    @GetMapping("/api/reservations/{id}")
    public ReservationResponse trouver(@PathVariable Long id) {
        return reservationService.trouver(id);
    }

    /** Role ADMIN ou AGENT (voir SecurityConfig). */
    @DeleteMapping("/api/reservations/{id}")
    public ReservationResponse annuler(@PathVariable Long id) {
        return reservationService.annuler(id);
    }
}
