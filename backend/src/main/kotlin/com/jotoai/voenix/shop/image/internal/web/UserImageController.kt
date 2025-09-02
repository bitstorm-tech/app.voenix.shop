package com.jotoai.voenix.shop.image.internal.web

import com.jotoai.voenix.shop.image.ImageService
import com.jotoai.voenix.shop.image.UserImagesFilter
import com.jotoai.voenix.shop.user.UserService
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user/images")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
class UserImageController(
    private val imageService: ImageService,
    private val userService: UserService,
) {
    @GetMapping("/{filename}")
    fun getImage(
        @PathVariable filename: String,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ResponseEntity<Resource> {
        val user = userService.getUserByEmail(userDetails.username)
        return imageService.serveUserImage(filename, user.id)
    }

    @Suppress("LongParameterList")
    @GetMapping
    fun getUserImages(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "all") type: String,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String,
    ): ResponseEntity<Map<String, Any>> {
        val user = userService.getUserByEmail(userDetails.username)
        
        val filter = UserImagesFilter(
            userId = user.id,
            page = page,
            size = size,
            type = type,
            sortBy = sortBy,
            sortDirection = sortDirection
        )
        
        val response = imageService.getUserImages(filter)
        
        // Transform PaginatedResponse to match frontend expectations
        // Map 'number' → 'currentPage' for frontend compatibility
        return ResponseEntity.ok(mapOf(
            "content" to response.content,
            "currentPage" to response.number,
            "totalPages" to response.totalPages,
            "totalElements" to response.totalElements,
            "size" to response.size
        ))
    }
}
