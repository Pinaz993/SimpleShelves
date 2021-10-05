# SimpleShelves
Fabric mod for 1.17 and up. Adds shelves to display items and books. Heavily inspired by BiblioCraft, but
coded entirely independently of said mod. 

## Features
* To insert a book (or stack of books), use (right click) the book on the book shelf. To extract a book, use an empty 
hand on the book you want to extract.
* You can insert up to 12 books on a book shelf, 3 per quadrant.
* Each quadrant can hold either up to three stacks of books, or one stack of generic items. (To insert or extract a 
generic item, see inserting and extracting books.)
* Hoppers can insert and extract from shelves, but only books, enchanted books, redstone books (see below), and anything
that can be put on a lectern.
* Redstone books cause bookshelves to emit strong redstone signal from their rear side. The strength of the signal 
depends on how many redstone books are in the slot.
* Redstone books stack to 15. The shelf emits a redstone value equivalent to the number of redstone books in the slot 
with the most redstone books in it. For example, if a shelf has one stack of 7 redstone books in one slot and a stack of
12 redstone books in another slot, the shelf will emit a redstone strength of 12.
* Redstone Books are crafted with a book and a redstone torch (shapeless).
* Shelves come in all eight vanilla wood colors. If a mod maker wishes to add their wood color to the mix, the shelves 
are easily extensible. (Open an issue if you need details.)

## Development Road Map
* I plan to update the shelves so that the books in each slot are not all the same color and size for all blocks. This 
will take a fair bit of work, and I'm not sure if I can pull it off while still making the mod performant. As such, 
I do not have a set date for this to be done. 
* I originally intended for the bookshelves to boost enchantment levels. However, the vanilla code behind that is not
extensible, and I'm not quite confident in my ability to use mixins, let alone build an API for boosting enchantments as
I would like to. However, such an API has been built, so it can be built again. If anyone would like to implement such
an API, I would be happy to use it and make it a requirement for Simple Shelves.
