package com.example.appteatrov1;

public class ConciertoClass {

    private int id;

    private String nombre;
    private String artista;
    private String ciudad;
    private String cartel;

    public ConciertoClass(int id, String nombre, String artista, String ciudad, String cartel) {
        this.id = id;
        this.nombre = nombre;
        this.artista = artista;
        this.ciudad = ciudad;
        this.cartel = cartel;
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
    public String getCartel() {
        return cartel;
    }
}
