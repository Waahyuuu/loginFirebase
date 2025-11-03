package com.example.tugas2pbb

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.tugas2pbb.databinding.ActivityMainBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var credentialManager: CredentialManager
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi
        credentialManager = CredentialManager.create(this)
        auth = FirebaseAuth.getInstance()

        // Handle insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Event button
        registerEvents()

        // Cek user login
        checkCurrentUser()
    }

    private fun registerEvents() {
        binding.btnGoogle.setOnClickListener {
            Log.d("GoogleLogin", "Button clicked")
            lifecycleScope.launch {
                try {
                    val request = prepareRequest()
                    loginByGoogle(request)
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Gagal mempersiapkan login", Toast.LENGTH_SHORT).show()
                    Log.e("GoogleLogin", "Error prepareRequest", e)
                }
            }
        }
    }

    private fun prepareRequest(): GetCredentialRequest {
        val serverClientId = "184622800143-pcvvuhi0cuaha7l2lit6a9ubaev33u2i.apps.googleusercontent.com"

        val googleOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()
    }

    private suspend fun loginByGoogle(request: GetCredentialRequest) {
        try {
            val result = credentialManager.getCredential(
                context = this,
                request = request
            )

            val credential = result.credential
            val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken

            if (idToken != null) {
                firebaseLogin(idToken)
            } else {
                Toast.makeText(this, "ID Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            }

        } catch (exc: NoCredentialException) {
            Toast.makeText(this, "Tidak ada akun Google ditemukan", Toast.LENGTH_LONG).show()
            Log.w("GoogleLogin", "No Google accounts found", exc)
        } catch (exc: Exception) {
            Toast.makeText(this, "Login gagal: ${exc.message}", Toast.LENGTH_LONG).show()
            Log.e("GoogleLogin", "Error during Google login", exc)
        }
    }

    private fun firebaseLogin(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login berhasil", Toast.LENGTH_LONG).show()
                    toTodoListPage()
                } else {
                    Toast.makeText(this, "Login gagal", Toast.LENGTH_LONG).show()
                    Log.e("GoogleLogin", "Firebase login failed", task.exception)
                }
            }
    }

    private fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("GoogleLogin", "User sudah login: ${currentUser.displayName}")
            toTodoListPage()
        }
    }

    private fun toTodoListPage() {
        val intent = Intent(this, TodoActivity::class.java)
        startActivity(intent)
        finish()
    }
}
