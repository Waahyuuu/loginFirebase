package com.example.tugas2pbb.usecases

import com.example.tugas2pbb.entity.Todo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TodoUseCase {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private fun getUserTodoCollection() =
        db.collection("users")
            .document(auth.currentUser?.uid ?: throw Exception("User belum login"))
            .collection("todo")

    suspend fun getTodo(): List<Todo> {
        val snapshot = getUserTodoCollection()
            .get()
            .await()

        return snapshot.documents.map {
            Todo(
                id = it.id,
                title = it.getString("title") ?: "",
                description = it.getString("description") ?: ""
            )
        }
    }

    suspend fun getTodo(id: String): Todo? {
        val doc = getUserTodoCollection()
            .document(id)
            .get()
            .await()

        if (!doc.exists()) return null

        return Todo(
            id = doc.id,
            title = doc.getString("title") ?: "",
            description = doc.getString("description") ?: ""
        )
    }

    suspend fun createTodo(todo: Todo): Todo {
        try {
            val payload = hashMapOf(
                "title" to todo.title,
                "description" to todo.description
            )
            val doc = getUserTodoCollection()
                .add(payload)
                .await()

            return todo.copy(id = doc.id)
        } catch (exc: Exception) {
            throw Exception("Gagal menyimpan data ke Firestore: ${exc.message}")
        }
    }

    suspend fun updateTodo(todo: Todo) {
        try {
            val payload = hashMapOf(
                "title" to todo.title,
                "description" to todo.description
            )
            getUserTodoCollection()
                .document(todo.id)
                .set(payload)
                .await()
        } catch (exc: Exception) {
            throw Exception("Gagal memperbarui data: ${exc.message}")
        }
    }

    suspend fun deleteTodo(id: String) {
        try {
            getUserTodoCollection()
                .document(id)
                .delete()
                .await()
        } catch (exc: Exception) {
            throw Exception("Gagal menghapus data: ${exc.message}")
        }
    }
}