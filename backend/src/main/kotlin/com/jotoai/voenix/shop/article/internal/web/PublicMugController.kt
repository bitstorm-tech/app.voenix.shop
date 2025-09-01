package com.jotoai.voenix.shop.article.internal.web

import com.jotoai.voenix.shop.article.api.ArticleQueryService
import com.jotoai.voenix.shop.article.api.dto.PublicMugDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/mugs")
class PublicMugController(
    private val articleQueryService: ArticleQueryService,
) {
    @GetMapping
    fun getAllMugs(): List<PublicMugDto> = articleQueryService.findPublicMugs()
}
