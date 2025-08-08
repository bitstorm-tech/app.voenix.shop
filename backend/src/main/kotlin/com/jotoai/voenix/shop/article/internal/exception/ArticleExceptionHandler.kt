package com.jotoai.voenix.shop.article.internal.exception

import com.jotoai.voenix.shop.article.api.exception.ArticleNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

data class ArticleErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
)

@RestControllerAdvice
class ArticleExceptionHandler {
    @ExceptionHandler(ArticleNotFoundException::class)
    fun handleArticleNotFoundException(ex: ArticleNotFoundException): ResponseEntity<ArticleErrorResponse> {
        val errorResponse =
            ArticleErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.NOT_FOUND.value(),
                error = "Article Not Found",
                message = ex.message ?: "Article not found",
                path = "",
            )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }
}
