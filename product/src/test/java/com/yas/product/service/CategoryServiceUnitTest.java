package com.yas.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.BadRequestException;
import com.yas.product.model.Category;
import com.yas.product.repository.CategoryRepository;
import com.yas.product.viewmodel.category.CategoryGetDetailVm;
import com.yas.product.viewmodel.category.CategoryPostVm;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceUnitTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MediaService mediaService;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void update_WhenParentIdNull_ShouldClearParent() {
        Category category = new Category();
        category.setId(1L);
        category.setParent(new Category());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryPostVm postVm = new CategoryPostVm(
            "Name",
            "slug",
            "desc",
            null,
            "meta",
            "meta",
            (short) 1,
            true,
            1L
        );

        categoryService.update(postVm, 1L);

        assertThat(category.getParent()).isNull();
    }

    @Test
    void update_WhenParentIsSelf_ShouldThrowBadRequestException() {
        Category category = new Category();
        category.setId(1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));

        CategoryPostVm postVm = new CategoryPostVm(
            "Name",
            "slug",
            "desc",
            2L,
            "meta",
            "meta",
            (short) 1,
            true,
            1L
        );

        assertThrows(BadRequestException.class, () -> categoryService.update(postVm, 1L));
    }

    @Test
    void create_WhenParentNotFound_ShouldThrowBadRequestException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        CategoryPostVm postVm = new CategoryPostVm(
            "Name",
            "slug",
            "desc",
            99L,
            "meta",
            "meta",
            (short) 1,
            true,
            1L
        );

        assertThrows(BadRequestException.class, () -> categoryService.create(postVm));
    }

    @Test
    void getCategoryById_WhenNoImageNoParent_ShouldReturnDefaults() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Name");
        category.setSlug("slug");
        category.setDescription("desc");
        category.setMetaKeyword("meta");
        category.setMetaDescription("meta");
        category.setDisplayOrder((short) 1);
        category.setIsPublished(true);
        category.setImageId(null);
        category.setParent(null);
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));

        CategoryGetDetailVm result = categoryService.getCategoryById(1L);

        assertThat(result.parentId()).isEqualTo(0L);
        assertThat(result.categoryImage()).isNull();
    }
}
