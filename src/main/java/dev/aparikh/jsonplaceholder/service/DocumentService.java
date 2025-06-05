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
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
    public Mono<String> renderPostsToHtml(List<Post> posts) {
        logger.info("Rendering {} posts to HTML", posts.size());
        return Mono.fromCallable(() -> {
            StringOutput output = new StringOutput();

            // Convert Post objects to PostDto objects to avoid classloader issues
            List<PostDto> postDtos = posts.stream()
                .map(PostDto::new)
                .collect(Collectors.toList());

            templateEngine.render("posts.jte", postDtos, output);
            return output.toString();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Converts HTML to PDF.
     *
     * @param html HTML content to convert
     * @return PDF as byte array
     */
    public Mono<byte[]> convertHtmlToPdf(String html) {
        logger.info("Converting HTML to PDF");
        return Mono.fromCallable(() -> {
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
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Converts HTML to DOCX using docx4j and jsoup.
     *
     * @param html HTML content to convert
     * @return DOCX as byte array
     */
    public Mono<byte[]> convertHtmlToDocx(String html) {
        logger.info("Converting HTML to DOCX");
        return Mono.fromCallable(() -> {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                // Create a new WordprocessingMLPackage
                WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();

                // Create an AlternativeFormatInputPart for the HTML content
                AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(AltChunkType.Html);

                // Ensure the HTML has proper structure with html, head, and body tags
                String processedHtml = html;
                if (!processedHtml.trim().toLowerCase().startsWith("<!doctype html>") && 
                    !processedHtml.trim().toLowerCase().startsWith("<html")) {
                    processedHtml = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head><body>" + processedHtml + "</body></html>";
                }

                // Set the HTML content
                afiPart.setBinaryData(processedHtml.getBytes(StandardCharsets.UTF_8));

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
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Converts HTML to RTF.
     *
     * @param html HTML content to convert
     * @return RTF as byte array
     */
    public Mono<byte[]> convertHtmlToRtf(String html) {
        logger.info("Converting HTML to RTF");
        return Mono.fromCallable(() -> {
            try {
                // Parse HTML with Jsoup
                Document document = Jsoup.parse(html);
                
                // Start RTF document
                StringBuilder rtf = new StringBuilder();
                rtf.append("{\\rtf1\\ansi\\deff0 {\\fonttbl {\\f0 Times New Roman;}}");
                
                // Process the document body
                Element body = document.body();
                processElementsToRtf(body, rtf);
                
                // Close RTF document
                rtf.append("}");
                
                return rtf.toString().getBytes(StandardCharsets.UTF_8);
            } catch (Exception e) {
                logger.error("Error converting HTML to RTF", e);
                throw new RuntimeException("Failed to convert HTML to RTF", e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    /**
     * Recursively processes HTML elements and converts them to RTF.
     *
     * @param element HTML element to process
     * @param rtf StringBuilder to append RTF content to
     */
    private void processElementsToRtf(Element element, StringBuilder rtf) {
        for (Element child : element.children()) {
            String tagName = child.tagName().toLowerCase();
            
            switch (tagName) {
                case "h1", "h2", "h3", "h4", "h5", "h6":
                    rtf.append("\\par\\b ");
                    rtf.append(escapeRtfText(child.text()));
                    rtf.append("\\b0\\par ");
                    break;
                case "p":
                    rtf.append("\\par ");
                    rtf.append(escapeRtfText(child.text()));
                    rtf.append("\\par ");
                    break;
                case "br":
                    rtf.append("\\par ");
                    break;
                case "b", "strong":
                    rtf.append("\\b ");
                    rtf.append(escapeRtfText(child.text()));
                    rtf.append("\\b0 ");
                    break;
                case "i", "em":
                    rtf.append("\\i ");
                    rtf.append(escapeRtfText(child.text()));
                    rtf.append("\\i0 ");
                    break;
                case "u":
                    rtf.append("\\ul ");
                    rtf.append(escapeRtfText(child.text()));
                    rtf.append("\\ul0 ");
                    break;
                case "ul":
                    processListToRtf(child, rtf, true);
                    break;
                case "ol":
                    processListToRtf(child, rtf, false);
                    break;
                case "li":
                    // List items are handled by processListToRtf
                    break;
                default:
                    // For other elements, just process their text content
                    if (child.hasText()) {
                        rtf.append(escapeRtfText(child.text()));
                        rtf.append(" ");
                    }
                    // Recursively process child elements
                    processElementsToRtf(child, rtf);
                    break;
            }
        }
    }
    
    /**
     * Processes HTML lists and converts them to RTF.
     *
     * @param listElement HTML list element (ul or ol)
     * @param rtf StringBuilder to append RTF content to
     * @param isUnordered true for unordered lists, false for ordered lists
     */
    private void processListToRtf(Element listElement, StringBuilder rtf, boolean isUnordered) {
        Elements listItems = listElement.children();
        int itemNumber = 1;
        
        for (Element item : listItems) {
            if ("li".equalsIgnoreCase(item.tagName())) {
                rtf.append("\\par ");
                if (isUnordered) {
                    rtf.append("\\bullet ");
                } else {
                    rtf.append(itemNumber).append(". ");
                    itemNumber++;
                }
                rtf.append(escapeRtfText(item.text()));
            }
        }
        rtf.append("\\par ");
    }
    
    /**
     * Escapes special characters for RTF format.
     *
     * @param text Text to escape
     * @return Escaped text
     */
    private String escapeRtfText(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                  .replace("{", "\\{")
                  .replace("}", "\\}")
                  .replace("\n", "\\par ")
                  .replace("\r", "");
    }

    /**
     * Converts HTML to the specified format.
     *
     * @param html HTML content to convert
     * @param format Target format (pdf, docx, rtf)
     * @return Converted document as byte array
     */
    public Mono<byte[]> convertHtmlToFormat(String html, String format) {
        logger.info("Converting HTML to format: {}", format);
        return switch (format.toLowerCase()) {
            case "pdf" -> convertHtmlToPdf(html);
            case "docx" -> convertHtmlToDocx(html);
            case "rtf" -> convertHtmlToRtf(html);
            default -> Mono.error(new IllegalArgumentException("Unsupported format: " + format));
        };
    }
}
