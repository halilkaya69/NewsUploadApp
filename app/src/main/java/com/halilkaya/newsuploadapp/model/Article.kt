package com.halilkaya.newsuploadapp.model

import com.google.firebase.Timestamp


data class Article(
    val id: Int? = null,
    val author: String? = "Unknown author",
    val title: String? = "Unknown title",
    val description: String? = "Unknown description", 
    val url: String? = "https://www.lipsum.com/",
    val urlToImage: String? = "https://i.imgur.com/GSTYABU.jpeg",
    val content: String? = "Unknown content",
    val status: ArticleStatus = ArticleStatus.DRAFT, // Yönetim için gerekli
    val publishedAt: Timestamp? = null,
    val updatedAt: Timestamp? = null, // Son güncelleme
    val moderatorId: String? = null, // Onaylayan/reddeden moderator
    val rejectionReason: String? = null, // Reddedilme sebebi
)

enum class ArticleStatus {
    DRAFT,        // Taslak
    PENDING,      // Onay bekliyor
    APPROVED,     // Onaylandı
    REJECTED,     // Reddedildi
}
