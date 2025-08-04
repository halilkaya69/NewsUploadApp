package com.halilkaya.newsuploadapp.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.halilkaya.newsuploadapp.model.Article
import com.halilkaya.newsuploadapp.model.ArticleStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.util.UUID
import kotlin.random.Random

class ArticleViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()





    private val _pendingArticles = MutableStateFlow<List<Article>>(emptyList())
    val pendingArticles = _pendingArticles.asStateFlow()

    // Loading state'leri
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting = _isSubmitting.asStateFlow()

    // Image upload state'leri
    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage = _isUploadingImage.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress = _uploadProgress.asStateFlow()

    //private val _uploadedImageUrl = MutableStateFlow<String?>(null)
    //val uploadedImageUrl = _uploadedImageUrl.asStateFlow()

    // Error state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // Success message
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    init {

    }

    // Yeni makale oluştur
    fun saveArticle(
        title: String,
        description: String,
        content: String,
        urlToImage: String?,
        isDraft: Boolean = true,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            onFailure("User not authenticated")
            return
        }

        _isSubmitting.value = true
        _errorMessage.value = null

        val article = Article(
            id = Random.nextInt(100000, 999999), // Rastgele 6 haneli ID
            author = auth.currentUser?.displayName ?: auth.currentUser?.email ?: "Unknown",
            title = title,
            description = description,
            content = content,
            url = "https://www.lipsum.com/", // Default URL
            urlToImage = urlToImage,
            status = if (isDraft) ArticleStatus.DRAFT else ArticleStatus.PENDING,
            publishedAt = Timestamp.now(),
            updatedAt = Timestamp.now(),
            moderatorId = null,
            rejectionReason = null,

        )

        db.collection("articles")
            .add(article)
            .addOnSuccessListener { documentReference ->
                _isSubmitting.value = false
                _successMessage.value = if (isDraft) "Article saved as draft" else "Article submitted for review"
                onSuccess()
            }
            .addOnFailureListener { exception ->
                _isSubmitting.value = false
                val errorMsg = exception.message ?: "Failed to save article"
                _errorMessage.value = errorMsg
                onFailure(errorMsg)
            }
    }


    // Resim yükleme fonksiyonu
    fun uploadImage(
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            onFailure("User not authenticated")
            return
        }

        _isUploadingImage.value = true
        _uploadProgress.value = 0f
        _errorMessage.value = null

        // Benzersiz dosya adı oluştur
        val fileName = "article_images/${currentUserId}/${UUID.randomUUID()}.jpg"
        val imageRef = storage.reference.child(fileName)

        val uploadTask = imageRef.putFile(imageUri)

        // Upload progress'i takip et
        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toFloat()
            _uploadProgress.value = progress / 100f
        }

        // Upload tamamlandığında download URL'i al
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            imageRef.downloadUrl
        }.addOnCompleteListener { task ->
            _isUploadingImage.value = false
            _uploadProgress.value = 0f

            if (task.isSuccessful) {
                val downloadUrl = task.result.toString()
                //_uploadedImageUrl.value = downloadUrl
                _successMessage.value = "Image uploaded successfully"
                onSuccess(downloadUrl)
            } else {
                val errorMsg = task.exception?.message ?: "Failed to upload image"
                _errorMessage.value = errorMsg
                onFailure(errorMsg)
            }
        }
    }


    // Resim ile birlikte makale kaydet
    fun saveArticleWithImage(
        title: String,
        description: String,
        content: String,
        imageUri: Uri?,
        isDraft: Boolean = true,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        if (imageUri != null) {
            // Önce resmi yükle
            uploadImage(
                imageUri = imageUri,
                onSuccess = { imageUrl ->
                    // Resim yüklendikten sonra makaleyi kaydet
                    saveArticle(
                        title = title,
                        description = description,
                        content = content,
                        urlToImage = imageUrl,
                        isDraft = isDraft,
                        onSuccess = onSuccess,
                        onFailure = onFailure
                    )
                },
                onFailure = { error ->
                    onFailure("Image upload failed: $error")
                }
            )
        } else {
            // Resim yoksa direkt makaleyi kaydet
            saveArticle(
                title = title,
                description = description,
                content = content,
                urlToImage = null,
                isDraft = isDraft,
                onSuccess = onSuccess,
                onFailure = onFailure
            )
        }
    }




    // Makaleyi onayla (Moderator için)
    fun approveArticle(articleId: Int) {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("articles")
            .whereEqualTo("id", articleId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val article = document.toObject(Article::class.java)
                    
                    article?.let { approvedArticle ->
                        // 1. News collection'ına taşı (diğer uygulamanın okuması için)
                        val newsArticle = approvedArticle.copy(
                            status = ArticleStatus.APPROVED,
                            moderatorId = currentUserId,
                            updatedAt = Timestamp.now()
                        )
                        
                        db.collection("news")
                            .add(newsArticle)
                            .addOnSuccessListener {
                                // 2. Articles collection'ından sil (artık news'te olduğu için)
                                document.reference.delete()
                                    .addOnSuccessListener {
                                        _successMessage.value = "Article approved and moved to news"
                                        Log.d("ArticleViewModel", "Article moved from articles to news collection")
                                        getPendingArticles()
                                    }
                                    .addOnFailureListener { deleteException ->
                                        Log.e("ArticleViewModel", "Failed to delete from articles collection", deleteException)
                                        _successMessage.value = "Article approved but failed to remove from articles"
                                        getPendingArticles()
                                    }
                            }
                            .addOnFailureListener { newsException ->
                                Log.e("ArticleViewModel", "Failed to add to news collection", newsException)
                                _errorMessage.value = "Failed to approve article: ${newsException.message}"
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                _errorMessage.value = exception.message ?: "Failed to find article"
            }
    }

    // Makaleyi reddet (Moderator için)
    fun rejectArticle(articleId: Int, reason: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        
        val updates = mapOf(
            "status" to ArticleStatus.REJECTED,
            "moderatorId" to currentUserId,
            "rejectionReason" to reason,
            "updatedAt" to Timestamp.now()
        )

        db.collection("articles")
            .whereEqualTo("id", articleId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    document.reference.update(updates)
                        .addOnSuccessListener {
                            _successMessage.value = "Article rejected"
                            getPendingArticles()
                        }
                        .addOnFailureListener { exception ->
                            _errorMessage.value = exception.message ?: "Failed to reject article"
                        }
                }
            }
    }




    // Onay bekleyen makaleleri getir (Moderator için)
    fun getPendingArticles() {
        _isLoading.value = true
        _errorMessage.value = null

        db.collection("articles")
            .whereEqualTo("status", ArticleStatus.PENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val articleList = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(Article::class.java)
                }.sortedByDescending { it.publishedAt } // Client-side sıralama
                _pendingArticles.value = articleList
                _isLoading.value = false
            }
            .addOnFailureListener { exception ->
                _isLoading.value = false
                _errorMessage.value = exception.message ?: "Failed to load pending articles"
            }
    }

    // Pending articles'ı yenile
    fun refreshPendingArticles() {
        getPendingArticles()
    }









    // Error ve success mesajlarını temizle
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }



   /*fun fetchRssAndSaveToFirestore() {
        //val firestore = Firebase.firestore
        val url = "https://www.aa.com.tr/tr/rss/default?cat=guncel"

        Thread {
            try {
                val doc = Jsoup.connect(url).userAgent("Mozilla").get()
                val items = doc.select("item")

                for (item in items) {
                    val title = item.selectFirst("title")?.text()
                    val link = item.selectFirst("link")?.text()
                    val description = item.selectFirst("description")?.text()
                    val pubDate = item.selectFirst("pubDate")?.text()
                    val image = item.selectFirst("image")?.text() // image varsa al

                    val newsData = hashMapOf(
                        "title" to (title ?: ""),
                        "link" to (link ?: ""),
                        "description" to (description ?: ""),
                        "published" to (pubDate ?: ""),
                        "image" to (image ?: "")
                    )

                    db.collection("haberler3")
                        .add(newsData)
                        .addOnSuccessListener {
                            Log.d("RSS", "Başarıyla eklendi: $title")
                        }
                        .addOnFailureListener {
                            Log.e("RSS", "Hata oluştu: ${it.message}")
                        }
                }
            } catch (e: Exception) {
                Log.e("RSS", "Veri çekme hatası: ${e.message}")
            }
        }.start()
    }*/

    fun fetchRssAndSaveToFirestore(category: String) {
        val url = when (category) {
            "Gündem" -> "https://www.aa.com.tr/tr/rss/default?cat=gundem"
            "Politika" -> "https://www.aa.com.tr/tr/rss/default?cat=politika"
            "Dünya" -> "https://www.aa.com.tr/tr/rss/default?cat=dunya"
            "Sağlık" -> "https://www.aa.com.tr/tr/rss/default?cat=saglik"
            "Ekonomi" -> "https://www.aa.com.tr/tr/rss/default?cat=ekonomi"
            "Eğitim" -> "https://www.aa.com.tr/tr/rss/default?cat=egitim"
            "Spor" -> "https://www.aa.com.tr/tr/rss/default?cat=spor"
            "Futbol" -> "https://www.aa.com.tr/tr/rss/default?cat=futbol"
            "Basketbol" -> "https://www.aa.com.tr/tr/rss/default?cat=basketbol"
            "Kültür" -> "https://www.aa.com.tr/tr/rss/default?cat=kultur"
            "Kurumsal Haberler" -> "https://www.aa.com.tr/tr/rss/default?cat=kurumsal-haberler"
            "Yaşam" -> "https://www.aa.com.tr/tr/rss/default?cat=yasam"
            "Dünyadan Spor" -> "https://www.aa.com.tr/tr/rss/default?cat=dunyadan-spor"
            "Analiz" -> "https://www.aa.com.tr/tr/rss/default?cat=analiz"
            else -> return
        }
        Thread {
            try {
                val doc = Jsoup.connect(url).userAgent("Mozilla").get()
                val items = doc.select("item")

                for (item in items) {
                    val title = item.selectFirst("title")?.text()?.trim() ?: continue
                    val link = item.selectFirst("link")?.text()?.trim()
                    val description = item.selectFirst("description")?.text()?.trim()
                    val pubDate = item.selectFirst("pubDate")?.text()?.trim()
                    val imageUrl = item.selectFirst("image")?.text()?.trim()

                    val article = Article(
                        id = Random.nextInt(100000, 999999),
                        author = "RSS_FEED",
                        title = title,
                        description = description ?: "",
                        content = description ?: "", // İstersen burada farklı veri kaynağı kullanabilirsin
                        url = link ?: "",
                        urlToImage = imageUrl,
                        status = ArticleStatus.PENDING,
                        publishedAt = Timestamp.now(),
                        updatedAt = Timestamp.now(),
                        moderatorId = null,
                        rejectionReason = null
                    )

                    db.collection("articles")
                        .add(article)
                        .addOnSuccessListener {
                            Log.d("RSS", "Makale eklendi: $title")
                        }
                        .addOnFailureListener {
                            Log.e("RSS", "Makale eklenemedi: ${it.message}")
                        }
                }

            } catch (e: Exception) {
                Log.e("RSS", "RSS çekme hatası: ${e.message}")
            }
        }.start()
    }



}
