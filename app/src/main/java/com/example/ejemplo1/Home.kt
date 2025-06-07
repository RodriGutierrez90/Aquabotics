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
import java.text.SimpleDateFormat
import java.util.*

class Home : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Configurar Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val tvFecha = findViewById<TextView>(R.id.tvFecha)
        val tvTempAmbiental = findViewById<TextView>(R.id.tvTempAmbiental)
        val tvTempCorporal = findViewById<TextView>(R.id.tvTempCorporal)
        val tvPHAgua = findViewById<TextView>(R.id.tvPHAgua)
        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarSesion)

        tvFecha.text = "Fecha: $currentDate"
        tvTempAmbiental.text = "Temperatura Ambiental: 25°C"
        tvTempCorporal.text = "Temperatura Corporal: 36.5°C"
        tvPHAgua.text = "pH del Agua: 7.0"

        btnCerrarSesion.setOnClickListener {
            // Cerrar sesión de Firebase
            auth.signOut()
            // Cerrar sesión de Google
            googleSignInClient.signOut().addOnCompleteListener {
                // Redirigir al Login
                val intent = Intent(this, Login::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}
