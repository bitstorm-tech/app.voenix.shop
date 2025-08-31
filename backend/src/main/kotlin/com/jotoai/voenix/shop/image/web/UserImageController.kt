package com.jotoai.voenix.shop.image.web

import com.jotoai.voenix.shop.image.api.ImageStorage
import com.jotoai.voenix.shop.user.api.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user/images")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
class UserImageController(
    private val imageStorage: ImageStorage,
    private val userService: UserService,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @GetMapping("/{filename}")
    fun getImage(
        @PathVariable filename: String,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ResponseEntity<Resource> {
        val user = userService.getUserByEmail(userDetails.username)
        logger.info { "User ${user.id} retrieving image: $filename" }

        // Delegate to storage service which validates access and streams the resource
        return imageStorage.serveUserImage(filename, user.id)
    }
}
