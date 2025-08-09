package com.jotoai.voenix.shop.auth.internal.service

import com.jotoai.voenix.shop.auth.api.AuthQueryService
import com.jotoai.voenix.shop.auth.api.dto.SessionInfo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthQueryServiceImpl(
    private val authService: AuthService,
) : AuthQueryService {
    @Transactional(readOnly = true)
    override fun getCurrentSession(): SessionInfo = authService.getCurrentSession()
}
