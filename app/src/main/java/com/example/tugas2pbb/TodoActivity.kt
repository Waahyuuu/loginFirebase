package com.example.tugas2pbb

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tugas2pbb.adapter.Todoadapter
import com.example.tugas2pbb.databinding.ActivityTodoBinding
import com.example.tugas2pbb.entity.Todo
import com.example.tugas2pbb.usecases.TodoUseCase
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class TodoActivity : AppCompatActivity() {
    private lateinit var activityBinding: ActivityTodoBinding
    private lateinit var todoAdapter: Todoadapter
    private lateinit var todoUseCase: TodoUseCase
    private lateinit var auth: FirebaseAuth
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        activityBinding = ActivityTodoBinding.inflate(layoutInflater)
        todoUseCase = TodoUseCase()
        auth = FirebaseAuth.getInstance()
        setContentView(activityBinding.root)

        // DrawerLayout dari layout
        drawerLayout = findViewById(R.id.drawerLayout)
        val navView: NavigationView = findViewById(R.id.navigationView)

        // Atur window inset (biar padding aman)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Tombol menu (☰) untuk buka drawer
        val menuButton = activityBinding.toolbar.findViewById<ImageView>(R.id.menuButton)
        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Tombol Logout di toolbar
        activityBinding.btnLogout.setOnClickListener {
            logoutUser()
        }

        // Isi header drawer dengan data user
        setupDrawerHeader(navView)

        // Event click di menu drawer
        setupDrawerMenu(navView)

        // RecyclerView
        setupRecyclerView()

        // Ambil data todo
        inisiasiData()

        // Tombol tambah
        registerEvents()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun registerEvents() {
        activityBinding.tombolTambah.setOnClickListener {
            toCreateTodoPage()
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
                val builder = AlertDialog.Builder(this@TodoActivity)
                builder.setTitle("Konfirmasi Hapus Data")
                builder.setMessage("Yakin ingin menghapus data ini?")
                builder.setPositiveButton("Ya") { dialog, _ ->
                    lifecycleScope.launch {
                        try {
                            todoUseCase.deleteTodo(todo.id)
                            inisiasiData()
                        } catch (exc: Exception) {
                            displayErrorMessage(exc.message)
                        }
                    }
                }
                builder.setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
                builder.create().show()
            }
        })
        activityBinding.container.adapter = todoAdapter
        activityBinding.container.layoutManager = LinearLayoutManager(this)
    }

    private fun inisiasiData() {
        activityBinding.container.visibility = View.GONE
        activityBinding.loading.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val data = todoUseCase.getTodo()
                activityBinding.container.visibility = View.VISIBLE
                activityBinding.loading.visibility = View.GONE
                todoAdapter.updateDataSet(data)
            } catch (e: Exception) {
                displayErrorMessage(e.message)
            }
        }
    }

    private fun toCreateTodoPage() {
        Log.d("TodoDebug", "Pindah ke CreateTodoActivity()")
        val intent = Intent(this@TodoActivity, CreateTodoActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun displayErrorMessage(message: String?) {
        Toast.makeText(this@TodoActivity, message ?: "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
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

        val user = auth.currentUser
        nameView.text = user?.displayName ?: "Pengguna"
        emailView.text = user?.email ?: "email@tidak.ada"
    }

    private fun setupDrawerMenu(navView: NavigationView) {
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, TodoActivity::class.java)
                    startActivity(intent)
                    finish() // optional: agar tidak menumpuk activity
                }
                R.id.nav_galeri -> {
                    val intent = Intent(this, GaleriActivity::class.java)
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }
}
