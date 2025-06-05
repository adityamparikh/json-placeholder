package dev.aparikh.jsonplaceholder.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

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
        StepVerifier.create(documentService.convertHtmlToDocx(html))
                .assertNext(docxBytes -> {
                    assertNotNull(docxBytes);
                    assertTrue(docxBytes.length > 0);
                    System.out.println("[DEBUG_LOG] DOCX conversion successful, output size: " + docxBytes.length + " bytes");
                })
                .verifyComplete();
    }

    @Test
    public void testConvertHtmlToRtf() {
        // Create sample HTML content with various elements
        String html = "<html><body>" +
                     "<h1>Test Document</h1>" +
                     "<p>This is a <b>bold</b> and <i>italic</i> paragraph.</p>" +
                     "<ul><li>First item</li><li>Second item</li></ul>" +
                     "<ol><li>Numbered item 1</li><li>Numbered item 2</li></ol>" +
                     "</body></html>";

        // Test conversion to RTF
        StepVerifier.create(documentService.convertHtmlToRtf(html))
                .assertNext(rtfBytes -> {
                    assertNotNull(rtfBytes);
                    assertTrue(rtfBytes.length > 0);

                    // Convert to string and verify RTF format
                    String rtfContent = new String(rtfBytes, java.nio.charset.StandardCharsets.UTF_8);
                    assertTrue(rtfContent.startsWith("{\\rtf1"));
                    assertTrue(rtfContent.endsWith("}"));
                    assertTrue(rtfContent.contains("Test Document"));
                    assertTrue(rtfContent.contains("bold"));
                    assertTrue(rtfContent.contains("italic"));

                    System.out.println("[DEBUG_LOG] RTF conversion successful, output size: " + rtfBytes.length + " bytes");
                })
                .verifyComplete();
    }

    @Test
    public void testConvertHtmlToRtfFormat() {
        // Test the generic format conversion method with RTF
        String html = "<html><body><h1>RTF Test</h1><p>Testing format conversion.</p></body></html>";

        StepVerifier.create(documentService.convertHtmlToFormat(html, "rtf"))
                .assertNext(rtfBytes -> {
                    assertNotNull(rtfBytes);
                    assertTrue(rtfBytes.length > 0);

                    String rtfContent = new String(rtfBytes, java.nio.charset.StandardCharsets.UTF_8);
                    assertTrue(rtfContent.startsWith("{\\rtf1"));
                    assertTrue(rtfContent.contains("RTF Test"));

                    System.out.println("[DEBUG_LOG] RTF format conversion successful, output size: " + rtfBytes.length + " bytes");
                })
                .verifyComplete();
    }

    @Test
    public void testRenderPostsToHtmlAndConvertToDocx() {
        // Create sample posts
        List<Post> posts = Arrays.asList(
            new Post(1L, 1L, "Test Post 1", "This is test post 1"),
            new Post(2L, 1L, "Test Post 2", "This is test post 2")
        );

        // Render posts to HTML and convert to DOCX
        StepVerifier.create(
                documentService.renderPostsToHtml(posts)
                        .flatMap(html -> documentService.convertHtmlToDocx(html))
        )
                .assertNext(docxBytes -> {
                    assertNotNull(docxBytes);
                    assertTrue(docxBytes.length > 0);
                    System.out.println("[DEBUG_LOG] Posts rendered and converted to DOCX, output size: " + docxBytes.length + " bytes");
                })
                .verifyComplete();
    }

    @Test
    public void testRenderPostsToHtmlAndConvertToRtf() {
        // Create sample posts
        List<Post> posts = Arrays.asList(
            new Post(1L, 1L, "RTF Test Post 1", "This is the first test post for RTF conversion"),
            new Post(2L, 1L, "RTF Test Post 2", "This is the second test post for RTF conversion")
        );

        // Render posts to HTML and convert to RTF
        StepVerifier.create(
                documentService.renderPostsToHtml(posts)
                        .flatMap(html -> documentService.convertHtmlToRtf(html))
        )
                .assertNext(rtfBytes -> {
                    assertNotNull(rtfBytes);
                    assertTrue(rtfBytes.length > 0);

                    // Verify RTF format
                    String rtfContent = new String(rtfBytes, java.nio.charset.StandardCharsets.UTF_8);
                    assertTrue(rtfContent.startsWith("{\\rtf1"));
                    assertTrue(rtfContent.endsWith("}"));
                    assertTrue(rtfContent.contains("RTF Test Post 1"));
                    assertTrue(rtfContent.contains("RTF Test Post 2"));

                    System.out.println("[DEBUG_LOG] Posts rendered and converted to RTF, output size: " + rtfBytes.length + " bytes");
                })
                .verifyComplete();
    }
}
