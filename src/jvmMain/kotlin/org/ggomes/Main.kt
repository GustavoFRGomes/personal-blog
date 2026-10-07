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

    // Model reviews
    val modelReviewsDir = File("content/model-reviews")
    val modelReviews = modelReviewsDir.listFiles { _, name -> name.endsWith(".md") }?.map { file ->
        val md = file.readText()
        val cleanMd = md.replace("""^---\s*\n(.*?\n)---\s*\n""".toRegex(RegexOption.DOT_MATCHES_ALL), "")
            .replace("""^date:\s*.+$""".toRegex(RegexOption.MULTILINE), "")
        val parser = Parser.builder().extensions(listOf(TablesExtension.create(), StrikethroughExtension.create())).build()
        val doc = parser.parse(cleanMd)
        val renderer = HtmlRenderer.builder().extensions(listOf(TablesExtension.create(), StrikethroughExtension.create())).build()
        val html = renderer.render(doc)
        val title = file.nameWithoutExtension.replace('-', ' ').capitalizeWords()
        val preview = extractPreview(html)
        val dateMatch = """^date:\s*(.+)$""".toRegex(RegexOption.MULTILINE).find(md)
        val date = dateMatch?.groupValues?.get(1)?.trim() ?: ""
        val draftMatch = """^draft:\s*(true|false)$""".toRegex(RegexOption.MULTILINE).find(md)
        val isDraft = draftMatch?.groupValues?.get(1)?.trim() == "true"
        Post(title, date, html, "model-reviews/" + file.nameWithoutExtension + ".html", preview, isDraft)
    }?.filter { !it.isDraft }?.sortedByDescending { it.date } ?: emptyList()

    val modelReviewsOutputDir = File(outputDir, "model-reviews")
    modelReviewsOutputDir.mkdirs()
    File(modelReviewsOutputDir, "index.html").writeText(generateModelReviewsIndex(modelReviews))
    modelReviews.forEach { review ->
        File(modelReviewsOutputDir, review.slug.removePrefix("model-reviews/")).writeText(generatePost(review, modelReviews = true))
    }

    // Presentations
    val presentationsDir = File(outputDir, "presentations")
    presentationsDir.mkdirs()
    File(presentationsDir, "index.html").writeText(generatePresentationIndex())
    File(presentationsDir, "droidcon-2026-berlin-vector-search.html").writeText(generatePresentationPage(
        title = "DroidCon Berlin 2026 - Vector Search",
        description = "Presentation on Vector Search techniques for mobile applications",
        pdfUrl = "presentations/droidcon-2026-berlin-vector-search.pdf",
        downloadUrl = "https://c.mail.com/@1237970809700491004/GyAkJIWvgDG4f-iCN7UoTQ",
        date = "2026-04-15"
    ))

    File(outputDir, "rss.xml").writeText(generateRSS(posts))
    // Puzzle page:
    File(outputDir, "puzzle.html").writeText(kotlinPuzzle)
    // Business card page:
    File(outputDir, "card.html").writeText(generateBusinessCard())

    // copy static assets from `static` directory to output (recursively)
    copyRecursively(File("static"), outputDir)
}

/**
 * Recursively copies files from source to destination directory
 */
