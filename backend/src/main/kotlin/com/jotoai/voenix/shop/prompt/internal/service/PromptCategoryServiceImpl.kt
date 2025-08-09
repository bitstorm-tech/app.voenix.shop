package com.jotoai.voenix.shop.prompt.internal.service

import com.jotoai.voenix.shop.prompt.api.PromptCategoryFacade
import com.jotoai.voenix.shop.prompt.api.PromptCategoryQueryService
import com.jotoai.voenix.shop.prompt.api.dto.categories.CreatePromptCategoryRequest
import com.jotoai.voenix.shop.prompt.api.dto.categories.PromptCategoryDto
import com.jotoai.voenix.shop.prompt.api.dto.categories.UpdatePromptCategoryRequest
import com.jotoai.voenix.shop.prompt.api.exceptions.PromptCategoryNotFoundException
import com.jotoai.voenix.shop.prompt.internal.entity.PromptCategory
import com.jotoai.voenix.shop.prompt.internal.repository.PromptCategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PromptCategoryServiceImpl(
    private val promptCategoryRepository: PromptCategoryRepository,
    private val promptCategoryAssembler: PromptCategoryAssembler,
) : PromptCategoryFacade,
    PromptCategoryQueryService {
    override fun getAllPromptCategories(): List<PromptCategoryDto> =
        promptCategoryRepository.findAll().map { category ->
            promptCategoryAssembler.toDto(category)
        }

    override fun getPromptCategoryById(id: Long): PromptCategoryDto =
        promptCategoryRepository
            .findById(id)
            .map { category ->
                promptCategoryAssembler.toDto(category)
            }.orElseThrow { PromptCategoryNotFoundException("PromptCategory", "id", id) }

    override fun searchPromptCategoriesByName(name: String): List<PromptCategoryDto> =
        promptCategoryRepository.findByNameContainingIgnoreCase(name).map { category ->
            promptCategoryAssembler.toDto(category)
        }

    override fun existsById(id: Long): Boolean = promptCategoryRepository.existsById(id)

    @Transactional
    override fun createPromptCategory(request: CreatePromptCategoryRequest): PromptCategoryDto {
        val promptCategory =
            PromptCategory(
                name = request.name,
            )

        val savedPromptCategory = promptCategoryRepository.save(promptCategory)
        val result = promptCategoryAssembler.toDto(savedPromptCategory)

        return result
    }

    @Transactional
    override fun updatePromptCategory(
        id: Long,
        request: UpdatePromptCategoryRequest,
    ): PromptCategoryDto {
        val promptCategory =
            promptCategoryRepository
                .findById(id)
                .orElseThrow { PromptCategoryNotFoundException("PromptCategory", "id", id) }

        val oldDto = getPromptCategoryById(id)

        request.name?.let { promptCategory.name = it }

        val updatedPromptCategory = promptCategoryRepository.save(promptCategory)
        val result = promptCategoryAssembler.toDto(updatedPromptCategory)

        return result
    }

    @Transactional
    override fun deletePromptCategory(id: Long) {
        if (!promptCategoryRepository.existsById(id)) {
            throw PromptCategoryNotFoundException("PromptCategory", "id", id)
        }

        val categoryDto = getPromptCategoryById(id)

        promptCategoryRepository.deleteById(id)
    }
}
