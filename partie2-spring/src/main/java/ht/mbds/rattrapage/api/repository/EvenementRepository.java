package ht.mbds.rattrapage.api.repository;

import ht.mbds.rattrapage.api.domaine.Categorie;
import ht.mbds.rattrapage.api.entite.EvenementEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvenementRepository extends JpaRepository<EvenementEntity, String> {

    Page<EvenementEntity> findByCategorie(Categorie categorie, Pageable pageable);
}
