package ap.be.backend;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

public class ImageControllerTest extends AbstractIntegrationTest {

    @Test
    public void givenJpegFile_whenUpload_thenReturnsAbsoluteUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", "fake-image-data".getBytes());

        mockMvc.perform(multipart("/api/images/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string(startsWith("http")))
                .andExpect(content().string(containsString("/api/images/")))
                .andExpect(content().string(endsWith(".jpg")));
    }

    @Test
    public void givenPngFile_whenUpload_thenReturnsAbsoluteUrlWithPngExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.png", "image/png", "fake-image-data".getBytes());

        mockMvc.perform(multipart("/api/images/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string(startsWith("http")))
                .andExpect(content().string(containsString("/api/images/")))
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

        String bestandsnaam = pad.substring(pad.lastIndexOf('/') + 1);

        mockMvc.perform(get("/api/images/" + bestandsnaam))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes("fake-image-data".getBytes()));
    }
}
