package com.halilkaya.newsuploadapp.components

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import java.io.File

@Composable
fun ImagePicker(
    selectedImageUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
    modifier: Modifier = Modifier,
    showDeleteButton: Boolean = true,
    imageHeight: Int = 200,
    imageWidth: Int = 300
) {
    val context = LocalContext.current
    var showImageSourceDialog by remember { mutableStateOf(false) }
    
    // Determine the permission based on the Android version
    val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    
    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onImageSelected(uri)
    }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (!success) {
            onImageSelected(null)
        }
    }
    
    // Create temporary file for camera
    val tempUri = remember {
        val tempFile = File.createTempFile("temp_image", ".jpg", context.cacheDir)
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            tempFile
        )
    }
    
    // Permission launchers
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            galleryLauncher.launch("image/*") // Open gallery if permission is granted
        } else {
            Toast.makeText(context, "Galeri izni reddedildi", Toast.LENGTH_SHORT).show()
        }
    }
    
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            onImageSelected(tempUri) // Set temp URI first
            cameraLauncher.launch(tempUri)
        } else {
            Toast.makeText(context, "Kamera izni reddedildi", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Image display area
        Box(
            modifier = Modifier
                .size(width = imageWidth.dp, height = imageHeight.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 2.dp,
                    color = if (selectedImageUri != null) Color.Transparent else MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { showImageSourceDialog = true },
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                // Show selected image
                Image(
                    painter = rememberAsyncImagePainter(selectedImageUri),
                    contentDescription = "Selected image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Delete button
                if (showDeleteButton) {
                    IconButton(
                        onClick = { onImageSelected(null) },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete image",
                            tint = Color.White,
                            modifier = Modifier
                                .size(24.dp)
                        )
                    }
                }
            } else {
                // Show placeholder
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add image",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Resim Ekle",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Add image button
        OutlinedButton(
            onClick = { showImageSourceDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Photo,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (selectedImageUri != null) "Resmi Değiştir" else "Resim Seç",
                fontWeight = FontWeight.Medium
            )
        }
    }
    
    // Image source selection dialog
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = {
                Text(
                    text = "Resim Kaynağı Seçin",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column {
                    // Gallery option
                    TextButton(
                        onClick = {
                            showImageSourceDialog = false
                            // Check if permission is already granted, else request permission
                            if (ContextCompat.checkSelfPermission(context, storagePermission) == PackageManager.PERMISSION_GRANTED) {
                                galleryLauncher.launch("image/*") // Launch gallery if permission already granted
                            } else {
                                storagePermissionLauncher.launch(storagePermission) // Request permission
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Photo,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Galeriden Seç",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    // Camera option
                    TextButton(
                        onClick = {
                            showImageSourceDialog = false
                            // Check if camera permission is already granted, else request permission
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                onImageSelected(tempUri) // Set temp URI first
                                cameraLauncher.launch(tempUri)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA) // Request permission
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Kamera ile Çek",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showImageSourceDialog = false }
                ) {
                    Text("İptal")
                }
            }
        )
    }
}
