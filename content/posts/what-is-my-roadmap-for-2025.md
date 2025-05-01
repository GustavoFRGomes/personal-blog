---
date: 2025-05-01
---

So first and foremost I want to say that I have some ideas and even some concrete things that I want
to accomplish by the end of the year. I also am aware that I am writing this with a little over half
a year to go, therefore I might not reach some of my goals but it's good to write them down 
nonetheless, because it creates a sense of commitment.

## Initiatives that I am starting
1. Create a newsletter
2. Improve this blog, mainly the looks of it
3. Publish one app in Google Play
4. Launch a product with apps written in KMP
5. Write at least 1 blogpost per month
6. Make a device info SDK

So that is my list, not extremely ambitious but also not that small. Maybe I can break down some of
the items.

### Create a newsletter
I have been looking at the newsletters that I follow along and they are already quite good, and my
main takeaway is that sometimes they get a bit too big and want to reach the biggest audience there
is. Of course that is a valid argument but then we start seeing newsletters with courses, a job
board, sponsored content, etc...and this kind of thing makes it a bit more harder to enjoy. You have
to filter out the non-important information and find the gold nuggets buried within them.

My idea which is not novel, in any way shape or form, is to "roll my own" newsletter. Basically I
have been playing with the idea of an aggregator of articles for some time and also launched a
Cloudflare worker already to parse the RSS feed for a set of blogs from developers inside the
Android and Kotlin community. I also have a list of blogs from other devs that do not have an RSS
feed but that I want to build scrapers for.
The main goal is to publish a set of articles monthly or bi-monthly. But I have to do a couple of
things first, namely I want to also reach out to them and ask if that is okay, and I also want to
build what is left of the aggregator. The last bit is far more fun for me which will be curating the
articles going into each new edition, it's going to be fun because I am sure I will learn a lot when
I am reading each article to see if it should go on the list or not.

So the goal is simple, but the breakdown will be long:
* Make HTML parser for blogs that do not have RSS
* Ask for consent from creators to link my newsletter to their website
* Carve out some time during the cadence period to keep reviewing articles
* Make a notification system so I know when new articles are published
* Create a pretty HTML (& CSS) newsletter template
* Make a webpage to enable users to subscribe via email
* Create mechanism to unsubscribe and manage subscription

### Improving this blog
I don't personally love the currently look, but I also do not want to "waste" time learning CSS in
order to make it pretty. This will be something that I want to do in the upcoming months, just tweak
the blog a bit here nad there to make it a bit nicer, but I also do not want to make it bloated and
super futuristic with the CSS skills I will probably vibe code and not really learn that much of.

I hope at least I can make a better UI, and I don't want to change too much, as I like the current
simplicity of it. I am thinking just of minor cosmetic improvements for now.

There is no concrete timeline for this, I will just iterate over it from time to time.

### Publish one app in Google Play
This one is a bit more vague, I want to publish an app but I am also haunted by the initial setup
that one must make to make it work.

I am also not sure which of my ideas will also become a reality, but this goal should push me to do
at least one of them to completion.

### Launch a product with apps written in KMP
This is going to be as vague and short as the previous one, perhaps they can overlap but it is
highly dependent on me learning KMP.

### Write at least 1 blog post per month
My initial goal was to write a lot of blogposts, but in the face of my ambitious goals and poor
execution I will actually be strict and aim low. I think 1 blog post is enough per month, that is a
low barrier and it does not in any way shape or form mean that I cannot write more posts.

There are also some ideas, specially around technical content that I will be posting soon. I have in
my personal notes topics such as:
* Android WorkManager deep dive
* Kotlin 2.x features of the language and future
* How I built the aggregator + Cloudflare setup
* CLI tools and how to build them

### Make a device info SDK
I have an idea to make a device info SDK, basically I want to fetch some information that the
Android OS can give both as a way to show all the things that are possible to "track" without the
need for runtime permissions (there is a nice chunk) and also to enable companies and indie devs to
use a nice SDK to get some of that info, that is currently scattered across multiple different
Android services/components.

Things that I am thinking about tracking:
* Current battery level
* When phone is charging
* When phone booted up
* Updates of a particular app (first install and last updated time)
* Storage Space (free space, total space, presence of SD card and that space too)
* Device Name and Model
* Amount of RAM
* Is device connected to wi-fi or mobile data
* Is connection metered or unmetered
* Mobile carrier and phone number
* Presence of multiple sim cards
* Can device support eSim
* apps that are applied
* reasons for process death

So the list is long and will probably grow longer, the main takeaway is that these are all present
and can be queried but they are also in distinct parts of the Android system services which you need
to know about. Perhaps I can make an SDK to gather these into a single callable component, there are
also lots of casts for each service that can happen and it's just boilerplate in the end. With these
reasons I think I can make improvements to the developer's lives here.

## Conclusion
So there are quite a few things here, and no concrete pipeline. I will need to think about what to
prioritise first, for example I have been wanting to do the Device Info SDK for some time and maybe
it's the thing I will try to do first. As for the rest some fo them can be done every now and then
such as the blogpost per month goal or the blog UX improvements.
For the rest it will take much longer to create the supporting systems to enable all of it.

I will leave this as open as I started it, and hope to finish it all by 31st of December of 2025.
