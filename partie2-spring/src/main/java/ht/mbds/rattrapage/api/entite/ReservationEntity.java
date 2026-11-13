package ht.mbds.rattrapage.api.entite;

import ht.mbds.rattrapage.api.domaine.StatutReservation;
import ht.mbds.rattrapage.api.domaine.TypeTarif;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "reservations")
public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "code_evenement", nullable = false)
    private EvenementEntity evenement;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TypeTarif tarif;

    private Integer pourcentage;

    @Column(name = "nombre_personnes")
    private Integer nombrePersonnes;

    @Column(nullable = false)
    private int places;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private StatutReservation statut;

    protected ReservationEntity() {
        // requis par JPA
    }

    public ReservationEntity(EvenementEntity evenement, String email, String nom, TypeTarif tarif,
                             Integer pourcentage, Integer nombrePersonnes, int places, BigDecimal montant,
                             StatutReservation statut) {
        this.evenement = evenement;
        this.email = email;
        this.nom = nom;
        this.tarif = tarif;
        this.pourcentage = pourcentage;
        this.nombrePersonnes = nombrePersonnes;
        this.places = places;
        this.montant = montant;
        this.statut = statut;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EvenementEntity getEvenement() {
        return evenement;
    }

    public String getEmail() {
        return email;
    }

    public String getNom() {
        return nom;
    }

    public TypeTarif getTarif() {
        return tarif;
    }

    public Integer getPourcentage() {
        return pourcentage;
    }

    public Integer getNombrePersonnes() {
        return nombrePersonnes;
    }

    public int getPlaces() {
        return places;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public StatutReservation getStatut() {
        return statut;
    }

    public void setStatut(StatutReservation statut) {
        this.statut = statut;
    }
}
