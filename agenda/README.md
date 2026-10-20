# Agenda de Contactos (Android · Java · MVC + DAO)

Examen PM01 – Parcial 1 – Versión A.

- **Registro:** foto con cámara, país (spinner con opción de agregar países con «+»), nombre, teléfono y nota. Validaciones con regex (nombre solo letras, teléfono de 8 dígitos) y alertas por campo vacío.
- **Lista:** búsqueda, compartir (`ACTION_SEND`), ver imagen, eliminar y actualizar. Pulsación larga sobre un contacto → «¿Desea llamar a …?».
- **Llamar:** `CALL_PHONE` en tiempo de ejecución + `ACTION_CALL`.

Estructura: `model/` (Contacto) · `db/` (SQLite) · `dao/` · `controller/` · `view/` (Activities).

Abrir con Android Studio: *File ▸ Open* y seleccionar esta carpeta; esperar la sincronización de Gradle.
