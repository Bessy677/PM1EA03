package com.uth.agendacontactos.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.uth.agendacontactos.R;
import com.uth.agendacontactos.controller.ContactoController;
import com.uth.agendacontactos.model.Contacto;

import java.io.File;
import java.io.IOException;
import java.util.List;

/** Pantalla 1: Registro (y edición) de contactos. */
public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id_contacto";

    private ImageView imgFoto;
    private EditText etNombre, etTelefono, etNota;
    private Spinner spPais;
    private Button btnSalvar;

    private ContactoController controller;
    private ArrayAdapter<String> adapterPaises;

    private String rutaFoto = null;       // foto que se guardará
    private String rutaTemporal = null;   // foto en proceso de captura
    private int idEdicion = -1;           // -1 = contacto nuevo

    private final ActivityResultLauncher<Uri> tomarFoto =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), ok -> {
                if (ok && rutaTemporal != null) {
                    rutaFoto = rutaTemporal;
                    mostrarFoto();
                } else if (rutaTemporal != null) {
                    new File(rutaTemporal).delete();
                }
                rutaTemporal = null;
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        controller = new ContactoController(this);

        imgFoto = findViewById(R.id.imgFoto);
        etNombre = findViewById(R.id.etNombre);
        etTelefono = findViewById(R.id.etTelefono);
        etNota = findViewById(R.id.etNota);
        spPais = findViewById(R.id.spPais);
        btnSalvar = findViewById(R.id.btnSalvar);
        ImageButton btnCamara = findViewById(R.id.btnCamara);
        Button btnLista = findViewById(R.id.btnLista);
        Button btnAgregarPais = findViewById(R.id.btnAgregarPais);

        adapterPaises = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
        spPais.setAdapter(adapterPaises);
        cargarPaises(null);

        if (savedInstanceState != null) {
            rutaFoto = savedInstanceState.getString("rutaFoto");
            rutaTemporal = savedInstanceState.getString("rutaTemporal");
        }

        idEdicion = getIntent().getIntExtra(EXTRA_ID, -1);
        if (idEdicion != -1 && savedInstanceState == null) {
            cargarContactoParaEditar();
        }
        if (idEdicion != -1) {
            btnSalvar.setText(R.string.actualizar_contacto);
        }
        mostrarFoto();

        btnCamara.setOnClickListener(v -> abrirCamara());
        btnAgregarPais.setOnClickListener(v -> dialogoAgregarPais());
        btnSalvar.setOnClickListener(v -> salvar());
        btnLista.setOnClickListener(v -> {
            startActivity(new Intent(this, ListaActivity.class));
            if (idEdicion != -1) finish();
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("rutaFoto", rutaFoto);
        outState.putString("rutaTemporal", rutaTemporal);
    }

    private void cargarPaises(String seleccionado) {
        List<String> paises = controller.listarPaises();
        adapterPaises.clear();
        adapterPaises.addAll(paises);
        adapterPaises.notifyDataSetChanged();
        if (seleccionado != null) {
            int pos = paises.indexOf(seleccionado);
            if (pos >= 0) spPais.setSelection(pos);
        }
    }

    private void cargarContactoParaEditar() {
        Contacto c = controller.obtener(idEdicion);
        if (c == null) {
            idEdicion = -1;
            return;
        }
        etNombre.setText(c.getNombre());
        etTelefono.setText(c.getTelefono());
        etNota.setText(c.getNota());
        rutaFoto = c.getImagen();
        int pos = adapterPaises.getPosition(c.getPais());
        if (pos >= 0) spPais.setSelection(pos);
    }

    private void mostrarFoto() {
        if (rutaFoto != null && new File(rutaFoto).exists()) {
            Bitmap bmp = ImagenUtil.cargar(rutaFoto, 800, 600);
            if (bmp != null) {
                imgFoto.setImageBitmap(bmp);
                return;
            }
        }
        imgFoto.setImageResource(R.drawable.ic_persona);
    }

    private void abrirCamara() {
        try {
            File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File archivo = File.createTempFile("contacto_" + System.currentTimeMillis() + "_", ".jpg", dir);
            rutaTemporal = archivo.getAbsolutePath();
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", archivo);
            tomarFoto.launch(uri);
        } catch (IOException | RuntimeException e) {
            Toast.makeText(this, "No se pudo abrir la cámara", Toast.LENGTH_SHORT).show();
        }
    }

    private void dialogoAgregarPais() {
        LinearLayout caja = new LinearLayout(this);
        caja.setOrientation(LinearLayout.VERTICAL);
        int p = (int) (20 * getResources().getDisplayMetrics().density);
        caja.setPadding(p, p / 2, p, 0);

        EditText etPais = new EditText(this);
        etPais.setHint("Nombre del país");
        etPais.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        caja.addView(etPais);

        EditText etCodigo = new EditText(this);
        etCodigo.setHint("Código (ej. 507)");
        etCodigo.setInputType(InputType.TYPE_CLASS_NUMBER);
        caja.addView(etCodigo);

        new AlertDialog.Builder(this)
                .setTitle("Agregar país")
                .setView(caja)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.aceptar, (d, w) -> {
                    String nombre = etPais.getText().toString().trim();
                    String codigo = etCodigo.getText().toString().trim();
                    if (nombre.isEmpty() || codigo.isEmpty()) {
                        alerta("Debe escribir el país y su código");
                    } else if (controller.agregarPais(nombre, codigo)) {
                        cargarPaises(nombre + " (" + codigo + ")");
                    } else {
                        alerta("Ese país ya existe");
                    }
                })
                .show();
    }

    private void salvar() {
        String nombre = etNombre.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String nota = etNota.getText().toString().trim();

        String error = controller.validar(nombre, telefono, nota);
        if (error != null) {
            alerta(error);
            return;
        }
        if (spPais.getSelectedItem() == null) {
            alerta("Debe seleccionar un país");
            return;
        }

        Contacto c = new Contacto(
                idEdicion,
                spPais.getSelectedItem().toString(),
                nombre, telefono, nota, rutaFoto);

        if (idEdicion == -1) {
            if (controller.guardar(c) != -1) {
                Toast.makeText(this, "Contacto salvado", Toast.LENGTH_SHORT).show();
                limpiar();
            } else {
                alerta("No se pudo guardar el contacto");
            }
        } else {
            if (controller.actualizar(c)) {
                Toast.makeText(this, "Contacto actualizado", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                alerta("No se pudo actualizar el contacto");
            }
        }
    }

    private void limpiar() {
        etNombre.setText("");
        etTelefono.setText("");
        etNota.setText("");
        rutaFoto = null;
        mostrarFoto();
        etNombre.requestFocus();
    }

    private void alerta(String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.alerta)
                .setMessage(mensaje)
                .setPositiveButton(R.string.aceptar, null)
                .show();
    }
}
