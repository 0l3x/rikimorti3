package models;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "episode")
public class Episode implements Serializable {

    @Id
    private int id;

    @Column(nullable = false)
    private String name;

    @Temporal(TemporalType.DATE)
    @Column(name = "air_date", nullable = false)
    private Date airDate;

    @Column(nullable = false)
    private String episode;

    @ManyToMany
    (mappedBy = "episodes")
    private List<Character> characters;


    // Constructor vacío requerido por Hibernate
    public Episode() {}

    public Episode(int id, String name, Date airDate, String episode) {
        this.id = id;
        this.name = name;
        this.airDate = airDate;
        this.episode = episode;
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
	
	public Date getAirDate() { 
		return airDate; 
	}
	
	public void setAirDate(Date airDate) { 
		this.airDate = airDate; 
	}

	public String getEpisode() {
		return episode;
	}
	public void setEpisode(String episode) {
		this.episode = episode;
	}
}
