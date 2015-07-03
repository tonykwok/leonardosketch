# Design Doc for scenegraph and UI controls #


The graphics stack for bedrock is actually implemented in three parts:

  * First you have the GFX object, which is an immediate mode API similar to the Graphics2D interface from Java2D.  Regardless of the current graphics backend, apps always draw on the screen through the GFX object.  There is currently a Java2D implementation of GFX and a partial implementation for OpenGL using JOGL.  There is also a version which draws into an offscreen buffer for rendering to images.

  * On top of the GFX interface is a scenegraph with the core class of Node.  The scenegraph is a tree of Node objects which each have a draw method. Drawing is done with a recursive tree traversal. Nodes each return their visual bounds.   There are currently four types of node:  Shape, with subclasses for some standard shapes; Image, for images, obviously, Parent, with the group and container implementations; and Control, for all UI controls.

  * UI Controls are all subclasses of Control, which defines both visual, input, and layout bounds.  This allows controls to draw outside of the bounds used for layout, and to also have input areas which are smaller or larger than they appear to be.  All controls are rendered in three phases:

# Skin Pass #

If a control has set it's skin to dirty then the system will traverse the tree calling `doSkins()` on each control.

`doSkins()`: the control should throw away any current skin information and regenerate it from the CSS or other skinning system.


# Preferred Layout Pass #

If a control has set it's layout to dirty, then the system will traverse the tree with two layout passes. In the first pass a control will have it's `doPrefLayout()` method call. In this method the control should calculate it's preferred size based on content, intrinsic sizing, CSS values, and if the developer has set an explicit preferred width or height. Once complete `getWidth` / `getHeight` should return the values of the preferred layout.


# Regular Layout Pass #


In the second pass a control will have it's `doLayout` method called. The control should lay out it's internal structure using the values in `getWidth` / `getHeight`. These may or may not be what was calculated by the `doPrefLayout`, depending on how the parent layout manager may have manipulated the child.


# Drawing Pass #

Once all skins are done and layout is complete, the system does a recursive tree traversal of the scenegraph to draw all of the children in painting order.

doDraw()
> the control should draw itself using the current width and height.


Controls should cache as much information as possible so that drawing can be fast. If a control does something that makes it's drawing dirty, then it should call `setDrawingDirty()` which will trigger a repaint. If a control does something which changes it's size then it should call `setLayoutDirty()` which will trigger a layout pass. If a control does something which invalidates the skin, such as changing the states it needs from CSS, then it should call `setSkinDirty()`.  Note that the skin, layout, and drawing passes may be triggered by something other than the control itself calling set\*Dirty(). For example, the system may switch themes and have to invalidate all CSS skins. Controls should be prepared to have their methods called at any time, but always in the correct order defined above.


# Panel Layouts #

Amino has several built in layouts such as `GridBox`, `VFlexBox`, and `HFlexBox`. A layout panel class will call the preferred layout of each child first in the preferred layout pass. Then it will calculate the position of each child, set the position and size of the children, and then call `doLayout` on each child.

Some controls need to know their width to calculate their height. A label with wrapping text is one example. To support this the layout should set the width of the first child, call it's `doLayout`, then read back the calculated height to continue on to the next child.

# Current Flaws #

  * nodes have translate x & y, but scaling, rotating, and shear isn't handled yet. Should this be done on node or on some sort of a transform node.

  * nodes don't have a standard form of buffer or effects, like the scenario scenegraph did.

  * it's very slow and naive. there is no dirty rect handling. the screen is always redrawn every time.

  * the way controls should cache parts of the css is currently unclear.

  * some layouts aren't supported well or implemented.

  * it's not clear how this system would work with GUI builder driven layout. an xml file?