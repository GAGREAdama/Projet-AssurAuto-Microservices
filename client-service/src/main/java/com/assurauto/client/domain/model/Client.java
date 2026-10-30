package com.assurauto.client.domain.model;

import com.assurauto.client.domain.valueobject.*;
import jakarta.persistence.*;
import java.time.*;
import java.util.*;

/** Agrégat racine du bounded context "Relation Client". */
@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String nom;
    @Column(nullable = false, length = 100)
    private String prenom;
    @Column(nullable = false)
    private LocalDate dateNaissance;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genre genre;
    @Embedded
    private Adresse adresse;
    @Column(nullable = false, unique = true)
    private String email;
    @ElementCollection
    @CollectionTable(name = "client_telephones", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "telephone", length = 30)
    private List<String> telephones = new ArrayList<>();
    @Column(nullable = false, unique = true, length = 50)
    private String numeroPieceIdentite;
    @Embedded
    private PermisConduire permisConduire;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituationFamiliale situationFamiliale;
    @Column(nullable = false)
    private String profession;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategorieClient categorie = CategorieClient.STANDARD;
    @Column(nullable = false)
    private int scoreFidelite = 100;
    @ElementCollection
    @CollectionTable(name = "client_historique_commercial", joinColumns = @JoinColumn(name = "client_id"))
    private List<EvenementCommercial> historiqueCommercial = new ArrayList<>();

    public Client() {}

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public LocalDate getDateNaissance() { return dateNaissance; }
    public Genre getGenre() { return genre; }
    public Adresse getAdresse() { return adresse; }
    public String getEmail() { return email; }
    public List<String> getTelephones() { return telephones; }
    public String getNumeroPieceIdentite() { return numeroPieceIdentite; }
    public PermisConduire getPermisConduire() { return permisConduire; }
    public SituationFamiliale getSituationFamiliale() { return situationFamiliale; }
    public String getProfession() { return profession; }
    public CategorieClient getCategorie() { return categorie; }
    public int getScoreFidelite() { return scoreFidelite; }
    public List<EvenementCommercial> getHistoriqueCommercial() { return historiqueCommercial; }
    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }
    public void setGenre(Genre genre) { this.genre = genre; }
    public void setAdresse(Adresse adresse) { this.adresse = adresse; }
    public void setEmail(String email) { this.email = email; }
    public void setTelephones(List<String> telephones) { this.telephones = telephones; }
    public void setNumeroPieceIdentite(String numeroPieceIdentite) { this.numeroPieceIdentite = numeroPieceIdentite; }
    public void setPermisConduire(PermisConduire permisConduire) { this.permisConduire = permisConduire; }
    public void setSituationFamiliale(SituationFamiliale situationFamiliale) { this.situationFamiliale = situationFamiliale; }
    public void setProfession(String profession) { this.profession = profession; }


    /** Enregistre un événement commercial et met à jour score de fidélité et catégorie (règle du domaine). */
    public void ajouterEvenement(TypeEvenement type, String description) {
        historiqueCommercial.add(new EvenementCommercial(LocalDateTime.now(), type, description, type.getPoints()));
        scoreFidelite = Math.max(0, Math.min(1000, scoreFidelite + type.getPoints()));
        recalculerCategorie();
    }

    /** VIP à partir de 800 points, PREMIUM à partir de 400, sinon STANDARD. */
    public void recalculerCategorie() {
        if (scoreFidelite >= 800) {
            categorie = CategorieClient.VIP;
        } else if (scoreFidelite >= 400) {
            categorie = CategorieClient.PREMIUM;
        } else {
            categorie = CategorieClient.STANDARD;
        }
    }
}
