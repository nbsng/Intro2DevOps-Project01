package com.yas.media.utils;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileTypeValidatorTest {

    private FileTypeValidator validator;

    @Mock
    private ValidFileType constraintAnnotation;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @Mock
    private MultipartFile file;

    // -------------------------------------------------------------------------
    // Setup
    // -------------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        validator = new FileTypeValidator();

        // Default annotation stubs used by initialize()
        when(constraintAnnotation.allowedTypes()).thenReturn(new String[]{"image/png", "image/jpeg"});
        when(constraintAnnotation.message()).thenReturn("Invalid file type");

        validator.initialize(constraintAnnotation);

        // Default context stubs for the violation-building chain
        lenient().when(context.buildConstraintViolationWithTemplate(anyString()))
                .thenReturn(violationBuilder);
        lenient().doNothing().when(context).disableDefaultConstraintViolation();
    }

    // -------------------------------------------------------------------------
    // Helper: build a real PNG byte stream so ImageIO.read() returns non-null
    // -------------------------------------------------------------------------
    private InputStream validPngStream() throws IOException {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "png", out);
        return new ByteArrayInputStream(out.toByteArray());
    }

    private InputStream validJpegStream() throws IOException {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "jpeg", out);
        return new ByteArrayInputStream(out.toByteArray());
    }

    // =========================================================================
    // Null / missing content-type
    // =========================================================================

    @Nested
    class NullFileOrContentType {

        @Test
        void isValid_NullFile_ReturnsFalse() {
            boolean result = validator.isValid(null, context);

            assertThat(result).isFalse();
            verify(context).disableDefaultConstraintViolation();
            verify(context).buildConstraintViolationWithTemplate("Invalid file type");
            verify(violationBuilder).addConstraintViolation();
        }

        @Test
        void isValid_NullContentType_ReturnsFalse() {
            when(file.getContentType()).thenReturn(null);

            boolean result = validator.isValid(file, context);

            assertThat(result).isFalse();
            verify(context).disableDefaultConstraintViolation();
            verify(context).buildConstraintViolationWithTemplate("Invalid file type");
        }
    }

    // =========================================================================
    // Allowed types — valid image bytes
    // =========================================================================

    @Nested
    class AllowedTypeWithValidImage {

        @Test
        void isValid_PngFileWithValidImage_ReturnsTrue() throws IOException {
            when(file.getContentType()).thenReturn("image/png");
            when(file.getInputStream()).thenReturn(validPngStream());

            boolean result = validator.isValid(file, context);

            assertThat(result).isTrue();
            verify(context, never()).disableDefaultConstraintViolation();
        }

        @Test
        void isValid_JpegFileWithValidImage_ReturnsTrue() throws IOException {
            when(file.getContentType()).thenReturn("image/jpeg");
            when(file.getInputStream()).thenReturn(validJpegStream());

            boolean result = validator.isValid(file, context);

            assertThat(result).isTrue();
        }
    }

    // =========================================================================
    // Allowed content-type but corrupted / unreadable image bytes
    // =========================================================================

    @Nested
    class AllowedTypeWithInvalidImageBytes {

        @Test
        void isValid_PngContentTypeButCorruptedBytes_ReturnsFalse() throws IOException {
            when(file.getContentType()).thenReturn("image/png");
            // ImageIO.read() returns null for non-image bytes
            when(file.getInputStream()).thenReturn(new ByteArrayInputStream("not-an-image".getBytes()));

            boolean result = validator.isValid(file, context);

            assertThat(result).isFalse();
        }

        @Test
        void isValid_PngContentTypeButStreamThrowsIOException_ReturnsFalse() throws IOException {
            when(file.getContentType()).thenReturn("image/png");
            InputStream brokenStream = mock(InputStream.class);
            when(brokenStream.read(any(byte[].class), anyInt(), anyInt()))
                    .thenThrow(new IOException("Disk error"));
            when(file.getInputStream()).thenReturn(brokenStream);

            boolean result = validator.isValid(file, context);

            assertThat(result).isFalse();
        }
    }

    // =========================================================================
    // Disallowed content-type
    // =========================================================================

    @Nested
    class DisallowedType {

        @Test
        void isValid_PdfContentType_ReturnsFalse() {
            when(file.getContentType()).thenReturn("application/pdf");

            boolean result = validator.isValid(file, context);

            assertThat(result).isFalse();
            verify(context).disableDefaultConstraintViolation();
            verify(context).buildConstraintViolationWithTemplate("Invalid file type");
            verify(violationBuilder).addConstraintViolation();
        }

        @Test
        void isValid_TextPlainContentType_ReturnsFalse() {
            when(file.getContentType()).thenReturn("text/plain");

            boolean result = validator.isValid(file, context);

            assertThat(result).isFalse();
        }

        @Test
        void isValid_EmptyStringContentType_ReturnsFalse() {
            when(file.getContentType()).thenReturn("");

            boolean result = validator.isValid(file, context);

            assertThat(result).isFalse();
        }
    }

    // =========================================================================
    // initialize() — annotation values are wired correctly
    // =========================================================================

    @Nested
    class Initialize {

        @Test
        void initialize_CustomAllowedTypes_UsedDuringValidation() throws IOException {
            // Re-initialise with only image/gif allowed
            when(constraintAnnotation.allowedTypes()).thenReturn(new String[]{"image/gif"});
            when(constraintAnnotation.message()).thenReturn("Only GIF allowed");
            validator.initialize(constraintAnnotation);

            // image/png is no longer in the allowed list
            when(file.getContentType()).thenReturn("image/png");

            boolean result = validator.isValid(file, context);

            assertThat(result).isFalse();
            verify(context).buildConstraintViolationWithTemplate("Only GIF allowed");
        }

        @Test
        void initialize_EmptyAllowedTypes_AlwaysReturnsFalse() {
            when(constraintAnnotation.allowedTypes()).thenReturn(new String[]{});
            validator.initialize(constraintAnnotation);

            when(file.getContentType()).thenReturn("image/png");

            boolean result = validator.isValid(file, context);

            assertThat(result).isFalse();
        }
    }
}