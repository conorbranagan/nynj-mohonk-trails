# SQLite Database Schema #

**id**: A basic identifier, must be a unique integer

**name**: The name of the map

**ekey**: Encryption key, can be any random string of characters and numbers

**fname**: The filename of the map JPEG. This must match the end of the URL

**url**: The full URL to the location of the image

_The next 4 columns come from the world file, in order_

**min\_longitude**

**max\_latitude**

**lat\_per\_pixel**

**lon\_per\_pixel**

_And then the last two columns..._

**polygon\_points**: These are the points, in pixels of the polygon of the map. On the final georeferenced image, there are areas that are not covered by the map. We want to make sure we are within the map polygon so we must get the points from that.

**description**: A description of the map