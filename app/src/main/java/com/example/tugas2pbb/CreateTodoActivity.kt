package com.example.tugas2pbb

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.tugas2pbb.databinding.ActivityCreateTodoBinding
import com.example.tugas2pbb.entity.Todo
import com.example.tugas2pbb.usecases.TodoUseCase
import kotlinx.coroutines.launch

class CreateTodoActivity : AppCompatActivity() {

    private lateinit var activityBinding: ActivityCreateTodoBinding
    private lateinit var todoUseCase: TodoUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        activityBinding = ActivityCreateTodoBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)

        todoUseCase = TodoUseCase()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        registerEvents()
    }

    private fun registerEvents() {

        activityBinding.btnTambah.setOnClickListener {
            saveTodoToFireStore()
        }

        activityBinding.btnBack.setOnClickListener {
            val intent = Intent(this, TodoActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun saveTodoToFireStore() {
        val title = activityBinding.etTitle.text.toString().trim()
        val description = activityBinding.etDescription.text.toString().trim()

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Judul dan deskripsi tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        val todo = Todo(
            id = "",
            title = title,
            description = description
        )

        lifecycleScope.launch {
            try {
                todoUseCase.createTodo(todo)
                Toast.makeText(this@CreateTodoActivity, "Sukses menambahkan data", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@CreateTodoActivity, TodoActivity::class.java)
                startActivity(intent)
                finish()

            } catch (exc: Exception) {
                Toast.makeText(
                    this@CreateTodoActivity,
                    "Gagal menambahkan data: ${exc.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
