package com.jotoai.voenix.shop.prompt.internal.service
import com.jotoai.voenix.shop.prompt.PromptCategoryDto
import com.jotoai.voenix.shop.prompt.internal.dto.categories.CreatePromptCategoryRequest
import com.jotoai.voenix.shop.prompt.internal.dto.categories.UpdatePromptCategoryRequest
import com.jotoai.voenix.shop.prompt.internal.entity.PromptCategory
import com.jotoai.voenix.shop.prompt.internal.repository.PromptCategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PromptCategoryServiceImpl(
    private val promptCategoryRepository: PromptCategoryRepository,
    private val promptCategoryAssembler: PromptCategoryAssembler,
) {
    fun getAllPromptCategories(): List<PromptCategoryDto> =
        promptCategoryRepository.findAll().map { category ->
            promptCategoryAssembler.toDto(category)
        }

    fun existsById(id: Long): Boolean = promptCategoryRepository.existsById(id)

    @Transactional
    fun createPromptCategory(request: CreatePromptCategoryRequest): PromptCategoryDto {
        val promptCategory =
            PromptCategory(
                name = request.name,
            )

        val savedPromptCategory = promptCategoryRepository.save(promptCategory)
        val result = promptCategoryAssembler.toDto(savedPromptCategory)

        return result
    }

    @Transactional
    fun updatePromptCategory(
        id: Long,
        request: UpdatePromptCategoryRequest,
    ): PromptCategoryDto {
        val promptCategory = promptCategoryRepository.getOrNotFound(id, "PromptCategory")

        request.name?.let { promptCategory.name = it }

        val updatedPromptCategory = promptCategoryRepository.save(promptCategory)
        val result = promptCategoryAssembler.toDto(updatedPromptCategory)

        return result
    }

    @Transactional
    fun deletePromptCategory(id: Long) {
        // Align semantics with update: throw if not found, then delete
        promptCategoryRepository.getOrNotFound(id, "PromptCategory")
        promptCategoryRepository.deleteById(id)
    }
}
