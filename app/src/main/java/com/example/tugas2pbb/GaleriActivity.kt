package com.example.tugas2pbb

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tugas2pbb.adapter.GaleriAdapter
import com.example.tugas2pbb.util.FileUtils
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.InputStream

class GaleriActivity : AppCompatActivity() {

    private val detailLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ){ result ->
        if(result.resultCode == Activity.RESULT_OK){

            val deletedId = result.data?.getStringExtra("deleted_id")
            if(deletedId != null){
                imageList.removeAll { it["id"] == deletedId }
                adapter.notifyDataSetChanged()
                return@registerForActivityResult
            }

            val updateId = result.data?.getStringExtra("updated_id")
            val newTitle = result.data?.getStringExtra("updated_title")
            if(updateId != null && newTitle != null){
                imageList.find { it["id"] == updateId }?.let {
                    (it as MutableMap)["judul"] = newTitle
                }
                adapter.notifyDataSetChanged()
            }

        }
    }

    companion object {
        private const val TAG = "GaleriActivity"
        private const val PICK_IMAGE = 100
        private const val CAPTURE_IMAGE = 101
        private const val CAMERA_PERMISSION_CODE = 200
    }

    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: GaleriAdapter
    private val imageList = mutableListOf<Map<String, String>>()

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var menuButton: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnTambah: Button
    private lateinit var progressBar: ProgressBar

    private var photoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_galeri)

        auth = FirebaseAuth.getInstance()
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        menuButton = findViewById(R.id.menuButton)
        recyclerView = findViewById(R.id.rvGaleri)
        btnTambah = findViewById(R.id.btnTambahGambar)
        progressBar = findViewById(R.id.progressBar)

        setupDrawer()

        recyclerView.layoutManager = GridLayoutManager(this, 3)
        adapter = GaleriAdapter(
            this,
            imageList,
            onClick = { data -> openDetail(data) },           // single click
            onDoubleClick = { data -> openEditDialog(data) }, // double click → edit
            onLongClick = { data -> confirmDelete(data) }     // long click → delete
        )
        recyclerView.adapter = adapter

        btnTambah.setOnClickListener { showImageSourceDialog() }

        loadImagesFromFirestore()
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Ambil dari Kamera", "Pilih dari Galeri")
        AlertDialog.Builder(this)
            .setTitle("Pilih Sumber Gambar")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }.show()
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
    }

    private fun openCamera() {
        if (!hasCameraPermission()) {
            requestCameraPermission()
            return
        }

        try {
            val imageFile = File.createTempFile("IMG_", ".jpg", cacheDir)
            photoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", imageFile)

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivityForResult(intent, CAPTURE_IMAGE)
        } catch (e: Exception) {
            Toast.makeText(this, "Kamera gagal dibuka: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Error membuka kamera", e)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            PICK_IMAGE -> data?.data?.let { uri -> showTitleDialog(uri) }
            CAPTURE_IMAGE -> photoUri?.let { uri -> showTitleDialog(uri) }
        }
    }

    private fun showTitleDialog(imageUri: Uri) {
        val input = EditText(this)
        input.hint = "Masukkan judul gambar"

        AlertDialog.Builder(this)
            .setTitle("Judul Gambar")
            .setView(input)
            .setPositiveButton("Simpan") { _, _ ->
                val title = input.text.toString().ifEmpty { "Tanpa Judul" }
                saveImageLocallyAndSendToFirestore(imageUri, title)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun saveImageLocallyAndSendToFirestore(imageUri: Uri, title: String) {
        progressBar.visibility = android.view.View.VISIBLE
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
            val bitmap = inputStream?.use { BitmapFactory.decodeStream(it) }

            if (bitmap == null) {
                progressBar.visibility = android.view.View.GONE
                Toast.makeText(this, "Gagal membaca gambar", Toast.LENGTH_SHORT).show()
                return
            }

            val fileName = FileUtils.saveImageToInternalStorage(this, bitmap)
            if (fileName.isNullOrBlank()) {
                progressBar.visibility = android.view.View.GONE
                Toast.makeText(this, "Gagal menyimpan file", Toast.LENGTH_SHORT).show()
                return
            }

            val imageData = hashMapOf(
                "namaFile" to fileName,
                "judul" to title,
                "tanggalUpload" to Timestamp.now()
            )

            db.collection("image").add(imageData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Gambar tersimpan", Toast.LENGTH_SHORT).show()
                    loadImagesFromFirestore()
                }
                .addOnFailureListener {
                    progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this, "Gagal simpan ke Firestore", Toast.LENGTH_SHORT).show()
                }

        } catch (e: Exception) {
            progressBar.visibility = android.view.View.GONE
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadImagesFromFirestore() {
        progressBar.visibility = android.view.View.VISIBLE
        db.collection("image")
            .orderBy("tanggalUpload", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                imageList.clear()
                for (doc in result) {
                    val namaFile = doc.getString("namaFile") ?: continue
                    val judul = doc.getString("judul") ?: "Tanpa Judul"
                    val tanggalUpload = when (val raw = doc.get("tanggalUpload")) {
                        is Timestamp -> android.text.format.DateFormat.format("dd MMM yyyy • HH:mm", raw.toDate()).toString()
                        is String -> raw
                        else -> "-"
                    }
                    imageList.add(
                        mapOf(
                            "id" to doc.id,
                            "namaFile" to namaFile,
                            "judul" to judul,
                            "tanggalUpload" to tanggalUpload
                        )
                    )
                }
                adapter.notifyDataSetChanged()
                progressBar.visibility = android.view.View.GONE
            }
            .addOnFailureListener {
                progressBar.visibility = android.view.View.GONE
                Toast.makeText(this, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openEditDialog(data: Map<String, String>) {
        val input = EditText(this)
        input.setText(data["judul"])

        AlertDialog.Builder(this)
            .setTitle("Edit Judul Gambar")
            .setView(input)
            .setPositiveButton("Simpan") { _, _ ->
                val newTitle = input.text.toString().ifEmpty { "Tanpa Judul" }
                val id = data["id"]!!

                db.collection("image").document(id)
                    .update(
                        mapOf(
                            "judul" to newTitle,
                            "tanggalUpload" to Timestamp.now()
                        )
                    ).addOnSuccessListener {
                        Toast.makeText(this, "Berhasil diupdate", Toast.LENGTH_SHORT).show()
                        loadImagesFromFirestore()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun confirmDelete(data: Map<String, String>) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Gambar?")
            .setMessage("Gambar akan dihapus permanen.")
            .setPositiveButton("Hapus") { _, _ ->
                val id = data["id"]!!
                val namaFile = data["namaFile"]

                db.collection("image").document(id).delete()
                    .addOnSuccessListener {
                        try {
                            val file = FileUtils.getImageFile(this, namaFile!!)
                            if (file.exists()) file.delete()
                        } catch (_: Exception) {}

                        // remove dari recycler list langsung
                        imageList.removeAll { it["id"] == id }
                        adapter.notifyDataSetChanged()

                        Toast.makeText(this, "Terhapus", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun setupDrawer() {
        try {
            val headerView = navigationView.getHeaderView(0)
            val user = auth.currentUser

            val nameView = headerView.findViewById<TextView>(R.id.profile_name)
            val emailView = headerView.findViewById<TextView>(R.id.profile_email)
            val imageView = headerView.findViewById<ImageView>(R.id.header_image)

            nameView.text = user?.displayName ?: "Pengguna"
            emailView.text = user?.email ?: "email@tidak.ada"

            // === load foto profil google ===
            user?.photoUrl?.let { url ->
                Glide.with(this)
                    .load(url)
                    .circleCrop()
                    .into(imageView)
            }

            navigationView.setNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.nav_home -> {
                        startActivity(Intent(this, TodoActivity::class.java))
                        finish()
                    }
                    R.id.nav_galeri -> drawerLayout.closeDrawer(GravityCompat.START)
                }
                true
            }

            menuButton.setOnClickListener {
                if (drawerLayout.isDrawerOpen(GravityCompat.START))
                    drawerLayout.closeDrawer(GravityCompat.START)
                else
                    drawerLayout.openDrawer(GravityCompat.START)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Navigation setup gagal: ${e.message}")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Izin kamera diperlukan untuk mengambil gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openDetail(data: Map<String, String>) {
        val intent = Intent(this, DetailImageActivity::class.java)
        intent.putExtra("id", data["id"])
        intent.putExtra("namaFile", data["namaFile"])
        intent.putExtra("judul", data["judul"])
        intent.putExtra("tanggalUpload", data["tanggalUpload"])

        detailLauncher.launch(intent)
    }
}
