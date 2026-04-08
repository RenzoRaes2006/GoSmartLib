package ap.be.backend;

import ap.be.backend.controller.ImageController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(ImageController.class)
@TestPropertySource(properties = "upload.path=${java.io.tmpdir}/gosmartlib-test-uploads")
public class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void givenJpegFile_whenUpload_thenReturnsRelativePath() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", "fake-image-data".getBytes());

        mockMvc.perform(multipart("/api/images/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string(startsWith("/api/images/")))
                .andExpect(content().string(endsWith(".jpg")));
    }

    @Test
    public void givenPngFile_whenUpload_thenReturnsRelativePathWithPngExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.png", "image/png", "fake-image-data".getBytes());

        mockMvc.perform(multipart("/api/images/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string(startsWith("/api/images/")))
                .andExpect(content().string(endsWith(".png")));
    }

    @Test
    public void givenInvalidFileType_whenUpload_thenReturnsBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "document.pdf", "application/pdf", "fake-pdf-data".getBytes());

        mockMvc.perform(multipart("/api/images/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Alleen JPEG en PNG afbeeldingen zijn toegestaan."));
    }

    @Test
    public void givenNoContentType_whenUpload_thenReturnsBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", null, "fake-image-data".getBytes());

        mockMvc.perform(multipart("/api/images/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Alleen JPEG en PNG afbeeldingen zijn toegestaan."));
    }

    @Test
    public void givenNonExistentFile_whenGetImage_thenReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/images/niet-bestaand.jpg"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenPathTraversalAttempt_whenGetImage_thenReturnsErrorStatus() throws Exception {
        mockMvc.perform(get("/api/images/..%2F..%2Fetc%2Fpasswd"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void givenUploadedFile_whenGetImage_thenReturnsImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", "fake-image-data".getBytes());

        String pad = mockMvc.perform(multipart("/api/images/upload").file(file))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String bestandsnaam = pad.replace("/api/images/", "");

        mockMvc.perform(get("/api/images/" + bestandsnaam))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes("fake-image-data".getBytes()));
    }
}
