# Event handling design #

Bedrock uses an event bus to process all events, rather than per component listeners.  Instead of adding a handler directly to a button you add it to a global system bus. ex:

```
Button b = new Button("a button");
EventBus.getSystem().add(b, MouseEvent.MousePressed, new Callback<MouseEvent>() {
     public void callback(MouseEvent event) {
          System.out.println("a button was pressed: " + event.getSource());
     }
});
```

This design has the following advantages:

  * Better separates UI controls from the event logic. Handlers don't need direct references to the controls they monitor.
  * All events are asynchronous and properly threaded. Never worry about sending events from the wrong thread. The system won't let you.
  * Callback, a single generified event handler interface, is used for all events. It is fully type safe and should be compatible with Java 7/8 closures when they become available.
  * We can introduce new types of events in the future very easily, such as multi-touch scroll, accelerometer, USB drive mounting, etc.
  * Testing your application with fake UI events should be as lot easier than the equivalent in Swing.
  * The same design pattern is used for UI events and other kinds of events (like background tasks).
  * You can have custom event buses if necessary.
  * Isolates the UI controls from the underlying event system (AWT, SDL, JOGL, etc.)


Currently we have the following bugs and design flaws that we need to work on:

  * the handler resolver is incredibly inefficient. it essentially loops through all registered handlers, which is slow.
  * there are memory leaks when you use anonymous event handlers
  * there is a bunch of awt specific code that should be factored out.
  * it's not clear how events should cascade down the scenegraph, especially for things like popup menus and tooltips.



# Event Bus implementation #

  * Event Bus has a singleton instance.

  * Callbacks are registered to this singleton. They can register an interest in particular types of events, or events on particular components, or to listen for all events.
Events are placed onto the bus either by components themselves (such as list selection events) or by the event processing system (such as mouse and keyboard events from the windowing system).

  * Callbacks are called for events on the bus, and in no particular order.

  * Callbacks are always called on the GUI thread, regardless of where they came from.

  * Callbacks are **always** called asynchronously.  Thus, if a component calls `EventBus.getSystem().publishEvent(event)`, the event callbacks will be invoked **after** the `publishEvent()` method has completed.