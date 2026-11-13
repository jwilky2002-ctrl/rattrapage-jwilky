package ht.mbds.rattrapage.api.repository;

import ht.mbds.rattrapage.api.domaine.StatutReservation;
import ht.mbds.rattrapage.api.entite.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    /** Nombre de reservations dans un statut different de celui donne, pour cet email (casse ignoree). */
    @Query("""
            select count(r) from ReservationEntity r
            where r.evenement.code = :code
              and lower(r.email) = lower(:email)
              and r.statut <> :statutExclu
            """)
    long compterReservations(@Param("code") String code, @Param("email") String email,
                             @Param("statutExclu") StatutReservation statutExclu);

    /** Somme des places pour un evenement et un statut donnes. */
    @Query("""
            select coalesce(sum(r.places), 0) from ReservationEntity r
            where r.evenement.code = :code and r.statut = :statut
            """)
    long sommePlaces(@Param("code") String code, @Param("statut") StatutReservation statut);

    /** Liste d'attente, par identifiant croissant (ordre d'arrivee). */
    List<ReservationEntity> findByEvenementCodeAndStatutOrderByIdAsc(String code, StatutReservation statut);
}
