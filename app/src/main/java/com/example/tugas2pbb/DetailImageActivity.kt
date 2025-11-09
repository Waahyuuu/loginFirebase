package com.example.tugas2pbb

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.tugas2pbb.util.FileUtils
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class DetailImageActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var textJudul: TextView
    private lateinit var textTanggal: TextView
    private lateinit var btnEdit: ImageView
    private lateinit var btnDelete: ImageView
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_image)

        imageView = findViewById(R.id.detailImage)
        textJudul = findViewById(R.id.detailJudul)
        textTanggal = findViewById(R.id.detailTanggal)
        btnEdit = findViewById(R.id.btnEdit)
        btnDelete = findViewById(R.id.btnDelete)

        val id = intent.getStringExtra("id")
        val namaFile = intent.getStringExtra("namaFile")
        val judul = intent.getStringExtra("judul")
        val tanggal = intent.getStringExtra("tanggalUpload")

        textJudul.text = judul
        textTanggal.text = tanggal

        // load gambar dari internal storage
        namaFile?.let {
            val file: File = FileUtils.getImageFile(this, it)
            if (file.exists()) {
                val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                imageView.setImageBitmap(bitmap)
            }
        }

        btnEdit.setOnClickListener {
            val editText = android.widget.EditText(this)
            editText.setText(judul)

            AlertDialog.Builder(this)
                .setTitle("Edit Judul")
                .setView(editText)
                .setPositiveButton("Simpan") { _, _ ->
                    val newJudul = editText.text.toString()
                    if (id != null) {
                        FirebaseFirestore.getInstance()
                            .collection("image")
                            .document(id)
                            .update("judul", newJudul)
                            .addOnSuccessListener {
                                textJudul.text = newJudul
                                Toast.makeText(this, "Judul diperbarui", Toast.LENGTH_SHORT).show()

                                // KIRIM RESULT (penting)
                                val i = Intent()
                                i.putExtra("updated_id", id)
                                i.putExtra("updated_title", newJudul)
                                setResult(Activity.RESULT_OK, i)
                            }
                    }
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Hapus Gambar?")
                .setMessage("Yakin hapus gambar?")
                .setPositiveButton("Hapus") { _, _ ->
                    if (id != null) {
                        FirebaseFirestore.getInstance()
                            .collection("image")
                            .document(id)
                            .delete().addOnSuccessListener {
                                if (namaFile != null) {
                                    val file = FileUtils.getImageFile(this, namaFile)
                                    if (file.exists()) file.delete()
                                }
                                Toast.makeText(this, "Terhapus", Toast.LENGTH_SHORT).show()

                                val intent = Intent()
                                intent.putExtra("deleted_id", id)
                                setResult(Activity.RESULT_OK, intent)
                                finish()
                            }
                    }
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }

    }
}
