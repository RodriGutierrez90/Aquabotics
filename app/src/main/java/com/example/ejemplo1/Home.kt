package com.example.aquabotics

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class Home : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var database: FirebaseDatabase

    // Views
    private lateinit var tvFecha: TextView
    private lateinit var tvTempAmbiental: TextView
    private lateinit var tvHumedad: TextView
    private lateinit var tvUvindex: TextView
    private lateinit var btnCerrarSesion: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Configurar Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Configurar views
        setupViews()

        // Configurar listeners de Firebase
        setupFirebaseListeners()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupViews() {
        tvFecha = findViewById(R.id.tvFecha)
        tvTempAmbiental = findViewById(R.id.tvTempAmbiental)
        tvHumedad = findViewById(R.id.tvHumedad)
        tvUvindex = findViewById(R.id.tvUvindex)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)

        // Fecha actual
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        tvFecha.text = "Fecha: $currentDate"

        // Botón cerrar sesión
        btnCerrarSesion.setOnClickListener {
            cerrarSesion()
        }
    }

    private fun setupFirebaseListeners() {
        val sensoresRef = database.getReference("sensores/aquabotics")

        sensoresRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val temperatura = snapshot.child("temperatura").getValue(Double::class.java) ?: 0.0
                    val humedad = snapshot.child("humedad").getValue(Double::class.java) ?: 0.0
                    val uvIndex = snapshot.child("uvIndex").getValue(Double::class.java) ?: 0.0

                    // Actualizar UI
                    tvTempAmbiental.text = "Temperatura: ${String.format("%.1f", temperatura)}°C"
                    tvHumedad.text = "Humedad: ${String.format("%.1f", humedad)}%"
                    
                    // Clasificar nivel UV
                    val nivelUV = when {
                        uvIndex > 3.0 -> "EXTREMO"
                        uvIndex > 2.0 -> "MUY ALTO"
                        uvIndex > 1.0 -> "ALTO"
                        uvIndex > 0.5 -> "MODERADO"
                        else -> "BAJO"
                    }

                    // SOLUCIÓN: Crear el texto completo de una vez
                    tvUvindex.text = "UV-A: ${String.format("%.2f", uvIndex)} mW/cm² ($nivelUV)"

                } catch (e: Exception) {
                    Toast.makeText(this@Home, "Error leyendo datos: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Home, "Error de conexión: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun cerrarSesion() {
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}