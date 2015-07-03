# Introduction #

All software needs to be tested. Unfortunately GUIs have historically be one of the hardest things to test.  To help remedy this we are working on a prototype of an integrated testing system.  This system lets you simulate user events that are sent to your application, but without having to worry about threading and waiting for responses. Our prototype API essentially lets you queue up a series of commands which are then executed serially on your application.


# Details #

All automated tests should subclass the `org.joshy.gfx.test.AutomationBase` class. See the `AutomationTest` class for an example.  First, start up Core then create your GUI.

```
    public static void main(String ... args) throws Exception {
        //turn on testing
        Core.setTesting(true);
        Core.init();

        //create a gui
        final Textbox box = new Textbox("");
        box.setId("textbox");
        Button button = new Button("Submit");
        button.setId("button");
        Callback<ActionEvent> callback = new Callback<ActionEvent>(){
            @Override
            public void call(ActionEvent event) throws Exception {
                u.p("box value = " + box.getText());
            }
        };
        button.onClicked(callback);

        final Stage stage = Stage.createStage();
        stage.setContent(new VFlexBox().setBoxAlign(VFlexBox.Align.Stretch)
            .add(box)
            .add(button));

```

Note the call above to `Core.setTesting(true);`. This lets you create your GUI on the main thread instead of the event thread. This makes it work properly with automated testing systems like JUnit.

Now queue up some events to test your GUI. In this example we will click the button, select the text field, type text into, then click the button again.  When `processAndQuit` is called the events will actually be executed.

```
        //queue up some events
        click(60,60);      //click at 60x60
        click("#button");  //click the button with the id 'button'
                           //it prints the default value
        click("#textbox"); //should find the text field and click it
        type("newvalue");  //type into the current focused node
        click("#button");  //click button, it prints the new value

        //process the events
        processAndQuit(stage);
```


Note references to `#button`.  This is an ID query, just like in CSS.  It will send a click event directly to the button with the ID `button`, rather than just clicking at particular coordinates. By using ID selectors your test won't break if the layout changes slightly.