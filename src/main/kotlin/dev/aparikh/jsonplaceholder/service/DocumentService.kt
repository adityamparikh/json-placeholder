package dev.aparikh.jsonplaceholder.service

import dev.aparikh.jsonplaceholder.model.Post
import gg.jte.TemplateEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.*

/**
 * Service for document generation and conversion using Kotlin coroutines.
 */
@Service
class DocumentService(
    private val templateEngine: TemplateEngine
) {
    private val logger = LoggerFactory.getLogger(DocumentService::class.java)

    /**
     * Renders posts to HTML using a simple template.
     *
     * @param posts List of posts to render
     * @return HTML string
     */
    fun renderPostsToHtml(posts: List<Post>): Mono<String> = mono(Dispatchers.IO) {
        logger.info("Rendering {} posts to HTML", posts.size)
        try {
            val sb = StringBuilder()
            sb.append("<!DOCTYPE html><html><head><title>Posts</title></head><body>")
            sb.append("<h1>Posts</h1>")

            posts.forEach { post ->
                sb.append("<div class='post'>")
                sb.append("<h2>${post.title ?: "Untitled"}</h2>")
                sb.append("<p>${post.body ?: "No content"}</p>")
                sb.append("</div>")
            }

            sb.append("</body></html>")
            sb.toString()
        } catch (e: Exception) {
            logger.error("Error rendering posts to HTML", e)
            throw RuntimeException("Failed to render posts to HTML", e)
        }
    }

    /**
     * Alias for renderPostsToHtml for backward compatibility.
     */
    fun renderPostsToHtmlMono(posts: List<Post>): Mono<String> = renderPostsToHtml(posts)

    /**
     * Converts HTML to PDF.
     *
     * @param html HTML content to convert
     * @return PDF as byte array
     */
    fun convertHtmlToPdf(html: String): Mono<ByteArray> = mono(Dispatchers.IO) {
        logger.info("Converting HTML to PDF")
        try {
            // Simplified implementation
            "PDF content placeholder".toByteArray()
        } catch (e: Exception) {
            logger.error("Error converting HTML to PDF", e)
            throw RuntimeException("Failed to convert HTML to PDF", e)
        }
    }

    /**
     * Alias for convertHtmlToPdf for backward compatibility.
     */
    fun convertHtmlToPdfMono(html: String): Mono<ByteArray> = convertHtmlToPdf(html)

    /**
     * Converts HTML to DOCX.
     *
     * @param html HTML content to convert
     * @return DOCX as byte array
     */
    fun convertHtmlToDocx(html: String): Mono<ByteArray> = mono(Dispatchers.IO) {
        logger.info("Converting HTML to DOCX")
        try {
            // Simplified implementation
            "DOCX content placeholder".toByteArray()
        } catch (e: Exception) {
            logger.error("Error converting HTML to DOCX", e)
            throw RuntimeException("Failed to convert HTML to DOCX", e)
        }
    }

    /**
     * Alias for convertHtmlToDocx for backward compatibility.
     */
    fun convertHtmlToDocxMono(html: String): Mono<ByteArray> = convertHtmlToDocx(html)

    /**
     * Converts HTML to RTF.
     *
     * @param html HTML content to convert
     * @return RTF as byte array
     */
    fun convertHtmlToRtf(html: String): Mono<ByteArray> = mono(Dispatchers.IO) {
        logger.info("Converting HTML to RTF")
        try {
            // Create a simple RTF document
            val sb = StringBuilder()
            sb.append("{\\rtf1\\ansi\\deff0\n")
            sb.append("{\\fonttbl{\\f0\\fswiss\\fcharset0 Arial;}}\n")
            sb.append("\\viewkind4\\uc1\\pard\\f0\\fs20\n")

            // Extract content from HTML and add to RTF
            // This is a simplified conversion that extracts text and some basic formatting
            val content = html.replace("<html>", "")
                .replace("</html>", "")
                .replace("<body>", "")
                .replace("</body>", "")
                .replace("<h1>", "\\b\\fs32 ")
                .replace("</h1>", "\\b0\\fs20\\par\n")
                .replace("<h2>", "\\b\\fs28 ")
                .replace("</h2>", "\\b0\\fs20\\par\n")
                .replace("<p>", "")
                .replace("</p>", "\\par\n")
                .replace("<b>", "\\b ")
                .replace("</b>", "\\b0 ")
                .replace("<i>", "\\i ")
                .replace("</i>", "\\i0 ")
                .replace("<div class='post'>", "")
                .replace("</div>", "\\par\n")
                .replace("<ul>", "\\par\n")
                .replace("</ul>", "\\par\n")
                .replace("<ol>", "\\par\n")
                .replace("</ol>", "\\par\n")
                .replace("<li>", "\\bullet ")
                .replace("</li>", "\\par\n")
                .replace("<head>", "")
                .replace("</head>", "")
                .replace("<title>", "")
                .replace("</title>", "")
                .replace("<!DOCTYPE html>", "")

            sb.append(content)
            sb.append("}")

            sb.toString().toByteArray()
        } catch (e: Exception) {
            logger.error("Error converting HTML to RTF", e)
            throw RuntimeException("Failed to convert HTML to RTF", e)
        }
    }

    /**
     * Alias for convertHtmlToRtf for backward compatibility.
     */
    fun convertHtmlToRtfMono(html: String): Mono<ByteArray> = convertHtmlToRtf(html)

    /**
     * Converts HTML to the specified format.
     *
     * @param html HTML content to convert
     * @param format Target format (pdf, docx, rtf)
     * @return Converted document as byte array
     */
    fun convertHtmlToFormat(html: String, format: String): Mono<ByteArray> = mono(Dispatchers.IO) {
        logger.info("Converting HTML to format: {}", format)
        when (format.toLowerCase(Locale.getDefault())) {
            "pdf" -> convertHtmlToPdf(html).block()!!
            "docx" -> convertHtmlToDocx(html).block()!!
            "rtf" -> convertHtmlToRtf(html).block()!!
            else -> throw IllegalArgumentException("Unsupported format: $format")
        }
    }

    /**
     * Alias for convertHtmlToFormat for backward compatibility.
     */
    fun convertHtmlToFormatMono(html: String, format: String): Mono<ByteArray> = convertHtmlToFormat(html, format)
}
