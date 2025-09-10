---
date: 2025-04-27
draft: true
---
# What can AI not do?
In this blog post I will try to define what AI can do, which helps us as developers and professionals, and perhaps what
are some limitations on the LLMs that currently exist.

I have been using a few different _AI Editors_ and I have some thoughts on it. Mainly I will list what are, in my
opinion, the advantages and disadvantages but also will talk about the final verdict.

So far I have used the following tools:
1. Cursor
2. Windsurf
3. GitHub Copilot in Android Studio
4. Junie in IntelliJ IDEA

I have to say that most of the time it does support what you want to achieve even if you only have a clue more or less about
what you want to do but you also are a lazy person that doesn't want to make the initial, often times boring, setup then
these tools are your best friend. You can quickly write some initial code or scaffolding to which you can easily then expand
with the core idea that you want to achieve.

As a personal example, I was trying to get to grips with making this blog, but I truly hate CSS and have been, at least to
me, spoiled about how to build UI in Android, both with XML views and the new Compose framework. So I always would postpone
making a website, because I also think that I can only be one of those "cool" devs if I roll my own blog from scratch. All
of this led me to Junie and actually trying to make a blog out of Kotlin, I wanted to be able to write posts
like this one using Markdown and then having it rendered as a static page in my website, therefore the Kotlin code would
mainly compile my Markdown into a somewhat readable and not super hideous web page.
I grabbed onto Windsurf and just prompted basically for just that, some Kotlin code that could let me go from Markdown all
the way into an index page which lists my articles by the title and by date, and when the user clicks on an article they
should be able to go into the full article view.

After waiting a few seconds Junie had basically coded for me all the initial setup. Afterwards I basically just had to adjust
a part of it, with things such as adding a "metadata" section with a date, adding also a flag to symbolize if a post is draft
so that it doesn't get published, as well as test out some Markdown things that I would need to have.

Overall Junie did everything I asked with a big degree of competency for the small tasks that I prompted every now and again.
But one thing that is also important was that Junie, and even general purpose LLMs failed me when it came to the CI that I
also inteded to have. Basically this project resides in the repo: https://github.com/GustavoFRGomes/kmp-blog/ and I intended
to have Github Actions that would let me commit something to the repo and the CI would build the website and publish it through
Cloudflare Pages. Junie and also Claude 3.7 Sonnet failed me when I wanted to write the CI for it, they did pushed me forward
but in the end the solution was failing the CI pipeline.
Mainly because Junie wanted to have a Github Token for some reason on the Action to publish and I also think that there were
issues with the version of the image that handles the publishing to Cloudflare Pages.
Claude also failed me, as Junie did before, for me to setup Cloudflare so that I could come up with a project and also a token
to be used, but these are just small hurdles that "vibe coders" like myself in this endeavour need to be aware of. Because
if we let the LLM simply go through all of the project then we completely lose the notion of how the project is structured,
as well as, what the goal is and how it is implemented.

## Would I use AI tools again for my coding?
In all honesty, I will, but not in the same way as for example Github Copilot, which acts more like an auto-complete++ rather
than a "rubber duck" that can talk back with good ideas. I also noted that every now and again, I see issues when it comes to
the versions that are being proposed or generated. They can be one of two categories:
1. older version that you can just normally upgrade
2. hallucinated version that you need to double check
For older versions most of the time you can realise it or your IDE will point them out, but the funniest occasions for me is
when there is code generated and you try to compile just to see your IDE break the build process saying that the version
could not be found. For the latter I have witnessed myself trying to see if my Internet dropped or my VPN was doing something
funny before I realised and learned that these AI tools somethings get things wrong. They can even give you methods that are
either non existent or were already deprecated and removed a long time ago.

So if you intend to use AI as a kind of assistant but still be able to just ask for some clarification, then I think it is
going to supercharge you. The other option is if you increasingly start trusting AI more and more, if you let it creep into
your daily work and you start to not vet the generated code/output then there will be a time that you will start to regress
on your expertise and knowledge, since you are not learning anything but rather blindly trusting some tool.
There is a parallel from the times of our good ol' StackOverflow, where some people would get their answers or adapt someone's
answers to their problem and understood what they were looking at, and then there were those devs who would just copy and
paste the answer, see if it compiled and just moved on to the next hurdle/blocker in their way. AI will have the same sort of
"mechanics" for its users.

## Will AI take over developer's jobs?
I, personally, think not. That's the short answer, and a more elaborate one is that there are these hallucinations, and
besides those we also need to think how AI works and is trained. If we boil it down, none of models can do human reasoning
at all. The current models can basically predict and seem to understand a problem, but they are very limited to what is prompted,
as in if you give them a very generic prompt they will also give either a generic answer or a specific answer that does not
actually solve your problem (maybe it will give you the wrong setup, wrong language or even fake methods).

As developers we need to look at AI as a tool that can help us do more coding with less typing. Specially mudane tasks such as
serializing JSON into a data class, or writing the initial setup for an endpoint. These simpler tasks can be a great way to
ease the mental burden and the strain on your wrists, but when you need to mishmash data together, build some beautiful animations
or make some wacky requests to a server because the person who built it is a couple generations older than you and "knows best"
then you should trust yourself to do it.

## Conclusion
You should try out some of the tools, I only brushed on a couple of them as I do not want to just delve into the pros and cons of
each one. Rather I want to just ask the reader to take a look at some of them, don't be afraid to try them out and see what it can
bring to each one of you; and always be aware that they are not perfect nor a replacement for our grey matter.
