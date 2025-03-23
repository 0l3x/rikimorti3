package models;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "character")
public class Character implements Serializable {

    @Id
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String species;

    private String type;

    @Column(nullable = false)
    private String gender;

    @ManyToOne
    @JoinColumn(name = "id_origin")
    private Location origin;

    @ManyToOne
    @JoinColumn(name = "id_location")
    private Location location;
    
    @ManyToMany
    @JoinTable(
        name = "character_in_episode",
        joinColumns = @JoinColumn(name = "id_character"),
        inverseJoinColumns = @JoinColumn(name = "id_episode")
    )
    private List<Episode> episodes;

    // Constructor vacío requerido por Hibernate
    public Character() {}

    // Constructor con todos los atributos
    public Character(int id, String name, String status, String species, String type, String gender, Location origin, Location location) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.species = species;
        this.type = type;
        this.gender = gender;
        this.origin = origin;
        this.location = location;
    }
    
    public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getSpecies() {
		return species;
	}
	public void setSpecies(String species) {
		this.species = species;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	
	public Location getOrigin() {
		return origin;
	}
	
	public void setOrigin(Location origin) {
		this.origin = origin;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}

	public List<Episode> getEpisodes() {
		return episodes;
	}
	
	public void setEpisodes(List<Episode> episodes) {
	    this.episodes = episodes;
	}

	
}
