package com.example.tugas2pbb

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class GaleriActivity : AppCompatActivity() {

    private val PICK_IMAGE = 100
    private val storageRef = FirebaseStorage.getInstance().reference
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: GaleriAdapter
    private val imageList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_galeri)

        val recyclerView = findViewById<RecyclerView>(R.id.rvGaleri)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        adapter = GaleriAdapter(imageList)
        recyclerView.adapter = adapter

        val btnTambah = findViewById<Button>(R.id.btnTambahGambar)
        btnTambah.setOnClickListener { openGallery() }

        loadImagesFromFirestore()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uploadImageToFirestore(it) }
        }
    }

    private fun uploadImageToFirestore(imageUri: Uri) {
        val filename = "image_${System.currentTimeMillis()}"
        val imageRef = storageRef.child("images/$filename.jpg")

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageData = hashMapOf(
                        "title" to "Foto Baru",
                        "tanggalUpload" to System.currentTimeMillis(),
                        "imageUrl" to uri.toString()
                    )
                    db.collection("image")
                        .add(imageData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Gambar berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                            imageList.add(uri.toString())
                            adapter.notifyDataSetChanged()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal upload gambar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadImagesFromFirestore() {
        db.collection("image")
            .get()
            .addOnSuccessListener { result ->
                imageList.clear()
                for (doc in result) {
                    doc.getString("imageUrl")?.let { imageList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }
    }
}
