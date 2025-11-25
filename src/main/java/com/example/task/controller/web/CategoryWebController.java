package com.example.task.controller.web;

import com.example.task.model.dto.CategoryDto;
import com.example.task.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryWebController {

    private final CategoryService categoryService;

    // Lista kategorii
    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "categories/list";
    }

    // Formularz nowej kategorii
    @GetMapping("/new")
    public String newCategoryForm(Model model) {
        model.addAttribute("category", new CategoryDto());
        return "categories/form";
    }

    // Formularz edycji kategorii
    @GetMapping("/{id}")
    public String editCategoryForm(@PathVariable Long id, Model model) {
        var category = categoryService.getCategoryById(id);
        model.addAttribute("category", category);
        return "categories/form";
    }

    // Utworzenie kategorii
    @PostMapping
    public String createCategory(@Valid @ModelAttribute("category") CategoryDto dto,
                                 BindingResult result) {
        if (result.hasErrors()) {
            return "categories/form";
        }
        categoryService.createCategory(dto);
        return "redirect:/categories";
    }

    // Aktualizacja kategorii
    @PostMapping("/{id}")
    public String updateCategory(@PathVariable Long id,
                                 @Valid @ModelAttribute("category") CategoryDto dto,
                                 BindingResult result) {
        if (result.hasErrors()) {
            return "categories/form";
        }
        categoryService.updateCategory(id, dto);
        return "redirect:/categories";
    }

    // Usunięcie kategorii
    @PostMapping(value = "/{id}", params = "_method=DELETE")
    public String deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return "redirect:/categories";
    }
}
