package ht.mbds.rattrapage.api.entite;

import ht.mbds.rattrapage.api.domaine.Categorie;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "evenements")
public class EvenementEntity {

    @Id
    @Column(length = 7)
    private String code;

    @Column(nullable = false, length = 100)
    private String titre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Categorie categorie;

    @Column(name = "date_evenement", nullable = false)
    private LocalDate dateEvenement;

    @Column(nullable = false)
    private int capacite;

    @Column(name = "prix_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal prixBase;

    protected EvenementEntity() {
        // requis par JPA
    }

    public EvenementEntity(String code, String titre, Categorie categorie, LocalDate dateEvenement,
                           int capacite, BigDecimal prixBase) {
        this.code = code;
        this.titre = titre;
        this.categorie = categorie;
        this.dateEvenement = dateEvenement;
        this.capacite = capacite;
        this.prixBase = prixBase;
    }

    public String getCode() {
        return code;
    }

    public String getTitre() {
        return titre;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public LocalDate getDateEvenement() {
        return dateEvenement;
    }

    public int getCapacite() {
        return capacite;
    }

    public BigDecimal getPrixBase() {
        return prixBase;
    }
}
