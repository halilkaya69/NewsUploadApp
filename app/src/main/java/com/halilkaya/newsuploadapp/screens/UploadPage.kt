package com.halilkaya.newsuploadapp.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.halilkaya.newsuploadapp.AppUtil
import com.halilkaya.newsuploadapp.R
import com.halilkaya.newsuploadapp.Routes
import com.halilkaya.newsuploadapp.UserRole
import com.halilkaya.newsuploadapp.components.DrawerLayout
import com.halilkaya.newsuploadapp.components.ImagePicker
import com.halilkaya.newsuploadapp.model.Article
import com.halilkaya.newsuploadapp.viewmodel.ArticleViewModel
import com.halilkaya.newsuploadapp.viewmodel.AuthState
import com.halilkaya.newsuploadapp.viewmodel.AuthViewModel
import com.halilkaya.newsuploadapp.viewmodel.LanguageViewModel
import com.halilkaya.newsuploadapp.viewmodel.RoleViewModel
import com.halilkaya.newsuploadapp.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    roleViewModel: RoleViewModel,
    articleViewModel: ArticleViewModel ,
    languageViewModel:LanguageViewModel
) {


    val user by userViewModel.user.collectAsState()
    val userRole = user.role

    val authState = authViewModel.authState.observeAsState()
    val context= LocalContext.current
    val currentLocale = languageViewModel.getCurrentLanguageCode()

    // ProfilePage her açıldığında fresh user data çek
    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Authenticated) {
            userViewModel.getUserProfile()
        }
    }


    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Unauthenticated -> navController.navigate(Routes.login)
            else -> Unit
        }
    }


    DrawerLayout(
        navController = navController,
        authViewModel = authViewModel,
        userViewModel = userViewModel,languageViewModel,
        title = AppUtil.getString(context, R.string.upload_page,currentLocale)
    ) { paddingValues ->
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (userRole) {
            UserRole.USER -> {
                // Sıradan kullanıcı - İzin yok
                NoPermissionView(languageViewModel)
            }
            UserRole.WRITER,UserRole.EDITOR, UserRole.ADMIN  -> {
                // Writer - Makale yükleme formu
                ArticleUploadView(articleViewModel = articleViewModel,languageViewModel)
            }
            /*UserRole.EDITOR, UserRole.ADMIN -> {
                // Moderator/Admin - Onay bekleyen makaleler
                PendingArticlesView(articleViewModel = articleViewModel,languageViewModel)
            }*/
        }
    }}
}

