# Getting Started with Amino #

Amino is a very easy to use UI toolkit. We recommend you start by constructing the UI and then adding data models and event handlers after the UI is up.

## Building a simple UI ##

Start by creating a main class. This example will do a Flickr search so we'll call it `FlickrReader`'. This main method will init the Core runtime and then launch the FlickrReader class on the correct UI thread. From now on you will always be on the GUI thread.

```
public class FlickrReader implements Runnable {
   
    public static void main(String ... args) throws Exception {
        Core.init();
        Core.getShared().defer(new FlickrReader());
    }

    public void run() {
    }

}
```

Now let's add a basic UI. Create fields for the search box and photo list, then build a simple GUI using VFlexBox and HFlexBox in the run method(). Finally put the surrounding VFlexBox in the stage, which is Amino's name for a window.

```
    private Textbox searchBox;
    private ListView<Photo> photoList;

    public void run() {

        //create the search box & photo list
        photoList = new ListView<Photo>();
        searchBox = new Textbox("lolcat");

        //set up the GUI
        Control panel = new VFlexBox()
                .setBoxAlign(FlexBox.Align.Stretch)
                //a hbox with the search field and button
                .add(new HFlexBox()
                        .setBoxAlign(FlexBox.Align.Stretch)
                        .add(searchBox,1)
                        .add(new Button("search")
                        )
                )
                //create a list of photos in a scroll pane. give it all the vspace
                .add(new ScrollPane(photoList),1)
                //an open button at the bottom
                .add(new Button("open"))
                ;
        // put it in a window
        Stage stage = Stage.createStage();
        stage.setContent(panel);
    }
```


If you run the program at this point you will have a GUI that looks fine but does nothing. It should look like this:

![http://projects.joshy.org/Leonardo/resources/org.joshy.gfx.test.full.FlickrReaderScreenSnapz001.png](http://projects.joshy.org/Leonardo/resources/org.joshy.gfx.test.full.FlickrReaderScreenSnapz001.png)

## Adding a Data Model and Search Query ##
Now let's add a data model and query to return a flickr search.  First create a Photo data class:

```
    public static class Photo {
        String title;
        String url;
    }
```

Now create a search handler which will do the actual search. This uses the XMLRequest task class which handles the background threading for us.

```
    private Callback<ActionEvent> searchHandler = new Callback<ActionEvent>() {
        public void call(ActionEvent event) {
            try {
                System.out.println("searching: " + searchBox.getText());
                XMLRequest req = new XMLRequest();
                req.setURL("http://api.flickr.com/services/feeds/photos_public.gne");
                req.setParameter("tags","lolcat");
                req.onComplete(resultsHandler);
                req.start();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
```

Notice that the request will invoke the `resultsHandler` when the request is complete. (For the sake of simplicity we are not handling the error case here).  The results handler will parse the returned XML document using the simplified XPath API:

```
    private Callback<Doc> resultsHandler = new Callback<Doc>() {
        public void call(Doc doc) {
            try {
                doc.dump();
                resultsModel.clear();
                for(Elem entry : doc.xpath("feed/entry")) {
                    Photo photo = new Photo();
                    photo.title = entry.xpathString("title/text()");
                    photo.url = entry.xpathString("link/@href");
                    resultsModel.add(photo);
                }
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
        }
    };
```

Once the `XMLRequest` has returned this handler will be called with the doc. It parses the doc into photos and then adds them to the `resultsModel`. This model needs to be a `java.util.List` implementation to hold the photo items, but it must also be a ListView.ListModel implementation so we can hook it up to the `ListView`. We will use the utility class `ArrayListModel` which implements both.

```
    private ArrayListModel<Photo> resultsModel = new ArrayListModel<Photo>();
```

Now let's hook up the request to the actual search button with by adding the onClicked line in the GUI code:

```
                        .add(new Button("search")
                        .onClicked(searchHandler))
```


Now the search will work. The returned photos will be put into the ListView like this. Of course this looks ugly, which we will fix in the next section.

![http://projects.joshy.org/Leonardo/resources/org.joshy.gfx.test.full.FlickrReaderScreenSnapz002.png](http://projects.joshy.org/Leonardo/resources/org.joshy.gfx.test.full.FlickrReaderScreenSnapz002.png)


## Adding a String Formatter ##

The `ListView` can have a full cell renderer which lets you completely customize the look of each list item. However, most of the time you just want to change the way the object is formatted into text without changing the full look and feel of the item.  You can do this with a `ListView.TextRenderer` object.  Just create one and set it on the list view.

```
// Format photos as 'Photo: %title%'
    private ListView.TextRenderer<Photo> photoFormatter = new ListView.TextRenderer<Photo>(){
        public String toString(ListView view, Photo item, int index) {
            if(item == null) return "";
            return "Photo: "+item.title;
        }
    };
```

...
```
        photoList = new ListView<Photo>()
//set the text renderer here
                        .setTextRenderer(photoFormatter)
                        .setModel(resultsModel);
```

## Open the Browser ##

Now it would be nice to open the browser to the actual Flickr page when the user selects a photo and clicks on the 'open' button. We do this with another button action handler:

```
    private Callback<ActionEvent> openHandler = new Callback<ActionEvent>() {
        public void call(ActionEvent event) {
            int n = photoList.getSelectedIndex();
            Photo photo = photoList.getModel().get(n);
            OSUtil.openBrowser(photo.url);
        }
    };
```
...
```
                .add(new Button("open")
//add the event handler to the 'open' button
                        .onClicked(openHandler))
                ;
```

We get the current photo from the list, and then call `OSUtil.openBrowser` to open the user's desktop browser. This is a convenience method provided by Amino.


## Conclusion ##

That's it.  Creating apps with Amino is easy. For more information you can read the Java Docs here http://projects.joshy.org/Leonardo/daily/docs/