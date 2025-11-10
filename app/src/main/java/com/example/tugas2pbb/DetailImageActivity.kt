package com.example.tugas2pbb

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.tugas2pbb.util.FileUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class DetailImageActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var textJudul: TextView
    private lateinit var textTanggal: TextView
    private lateinit var btnEdit: ImageView
    private lateinit var btnDelete: ImageView
    private lateinit var btnBack: ImageView

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var docId: String? = null
    private var namaFile: String? = null
    private var judul: String? = null
    private var tanggal: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_image)

        imageView = findViewById(R.id.detailImage)
        textJudul = findViewById(R.id.detailJudul)
        textTanggal = findViewById(R.id.detailTanggal)
        btnEdit = findViewById(R.id.btnEdit)
        btnDelete = findViewById(R.id.btnDelete)
        btnBack = findViewById(R.id.btnBack)

        docId = intent.getStringExtra("id")
        namaFile = intent.getStringExtra("namaFile")
        judul = intent.getStringExtra("judul")
        tanggal = intent.getStringExtra("tanggalUpload")

        textJudul.text = judul
        textTanggal.text = tanggal

        namaFile?.let {
            val file = FileUtils.getImageFile(this, it)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                imageView.setImageBitmap(bitmap)
            } else {
                Toast.makeText(this, "File gambar tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener { finish() }

        btnEdit.setOnClickListener {
            showEditDialog()
        }

        btnDelete.setOnClickListener {
            confirmDelete()
        }
    }

    private fun showEditDialog() {
        if (auth.currentUser == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser!!.uid
        val editText = EditText(this)
        editText.setText(judul)

        AlertDialog.Builder(this)
            .setTitle("Edit Judul")
            .setView(editText)
            .setPositiveButton("Simpan") { _, _ ->
                val newJudul = editText.text.toString().trim()
                if (newJudul.isEmpty()) {
                    Toast.makeText(this, "Judul tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                docId?.let { id ->
                    db.collection("users")
                        .document(userId)
                        .collection("image")
                        .document(id)
                        .update("judul", newJudul)
                        .addOnSuccessListener {
                            judul = newJudul
                            textJudul.text = newJudul
                            Toast.makeText(this, "Judul berhasil diperbarui", Toast.LENGTH_SHORT).show()

                            // 🔹 Kirim result agar list bisa update tanpa reload
                            val resultIntent = Intent().apply {
                                putExtra("updated_id", id)
                                putExtra("updated_title", newJudul)
                            }
                            setResult(Activity.RESULT_OK, resultIntent)
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Gagal memperbarui: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun confirmDelete() {
        if (auth.currentUser == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser!!.uid

        AlertDialog.Builder(this)
            .setTitle("Hapus Gambar?")
            .setMessage("Apakah kamu yakin ingin menghapus gambar ini?")
            .setPositiveButton("Hapus") { _, _ ->
                docId?.let { id ->
                    db.collection("users")
                        .document(userId)
                        .collection("image")
                        .document(id)
                        .delete()
                        .addOnSuccessListener {
                            namaFile?.let { nama ->
                                val file = FileUtils.getImageFile(this, nama)
                                if (file.exists()) file.delete()
                            }

                            Toast.makeText(this, "Gambar berhasil dihapus", Toast.LENGTH_SHORT).show()

                            val resultIntent = Intent().apply {
                                putExtra("deleted_id", id)
                            }
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Gagal menghapus: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
