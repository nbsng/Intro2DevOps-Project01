package com.yas.media.mapper;

import com.yas.media.model.Media;
import com.yas.media.viewmodel.MediaVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MediaVmMapperImplTest {

    private MediaVmMapperImpl mapper;

    @BeforeEach
    void setUp() {
        mapper = new MediaVmMapperImpl();
    }

    // =========================================================================
    // toModel(MediaVm) — MediaVm → Media
    // =========================================================================

    @Nested
    class ToModel {

        @Test
        void toModel_NullInput_ReturnsNull() {
            assertThat(mapper.toModel(null)).isNull();
        }

        @Test
        void toModel_ValidVm_MapsAllFields() {
            MediaVm vm = new MediaVm(1L, "caption", "file.png", "image/png", "http://url");

            Media result = mapper.toModel(vm);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getCaption()).isEqualTo("caption");
            assertThat(result.getFileName()).isEqualTo("file.png");
            assertThat(result.getMediaType()).isEqualTo("image/png");
        }

        @Test
        void toModel_VmWithNullFields_MapsNullsCorrectly() {
            MediaVm vm = new MediaVm(null, null, null, null, null);

            Media result = mapper.toModel(vm);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isNull();
            assertThat(result.getCaption()).isNull();
            assertThat(result.getFileName()).isNull();
            assertThat(result.getMediaType()).isNull();
        }
    }

    // =========================================================================
    // toVm(Media) — Media → MediaVm
    // =========================================================================

    @Nested
    class ToVm {

        @Test
        void toVm_NullInput_ReturnsNull() {
            assertThat(mapper.toVm(null)).isNull();
        }

        @Test
        void toVm_ValidMedia_MapsAllFields() {
            Media media = buildMedia(2L, "my caption", "photo.jpg", "image/jpeg");

            MediaVm result = mapper.toVm(media);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(2L);
            assertThat(result.getCaption()).isEqualTo("my caption");
            assertThat(result.getFileName()).isEqualTo("photo.jpg");
            assertThat(result.getMediaType()).isEqualTo("image/jpeg");
        }

        @Test
        void toVm_UrlAlwaysNull_BecauseMapperDoesNotMapUrl() {
            // MapStruct generated code sets url = null explicitly
            Media media = buildMedia(3L, "cap", "img.png", "image/png");

            MediaVm result = mapper.toVm(media);

            assertThat(result.getUrl()).isNull();
        }

        @Test
        void toVm_MediaWithNullFields_MapsNullsCorrectly() {
            Media media = buildMedia(null, null, null, null);

            MediaVm result = mapper.toVm(media);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isNull();
            assertThat(result.getCaption()).isNull();
            assertThat(result.getFileName()).isNull();
            assertThat(result.getMediaType()).isNull();
        }
    }

    // =========================================================================
    // partialUpdate(Media, MediaVm) — only non-null fields are copied
    // =========================================================================

    @Nested
    class PartialUpdate {

        @Test
        void partialUpdate_NullVm_MediaUnchanged() {
            Media media = buildMedia(1L, "original", "old.png", "image/png");

            mapper.partialUpdate(media, null);

            assertThat(media.getId()).isEqualTo(1L);
            assertThat(media.getCaption()).isEqualTo("original");
            assertThat(media.getFileName()).isEqualTo("old.png");
            assertThat(media.getMediaType()).isEqualTo("image/png");
        }

        @Test
        void partialUpdate_AllNonNullFields_UpdatesAllFields() {
            Media media = buildMedia(1L, "old caption", "old.png", "image/png");
            MediaVm vm = new MediaVm(2L, "new caption", "new.jpg", "image/jpeg", null);

            mapper.partialUpdate(media, vm);

            assertThat(media.getId()).isEqualTo(2L);
            assertThat(media.getCaption()).isEqualTo("new caption");
            assertThat(media.getFileName()).isEqualTo("new.jpg");
            assertThat(media.getMediaType()).isEqualTo("image/jpeg");
        }

        @Test
        void partialUpdate_AllNullFields_MediaUnchanged() {
            Media media = buildMedia(1L, "caption", "file.png", "image/png");
            MediaVm vm = new MediaVm(null, null, null, null, null);

            mapper.partialUpdate(media, vm);

            // All vm fields are null → nothing should be overwritten
            assertThat(media.getId()).isEqualTo(1L);
            assertThat(media.getCaption()).isEqualTo("caption");
            assertThat(media.getFileName()).isEqualTo("file.png");
            assertThat(media.getMediaType()).isEqualTo("image/png");
        }

        @Test
        void partialUpdate_OnlyCaptionNonNull_UpdatesOnlyCaption() {
            Media media = buildMedia(5L, "old", "file.png", "image/png");
            MediaVm vm = new MediaVm(null, "updated caption", null, null, null);

            mapper.partialUpdate(media, vm);

            assertThat(media.getId()).isEqualTo(5L);           // unchanged
            assertThat(media.getCaption()).isEqualTo("updated caption"); // updated
            assertThat(media.getFileName()).isEqualTo("file.png");       // unchanged
            assertThat(media.getMediaType()).isEqualTo("image/png");     // unchanged
        }

        @Test
        void partialUpdate_OnlyFileNameNonNull_UpdatesOnlyFileName() {
            Media media = buildMedia(5L, "caption", "old.png", "image/png");
            MediaVm vm = new MediaVm(null, null, "new.jpg", null, null);

            mapper.partialUpdate(media, vm);

            assertThat(media.getCaption()).isEqualTo("caption"); // unchanged
            assertThat(media.getFileName()).isEqualTo("new.jpg"); // updated
            assertThat(media.getMediaType()).isEqualTo("image/png"); // unchanged
        }

        @Test
        void partialUpdate_OnlyMediaTypeNonNull_UpdatesOnlyMediaType() {
            Media media = buildMedia(5L, "caption", "file.png", "image/png");
            MediaVm vm = new MediaVm(null, null, null, "image/jpeg", null);

            mapper.partialUpdate(media, vm);

            assertThat(media.getCaption()).isEqualTo("caption");    // unchanged
            assertThat(media.getFileName()).isEqualTo("file.png");  // unchanged
            assertThat(media.getMediaType()).isEqualTo("image/jpeg"); // updated
        }
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private Media buildMedia(Long id, String caption, String fileName, String mediaType) {
        Media media = new Media();
        media.setId(id);
        media.setCaption(caption);
        media.setFileName(fileName);
        media.setMediaType(mediaType);
        return media;
    }
}