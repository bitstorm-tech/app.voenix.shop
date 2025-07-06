package com.jotoai.voenix.shop.prompts.repository;

import com.jotoai.voenix.shop.prompts.entity.Prompt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromptRepository extends JpaRepository<Prompt, Long> {
    
    List<Prompt> findByTitleContainingIgnoreCase(String title);
}