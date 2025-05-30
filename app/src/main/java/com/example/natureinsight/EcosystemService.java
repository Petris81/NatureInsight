package com.example.natureinsight;

/**
 * Model class representing an ecosystem service record.
 */
public class EcosystemService {
    private long id;
    private String service;
    private String species;
    private float value;
    private float reliability;

    public EcosystemService(long id, String service, String species, float value, float reliability) {
        this.id = id;
        this.service = service;
        this.species = species;
        this.value = value;
        this.reliability = reliability;
    }

    public EcosystemService(String service, String species, float value, float reliability) {
        this.service = service;
        this.species = species;
        this.value = value;
        this.reliability = reliability;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public float getReliability() {
        return reliability;
    }

    public void setReliability(float reliability) {
        this.reliability = reliability;
    }

    @Override
    public String toString() {
        return "EcosystemService{" +
                "id=" + id +
                ", service='" + service + '\'' +
                ", species='" + species + '\'' +
                ", value=" + value +
                ", reliability=" + reliability +
                '}';
    }
}