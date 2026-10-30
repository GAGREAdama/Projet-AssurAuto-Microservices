package com.assurauto.vehicule.domain.model;

import com.assurauto.vehicule.domain.exception.BusinessRuleException;
import com.assurauto.vehicule.domain.valueobject.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.*;

/** Agrégat racine du bounded context "Biens assurés". */
@Entity
@Table(name = "vehicules")
public class Vehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long contratId;
    @Column(nullable = false, unique = true, length = 20)
    private String immatriculation;
    @Column(nullable = false, length = 60)
    private String marque;
    @Column(nullable = false, length = 60)
    private String modele;
    @Column(nullable = false)
    private int anneeFabrication;
    @Column(nullable = false, unique = true, length = 17)
    private String vin;
    @Column(nullable = false)
    private LocalDate datePremiereMiseEnCirculation;
    @Column(nullable = false)
    private int kilometrageDeclare;
    @Column(nullable = false)
    private String zoneStationnement;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeGarage typeGarage;
    @Enumerated(EnumType.STRING)
    @Column(name = "usage_principal", nullable = false)
    private UsageVehicule usagePrincipal;
    @Column(nullable = false)
    private int kilometrageAnnuelEstime;
    @Embedded
    private EquipementsSecurite equipements = new EquipementsSecurite(false, false);
    @ElementCollection
    @CollectionTable(name = "vehicule_autres_equipements", joinColumns = @JoinColumn(name = "vehicule_id"))
    @Column(name = "equipement")
    private Set<String> autresEquipements = new HashSet<>();

    public Vehicule() {}

    public Long getId() { return id; }
    public Long getContratId() { return contratId; }
    public String getImmatriculation() { return immatriculation; }
    public String getMarque() { return marque; }
    public String getModele() { return modele; }
    public int getAnneeFabrication() { return anneeFabrication; }
    public String getVin() { return vin; }
    public LocalDate getDatePremiereMiseEnCirculation() { return datePremiereMiseEnCirculation; }
    public int getKilometrageDeclare() { return kilometrageDeclare; }
    public String getZoneStationnement() { return zoneStationnement; }
    public TypeGarage getTypeGarage() { return typeGarage; }
    public UsageVehicule getUsagePrincipal() { return usagePrincipal; }
    public int getKilometrageAnnuelEstime() { return kilometrageAnnuelEstime; }
    public EquipementsSecurite getEquipements() { return equipements; }
    public Set<String> getAutresEquipements() { return autresEquipements; }
    public void setContratId(Long contratId) { this.contratId = contratId; }
    public void setImmatriculation(String immatriculation) { this.immatriculation = immatriculation; }
    public void setMarque(String marque) { this.marque = marque; }
    public void setModele(String modele) { this.modele = modele; }
    public void setAnneeFabrication(int anneeFabrication) { this.anneeFabrication = anneeFabrication; }
    public void setVin(String vin) { this.vin = vin; }
    public void setDatePremiereMiseEnCirculation(LocalDate datePremiereMiseEnCirculation) { this.datePremiereMiseEnCirculation = datePremiereMiseEnCirculation; }
    public void setZoneStationnement(String zoneStationnement) { this.zoneStationnement = zoneStationnement; }
    public void setTypeGarage(TypeGarage typeGarage) { this.typeGarage = typeGarage; }
    public void setUsagePrincipal(UsageVehicule usagePrincipal) { this.usagePrincipal = usagePrincipal; }
    public void setKilometrageAnnuelEstime(int kilometrageAnnuelEstime) { this.kilometrageAnnuelEstime = kilometrageAnnuelEstime; }
    public void setEquipements(EquipementsSecurite equipements) { this.equipements = equipements; }
    public void setAutresEquipements(Set<String> autresEquipements) { this.autresEquipements = autresEquipements; }


    /** Score de risque 0..100 (plus il est élevé, plus le risque est important) - règle métier de tarification. */
    public int scoreRisque() {
        int score = 50;
        score += switch (typeGarage) {
            case RUE -> 10;
            case PUBLIC -> 5;
            case PRIVE -> -5;
        };
        score += switch (usagePrincipal) {
            case PROFESSIONNEL -> 10;
            case MIXTE -> 5;
            case PERSONNEL -> 0;
        };
        if (kilometrageAnnuelEstime > 20000) {
            score += 10;
        } else if (kilometrageAnnuelEstime < 8000) {
            score -= 5;
        }
        if (equipements.isAlarme()) {
            score -= 5;
        }
        if (equipements.isGeolocalisation()) {
            score -= 10;
        }
        return Math.max(0, Math.min(100, score));
    }

    /** Le kilométrage déclaré ne peut que croître. */
    public void mettreAJourKilometrage(int nouveau) {
        if (nouveau < kilometrageDeclare) {
            throw new BusinessRuleException("Le kilométrage ne peut pas diminuer (actuel : " + kilometrageDeclare + ")");
        }
        this.kilometrageDeclare = nouveau;
    }

    /** Invariants d'identité et de dates du véhicule. */
    public void verifierCoherence() {
        int anneeCourante = LocalDate.now().getYear();
        if (anneeFabrication > anneeCourante) {
            throw new BusinessRuleException("L'année de fabrication ne peut pas être dans le futur");
        }
        if (datePremiereMiseEnCirculation.getYear() < anneeFabrication) {
            throw new BusinessRuleException("La première mise en circulation ne peut pas précéder l'année de fabrication");
        }
        if (datePremiereMiseEnCirculation.isAfter(LocalDate.now())) {
            throw new BusinessRuleException("La date de première mise en circulation est dans le futur");
        }
    }
}
