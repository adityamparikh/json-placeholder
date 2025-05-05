package dev.aparikh.jsonplaceholder.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import dev.aparikh.jsonplaceholder.dto.PostDto;
import dev.aparikh.jsonplaceholder.model.Post;
import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.WordprocessingML.AltChunkType;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.wml.CTAltChunk;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for document generation and conversion.
 */
@Service
public class DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);
    private final TemplateEngine templateEngine;

    @Autowired
    public DocumentService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * Renders posts to HTML using JTE template.
     *
     * @param posts List of posts to render
     * @return HTML string
     */
    public String renderPostsToHtml(List<Post> posts) {
        logger.info("Rendering {} posts to HTML", posts.size());
        StringOutput output = new StringOutput();

        // Convert Post objects to PostDto objects to avoid classloader issues
        List<PostDto> postDtos = posts.stream()
            .map(PostDto::new)
            .collect(Collectors.toList());

        templateEngine.render("posts.jte", postDtos, output);
        return output.toString();
    }

    /**
     * Converts HTML to PDF.
     *
     * @param html HTML content to convert
     * @return PDF as byte array
     */
    public byte[] convertHtmlToPdf(String html) {
        logger.info("Converting HTML to PDF");
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (IOException e) {
            logger.error("Error converting HTML to PDF", e);
            throw new RuntimeException("Failed to convert HTML to PDF", e);
        }
    }

    /**
     * Converts HTML to DOCX using docx4j and jsoup.
     *
     * @param html HTML content to convert
     * @return DOCX as byte array
     */
    public byte[] convertHtmlToDocx(String html) {
        logger.info("Converting HTML to DOCX");
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // Create a new WordprocessingMLPackage
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();

            // Create an AlternativeFormatInputPart for the HTML content
            AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(AltChunkType.Html);

            // Ensure the HTML has proper structure with html, head, and body tags
            if (!html.trim().toLowerCase().startsWith("<!doctype html>") && 
                !html.trim().toLowerCase().startsWith("<html")) {
                html = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head><body>" + html + "</body></html>";
            }

            // Set the HTML content
            afiPart.setBinaryData(html.getBytes(StandardCharsets.UTF_8));

            // Add the part to the package
            org.docx4j.relationships.Relationship rel = 
                wordMLPackage.getMainDocumentPart().addTargetPart(afiPart);

            // Create a CTAltChunk to reference the HTML part
            CTAltChunk altChunk = new ObjectFactory().createCTAltChunk();
            altChunk.setId(rel.getId());

            // Add the altChunk to the document
            wordMLPackage.getMainDocumentPart().getContent().add(altChunk);

            // Save the DOCX to the output stream
            wordMLPackage.save(outputStream);

            return outputStream.toByteArray();
        } catch (Docx4JException | IOException e) {
            logger.error("Error converting HTML to DOCX", e);
            throw new RuntimeException("Failed to convert HTML to DOCX", e);
        }
    }

    /**
     * Converts HTML to RTF.
     * Note: This is a placeholder implementation.
     *
     * @param html HTML content to convert
     * @return RTF as byte array
     */
    public byte[] convertHtmlToRtf(String html) {
        logger.info("Converting HTML to RTF (placeholder implementation)");
        // This is a placeholder implementation
        // In a real implementation, you would use a library like jrtf to convert HTML to RTF
        String message = "RTF conversion not fully implemented. HTML content: " + html.substring(0, Math.min(100, html.length())) + "...";
        return message.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Converts HTML to the specified format.
     *
     * @param html HTML content to convert
     * @param format Target format (pdf, docx, rtf)
     * @return Converted document as byte array
     */
    public byte[] convertHtmlToFormat(String html, String format) {
        logger.info("Converting HTML to format: {}", format);
        return switch (format.toLowerCase()) {
            case "pdf" -> convertHtmlToPdf(html);
            case "docx" -> convertHtmlToDocx(html);
            case "rtf" -> convertHtmlToRtf(html);
            default -> throw new IllegalArgumentException("Unsupported format: " + format);
        };
    }
}