@Composable
fun NoPermissionView(languageViewModel: LanguageViewModel) {
    val context= LocalContext.current
    val currentLocale = languageViewModel.getCurrentLanguageCode()
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Block,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = AppUtil.getString(context, R.string.no_permission,currentLocale),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = AppUtil.getString(context, R.string.permission_required,currentLocale),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ArticleUploadView(articleViewModel: ArticleViewModel, languageViewModel: LanguageViewModel) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val isSubmitting by articleViewModel.isSubmitting.collectAsState()
    val isUploadingImage by articleViewModel.isUploadingImage.collectAsState()
    val uploadProgress by articleViewModel.uploadProgress.collectAsState()
    val errorMessage by articleViewModel.errorMessage.collectAsState()
    val successMessage by articleViewModel.successMessage.collectAsState()

    val context= LocalContext.current
    val currentLocale = languageViewModel.getCurrentLanguageCode()

    // Success message handling
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            // Reset form
            title = ""
            description = ""
            content = ""
            selectedImageUri = null
            articleViewModel.clearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = AppUtil.getString(context, R.string.new_article_upload,currentLocale),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        // Title field
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(AppUtil.getString(context, R.string.title,currentLocale)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        // Description field
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(AppUtil.getString(context, R.string.description,currentLocale)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )
        
        // Image picker
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = AppUtil.getString(context, R.string.article_image,currentLocale),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                ImagePicker(
                    selectedImageUri = selectedImageUri,
                    onImageSelected = { uri -> selectedImageUri = uri }
                )
                
                // Upload progress
                if (isUploadingImage) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = uploadProgress,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = AppUtil.getString(context, R.string.image_uploading,currentLocale)+" ${(uploadProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        // Content field
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text(AppUtil.getString(context, R.string.content,currentLocale)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 10,
            maxLines = 20
        )
        
        // Success message
        successMessage?.let { success ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.Green.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = success,
                    modifier = Modifier.padding(12.dp),
                    color = Color.Green,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // Error message
        errorMessage?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Save as draft button
            OutlinedButton(
                onClick = {
                    articleViewModel.saveArticleWithImage(
                        title = title,
                        description = description,
                        content = content,
                        imageUri = selectedImageUri,
                        isDraft = true
                    )
                },
                modifier = Modifier.weight(1f),
                enabled = !isSubmitting && !isUploadingImage && title.isNotBlank()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(AppUtil.getString(context, R.string.save_draft,currentLocale))
                }
            }
            
            // Submit for review button
            Button(
                onClick = {
                    articleViewModel.saveArticleWithImage(
                        title = title,
                        description = description,
                        content = content,
                        imageUri = selectedImageUri,
                        isDraft = false
                    )
                },
                modifier = Modifier.weight(1f),
                enabled = !isSubmitting && !isUploadingImage && 
                         title.isNotBlank() && description.isNotBlank() && content.isNotBlank()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(AppUtil.getString(context, R.string.submit_for_review,currentLocale))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun PendingArticlesView(articleViewModel: ArticleViewModel, languageViewModel: LanguageViewModel) {
    val pendingArticles by articleViewModel.pendingArticles.collectAsState()
    val isLoading by articleViewModel.isLoading.collectAsState()
    val successMessage by articleViewModel.successMessage.collectAsState()
    val errorMessage by articleViewModel.errorMessage.collectAsState()

    val context= LocalContext.current
    val currentLocale = languageViewModel.getCurrentLanguageCode()
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        articleViewModel.getPendingArticles()
    }
    
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            articleViewModel.clearMessages()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = AppUtil.getString(context, R.string.pending_articles,currentLocale),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            /*Button(onClick = {
                articleViewModel.fetchRssAndSaveToFirestore()
            }) {
                Text("Veriyi Firebase'e Kaydet")
            }*/

            /*IconButton(
                onClick = { articleViewModel.fetchRssAndSaveToFirestore() },
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = AppUtil.getString(context, R.string.refresh,currentLocale),
                    tint = MaterialTheme.colorScheme.primary
                )
            }*/
            CategoryPickerDialog(
                showDialog = showDialog,
                onDismiss = { showDialog = false },
                onCategorySelected = { category ->
                    showDialog = false
                    articleViewModel.fetchRssAndSaveToFirestore(category)
                }
            )

            IconButton(
                onClick = { showDialog = true },
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = AppUtil.getString(context, R.string.refresh, currentLocale),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            IconButton(
                onClick = { articleViewModel.getPendingArticles() },
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = AppUtil.getString(context, R.string.refresh,currentLocale),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Success message
        successMessage?.let { success ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.Green.copy(alpha = 0.1f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = success,
                    modifier = Modifier.padding(12.dp),
                    color = Color.Green,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // Error message
        errorMessage?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (pendingArticles.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text =AppUtil.getString(context, R.string.no_pending_articles,currentLocale),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text =AppUtil.getString(context, R.string.new_articles_will_appear,currentLocale),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pendingArticles) { article ->
                    PendingArticleCard(
                        article = article,languageViewModel,
                        onApprove = { articleId -> 
                            articleViewModel.approveArticle(articleId)
                        },
                        onReject = { articleId, reason ->
                            articleViewModel.rejectArticle(articleId, reason)
                        }
                    )
                }
                
                // Debug bilgisi
                /*item {
                    if (pendingArticles.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "Debug Bilgisi",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Toplam PENDING makale: ${pendingArticles.size}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                pendingArticles.firstOrNull()?.let { article ->
                                    Text(
                                        text = "İlk makale ID: ${article.id}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Status: ${article.status}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }*/
            }
        }
    }
}

@Composable
fun PendingArticleCard(
    article: Article,
    languageViewModel: LanguageViewModel,
    onApprove: (Int) -> Unit,
    onReject: (Int, String) -> Unit
) {
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectionReason by remember { mutableStateOf("") }

    val context= LocalContext.current
    val currentLocale = languageViewModel.getCurrentLanguageCode()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = article.title ?: "Başlıksız",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = AppUtil.getString(context, R.string.author,currentLocale)+" ${article.author}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = article.description ?: "Açıklama yok",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showRejectDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(AppUtil.getString(context, R.string.reject,currentLocale))
                }
                
                Button(
                    onClick = { 
                        article.id?.let { onApprove(it) }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(AppUtil.getString(context, R.string.approve,currentLocale))
                }
            }
        }
    }
    
    // Reject dialog
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text(AppUtil.getString(context, R.string.reject_article,currentLocale)) },
            text = {
                Column {
                    Text(AppUtil.getString(context, R.string.rejection_reason,currentLocale))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        label = { Text(AppUtil.getString(context, R.string.rejection_reason_hint,currentLocale)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rejectionReason.isNotBlank()) {
                            article.id?.let { onReject(it, rejectionReason) }
                            showRejectDialog = false
                            rejectionReason = ""
                        }
                    },
                    enabled = rejectionReason.isNotBlank()
                ) {
                    Text(AppUtil.getString(context, R.string.reject,currentLocale))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showRejectDialog = false
                        rejectionReason = ""
                    }
                ) {
                    Text(AppUtil.getString(context, R.string.cancel,currentLocale))
                }
            }
        )
    }
}

@Composable
fun CategoryPickerDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onCategorySelected: (String) -> Unit
) {
    if (showDialog) {
        val categories = listOf(
            "Gündem",
            "Politika",
            "Dünya",
            "Sağlık",
            "Ekonomi",
            "Eğitim",
            "Spor",
            "Futbol",
            "Basketbol",
            "Kültür",
            "Kurumsal Haberler",
            "Yaşam",
            "Dünyadan Spor",
            "Analiz"
        )
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Kategori Seçin") },
            text = {
                Column {
                    categories.forEach { category ->
                        TextButton(onClick = {
                            onCategorySelected(category)
                        }) {
                            Text(text = category)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}
