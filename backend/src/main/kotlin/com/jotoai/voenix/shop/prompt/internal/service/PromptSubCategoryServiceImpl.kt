package com.jotoai.voenix.shop.prompt.internal.service
import com.jotoai.voenix.shop.prompt.internal.dto.subcategories.CreatePromptSubCategoryRequest
import com.jotoai.voenix.shop.prompt.PromptSubCategoryDto
import com.jotoai.voenix.shop.prompt.internal.dto.subcategories.UpdatePromptSubCategoryRequest
import com.jotoai.voenix.shop.prompt.internal.entity.PromptSubCategory
import com.jotoai.voenix.shop.prompt.internal.repository.PromptCategoryRepository
import com.jotoai.voenix.shop.prompt.internal.repository.PromptSubCategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PromptSubCategoryServiceImpl(
    private val promptSubCategoryRepository: PromptSubCategoryRepository,
    private val promptCategoryRepository: PromptCategoryRepository,
    private val promptSubCategoryAssembler: PromptSubCategoryAssembler,
) {
    fun getAllPromptSubCategories(): List<PromptSubCategoryDto> =
        promptSubCategoryRepository.findAll().map {
            promptSubCategoryAssembler.toDto(it)
        }

    fun getPromptSubCategoriesByCategory(categoryId: Long): List<PromptSubCategoryDto> =
        promptSubCategoryRepository.findByPromptCategoryId(categoryId).map { promptSubCategoryAssembler.toDto(it) }

    fun existsById(id: Long): Boolean = promptSubCategoryRepository.existsById(id)

    @Transactional
    fun createPromptSubCategory(request: CreatePromptSubCategoryRequest): PromptSubCategoryDto {
        val category = promptCategoryRepository.getOrNotFound(request.promptCategoryId, "PromptCategory")

        val promptSubCategory =
            PromptSubCategory(
                name = request.name,
                description = request.description,
                promptCategory = category,
            )

        val saved = promptSubCategoryRepository.save(promptSubCategory)
        val result = promptSubCategoryAssembler.toDto(saved)

        return result
    }

    @Transactional
    fun updatePromptSubCategory(
        id: Long,
        request: UpdatePromptSubCategoryRequest,
    ): PromptSubCategoryDto {
        val promptSubCategory = promptSubCategoryRepository.getOrNotFound(id, "PromptSubCategory")

        request.name?.let { promptSubCategory.name = it }
        request.description?.let { promptSubCategory.description = it }
        request.promptCategoryId?.let { categoryId ->
            val category = promptCategoryRepository.getOrNotFound(categoryId, "PromptCategory")
            promptSubCategory.promptCategory = category
        }

        val saved = promptSubCategoryRepository.save(promptSubCategory)
        val result = promptSubCategoryAssembler.toDto(saved)

        return result
    }

    @Transactional
    fun deletePromptSubCategory(id: Long) = promptSubCategoryRepository.deleteById(id)
}
