package models;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "location")
public class Location implements Serializable {

    @Id
    private int id;

    @Column(nullable = false)
    private String name;

    private String type;
    private String dimension;

    @OneToMany(mappedBy = "origin")
    private List<Character> originCharacters;

    @OneToMany(mappedBy = "location")
    private List<Character> locationCharacters;

    // Constructor vacío requerido por Hibernate
    public Location() {}

    public Location(int id, String name, String type, String dimension) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.dimension = dimension;
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
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDimension() {
		return dimension;
	}
	public void setDimension(String dimension) {
		this.dimension = dimension;
	}

	public List<Character> getOriginCharacters() {
	    return originCharacters;
	}

	public void setOriginCharacters(List<Character> originCharacters) {
	    this.originCharacters = originCharacters;
	}

	public List<Character> getLocationCharacters() {
	    return locationCharacters;
	}

	public void setLocationCharacters(List<Character> locationCharacters) {
	    this.locationCharacters = locationCharacters;
	}

	
}
