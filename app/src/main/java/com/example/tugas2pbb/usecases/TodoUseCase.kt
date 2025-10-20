package com.example.tugas2pbb.usecases

import com.example.tugas2pbb.entity.Todo
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class TodoUseCase {
    private val db: FirebaseFirestore = Firebase.firestore

    suspend fun getTodo(): List<Todo> {
        val data = db.collection("todo")
            .get()
            .await()

        if (data.isEmpty) {
            throw Exception("Data di servermu ga ada")
        }

        return data.documents.map {
            Todo (
                id = it.id,
                title = it.get("title").toString(),
                description = it.get("description").toString(),
            )
        }
    }

    suspend fun createTodo(todo: Todo): Todo{
        try {
            val payload = hashMapOf(
                "title" to todo.title,
                "description" to todo.description
            )
            val data = db.collection("todo")
                .add(payload)
                .await()

            return todo.copy(id = data.id)
        } catch (exc: Exception) {
            throw Exception("gagal menyimpan data ke firestore")
        }
    }

    companion object
}