package com.example.tugas2pbb

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.tugas2pbb.adapter.Todoadapter
import com.example.tugas2pbb.databinding.ActivityTodoBinding
import com.example.tugas2pbb.entity.Todo
import com.example.tugas2pbb.usecases.TodoUseCase
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class TodoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTodoBinding
    private lateinit var todoAdapter: Todoadapter
    private lateinit var todoUseCase: TodoUseCase
    private lateinit var auth: FirebaseAuth
    private lateinit var drawerLayout: DrawerLayout

    // 🔹 Launcher untuk menerima hasil dari CreateTodoActivity
    private val createTodoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.d("TodoDebug", "Result OK diterima, memuat ulang data...")
            loadTodoData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTodoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        todoUseCase = TodoUseCase()
        drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navigationView

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val menuButton = binding.toolbar.findViewById<ImageView>(R.id.menuButton)
        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.btnLogout.setOnClickListener {
            logoutUser()
        }

        setupDrawerHeader(navView)
        setupDrawerMenu(navView)
        setupRecyclerView()
        loadTodoData()

        binding.tombolTambah.setOnClickListener {
            goToCreateTodo()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun setupRecyclerView() {
        todoAdapter = Todoadapter(mutableListOf(), object : Todoadapter.TodoItemEvents {
            override fun onTodoItemEdit(todo: Todo) {
                val intent = Intent(this@TodoActivity, EditTodoActivity::class.java)
                intent.putExtra("todo_item_id", todo.id)
                startActivity(intent)
            }

            override fun onTodoItemDelete(todo: Todo) {
                AlertDialog.Builder(this@TodoActivity)
                    .setTitle("Konfirmasi Hapus Data")
                    .setMessage("Yakin ingin menghapus data ini?")
                    .setPositiveButton("Ya") { dialog, _ ->
                        lifecycleScope.launch {
                            try {
                                todoUseCase.deleteTodo(todo.id)
                                loadTodoData()
                                Toast.makeText(this@TodoActivity, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                            } catch (exc: Exception) {
                                displayErrorMessage(exc.message)
                            }
                        }
                    }
                    .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        })
        binding.container.apply {
            adapter = todoAdapter
            layoutManager = LinearLayoutManager(this@TodoActivity)
        }
    }

    private fun loadTodoData() {
        binding.container.visibility = View.GONE
        binding.loading.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val data = todoUseCase.getTodo()
                binding.loading.visibility = View.GONE
                binding.container.visibility = View.VISIBLE
                todoAdapter.updateDataSet(data)
            } catch (e: Exception) {
                displayErrorMessage(e.message)
            }
        }
    }

    private fun goToCreateTodo() {
        val intent = Intent(this@TodoActivity, CreateTodoActivity::class.java)
        createTodoLauncher.launch(intent)
    }

    private fun displayErrorMessage(message: String?) {
        Toast.makeText(this, message ?: "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
    }

    private fun logoutUser() {
        auth.signOut()
        Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun setupDrawerHeader(navView: NavigationView) {
        val headerView = navView.getHeaderView(0)
        val nameView = headerView.findViewById<TextView>(R.id.profile_name)
        val emailView = headerView.findViewById<TextView>(R.id.profile_email)
        val imageView = headerView.findViewById<ImageView>(R.id.header_image)

        val user = auth.currentUser
        nameView.text = user?.displayName ?: "Pengguna"
        emailView.text = user?.email ?: "email@tidak.ada"

        user?.photoUrl?.let { url ->
            Glide.with(this)
                .load(url)
                .circleCrop()
                .into(imageView)
        }
    }

    private fun setupDrawerMenu(navView: NavigationView) {
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_galeri -> {
                    startActivity(Intent(this, GaleriActivity::class.java))
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }
}
