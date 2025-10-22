package com.example.tugas2pbb

import android.content.Intent
import android.nfc.NdefMessage
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.tugas2pbb.databinding.ActivityCreateTodoBinding
import com.example.tugas2pbb.databinding.ActivityEditTodoBinding
import com.example.tugas2pbb.entity.Todo
import com.example.tugas2pbb.usecases.TodoUseCase
import kotlinx.coroutines.launch

class EditTodoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditTodoBinding
    private lateinit var todoItemId: String
    private lateinit var todoUseCase: TodoUseCase

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

        todoItemId = intent.getStringExtra("todo_item_id").toString()
        todoUseCase = TodoUseCase()
        registerEvents()
    }

    override fun onStart() {
        super.onStart()
        loadData()
    }

    fun registerEvents() {
        binding.btnEdit.setOnClickListener {
            lifecycleScope.launch {
                val title = binding.etTitle.text.toString()
                val description = binding.etDescription.text.toString()
                val payload = Todo(
                    id = todoItemId,
                    title = title,
                    description = description
                )

                try {
                    todoUseCase.updateTodo(payload)
                    displayMessage("berhasil diperbaharui")
                    back()
                } catch (exc: Exception) {
                    displayMessage("gagal diperbaharui data : ${exc.message}")
                }

            }
        }
    }

    fun loadData() {
        lifecycleScope.launch {
            val data = todoUseCase.getTodo(todoItemId)
            if (data == null) {
                displayMessage("Data yang akan mau diedit gak ada")
                back()
            }

            binding.etTitle.setText(data?.title)
            binding.etDescription.setText(data?.description)
        }
    }

    fun back() {
        val intent = Intent(this, TodoActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun displayMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}