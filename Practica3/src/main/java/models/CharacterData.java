package models;

import java.util.List;

public class CharacterData {
    private int id;
    private String name;
    private String status;
    private String species;
    private String type;
    private String gender;
    private int id_origin;
    private int id_location;
    private List<Integer> episodeIds;
    public CharacterData(int id, String name, String status, String species, String type, String gender, int id_origin, int id_location, List<Integer> episodeIds) {
        this.setId(id);
        this.setName(name);
        this.setStatus(status);
        this.setSpecies(species);
        this.setType(type);
        this.setGender(gender);
        this.setId_origin(id_origin);
        this.setId_location(id_location);
        this.setEpisodeIds(episodeIds);
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
	public int getId_origin() {
		return id_origin;
	}
	public void setId_origin(int id_origin) {
		this.id_origin = id_origin;
	}
	public int getId_location() {
		return id_location;
	}
	public void setId_location(int id_location) {
		this.id_location = id_location;
	}
	public List<Integer> getEpisodeIds() {
		return episodeIds;
	}
	public void setEpisodeIds(List<Integer> episodeIds) {
		this.episodeIds = episodeIds;
	}
}
