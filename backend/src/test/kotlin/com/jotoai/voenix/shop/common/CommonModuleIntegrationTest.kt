package com.jotoai.voenix.shop.common

import com.jotoai.voenix.shop.common.api.dto.ErrorResponse
import com.jotoai.voenix.shop.common.api.dto.PaginatedResponse
import com.jotoai.voenix.shop.common.api.exception.BadRequestException
import com.jotoai.voenix.shop.common.api.exception.ResourceAlreadyExistsException
import com.jotoai.voenix.shop.common.api.exception.ResourceNotFoundException
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class CommonModuleIntegrationTest {
    @Test
    fun `should create ErrorResponse with all fields`() {
        val errorResponse =
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = 404,
                error = "Not Found",
                message = "Resource not found",
                path = "/api/test",
                validationErrors = mapOf("field" to "error message"),
            )

        assert(errorResponse.status == 404)
        assert(errorResponse.error == "Not Found")
        assert(errorResponse.message == "Resource not found")
        assert(errorResponse.path == "/api/test")
        assert(errorResponse.validationErrors?.get("field") == "error message")
    }

    @Test
    fun `should create PaginatedResponse with all fields`() {
        val content = listOf("item1", "item2", "item3")
        val paginatedResponse =
            PaginatedResponse(
                content = content,
                totalElements = 10L,
                totalPages = 4,
                size = 3,
                number = 0,
                first = true,
                last = false,
                numberOfElements = 3,
                empty = false,
            )

        assert(paginatedResponse.content.size == 3)
        assert(paginatedResponse.totalElements == 10L)
        assert(paginatedResponse.totalPages == 4)
        assert(paginatedResponse.first)
        assert(!paginatedResponse.last)
        assert(!paginatedResponse.empty)
    }

    @Test
    fun `should create and throw ResourceNotFoundException`() {
        val exception = object : ResourceNotFoundException("Resource not found") {}

        assert(exception.message == "Resource not found")

        try {
            throw exception
        } catch (e: ResourceNotFoundException) {
            assert(e.message == "Resource not found")
        }
    }

    @Test
    fun `should create and throw ResourceAlreadyExistsException`() {
        val exception = object : ResourceAlreadyExistsException("Resource already exists") {}

        assert(exception.message == "Resource already exists")

        try {
            throw exception
        } catch (e: ResourceAlreadyExistsException) {
            assert(e.message == "Resource already exists")
        }
    }

    @Test
    fun `should create and throw BadRequestException`() {
        val exception = BadRequestException("Invalid request")

        assert(exception.message == "Invalid request")

        try {
            throw exception
        } catch (e: BadRequestException) {
            assert(e.message == "Invalid request")
        }
    }
}
