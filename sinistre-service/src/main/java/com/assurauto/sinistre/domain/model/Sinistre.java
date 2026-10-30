package com.assurauto.sinistre.domain.model;

import com.assurauto.sinistre.domain.exception.BusinessRuleException;
import com.assurauto.sinistre.domain.valueobject.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** Agrégat racine du bounded context "Gestion des incidents". */
@Entity
@Table(name = "sinistres")
public class Sinistre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, updatable = false)
    private Long contratId;
    @Column(nullable = false, updatable = false)
    private Long vehiculeId;
    @Column(nullable = false)
    private LocalDate dateSinistre;
    @Column(nullable = false, updatable = false)
    private LocalDate dateDeclaration;
    @Column(nullable = false, length = 2000)
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeIncident typeIncident;
    @Column(nullable = false)
    private String lieu;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutSinistre statut;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal montantEstime;
    @Column(precision = 14, scale = 2)
    private BigDecimal montantIndemnisation;
    @ElementCollection
    @CollectionTable(name = "sinistre_historique", joinColumns = @JoinColumn(name = "sinistre_id"))
    @OrderColumn(name = "ordre")
    private List<SuiviTraitement> historique = new ArrayList<>();

    public Sinistre() {}

    public Long getId() { return id; }
    public Long getContratId() { return contratId; }
    public Long getVehiculeId() { return vehiculeId; }
    public LocalDate getDateSinistre() { return dateSinistre; }
    public LocalDate getDateDeclaration() { return dateDeclaration; }
    public String getDescription() { return description; }
    public TypeIncident getTypeIncident() { return typeIncident; }
    public String getLieu() { return lieu; }
    public StatutSinistre getStatut() { return statut; }
    public BigDecimal getMontantEstime() { return montantEstime; }
    public BigDecimal getMontantIndemnisation() { return montantIndemnisation; }
    public List<SuiviTraitement> getHistorique() { return historique; }


    /** Déclare un nouveau sinistre (statut initial DECLARE). */
    public static Sinistre declarer(Long contratId, Long vehiculeId, LocalDate dateSinistre, String description,
                                    TypeIncident type, String lieu, BigDecimal montantEstime) {
        Sinistre s = new Sinistre();
        s.contratId = contratId;
        s.vehiculeId = vehiculeId;
        s.dateSinistre = dateSinistre;
        s.dateDeclaration = LocalDate.now();
        s.description = description;
        s.typeIncident = type;
        s.lieu = lieu;
        s.montantEstime = montantEstime;
        s.statut = StatutSinistre.DECLARE;
        s.historique.add(new SuiviTraitement(StatutSinistre.DECLARE, LocalDateTime.now(), "Sinistre déclaré"));
        return s;
    }

    /** Les informations descriptives ne sont modifiables que tant que le dossier n'est pas décidé. */
    public void modifier(LocalDate dateSinistre, String description, TypeIncident type, String lieu,
                         BigDecimal montantEstime) {
        if (statut != StatutSinistre.DECLARE && statut != StatutSinistre.EN_EXPERTISE) {
            throw new BusinessRuleException("Un sinistre au statut " + statut + " ne peut plus être modifié");
        }
        this.dateSinistre = dateSinistre;
        this.description = description;
        this.typeIncident = type;
        this.lieu = lieu;
        this.montantEstime = montantEstime;
    }

    /** Fait avancer le traitement selon le workflow autorisé. */
    public void changerStatut(StatutSinistre cible, String commentaire, BigDecimal indemnisation) {
        if (!statut.transitionAutorisee(cible)) {
            throw new BusinessRuleException("Transition interdite : " + statut + " -> " + cible);
        }
        if (cible == StatutSinistre.INDEMNISE) {
            if (indemnisation == null || indemnisation.signum() <= 0) {
                throw new BusinessRuleException("Un montant d'indemnisation strictement positif est requis");
            }
            this.montantIndemnisation = indemnisation;
        }
        this.statut = cible;
        historique.add(new SuiviTraitement(cible, LocalDateTime.now(), commentaire));
    }

    public boolean supprimable() {
        return statut == StatutSinistre.DECLARE;
    }
}
