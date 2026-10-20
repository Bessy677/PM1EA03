package com.uth.agendacontactos.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.uth.agendacontactos.R;
import com.uth.agendacontactos.controller.ContactoController;
import com.uth.agendacontactos.model.Contacto;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Pantalla 2: lista de contactos guardados con sus acciones. */
public class ListaActivity extends AppCompatActivity {

    private ContactoController controller;
    private ListView lvContactos;
    private EditText etBuscar;
    private ContactoAdapter adapter;
    private int posSeleccionada = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        controller = new ContactoController(this);
        lvContactos = findViewById(R.id.lvContactos);
        etBuscar = findViewById(R.id.etBuscar);

        adapter = new ContactoAdapter(new ArrayList<>());
        lvContactos.setAdapter(adapter);

        lvContactos.setOnItemClickListener((parent, view, position, id) -> {
            posSeleccionada = position;
            adapter.notifyDataSetChanged();
        });

        // Pulsación larga: pregunta si desea llamar (AlertDialog.Builder)
        lvContactos.setOnItemLongClickListener((parent, view, position, id) -> {
            posSeleccionada = position;
            adapter.notifyDataSetChanged();
            confirmarLlamada(adapter.getItem(position));
            return true;
        });

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) { cargarLista(); }
        });

        findViewById(R.id.btnAtras).setOnClickListener(v -> finish());
        findViewById(R.id.btnCompartir).setOnClickListener(v -> compartir());
        findViewById(R.id.btnVerImagen).setOnClickListener(v -> verImagen());
        findViewById(R.id.btnEliminar).setOnClickListener(v -> eliminar());
        findViewById(R.id.btnActualizar).setOnClickListener(v -> actualizar());
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarLista();
    }

    private void cargarLista() {
        List<Contacto> datos = controller.listar(etBuscar.getText().toString());
        posSeleccionada = -1;
        adapter.clear();
        adapter.addAll(datos);
        adapter.notifyDataSetChanged();
    }

    private Contacto seleccionado() {
        if (posSeleccionada < 0 || posSeleccionada >= adapter.getCount()) {
            Toast.makeText(this, "Seleccione un contacto de la lista", Toast.LENGTH_SHORT).show();
            return null;
        }
        return adapter.getItem(posSeleccionada);
    }

    // ---------- Llamar ----------
    private void confirmarLlamada(Contacto c) {
        String primerNombre = c.getNombre().trim().split("\\s+")[0];
        new AlertDialog.Builder(this)
                .setTitle(R.string.accion)
                .setMessage("¿Desea llamar a " + primerNombre + "?")
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.si, (d, w) -> {
                    Intent i = new Intent(this, LlamarActivity.class);
                    i.putExtra(LlamarActivity.EXTRA_NOMBRE, c.getNombre());
                    i.putExtra(LlamarActivity.EXTRA_CODIGO, ContactoController.codigoPais(c.getPais()));
                    i.putExtra(LlamarActivity.EXTRA_TELEFONO, c.getTelefono());
                    startActivity(i);
                })
                .show();
    }

    // ---------- Compartir (Intent.ACTION_SEND) ----------
    private void compartir() {
        Contacto c = seleccionado();
        if (c == null) return;

        String texto = "Contacto: " + c.getNombre()
                + "\nTeléfono: +" + ContactoController.codigoPais(c.getPais()) + " " + c.getTelefono()
                + "\nPaís: " + c.getPais()
                + "\nNota: " + c.getNota();

        Intent envio = new Intent(Intent.ACTION_SEND);
        envio.putExtra(Intent.EXTRA_SUBJECT, "Contacto: " + c.getNombre());
        envio.putExtra(Intent.EXTRA_TEXT, texto);

        File foto = c.getImagen() == null ? null : new File(c.getImagen());
        if (foto != null && foto.exists()) {
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", foto);
            envio.setType("image/*");
            envio.putExtra(Intent.EXTRA_STREAM, uri);
            envio.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            envio.setType("text/plain");
        }
        startActivity(Intent.createChooser(envio, "Compartir contacto"));
    }

    // ---------- Ver imagen ----------
    private void verImagen() {
        Contacto c = seleccionado();
        if (c == null) return;

        Bitmap bmp = ImagenUtil.cargar(c.getImagen(), 900, 900);
        if (bmp == null) {
            Toast.makeText(this, "Este contacto no tiene imagen guardada", Toast.LENGTH_SHORT).show();
            return;
        }
        ImageView iv = new ImageView(this);
        iv.setAdjustViewBounds(true);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setImageBitmap(bmp);
        int p = (int) (12 * getResources().getDisplayMetrics().density);
        iv.setPadding(p, p, p, p);

        new AlertDialog.Builder(this)
                .setTitle(c.getNombre())
                .setView(iv)
                .setPositiveButton(R.string.aceptar, null)
                .show();
    }

    // ---------- Eliminar ----------
    private void eliminar() {
        Contacto c = seleccionado();
        if (c == null) return;
        new AlertDialog.Builder(this)
                .setTitle(R.string.eliminar_contacto)
                .setMessage("¿Desea eliminar a " + c.getNombre() + "?")
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.si, (d, w) -> {
                    if (controller.eliminar(c.getId())) {
                        if (c.getImagen() != null) new File(c.getImagen()).delete();
                        Toast.makeText(this, "Contacto eliminado", Toast.LENGTH_SHORT).show();
                        cargarLista();
                    }
                })
                .show();
    }

    // ---------- Actualizar ----------
    private void actualizar() {
        Contacto c = seleccionado();
        if (c == null) return;
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra(MainActivity.EXTRA_ID, c.getId());
        startActivity(i);
    }

    /** Adaptador que resalta en verde la fila seleccionada. */
    private class ContactoAdapter extends ArrayAdapter<Contacto> {
        ContactoAdapter(List<Contacto> datos) {
            super(ListaActivity.this, R.layout.item_contacto, datos);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_contacto, parent, false);
            }
            TextView tv = convertView.findViewById(R.id.tvItem);
            Contacto c = getItem(position);
            tv.setText("●  " + c.getNombre() + " | " + ContactoController.telefonoCompleto(c));

            if (position == posSeleccionada) {
                convertView.setBackgroundColor(getColor(R.color.verde_primario));
                tv.setTextColor(Color.WHITE);
                tv.setTypeface(null, Typeface.BOLD);
            } else {
                convertView.setBackgroundColor(getColor(
                        position % 2 == 0 ? R.color.verde_fila_par : R.color.blanco));
                tv.setTextColor(getColor(R.color.negro));
                tv.setTypeface(null, Typeface.NORMAL);
            }
            return convertView;
        }
    }
}
