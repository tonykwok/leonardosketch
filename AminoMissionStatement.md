# Amino Mission Statement #

Amino is a graphics stack and UI toolkit for desktop apps of the 21st century.

A decade from now 90% of people will use phones, slates, or netbooks as their primary computing device. This is a very exciting development in the software world and promises to reshape the way we make software (check out the great stuff our lead developer is doing in his day job at [Palm](http://developer.palm.com/)), but Amino is different. It's about that 10%: the content creators who need killer **desktop** apps, the programmers who want great tools, and the knowledge workers who need to manage incredible amounts of information at lightning speed.  Amino is the toolkit to build these apps.

# What is Amino? #

Amino is :
  * a 2D/3D scenegraph with multiple backends (Java2D, JOGL, and more coming).
  * a set of UI controls, skinnable with CSS.
  * utilities to help you build desktop apps quickly.
  * extremely testable.
  * 100% open source (BSD), redistributable, and embeddable.
  * 100% Java, ready for use by any JVM dynamic language (Groovy, JRuby, Jython, JavaScript, JavaFX Script, etc.)


# Can you give me an example? #

Our AminoGettingStartedTutorial shows how to build a basic application which opens a window, adds some buttons and controls, then hooks up event handlers to do a Flickr
search.


# How is Amino different than JavaFX and other UI toolkits? #

Amino is not innovative. On the contrary, Amino takes the best ideas from the past 20 years of UI toolkits, throws out the stuff that hasn't worked well, and bundles the rest up in a nice clean package with no legacy issues.

Amino is inspired by JavaFX and Swing. At JavaOne 2010 Oracle announced a new Java only direction for JavaFX 2.0, which makes JavaFX even closer to Amino.  Though similar, there are some differences:

  * Amino is BSD licensed and community developed, allowing you to do things you can't do with the Oracle owned JavaFX, such as: subsetting, bundling with your application, embedding on a mobile platform, recompiling with GCJ or Kaffe, or forking it for your own uses.

  * Amino is focused expressly on desktop apps. JavaFX is focused web based and mobile applications, similar to Flex and Sliverlight.

  * Amino is significantly smaller than JavaFX.  It aims to be fast, easy to learn, and very, very lean; at the expense of backwards compatibility and features.

  * Amino is embeddable in Swing panels, so you can add just a little bit of Amino to your existing apps.