package com.example.tugas2pbb

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.tugas2pbb.databinding.ActivityEditTodoBinding
import com.example.tugas2pbb.entity.Todo
import com.example.tugas2pbb.usecases.TodoUseCase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class EditTodoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditTodoBinding
    private lateinit var todoItemId: String
    private lateinit var todoUseCase: TodoUseCase
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditTodoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (auth.currentUser == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        todoItemId = intent.getStringExtra("todo_item_id") ?: ""

        todoUseCase = TodoUseCase()
        registerEvents()
    }

    override fun onStart() {
        super.onStart()
        loadData()
    }

    private fun registerEvents() {
        binding.btnEdit.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()

            if (title.isEmpty() || description.isEmpty()) {
                displayMessage("Judul dan deskripsi tidak boleh kosong")
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val payload = Todo(
                    id = todoItemId,
                    title = title,
                    description = description
                )

                try {
                    todoUseCase.updateTodo(payload)
                    displayMessage("Data berhasil diperbarui ✅")
                    back()
                } catch (exc: Exception) {
                    displayMessage("Gagal memperbarui data: ${exc.message}")
                }
            }
        }

        binding.btnKembali.setOnClickListener {
            back()
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                val data = todoUseCase.getTodo(todoItemId)
                if (data == null) {
                    displayMessage("Data yang akan diedit tidak ditemukan")
                    back()
                    return@launch
                }

                binding.etTitle.setText(data.title)
                binding.etDescription.setText(data.description)
            } catch (exc: Exception) {
                displayMessage("Gagal memuat data: ${exc.message}")
                back()
            }
        }
    }

    private fun back() {
        val intent = Intent(this, TodoActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun displayMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}