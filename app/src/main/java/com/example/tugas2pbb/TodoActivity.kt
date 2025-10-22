package com.example.tugas2pbb

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tugas2pbb.adapter.Todoadapter
import com.example.tugas2pbb.databinding.ActivityTodoBinding
import com.example.tugas2pbb.entity.Todo
import com.example.tugas2pbb.usecases.TodoUseCase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class TodoActivity : AppCompatActivity() {
    private lateinit var activityBinding: ActivityTodoBinding
    private lateinit var todoAdapter: Todoadapter
    private lateinit var todoUseCase: TodoUseCase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        activityBinding = ActivityTodoBinding.inflate(layoutInflater)
        todoUseCase = TodoUseCase()
        auth = FirebaseAuth.getInstance()
        setContentView(activityBinding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        activityBinding.btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        setupRecyclerView()
        inisiasiData()
        registerEvents()

    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
    }

    fun registerEvents (){
        activityBinding.tombolTambah.setOnClickListener {
            toCreateTodoPage()
        }
    }

    private fun setupRecyclerView() {
        todoAdapter = Todoadapter(mutableListOf(), object : Todoadapter.TodoItemEvents{
            override fun onTodoItemEdit(todo: Todo) {
                val intent = Intent(this@TodoActivity, EditTodoActivity::class.java)
                intent.putExtra("todo_item_id", todo.id)
                startActivity(intent)
            }

            override fun onTodoItemDelete(todo: Todo) {
                val builder = AlertDialog.Builder(this@TodoActivity)

                builder.setTitle("Konfirmasi Hapus Data")
                builder.setMessage("Really? Mau hapus ini? Mikir dulu deh")

                builder.setPositiveButton("Iya lah") { dialog, _ ->
                    lifecycleScope.launch {
                        try {
                            todoUseCase.deleteTodo(todo.id)
                            inisiasiData()
                        }catch (exc : Exception) {
                            displayErrorMessage(exc.message)
                        }
                    }
                }

                builder.setNegativeButton("Jangan Dulu Deh") { dialog, _ ->
                    dialog.dismiss()
                }

                val dialog = builder.create()
                dialog.show()
            }

        })
        activityBinding.container.adapter = todoAdapter
        activityBinding.container.layoutManager = LinearLayoutManager(this)
    }

    private fun inisiasiData() {
        activityBinding.container.visibility = View.GONE
        activityBinding.loading.visibility = View.VISIBLE

        lifecycleScope.launch {
            val data = todoUseCase.getTodo()
            activityBinding.container.visibility = View.VISIBLE
            activityBinding.loading.visibility = View.GONE
            todoAdapter.updateDataSet(data)
        }
    }

    private fun toCreateTodoPage() {
        Log.d("TodoDebug", "Pindah ke CreateTodoActivity()")
        val intent = Intent(this@TodoActivity, CreateTodoActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun displayErrorMessage(message: String?){
        Toast.makeText(this@TodoActivity, message, Toast.LENGTH_SHORT).show()
    }
}
