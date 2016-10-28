package de.m4lik.monarchcode.burningseries.objects;

/**
 * Created by Malik on 01.10.2016.
 */

public class ShowObj {

    private String name;
    private Integer id;

    public ShowObj(String name, Integer id) {

        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Integer getId() {
        return id;
    }
}
