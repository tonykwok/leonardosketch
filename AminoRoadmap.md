Our primary goal for Amino is to create a UI toolkit functional enough to build some decent desktop apps. Secondary goals are speed, features (tons of controls), animation, and effects.

This is the basic roadmap for the next few releases:

### release 1: 0.5 ###

goals: good enough to build a hiqh quality twitter app with multiple skins.


  * enough speed improvements to make Leonardo Sketch usable again
  * actual performance tracking across releases
  * testability: be able to write unit tests for your apps
  * build one real app with Amino in addition to Leo. (Twitter app?)
  * Javadocs for the controls
  * real scenegraph, capable of basic javafx stuff (no effects)

the following features might be for 0.5 or might be pushed back
  * integrate javafxdoc for better looking docs?
  * api diffing?
  * improve the css parser to handle more shortcut cases

### release 2: 0.75 ###

  * fix enough bugs in gl layer to get Grand Tour to run
  * initial start on sdl layer
  * tree control
  * rewrite text api and implementation
  * finish animation layer or adopt another toolkit like trident
  * design the app framework
  * get an alpha version working on openjdk for mac.

### release 3: 1.0 ###
create an optional media package**write video
  * play audio
  * pluggable codecs
  * research pulse and Gstreamer for java
  * hw accelerated pixel effects.**

research JNI / JNA options for tilt sensor, multi-touch, and game controller.

improve app bundlers to make it super easy to support JNI libs, esp. game controller and JOGL

rewrite text package to be faster, easier to use, and support simple styled text, including bulleted lists and inline enhancements (spell checker, format recognizers, etc).