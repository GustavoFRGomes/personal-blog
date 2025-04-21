package org.ggomes

import java.io.File
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import kotlinx.html.*
import kotlinx.html.stream.createHTML

private const val URL = "https://ggomes.org"
private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy")
private val ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")

fun main() {
    val contentDir = File("content/posts")
    val outputDir = File("build/site")
    val posts = contentDir.listFiles { _, name -> name.endsWith(".md") }?.map { file ->
        var markdown = file.readText()

        // Check for date metadata
        val dateMetadataRegex = """^date:\s*(.+)$""".toRegex(RegexOption.MULTILINE)
        val dateMatch = dateMetadataRegex.find(markdown)

        // Check for draft metadata
        val draftMetadataRegex = """^draft:\s*(true|false)$""".toRegex(RegexOption.MULTILINE)
        val draftMatch = draftMetadataRegex.find(markdown)
        val isDraft = draftMatch?.groupValues?.get(1)?.trim() == "true"


        val date = if (dateMatch != null) {
            // Use existing date from metadata
            dateMatch.groupValues[1].trim()
        } else {
            // Add date metadata to the file in YAML frontmatter format
            val currentDate = ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val dateMetadata = "---\ndate: $currentDate\n---\n"

            // Clean up the markdown content first
            val cleanMarkdown = markdown.trimStart()

            // Add YAML frontmatter at the beginning of the file
            markdown = dateMetadata + cleanMarkdown

            // Update the file with the new metadata
            file.writeText(markdown)

            // Return the current date in ISO format for the Post object
            ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        }

        // Remove YAML frontmatter and standalone date lines before parsing to avoid them showing in the rendered HTML
        var markdownWithoutMetadata = markdown

        // Remove YAML frontmatter if present
        markdownWithoutMetadata = markdownWithoutMetadata.replace("""^---\s*\n(.*?\n)---\s*\n""".toRegex(RegexOption.DOT_MATCHES_ALL), "")

        // Also remove standalone date lines (for backward compatibility)
        markdownWithoutMetadata = markdownWithoutMetadata.replace("""^date:\s*.+$""".toRegex(RegexOption.MULTILINE), "")

        val parser = Parser.builder()
            .extensions(listOf(TablesExtension.create(), StrikethroughExtension.create()))
            .build()
        val doc = parser.parse(markdownWithoutMetadata)
        val renderer = HtmlRenderer.builder()
            .extensions(listOf(TablesExtension.create(), StrikethroughExtension.create()))
            .build()
        val html = renderer.render(doc)
        val title = file.nameWithoutExtension.replace('-', ' ').capitalizeWords()

        // Extract preview - either first paragraph or first 3000 characters
        val preview = extractPreview(html)

        Post(title, date, html, file.nameWithoutExtension + ".html", preview, isDraft)
    }?.filter { !it.isDraft }?.sortedByDescending { it.date } ?: emptyList()

    // Clean up the output directory and create it if it doesn't exist
    if (outputDir.exists()) {
        outputDir.listFiles()?.forEach { file ->
            if (file.isFile && file.extension == "html") {
                file.delete()
            }
        }
    } else {
        outputDir.mkdirs()
    }

    // Generate site files
    File(outputDir, "index.html").writeText(generateIndex(posts))
    posts.forEach { post ->
        File(outputDir, post.slug).writeText(generatePost(post))
    }
    File(outputDir, "rss.xml").writeText(generateRSS(posts))

    // copy static assets from `static` directory to output
    val staticDir = File("static")
    if (staticDir.exists()) {
        staticDir.listFiles()?.forEach { file ->
            file.copyTo(File(outputDir, file.name), overwrite = true)
        }
    }
}

data class Post(val title: String, val date: String, val content: String, val slug: String, val preview: String, val isDraft: Boolean = false) {
    fun formattedDate(): String {
        return try {
            // Try to parse the date using ISO_DATE_FORMATTER
            val localDate = java.time.LocalDate.parse(date, ISO_DATE_FORMATTER)
            // Convert to ZonedDateTime and format with DATE_FORMATTER
            localDate.atStartOfDay(java.time.ZoneId.of("UTC")).format(DATE_FORMATTER)
        } catch (e: Exception) {
            // If parsing fails, return the original date string
            date
        }
    }
}

fun generateSocialLinks(): FlowContent.() -> Unit = {
    div(classes = "social-links") {
        h3 { +"Connect with me" }
        ul {
            li { a(href = "https://github.com/GustavoFRGomes", classes = "icon github") { +"" } }
            li { a(href = "https://bsky.app/profile/gfrgomes.bsky.social", classes = "icon bluesky") { +"" } }
            li { a(href = "https://linkedin.com/in/gustavo-gomes-501878b0", classes = "icon linkedin") { +"" } }
            li { a(href = "mailto:ggomes@mail.com", classes = "icon email") { +"" } }
            li { a(href = "rss.xml", classes = "icon rss") { +"" } }
        }
    }
}

fun generateIndex(posts: List<Post>): String = createHTML().html {
    head {
        meta(charset = "utf-8")
        title("My Blog")
        link(rel = "stylesheet", href = "style.css")
    }
    body {
        h1 { +"My Blog" }
        div(classes = "post-list") {
            posts.forEach { post ->
                div(classes = "post-item") {
                    h2 {
                        a(href = post.slug) { +post.title }
                    }
                    p(classes = "post-date") { +post.formattedDate() }
                    div(classes = "post-preview") {
                        unsafe { +post.preview }
                    }
                    a(href = post.slug, classes = "read-more") { +"Read more..." }
                }
            }
        }
        hr {}
        generateSocialLinks()()
    }
}

fun generatePost(post: Post): String = createHTML().html {
    head {
        meta(charset = "utf-8")
        title(post.title)
        link(rel = "stylesheet", href = "style.css")
    }
    body {
        a(href = "index.html") { +"← Back to home" }
        h1 { +post.title }
        p { +post.formattedDate() }
        unsafe { +post.content }
        hr {}
        generateSocialLinks()()
    }
}

fun generateRSS(posts: List<Post>): String {
    val now = ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME)
    return """<?xml version="1.0" encoding="UTF-8"?>
<rss version="2.0">
<channel>
  <title>My Blog</title>
  <link>$URL</link>
  <description>My personal blog</description>
  <lastBuildDate>$now</lastBuildDate>
${posts.joinToString("\n") { post ->
    "  <item>\n    <title>${post.title}</title>\n    <link>$URL/${post.slug}</link>\n    <pubDate>${ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME)}</pubDate>\n    <description><![CDATA[${post.content}]]></description>\n  </item>"
}}
</channel>
</rss>"""
}

fun String.capitalizeWords(): String = split(' ').joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

/**
 * Extracts a preview from HTML content.
 * Returns either the first paragraph or the first 3000 characters.
 */
fun extractPreview(html: String): String {
    // Try to find the first paragraph
    val paragraphRegex = "<p>(.*?)</p>".toRegex(RegexOption.DOT_MATCHES_ALL)
    val firstParagraphMatch = paragraphRegex.find(html)

    return if (firstParagraphMatch != null) {
        // Return the content of the first paragraph
        firstParagraphMatch.groupValues[1]
    } else {
        // If no paragraph found, return first 3000 characters (stripped of HTML tags)
        val noHtmlTags = html.replace("<[^>]*>".toRegex(), "")
        if (noHtmlTags.length <= 3000) noHtmlTags else noHtmlTags.take(3000) + "..."
    }
}
