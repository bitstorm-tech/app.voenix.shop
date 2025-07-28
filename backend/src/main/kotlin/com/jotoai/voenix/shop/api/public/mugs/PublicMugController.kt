package com.jotoai.voenix.shop.api.public.mugs

import com.jotoai.voenix.shop.domain.articles.dto.PublicMugDto
import com.jotoai.voenix.shop.domain.articles.service.ArticleService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/mugs")
class PublicMugController(
    private val articleService: ArticleService,
) {
    @GetMapping
    fun getAllMugs(): List<PublicMugDto> = articleService.findPublicMugs()
}
