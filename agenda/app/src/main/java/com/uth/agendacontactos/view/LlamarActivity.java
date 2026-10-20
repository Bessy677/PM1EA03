package com.uth.agendacontactos.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.uth.agendacontactos.R;

/** Pantalla 3: Llamar. Solicita CALL_PHONE en tiempo de ejecución y marca con ACTION_CALL. */
public class LlamarActivity extends AppCompatActivity {

    public static final String EXTRA_NOMBRE = "nombre";
    public static final String EXTRA_CODIGO = "codigo";
    public static final String EXTRA_TELEFONO = "telefono";

    private String numero; // formato internacional: +50499765645

    private final ActivityResultLauncher<String> pedirPermiso =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedido -> {
                if (concedido) {
                    llamar();
                } else {
                    Toast.makeText(this, "Permiso de llamada denegado", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llamar);

        String nombre = getIntent().getStringExtra(EXTRA_NOMBRE);
        String codigo = getIntent().getStringExtra(EXTRA_CODIGO);
        String telefono = getIntent().getStringExtra(EXTRA_TELEFONO);
        if (nombre == null || telefono == null) {
            finish();
            return;
        }
        if (codigo == null) codigo = "";

        numero = "+" + codigo + telefono;

        ((TextView) findViewById(R.id.tvNombre)).setText(nombre);
        // +504 9976-5645
        String bonito = "+" + codigo + " " + telefono.substring(0, 4) + "-" + telefono.substring(4);
        ((TextView) findViewById(R.id.tvTelefono)).setText(bonito);

        findViewById(R.id.btnAtras).setOnClickListener(v -> finish());
        findViewById(R.id.btnLlamar).setOnClickListener(v -> confirmar(nombre));
    }

    private void confirmar(String nombre) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.accion)
                .setMessage("¿Desea llamar a " + nombre.trim().split("\\s+")[0] + "?")
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.si, (d, w) -> verificarPermiso())
                .show();
    }

    private void verificarPermiso() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            llamar();
        } else {
            pedirPermiso.launch(Manifest.permission.CALL_PHONE);
        }
    }

    private void llamar() {
        try {
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + numero)));
        } catch (SecurityException e) {
            Toast.makeText(this, "No hay permiso para llamar", Toast.LENGTH_LONG).show();
        }
    }
}
