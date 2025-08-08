package com.jotoai.voenix.shop.prompt.internal.service

import com.jotoai.voenix.shop.prompt.api.PromptCategoryFacade
import com.jotoai.voenix.shop.prompt.api.PromptCategoryQueryService
import com.jotoai.voenix.shop.prompt.api.dto.categories.CreatePromptCategoryRequest
import com.jotoai.voenix.shop.prompt.api.dto.categories.PromptCategoryDto
import com.jotoai.voenix.shop.prompt.api.dto.categories.UpdatePromptCategoryRequest
import com.jotoai.voenix.shop.prompt.api.exceptions.PromptCategoryNotFoundException
import com.jotoai.voenix.shop.prompt.events.PromptCategoryCreatedEvent
import com.jotoai.voenix.shop.prompt.events.PromptCategoryDeletedEvent
import com.jotoai.voenix.shop.prompt.events.PromptCategoryUpdatedEvent
import com.jotoai.voenix.shop.prompt.internal.entity.PromptCategory
import com.jotoai.voenix.shop.prompt.internal.repository.PromptCategoryRepository
import com.jotoai.voenix.shop.prompt.internal.repository.PromptRepository
import com.jotoai.voenix.shop.prompt.internal.repository.PromptSubCategoryRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PromptCategoryServiceImpl(
    private val promptCategoryRepository: PromptCategoryRepository,
    private val promptRepository: PromptRepository,
    private val promptSubCategoryRepository: PromptSubCategoryRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : PromptCategoryFacade,
    PromptCategoryQueryService {
    override fun getAllPromptCategories(): List<PromptCategoryDto> =
        promptCategoryRepository.findAll().map { category ->
            PromptCategoryDto(
                id = category.id!!,
                name = category.name,
                promptsCount = promptRepository.countByCategoryId(category.id),
                subcategoriesCount = promptSubCategoryRepository.countByPromptCategoryId(category.id).toInt(),
                createdAt = category.createdAt,
                updatedAt = category.updatedAt,
            )
        }

    override fun getPromptCategoryById(id: Long): PromptCategoryDto =
        promptCategoryRepository
            .findById(id)
            .map { category ->
                PromptCategoryDto(
                    id = category.id!!,
                    name = category.name,
                    promptsCount = promptRepository.countByCategoryId(category.id),
                    subcategoriesCount = promptSubCategoryRepository.countByPromptCategoryId(category.id).toInt(),
                    createdAt = category.createdAt,
                    updatedAt = category.updatedAt,
                )
            }.orElseThrow { PromptCategoryNotFoundException("PromptCategory", "id", id) }

    override fun searchPromptCategoriesByName(name: String): List<PromptCategoryDto> =
        promptCategoryRepository.findByNameContainingIgnoreCase(name).map { category ->
            PromptCategoryDto(
                id = category.id!!,
                name = category.name,
                promptsCount = promptRepository.countByCategoryId(category.id),
                subcategoriesCount = promptSubCategoryRepository.countByPromptCategoryId(category.id).toInt(),
                createdAt = category.createdAt,
                updatedAt = category.updatedAt,
            )
        }

    override fun existsById(id: Long): Boolean = promptCategoryRepository.existsById(id)

    @Transactional
    override fun createPromptCategory(request: CreatePromptCategoryRequest): PromptCategoryDto {
        val promptCategory =
            PromptCategory(
                name = request.name,
            )

        val savedPromptCategory = promptCategoryRepository.save(promptCategory)
        val result =
            PromptCategoryDto(
                id = savedPromptCategory.id!!,
                name = savedPromptCategory.name,
                promptsCount = 0, // New category has no prompts
                subcategoriesCount = 0, // New category has no subcategories
                createdAt = savedPromptCategory.createdAt,
                updatedAt = savedPromptCategory.updatedAt,
            )

        // Publish event
        eventPublisher.publishEvent(PromptCategoryCreatedEvent(result))

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
        val result =
            PromptCategoryDto(
                id = updatedPromptCategory.id!!,
                name = updatedPromptCategory.name,
                promptsCount = promptRepository.countByCategoryId(updatedPromptCategory.id),
                subcategoriesCount = promptSubCategoryRepository.countByPromptCategoryId(updatedPromptCategory.id).toInt(),
                createdAt = updatedPromptCategory.createdAt,
                updatedAt = updatedPromptCategory.updatedAt,
            )

        // Publish event
        eventPublisher.publishEvent(PromptCategoryUpdatedEvent(oldDto, result))

        return result
    }

    @Transactional
    override fun deletePromptCategory(id: Long) {
        if (!promptCategoryRepository.existsById(id)) {
            throw PromptCategoryNotFoundException("PromptCategory", "id", id)
        }

        val categoryDto = getPromptCategoryById(id)

        promptCategoryRepository.deleteById(id)

        // Publish event
        eventPublisher.publishEvent(PromptCategoryDeletedEvent(categoryDto))
    }
}
