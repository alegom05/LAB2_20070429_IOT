package com.example.lab2;



import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lab2.R;

import java.util.*;

public class MyGameActivity extends AppCompatActivity {

    private TextView tvTema, tvIntentos;
    private GridLayout gridPalabras;
    private Button btnNuevoJuego;

    private List<String> oracionCorrecta;
    private List<String> palabrasSeleccionadas = new ArrayList<>();
    private int intentoActual = 0;
    private final int MAX_INTENTOS = 3;

    private long startTime;

    private String tema;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        tvTema = findViewById(R.id.tvTema);
        tvIntentos = findViewById(R.id.tvIntentos);
        gridPalabras = findViewById(R.id.gridPalabras);
        btnNuevoJuego = findViewById(R.id.btnNuevoJuego);

        tema = getIntent().getStringExtra("tema");
        tvTema.setText("Tema: " + tema);

        startTime = System.currentTimeMillis();

        generarPalabras();

        btnNuevoJuego.setOnClickListener(v -> {
            // Volver al inicio
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("cancelado", true);
            startActivity(intent);
            finish();
        });

        Button btnEstadisticas = findViewById(R.id.buttonStats);
        btnEstadisticas.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyStatisticsActivity.class);
            startActivity(intent);
        });

    }


    private void generarPalabras() {
        oracionCorrecta = obtenerOracionAleatoria(tema);
        List<String> palabrasDesordenadas = new ArrayList<>(oracionCorrecta);
        Collections.shuffle(palabrasDesordenadas);

        gridPalabras.removeAllViews();

        for (String palabra : palabrasDesordenadas) {
            Button btn = new Button(this);
            btn.setText(palabra);
            btn.setOnClickListener(v -> manejarSeleccion(btn));
            gridPalabras.addView(btn);
        }
    }

    private void manejarSeleccion(Button btn) {
        String palabra = btn.getText().toString();

        if (palabra.equals(oracionCorrecta.get(palabrasSeleccionadas.size()))) {
            palabrasSeleccionadas.add(palabra);
            btn.setEnabled(false);
            btn.setBackgroundColor(0xFFB2FF59); // verde claro

            if (palabrasSeleccionadas.size() == oracionCorrecta.size()) {
                mostrarResultado(true);
            }
        } else {
            intentoActual++;
            palabrasSeleccionadas.clear();
            tvIntentos.setText("Intentos: " + intentoActual + " / " + MAX_INTENTOS);
            Toast.makeText(this, "Palabra incorrecta. Intentos restantes: " + (MAX_INTENTOS - intentoActual), Toast.LENGTH_SHORT).show();

            // Reiniciar botones
            for (int i = 0; i < gridPalabras.getChildCount(); i++) {
                View child = gridPalabras.getChildAt(i);
                if (child instanceof Button) {
                    child.setEnabled(true);
                    child.setBackgroundColor(0xFFDDDDDD); // gris
                }
            }

            if (intentoActual >= MAX_INTENTOS) {
                mostrarResultado(false);
            }
        }
    }

    private void mostrarResultado(boolean gano) {
        long tiempo = (System.currentTimeMillis() - startTime) / 1000;
        String mensaje = gano ?
                "¡Ganaste! Tiempo: " + tiempo + "s. Intentos: " + intentoActual :
                "Perdiste. Tiempo: " + tiempo + "s.";

        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();

        // Guardar resultado
        String resultado = gano ?
                "Ganó – Tema: " + tema + " – Intentos: " + intentoActual + " – Tiempo: " + tiempo + "s" :
                "Perdió – Tema: " + tema + " – Tiempo: " + tiempo + "s";

        SharedPreferences prefs = getSharedPreferences("stats", MODE_PRIVATE);
        Set<String> historial = new HashSet<>(prefs.getStringSet("historial", new HashSet<>()));
        historial.add(resultado);

        prefs.edit().putStringSet("historial", historial).apply();

        // Deshabilitar botones
        for (int i = 0; i < gridPalabras.getChildCount(); i++) {
            gridPalabras.getChildAt(i).setEnabled(false);
        }
    }


    private List<String> obtenerOracionAleatoria(String tema) {
        Random rand = new Random();
        String frase;

        switch (tema) {
            case "Software":
                frase = rand.nextBoolean() ?
                        "La fibra óptica envía datos a gran velocidad evitando cualquier interferencia eléctrica" :
                        "Los amplificadores EDFA mejoran la señal óptica en redes de larga distancia";
                break;
            case "Ciberseguridad":
                frase = rand.nextBoolean() ?
                        "Una VPN encripta tu conexión para navegar de forma anónima y segura" :
                        "El ataque DDoS satura servidores con tráfico falso y causa caídas masivas";
                break;
            case "Ópticas":
            default:
                frase = rand.nextBoolean() ?
                        "Los fragments reutilizan partes de pantalla en distintas actividades de la app" :
                        "Los intents permiten acceder a apps como la cámara o WhatsApp directamente";
                break;
        }

        return Arrays.asList(frase.split(" "));
    }
}
