package dev.aparikh.jsonplaceholder.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import dev.aparikh.jsonplaceholder.model.Post;
import gg.jte.TemplateEngine;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DocumentServiceTest {

    @Autowired
    private DocumentService documentService;

    @Test
    public void testConvertHtmlToDocx() {
        // Create sample HTML content
        String html = "<html><body><h1>Test Document</h1><p>This is a test paragraph.</p></body></html>";

        // Test conversion to DOCX
        byte[] docxBytes = documentService.convertHtmlToDocx(html);

        // Verify that the conversion produced non-empty output
        assertNotNull(docxBytes);
        assertTrue(docxBytes.length > 0);

        System.out.println("[DEBUG_LOG] DOCX conversion successful, output size: " + docxBytes.length + " bytes");
    }

    @Test
    public void testRenderPostsToHtmlAndConvertToDocx() {
        // Create sample posts
        List<Post> posts = Arrays.asList(
            new Post(1L, 1L, "Test Post 1", "This is test post 1"),
            new Post(2L, 1L, "Test Post 2", "This is test post 2")
        );

        // Render posts to HTML
        String html = documentService.renderPostsToHtml(posts);

        // Convert HTML to DOCX
        byte[] docxBytes = documentService.convertHtmlToDocx(html);

        // Verify that the conversion produced non-empty output
        assertNotNull(docxBytes);
        assertTrue(docxBytes.length > 0);

        System.out.println("[DEBUG_LOG] Posts rendered and converted to DOCX, output size: " + docxBytes.length + " bytes");
    }
}
