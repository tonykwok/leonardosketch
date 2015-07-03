# Introduction #

Not only does Amino let you use CSS to style UI controls, but if follows the CSS cascade with multiple files. This means you can override the default styles by including your own CSS file in your app.  To help with development Amino provides both static and interactive loading of CSS files.  In interactive mode the system will check the CSS file for updates every second.  If the file changes then it will reload all of the skins in your app.  This lets you interactively tweak your app's style with immediate feedback.

# Static Mode #

In your `main` method after calling `Core.init()`, call `Core.getShared.loadCSS()` with the file containing your CSS rules.

```
    public static void main(String... args) throws Exception, InterruptedException {
        Core.setUseJOGL(false);
        Core.init();
        Core.getShared().loadCSS(new File("test.css"));
        Core.getShared().defer(new GrandTour());
    }
```


# Interactive Mode #

In interactive mode Amino will check the file on disk for updates every second.  To use it, just call `setDebugCSS()` with your file after `Core.init()`. Alternatively you can call `Core.requestDebugCSS()`, which will open a file dialog asking you to select a CSS file when you launch the app.

```
    public static void main(String... args) throws Exception, InterruptedException {
        Core.setUseJOGL(false);
        Core.init();
        Core.setDebugCSS(new File("test.css"));
        Core.getShared().defer(new GrandTour());
    }
```