fun copyRecursively(source: File, destination: File) {
    if (source.isDirectory) {
        destination.mkdirs()
        source.listFiles()?.forEach { file ->
            copyRecursively(file, File(destination, file.name))
        }
    } else if (source.isFile) {
        destination.parentFile?.mkdirs()
        source.copyTo(destination, overwrite = true)
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

fun generateModelReviewsIndex(reviews: List<Post>): String = createHTML().html {
    head {
        meta(charset = "utf-8")
        title("AI Model Reviews")
        link(rel = "stylesheet", href = "../style.css")
    }
    body {
        button(classes = "theme-toggle") { id = "theme-toggle"; +"🌙" }
        a(href = "../index.html") { +"← My Blog" }
        h1 { +"AI Model Reviews" }
        div(classes = "post-list") {
            reviews.forEach { review ->
                div(classes = "post-item") {
                    h2 { a(href = review.slug.removePrefix("model-reviews/")) { +review.title } }
                    p(classes = "post-date") { +review.formattedDate() }
                    div(classes = "post-preview") { unsafe { +review.preview } }
                    a(href = review.slug.removePrefix("model-reviews/"), classes = "read-more") { +"Read review..." }
                }
            }
        }
        hr {}
        generateSocialLinks()()
        script(src = "../theme.js") {}
    }
}

fun generateSocialLinks(): FlowContent.() -> Unit = {
    div(classes = "social-links") {
        h3 { +"Connect with me" }
        ul {
            li { 
                a(href = "https://github.com/GustavoFRGomes", classes = "icon github") { 
                    span(classes = "sr-only") { +"GitHub" }
                } 
            }
            li { 
                a(href = "https://codeberg.org/ggomes", classes = "icon codeberg") { 
                    span(classes = "sr-only") { +"Codeberg" }
                } 
            }
            li { 
                a(href = "https://bsky.app/profile/gfrgomes.bsky.social", classes = "icon bluesky") { 
                    span(classes = "sr-only") { +"Bluesky" }
                } 
            }
            li { 
                a(href = "https://linkedin.com/in/gustavo-gomes-501878b0", classes = "icon linkedin") { 
                    span(classes = "sr-only") { +"LinkedIn" }
                } 
            }
            li { 
                a(href = "mailto:ggomes@mail.com", classes = "icon email") { 
                    span(classes = "sr-only") { +"Email" }
                } 
            }
            li { 
                a(href = "rss.xml", classes = "icon rss") { 
                    span(classes = "sr-only") { +"RSS" }
                } 
            }
            li { 
                a(href = "presentations/index.html", classes = "icon presentations") { 
                    span(classes = "sr-only") { +"Presentations" }
                } 
            }
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
        button(classes = "theme-toggle") { id = "theme-toggle"; +"🌙" }
        nav {
            a(href = "index.html") { +"My Blog" }
            +" | "
            a(href = "model-reviews/index.html") { +"AI Model Reviews" }
            +" | "
            a(href = "presentations/index.html") { +"Presentations" }
        }
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
        script(src = "theme.js") {}
    }
}

fun generatePost(post: Post, modelReviews: Boolean = false): String = createHTML().html {
    head {
        meta(charset = "utf-8")
        title(post.title)
        link(rel = "stylesheet", href = if (modelReviews) "../style.css" else "style.css")
    }
    body {
        button(classes = "theme-toggle") { id = "theme-toggle"; +"🌙" }
        a(href = if (modelReviews) "index.html" else "index.html") { if (modelReviews) +"← Back" else +"← Back to home" }
        h1 { +post.title }
        p { +post.formattedDate() }
        unsafe { +post.content }
        hr {}
        generateSocialLinks()()
        script(src = if (modelReviews) "../theme.js" else "theme.js") {}
    }
}

fun generateBusinessCard(): String = createHTML().html {
    head {
        meta(charset = "utf-8")
        title("Business Card")
        link(rel = "stylesheet", href = "style.css")
    }
    body {
        button(classes = "theme-toggle") { id = "theme-toggle"; +"🌙" }
        a(href = "index.html") { +"← Back to home" }
        div(classes = "business-card") {
            h1 { +"Gustavo Gomes" }
            p { +"Senior Mobile Developer @ 1&1 Mail & Media" }
            div(classes = "links") {
                a(href = "https://github.com/GustavoFRGomes", classes = "icon github") {
                    span(classes = "sr-only") { +"GitHub" }
                }
                a(href = "https://codeberg.org/ggomes", classes = "icon codeberg") {
                    span(classes = "sr-only") { +"Codeberg" }
                }
                a(href = "https://bsky.app/profile/gfrgomes.bsky.social", classes = "icon bluesky") {
                    span(classes = "sr-only") { +"Bluesky" }
                }
                a(href = "https://linkedin.com/in/gustavo-gomes-501878b0", classes = "icon linkedin") {
                    span(classes = "sr-only") { +"LinkedIn" }
                }
                a(href = "mailto:ggomes@mail.com", classes = "icon email") {
                    span(classes = "sr-only") { +"Email" }
                }
                a(href = "rss.xml", classes = "icon rss") {
                    span(classes = "sr-only") { +"RSS" }
                }
            }
        }
        script(src = "theme.js") {}
    }
}

fun generatePresentationIndex(): String = createHTML().html {
    head {
        meta(charset = "utf-8")
        title("Presentations")
        link(rel = "stylesheet", href = "style.css")
    }
    body {
        button(classes = "theme-toggle") { id = "theme-toggle"; +"🌙" }
        a(href = "../index.html") { +"← Back to home" }
        h1 { +"Presentations" }
        div(classes = "post-list") {
            div(classes = "post-item") {
                h2 { a(href = "droidcon-2026-berlin-vector-search.html") { +"DroidCon Berlin 2026 - Vector Search" } }
                p(classes = "post-date") { +"April 15, 2026" }
                p(classes = "post-preview") { +"Presentation on Vector Search techniques for mobile applications" }
                a(href = "droidcon-2026-berlin-vector-search.html", classes = "read-more") { +"View presentation..." }
            }
        }
        hr {}
        generateSocialLinks()()
        script(src = "../theme.js") {}
    }
}

fun generatePresentationPage(title: String, description: String, pdfUrl: String, downloadUrl: String, date: String): String = createHTML().html {
    head {
        meta(charset = "utf-8")
        title(title)
        link(rel = "stylesheet", href = "../style.css")
    }
    body {
        button(classes = "theme-toggle") { id = "theme-toggle"; +"🌙" }
        a(href = "../index.html") { +"← Back to home" }
        div(classes = "business-card") {
            h1 { +title }
            p { +description }
            p(classes = "post-date") { 
                a(href = pdfUrl, classes = "download-link") { +"Download slides (PDF)" }
                a(href = downloadUrl, classes = "download-link", target = "_blank") { +"View on Mail.com" }
            }
        }
        hr {}
        generateSocialLinks()()
        script(src = "../theme.js") {}
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

private val kotlinPuzzle = """
<!DOCTYPE html>
<html>
<head>
    <title>Kotlin Brain-Teaser: Enum Class</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
        }
        pre {
            background: #f4f4f4;
            padding: 15px;
            border-radius: 5px;
            overflow-x: auto;
        }
        code {
            font-family: 'Courier New', monospace;
        }
        button {
            padding: 8px 16px;
            background: #0073aa;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 16px;
            margin: 10px 0;
        }
        button:hover {
            background: #005f8a;
        }
        #explanation {
            display: none;
            background: #f9f9f9;
            padding: 15px;
            border-radius: 5px;
            border-left: 4px solid #0073aa;
        }
    </style>
</head>
<body>
    <h1>Kotlin Brain-Teaser</h1>
    <p>What does this code output? Or does it error?</p>
    <pre><code>
enum class
private enum class Visibility { Visible, Hidden }
fun main() {
    println("Nothing to see here…")
}
    </code></pre>
    <button onclick="reveal()">Reveal Explanation</button>
    <div id="explanation">
        <p>
            This puzzle's first line of code is an unfinished enum class declaration that's missing its name and body.
            So why doesn't the compiler reject it?
            A class declaration doesn't end at the first line break—it can span multiple lines, continuing until the compiler determines that it's syntactically complete.
            The formatting makes it look like <code>private</code> is the start of a new declaration, but in fact, it's the end of the first one, and acts as our missing class name.
            A class doesn't require a body, and doesn't need to end with a final line break.
            That means <code>enum class private</code> is a complete (if oddly named) enum declaration, and the code compiles and runs, printing:
        </p>
        <pre><code>Nothing to see here…</code></pre>
    </div>

    <script>
        function reveal() {
            document.getElementById('explanation').style.display = 'block';
        }
    </script>
</body>
</html>
"""