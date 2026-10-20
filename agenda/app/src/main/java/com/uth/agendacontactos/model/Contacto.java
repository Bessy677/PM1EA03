package com.uth.agendacontactos.model;

/** Modelo (M de MVC): representa un contacto de la agenda. */
public class Contacto {
    private int id;
    private String pais;      // Ej.: "Honduras (504)"
    private String nombre;
    private String telefono;  // 8 dígitos, sin código de país
    private String nota;
    private String imagen;    // Ruta absoluta de la fotografía (puede ser null)

    public Contacto() { }

    public Contacto(int id, String pais, String nombre, String telefono, String nota, String imagen) {
        this.id = id;
        this.pais = pais;
        this.nombre = nombre;
        this.telefono = telefono;
        this.nota = nota;
        this.imagen = imagen;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
}
