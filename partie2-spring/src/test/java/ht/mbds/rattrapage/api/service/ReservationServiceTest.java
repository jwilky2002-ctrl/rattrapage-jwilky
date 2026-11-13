package ht.mbds.rattrapage.api.service;

import ht.mbds.rattrapage.api.domaine.Categorie;
import ht.mbds.rattrapage.api.domaine.StatutReservation;
import ht.mbds.rattrapage.api.domaine.TypeTarif;
import ht.mbds.rattrapage.api.dto.ReservationRequest;
import ht.mbds.rattrapage.api.dto.ReservationResponse;
import ht.mbds.rattrapage.api.entite.EvenementEntity;
import ht.mbds.rattrapage.api.entite.ReservationEntity;
import ht.mbds.rattrapage.api.exception.ConflitException;
import ht.mbds.rattrapage.api.exception.RequeteInvalideException;
import ht.mbds.rattrapage.api.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private EvenementService evenementService;

    @InjectMocks
    private ReservationService reservationService;

    private EvenementEntity evenement;

    @BeforeEach
    void initialiser() {
        evenement = new EvenementEntity("CON-001", "Kompa Live", Categorie.CONCERT,
                LocalDate.of(2030, 6, 15), 500, new BigDecimal("25.00"));
    }

    @Test
    void reservationReduiteConfirmee() {
        when(evenementService.chargerEvenement("CON-001")).thenReturn(evenement);
        when(evenementService.placesRestantes(evenement)).thenReturn(500);
        when(reservationRepository.compterReservations("CON-001", "jean@mail.ht", StatutReservation.ANNULEE))
                .thenReturn(0L);
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> {
            ReservationEntity r = invocation.getArgument(0);
            r.setId(12L);
            return r;
        });

        ReservationResponse reponse = reservationService.reserver("CON-001",
                new ReservationRequest("Jean@Mail.HT", "Jean", TypeTarif.REDUIT, 30, null));

        assertThat(reponse.id()).isEqualTo(12L);
        assertThat(reponse.email()).isEqualTo("jean@mail.ht");
        assertThat(reponse.montant()).isEqualByComparingTo("17.50");
        assertThat(reponse.places()).isEqualTo(1);
        assertThat(reponse.statut()).isEqualTo(StatutReservation.CONFIRMEE);
    }

    @Test
    void groupeSansPlacesSuffisantesEnAttente() {
        when(evenementService.chargerEvenement("CON-001")).thenReturn(evenement);
        when(evenementService.placesRestantes(evenement)).thenReturn(4);
        when(reservationRepository.compterReservations(eq("CON-001"), eq("g@mail.ht"), eq(StatutReservation.ANNULEE)))
                .thenReturn(0L);
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReservationResponse reponse = reservationService.reserver("CON-001",
                new ReservationRequest("g@mail.ht", "Groupe", TypeTarif.GROUPE, null, 5));

        assertThat(reponse.statut()).isEqualTo(StatutReservation.EN_ATTENTE);
        assertThat(reponse.places()).isEqualTo(5);
        assertThat(reponse.montant()).isEqualByComparingTo("106.25");
    }

    @Test
    void reservationDupliqueeRefusee() {
        when(evenementService.chargerEvenement("CON-001")).thenReturn(evenement);
        when(reservationRepository.compterReservations("CON-001", "jean@mail.ht", StatutReservation.ANNULEE))
                .thenReturn(1L);

        assertThatThrownBy(() -> reservationService.reserver("CON-001",
                new ReservationRequest("JEAN@mail.ht", "Jean", TypeTarif.PLEIN, null, null)))
                .isInstanceOf(ConflitException.class);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void tarifReduitSansPourcentageInvalide() {
        assertThatThrownBy(() -> reservationService.reserver("CON-001",
                new ReservationRequest("jean@mail.ht", "Jean", TypeTarif.REDUIT, null, null)))
                .isInstanceOf(RequeteInvalideException.class)
                .satisfies(e -> assertThat(((RequeteInvalideException) e).getErreurs()).containsKey("pourcentage"));
    }
}
