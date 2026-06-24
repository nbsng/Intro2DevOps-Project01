package com.yas.media.controller;

import com.yas.media.model.Media;
import com.yas.media.model.dto.MediaDto;
import com.yas.media.service.MediaService;
import com.yas.media.viewmodel.MediaPostVm;
import com.yas.media.viewmodel.MediaVm;
import com.yas.media.viewmodel.NoFileMediaVm;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaControllerTest {

    @Mock
    private MediaService mediaService;

    @InjectMocks
    private MediaController mediaController;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Media buildMedia(Long id, String caption, String fileName, String mediaType) {
        Media media = new Media();
        media.setId(id);
        media.setCaption(caption);
        media.setFileName(fileName);
        media.setMediaType(mediaType);
        return media;
    }

    private MediaVm buildMediaVm(Long id, String caption, String fileName, String mediaType) {
        // MediaVm is a plain class with a 5-arg constructor
        return new MediaVm(id, caption, fileName, mediaType, "/medias/" + id + "/file/" + fileName);
    }

    /** MediaDto uses @Builder — never call its constructor directly. */
    private MediaDto buildMediaDto(byte[] content, org.springframework.http.MediaType mediaType) {
        return MediaDto.builder()
                .content(new ByteArrayInputStream(content))
                .mediaType(mediaType)
                .build();
    }

    // =========================================================================
    // POST /medias  →  create()
    // =========================================================================

    @Nested
    class CreateMedia {

        @Test
        void create_ValidRequest_Returns200AndNoFileMediaVm() {
            MediaPostVm postVm = mock(MediaPostVm.class);
            Media saved = buildMedia(1L, "A caption", "image.png", MediaType.IMAGE_PNG_VALUE);
            when(mediaService.saveMedia(postVm)).thenReturn(saved);

            ResponseEntity<Object> response = mediaController.create(postVm);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isInstanceOf(NoFileMediaVm.class);

            // NoFileMediaVm is a record → use record accessors
            NoFileMediaVm body = (NoFileMediaVm) response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.id()).isEqualTo(1L);
            assertThat(body.caption()).isEqualTo("A caption");
            assertThat(body.fileName()).isEqualTo("image.png");
            assertThat(body.mediaType()).isEqualTo(MediaType.IMAGE_PNG_VALUE);

            verify(mediaService, times(1)).saveMedia(postVm);
        }

        @Test
        void create_NullCaption_MapsCorrectly() {
            MediaPostVm postVm = mock(MediaPostVm.class);
            Media saved = buildMedia(42L, null, "doc.pdf", "application/pdf");
            when(mediaService.saveMedia(postVm)).thenReturn(saved);

            ResponseEntity<Object> response = mediaController.create(postVm);

            NoFileMediaVm body = (NoFileMediaVm) response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.id()).isEqualTo(42L);
            assertThat(body.caption()).isNull();
            assertThat(body.fileName()).isEqualTo("doc.pdf");
        }
    }

    // =========================================================================
    // DELETE /medias/{id}  →  delete()
    // =========================================================================

    @Nested
    class DeleteMedia {

        @Test
        void delete_ExistingId_Returns204NoContent() {
            doNothing().when(mediaService).removeMedia(1L);

            ResponseEntity<Void> response = mediaController.delete(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(response.getBody()).isNull();
            verify(mediaService, times(1)).removeMedia(1L);
        }

        @Test
        void delete_CallsServiceWithExactId() {
            doNothing().when(mediaService).removeMedia(99L);

            mediaController.delete(99L);

            verify(mediaService).removeMedia(99L);
            verify(mediaService, never()).removeMedia(1L);
        }
    }

    // =========================================================================
    // GET /medias/{id}  →  get()
    // =========================================================================

    @Nested
    class GetMediaById {

        @Test
        void get_ExistingId_Returns200WithMediaVm() {
            // MediaVm is a Lombok class → use getXxx()
            MediaVm vm = buildMediaVm(1L, "Caption", "photo.jpg", MediaType.IMAGE_JPEG_VALUE);
            when(mediaService.getMediaById(1L)).thenReturn(vm);

            ResponseEntity<MediaVm> response = mediaController.get(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
            assertThat(response.getBody().getCaption()).isEqualTo("Caption");
            assertThat(response.getBody().getFileName()).isEqualTo("photo.jpg");
        }

        @Test
        void get_NonExistingId_Returns404() {
            when(mediaService.getMediaById(99L)).thenReturn(null);

            ResponseEntity<MediaVm> response = mediaController.get(99L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNull();
        }

        @Test
        void get_ServiceReturnsNull_BodyIsNull() {
            when(mediaService.getMediaById(anyLong())).thenReturn(null);

            ResponseEntity<MediaVm> response = mediaController.get(0L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    // =========================================================================
    // GET /medias?ids=...  →  getByIds()
    // =========================================================================

    @Nested
    class GetMediaByIds {

        @Test
        void getByIds_ValidIds_Returns200WithList() {
            List<Long> ids = List.of(1L, 2L);
            List<MediaVm> vms = List.of(
                    buildMediaVm(1L, "Cap1", "a.jpg", MediaType.IMAGE_JPEG_VALUE),
                    buildMediaVm(2L, "Cap2", "b.png", MediaType.IMAGE_PNG_VALUE)
            );
            when(mediaService.getMediaByIds(ids)).thenReturn(vms);

            ResponseEntity<List<MediaVm>> response = mediaController.getByIds(ids);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(2);
            assertThat(response.getBody().get(0).getId()).isEqualTo(1L);
            assertThat(response.getBody().get(1).getId()).isEqualTo(2L);
        }

        @Test
        void getByIds_NoneFound_Returns404() {
            when(mediaService.getMediaByIds(anyList())).thenReturn(Collections.emptyList());

            ResponseEntity<List<MediaVm>> response = mediaController.getByIds(List.of(999L));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void getByIds_SingleId_Returns200WithOneItem() {
            List<Long> ids = List.of(5L);
            when(mediaService.getMediaByIds(ids))
                    .thenReturn(List.of(buildMediaVm(5L, "One", "one.png", MediaType.IMAGE_PNG_VALUE)));

            ResponseEntity<List<MediaVm>> response = mediaController.getByIds(ids);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }
    }

    // =========================================================================
    // GET /medias/{id}/file/{fileName}  →  getFile()
    // =========================================================================

    @Nested
    class GetFile {

        @Test
        void getFile_ExistingFile_Returns200WithCorrectHeaders() {
            // MediaDto uses @Builder — never new MediaDto(...)
            MediaDto dto = buildMediaDto("file-bytes".getBytes(), org.springframework.http.MediaType.IMAGE_PNG);
            when(mediaService.getFile(1L, "image.png")).thenReturn(dto);

            ResponseEntity<InputStreamResource> response = mediaController.getFile(1L, "image.png");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().getFirst("Content-Disposition"))
                    .isEqualTo("attachment; filename=\"image.png\"");
            assertThat(response.getHeaders().getContentType())
                    .isEqualTo(org.springframework.http.MediaType.IMAGE_PNG);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        void getFile_ContentDispositionContainsFileName() {
            MediaDto dto = buildMediaDto("pdf".getBytes(), org.springframework.http.MediaType.APPLICATION_PDF);
            when(mediaService.getFile(2L, "report.pdf")).thenReturn(dto);

            ResponseEntity<InputStreamResource> response = mediaController.getFile(2L, "report.pdf");

            assertThat(response.getHeaders().getFirst("Content-Disposition"))
                    .contains("report.pdf");
        }

        @Test
        void getFile_CallsServiceWithCorrectArguments() {
            MediaDto dto = buildMediaDto(new byte[0], org.springframework.http.MediaType.IMAGE_JPEG);
            when(mediaService.getFile(3L, "photo.jpg")).thenReturn(dto);

            mediaController.getFile(3L, "photo.jpg");

            verify(mediaService, times(1)).getFile(3L, "photo.jpg");
        }
    }
}