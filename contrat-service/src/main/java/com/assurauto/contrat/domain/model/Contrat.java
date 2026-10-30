package com.assurauto.contrat.domain.model;

import com.assurauto.contrat.domain.exception.BusinessRuleException;
import com.assurauto.contrat.domain.valueobject.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** Agrégat racine du bounded context "Gestion contractuelle". */
@Entity
@Table(name = "contrats")
public class Contrat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, updatable = false)
    private Long clientId;
    @Column(nullable = false, unique = true, updatable = false, length = 30)
    private String numeroPolice = "POL-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeContrat typeContrat;
    @Column(nullable = false)
    private LocalDate dateDebut;
    @Column(nullable = false)
    private LocalDate dateFin;
    private boolean renouvellementAutomatique;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutContrat statut = StatutContrat.EN_COURS;
    @Column(precision = 14, scale = 2)
    private BigDecimal primeAnnuelle;
    @ElementCollection
    @CollectionTable(name = "contrat_garanties", joinColumns = @JoinColumn(name = "contrat_id"))
    private List<Garantie> garanties = new ArrayList<>();
    @ElementCollection
    @CollectionTable(name = "contrat_franchises", joinColumns = @JoinColumn(name = "contrat_id"))
    private List<Franchise> franchises = new ArrayList<>();
    @ElementCollection
    @CollectionTable(name = "contrat_options", joinColumns = @JoinColumn(name = "contrat_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "option_supplementaire")
    private Set<OptionSupplementaire> options = new HashSet<>();
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    public Contrat() {}

    public Long getId() { return id; }
    public Long getClientId() { return clientId; }
    public String getNumeroPolice() { return numeroPolice; }
    public TypeContrat getTypeContrat() { return typeContrat; }
    public LocalDate getDateDebut() { return dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public boolean isRenouvellementAutomatique() { return renouvellementAutomatique; }
    public StatutContrat getStatut() { return statut; }
    public BigDecimal getPrimeAnnuelle() { return primeAnnuelle; }
    public List<Garantie> getGaranties() { return garanties; }
    public List<Franchise> getFranchises() { return franchises; }
    public Set<OptionSupplementaire> getOptions() { return options; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setClientId(Long clientId) { this.clientId = clientId; }


    /** Définit la couverture en respectant les invariants de l'agrégat. */
    public void definirCouverture(List<Garantie> nouvellesGaranties, List<Franchise> nouvellesFranchises,
                                  Set<OptionSupplementaire> nouvellesOptions) {
        if (nouvellesGaranties == null || nouvellesGaranties.isEmpty()) {
            throw new BusinessRuleException("Un contrat doit comporter au moins une garantie");
        }
        Set<TypeGarantie> types = new HashSet<>();
        for (Garantie g : nouvellesGaranties) {
            if (g.getPlafond().signum() <= 0) {
                throw new BusinessRuleException("Le plafond de la garantie " + g.getType() + " doit être strictement positif");
            }
            if (!types.add(g.getType())) {
                throw new BusinessRuleException("Garantie en double : " + g.getType());
            }
        }
        if (!types.contains(TypeGarantie.RESPONSABILITE_CIVILE)) {
            throw new BusinessRuleException("La garantie RESPONSABILITE_CIVILE est obligatoire (assurance auto)");
        }
        if (typeContrat == TypeContrat.TOUS_RISQUES && !types.contains(TypeGarantie.DOMMAGES_COLLISION)) {
            throw new BusinessRuleException("Un contrat TOUS_RISQUES doit inclure la garantie DOMMAGES_COLLISION");
        }
        for (Franchise f : nouvellesFranchises) {
            if (!types.contains(f.getGarantie())) {
                throw new BusinessRuleException("Franchise définie pour une garantie absente du contrat : " + f.getGarantie());
            }
            if (f.getMontant().signum() < 0) {
                throw new BusinessRuleException("Une franchise ne peut pas être négative");
            }
        }
        garanties.clear();
        garanties.addAll(nouvellesGaranties);
        franchises.clear();
        franchises.addAll(nouvellesFranchises);
        options.clear();
        if (nouvellesOptions != null) {
            options.addAll(nouvellesOptions);
        }
    }

    /** Modifie les informations générales (impossible sur un contrat résilié). */
    public void modifierConditions(TypeContrat type, LocalDate debut, LocalDate fin, boolean renouvellementAuto,
                                   BigDecimal prime) {
        if (statut == StatutContrat.RESILIE) {
            throw new BusinessRuleException("Un contrat résilié ne peut plus être modifié");
        }
        if (!fin.isAfter(debut)) {
            throw new BusinessRuleException("La date de fin doit être postérieure à la date de début");
        }
        this.typeContrat = type;
        this.dateDebut = debut;
        this.dateFin = fin;
        this.renouvellementAutomatique = renouvellementAuto;
        this.primeAnnuelle = prime;
    }

    public void suspendre() {
        if (statut != StatutContrat.EN_COURS) {
            throw new BusinessRuleException("Seul un contrat EN_COURS peut être suspendu (statut actuel : " + statut + ")");
        }
        statut = StatutContrat.SUSPENDU;
    }

    public void reactiver() {
        if (statut != StatutContrat.SUSPENDU) {
            throw new BusinessRuleException("Seul un contrat SUSPENDU peut être réactivé (statut actuel : " + statut + ")");
        }
        if (dateFin.isBefore(LocalDate.now())) {
            throw new BusinessRuleException("Contrat échu : utiliser le renouvellement");
        }
        statut = StatutContrat.EN_COURS;
    }

    public void resilier() {
        if (statut == StatutContrat.RESILIE) {
            throw new BusinessRuleException("Le contrat est déjà résilié");
        }
        statut = StatutContrat.RESILIE;
    }

    public void renouveler(LocalDate nouvelleDateFin) {
        if (statut == StatutContrat.RESILIE) {
            throw new BusinessRuleException("Un contrat résilié ne peut pas être renouvelé");
        }
        if (!nouvelleDateFin.isAfter(dateFin)) {
            throw new BusinessRuleException("La nouvelle date de fin doit être postérieure à la date de fin actuelle");
        }
        dateFin = nouvelleDateFin;
        statut = StatutContrat.EN_COURS;
    }

    public boolean couvre(LocalDate date) {
        return statut == StatutContrat.EN_COURS && !date.isBefore(dateDebut) && !date.isAfter(dateFin);
    }
}
