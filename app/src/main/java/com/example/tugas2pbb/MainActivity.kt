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

        credentialManager = CredentialManager.create(this)
        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        registerEvents()
    }

    private fun registerEvents() {
        // Tombol login Google
        binding.btnGoogle.setOnClickListener {
            Log.d("GoogleLogin", "Button clicked")
            lifecycleScope.launch {
                val request = prepareRequest()
                loginByGoogle(request)
            }
        }
    }

    private fun prepareRequest(): GetCredentialRequest {
        val serverClientId = "184622800143-pcvvuhi0cuaha7l2lit6a9ubaev33u2i.apps.googleusercontent.com"

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
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
            firebaseLogin(idToken)

        } catch (exc: NoCredentialException) {
            Toast.makeText(this, "Tidak ada akun Google ditemukan", Toast.LENGTH_LONG).show()
        } catch (exc: Exception) {
            Toast.makeText(this, "Login gagal: ${exc.message}", Toast.LENGTH_LONG).show()
            Log.e("GoogleLogin", "Error: ", exc)
        }
    }

    private fun firebaseLogin(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "Login berhasil: ${user?.displayName}", Toast.LENGTH_LONG).show()
                    Log.d("LoginDebug", "Login sukses, berpindah ke TodoActivity")
                    toTodoListPage()
                } else {
                    Toast.makeText(this, "Login gagal", Toast.LENGTH_LONG).show()
                    Log.e("LoginDebug", "Login gagal: ${task.exception?.message}")
                }
            }
    }

    private fun toTodoListPage() {
        Log.d("TodoDebug", "Pindah ke TodoActivity()")
        val intent = Intent(this@MainActivity, TodoActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("AutoLogin", "User sudah login: ${currentUser.displayName}")
            toTodoListPage()
        } else {
            Log.d("AutoLogin", "Belum login, tetap di MainActivity")
        }
    }
}
