package com.uth.agendacontactos.controller;

import android.content.Context;

import com.uth.agendacontactos.dao.ContactoDAO;
import com.uth.agendacontactos.dao.PaisDAO;
import com.uth.agendacontactos.model.Contacto;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Controlador (C de MVC): validaciones y coordinación entre las vistas y los DAO. */
public class ContactoController {

    /** Solo letras (con tildes, ñ y ü) y espacios. */
    public static final Pattern REGEX_NOMBRE =
            Pattern.compile("^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+(\\s+[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+)*$");
    /** Exactamente 8 dígitos. */
    public static final Pattern REGEX_TELEFONO = Pattern.compile("^\\d{8}$");
    /** Código entre paréntesis: "Honduras (504)" -> 504. */
    private static final Pattern REGEX_CODIGO = Pattern.compile("\\((\\d+)\\)");

    private final ContactoDAO contactoDAO;
    private final PaisDAO paisDAO;

    public ContactoController(Context context) {
        contactoDAO = new ContactoDAO(context);
        paisDAO = new PaisDAO(context);
    }

    /** @return mensaje de error, o null si los datos son válidos. */
    public String validar(String nombre, String telefono, String nota) {
        if (nombre.trim().isEmpty()) {
            return "Debe escribir un nombre";
        }
        if (!REGEX_NOMBRE.matcher(nombre.trim()).matches()) {
            return "El nombre solo puede contener letras";
        }
        if (telefono.trim().isEmpty() || !REGEX_TELEFONO.matcher(telefono.trim()).matches()) {
            return "Debe escribir un teléfono de 8 dígitos";
        }
        if (nota.trim().isEmpty()) {
            return "Debe escribir una nota";
        }
        return null;
    }

    public long guardar(Contacto c) { return contactoDAO.insertar(c); }

    public boolean actualizar(Contacto c) { return contactoDAO.actualizar(c) > 0; }

    public boolean eliminar(int id) { return contactoDAO.eliminar(id) > 0; }

    public Contacto obtener(int id) { return contactoDAO.obtener(id); }

    public List<Contacto> listar(String filtro) { return contactoDAO.listar(filtro); }

    public List<String> listarPaises() { return paisDAO.listar(); }

    public boolean agregarPais(String nombre, String codigo) { return paisDAO.insertar(nombre, codigo); }

    /** Extrae el código de país: "Honduras (504)" -> "504". */
    public static String codigoPais(String pais) {
        if (pais == null) return "";
        Matcher m = REGEX_CODIGO.matcher(pais);
        return m.find() ? m.group(1) : "";
    }

    /** Teléfono con código para mostrar en la lista: 50499765645. */
    public static String telefonoCompleto(Contacto c) {
        return codigoPais(c.getPais()) + c.getTelefono();
    }
}
