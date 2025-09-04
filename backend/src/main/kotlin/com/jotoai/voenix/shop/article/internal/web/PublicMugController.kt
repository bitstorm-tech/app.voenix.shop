package com.jotoai.voenix.shop.article.internal.web

import com.jotoai.voenix.shop.article.api.dto.PublicMugDto
import com.jotoai.voenix.shop.article.internal.service.ArticleServiceImpl
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/mugs")
class PublicMugController(
    private val articleService: ArticleServiceImpl,
) {
    @GetMapping
    fun getAllMugs(): List<PublicMugDto> = articleService.findPublicMugs()
}
