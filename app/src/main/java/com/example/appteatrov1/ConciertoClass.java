package com.example.appteatrov1;

public class ConciertoClass {

    private int id;

    private String nombre;
    private String artista;
    private String ciudad;

    public ConciertoClass(int id, String nombre, String artista, String ciudad) {
        this.id = id;
        this.nombre = nombre;
        this.artista = artista;
        this.ciudad = ciudad;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
    public String getArtista() {
        return artista;
    }
    public String getCiudad() {
        return ciudad;
    }
}
