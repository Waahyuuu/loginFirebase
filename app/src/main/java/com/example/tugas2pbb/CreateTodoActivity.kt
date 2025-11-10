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

    private lateinit var binding: ActivityCreateTodoBinding
    private lateinit var todoUseCase: TodoUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCreateTodoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        todoUseCase = TodoUseCase()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        registerEvents()
    }

    private fun registerEvents() {
        binding.btnTambah.setOnClickListener {
            saveTodoToFirestore()
        }
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun saveTodoToFirestore() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

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

                val resultIntent = Intent().apply {
                    putExtra("new_todo_title", title)
                }
                setResult(RESULT_OK, resultIntent)
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
