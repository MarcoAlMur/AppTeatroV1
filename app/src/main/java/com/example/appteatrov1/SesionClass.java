package com.example.appteatrov1;

public class SesionClass {
    private int id;
    private String fecha;
    private String hora;

    public SesionClass(int id, String fecha, String hora) {
        this.id = id;
        this.fecha = fecha;
        this.hora = hora;
    }

    public int getId() {
        return id;
    }

    public String getFecha() {
        return fecha;
    }

    public String getHora() {
        return hora;
    }
}
