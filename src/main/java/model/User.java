package model;
import jakarta.persistence.*;

@Entity
@Table(name="Utilisateur")
public class User {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long user_id;

    @Column(nullable = false)
    private String name; 

    @Column(nullable = false)
    private String prenom; 

    @Column(nullable = false, unique = true)
    private String email; 

    @Column(nullable = false)
    private String motDePasse;

    @Column(nullable = false)
    private boolean actif; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, unique = true)
    private String telephone;

    // @OneToMany 
    // @JoinColumn(name = "")

}
