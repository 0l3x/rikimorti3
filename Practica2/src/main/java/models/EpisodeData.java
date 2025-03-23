package models;

import java.sql.Date;

public class EpisodeData {
    private int id;
    private String name;
    private Date air_date;
    private String episode;
    public EpisodeData(int id, String name, Date air_date, String episode) {
        this.setId(id);
        this.setName(name);
        this.setAir_date(air_date);
        this.setEpisode(episode);
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
	public Date getAir_date() {
		return air_date;
	}
	public void setAir_date(Date air_date) {
		this.air_date = air_date;
	}
	public String getEpisode() {
		return episode;
	}
	public void setEpisode(String episode) {
		this.episode = episode;
	}
}
