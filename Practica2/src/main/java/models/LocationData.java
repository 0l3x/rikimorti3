package models;

public class LocationData {
    private int id;
    private String name;
    private String type;
    private String dimension;
    public LocationData(int id, String name, String type, String dimension) {
        this.setId(id);
        this.setName(name);
        this.setType(type);
        this.setDimension(dimension);
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
}
