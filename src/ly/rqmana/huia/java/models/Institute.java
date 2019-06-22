package ly.rqmana.huia.java.models;

public class Institute {

    private final long id;
    private String name;
    private String description;

    public Institute(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Institute(long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return name;
    }
}
