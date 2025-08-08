package com.jotoai.voenix.shop.prompt.internal.service

import com.jotoai.voenix.shop.prompt.api.PromptSubCategoryFacade
import com.jotoai.voenix.shop.prompt.api.PromptSubCategoryQueryService
import com.jotoai.voenix.shop.prompt.api.dto.subcategories.CreatePromptSubCategoryRequest
import com.jotoai.voenix.shop.prompt.api.dto.subcategories.PromptSubCategoryDto
import com.jotoai.voenix.shop.prompt.api.dto.subcategories.UpdatePromptSubCategoryRequest
import com.jotoai.voenix.shop.prompt.api.exceptions.PromptSubCategoryNotFoundException
import com.jotoai.voenix.shop.prompt.events.PromptSubCategoryCreatedEvent
import com.jotoai.voenix.shop.prompt.events.PromptSubCategoryDeletedEvent
import com.jotoai.voenix.shop.prompt.events.PromptSubCategoryUpdatedEvent
import com.jotoai.voenix.shop.prompt.internal.entity.PromptSubCategory
import com.jotoai.voenix.shop.prompt.internal.repository.PromptCategoryRepository
import com.jotoai.voenix.shop.prompt.internal.repository.PromptRepository
import com.jotoai.voenix.shop.prompt.internal.repository.PromptSubCategoryRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PromptSubCategoryServiceImpl(
    private val promptSubCategoryRepository: PromptSubCategoryRepository,
    private val promptCategoryRepository: PromptCategoryRepository,
    private val promptRepository: PromptRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : PromptSubCategoryFacade,
    PromptSubCategoryQueryService {
    override fun getAllPromptSubCategories(): List<PromptSubCategoryDto> = promptSubCategoryRepository.findAll().map { it.toDto() }

    override fun getPromptSubCategoryById(id: Long): PromptSubCategoryDto =
        promptSubCategoryRepository
            .findById(id)
            .map { it.toDto() }
            .orElseThrow { PromptSubCategoryNotFoundException("PromptSubCategory", "id", id) }

    override fun getPromptSubCategoriesByCategory(categoryId: Long): List<PromptSubCategoryDto> =
        promptSubCategoryRepository.findByPromptCategoryId(categoryId).map { it.toDto() }

    override fun searchPromptSubCategoriesByName(name: String): List<PromptSubCategoryDto> =
        promptSubCategoryRepository.findByNameContainingIgnoreCase(name).map { it.toDto() }

    override fun existsById(id: Long): Boolean = promptSubCategoryRepository.existsById(id)

    @Transactional
    override fun createPromptSubCategory(request: CreatePromptSubCategoryRequest): PromptSubCategoryDto {
        val category =
            promptCategoryRepository
                .findById(request.promptCategoryId)
                .orElseThrow { PromptSubCategoryNotFoundException("PromptCategory", "id", request.promptCategoryId) }

        val promptSubCategory =
            PromptSubCategory(
                name = request.name,
                description = request.description,
                promptCategory = category,
            )

        val saved = promptSubCategoryRepository.save(promptSubCategory)
        val result = saved.toDto()

        eventPublisher.publishEvent(PromptSubCategoryCreatedEvent(result))
        return result
    }

    @Transactional
    override fun updatePromptSubCategory(
        id: Long,
        request: UpdatePromptSubCategoryRequest,
    ): PromptSubCategoryDto {
        val promptSubCategory =
            promptSubCategoryRepository
                .findById(id)
                .orElseThrow { PromptSubCategoryNotFoundException("PromptSubCategory", "id", id) }

        val oldDto = promptSubCategory.toDto()

        request.name?.let { promptSubCategory.name = it }
        request.description?.let { promptSubCategory.description = it }
        request.promptCategoryId?.let { categoryId ->
            val category =
                promptCategoryRepository
                    .findById(categoryId)
                    .orElseThrow { PromptSubCategoryNotFoundException("PromptCategory", "id", categoryId) }
            promptSubCategory.promptCategory = category
        }

        val saved = promptSubCategoryRepository.save(promptSubCategory)
        val result = saved.toDto()

        eventPublisher.publishEvent(PromptSubCategoryUpdatedEvent(oldDto, result))
        return result
    }

    @Transactional
    override fun deletePromptSubCategory(id: Long) {
        val promptSubCategory =
            promptSubCategoryRepository
                .findById(id)
                .orElseThrow { PromptSubCategoryNotFoundException("PromptSubCategory", "id", id) }

        val dto = promptSubCategory.toDto()
        promptSubCategoryRepository.deleteById(id)

        eventPublisher.publishEvent(PromptSubCategoryDeletedEvent(dto))
    }

    private fun PromptSubCategory.toDto(): PromptSubCategoryDto =
        PromptSubCategoryDto(
            id = this.id!!,
            promptCategoryId = this.promptCategory.id!!,
            name = this.name,
            description = this.description,
            promptsCount = promptRepository.countBySubcategoryId(this.id),
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )
}
