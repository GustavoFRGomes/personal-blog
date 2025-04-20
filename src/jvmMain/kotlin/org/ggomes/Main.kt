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

fun main() {
    val contentDir = File("content/posts")
    val outputDir = File("build/site")
    val posts = contentDir.listFiles { _, name -> name.endsWith(".md") }?.map { file ->
        val markdown = file.readText()
        val parser = Parser.builder()
            .extensions(listOf(TablesExtension.create(), StrikethroughExtension.create()))
            .build()
        val doc = parser.parse(markdown)
        val renderer = HtmlRenderer.builder()
            .extensions(listOf(TablesExtension.create(), StrikethroughExtension.create()))
            .build()
        val html = renderer.render(doc)
        val title = file.nameWithoutExtension.replace('-', ' ').capitalizeWords()
        val date = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        Post(title, date, html, file.nameWithoutExtension + ".html")
    }?.sortedByDescending { it.date } ?: emptyList()

    outputDir.mkdirs()
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

data class Post(val title: String, val date: String, val content: String, val slug: String)

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
        ul {
            posts.forEach { post ->
                li {
                    a(href = post.slug) { +post.title }
                    span { +" - ${post.date}" }
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
        p { +post.date }
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
    "  <item>\n    <title>${post.title}</title>\n    <link>$URL/${post.slug}</link>\n    <pubDate>${ZonedDateTime.parse(post.date).format(DateTimeFormatter.RFC_1123_DATE_TIME)}</pubDate>\n    <description><![CDATA[${post.content}]]></description>\n  </item>"
}}
</channel>
</rss>"""
}

fun String.capitalizeWords(): String = split(' ').joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
