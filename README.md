# Plato

> Our object in the construction of the state is the greatest happiness of the whole, and not that of any one class. 
> 
> *Plato*

This is why I try to keep the state separate from the rest of the code.


## Purpose
The purpose of Plato is to store and retrieve the state of an atom to (and from) Local Storage, automatically or, by varying degrees, manually.

This library grew out of the need to keep storage of application state separate from the application logic. I noticed that I interspersed my application logic with a lot of storage and retrieval from local storage, a task that is in actuality orthogonal to the reasoning happening in the application logic functions. Therefore, those functions were extracted and put in a separate library.

Plato parses arbitrarily nested hash-maps in atoms and constructs local storage keys from their relative paths.

For example, given the ```base-key``` (base-keys are explained below) "myproject" ```Clojure {:a 1 :b {:c 2 :d 3}``` is stored in local storage as:
```
  Key: myproject:a,   value: 1
  Key: myproject:b:c, value: 2
  Key: myproject:b:d, value: 3
```

## Usage
Almost all Plato functions take a ```base-key``` as the first argument. The reason for this is to make sure that there are no collisions between the keys stored in local storage. Make sure you use a different base-key for every atom you intend to persist.

If you're only using a single atom, and wish to omit the base-key, you can use ```partial```, like this:

```Clojure
(ns myproject.core
  (:require [plato.core :as plato]))

(def base-key "myproject")

(def put-all (partial plato/put-all base-key))
```

### Storing and restoring state
The two main functions of Plato is ```keep-updated!``` and ```reset-from-ls!```. They are used to store and restore state, respectively. There are also a number of functions that can be used to store data more manually. In particular, you may need to run ```put-all``` once before running ```keep-updated!```, since ```keep-updated!``` only will persist the *changes* made to the atom.

#### Storing state

```keep-updated!``` adds a watch to the atom in question that reacts to changes in the atom and updates Local Storage accordingly. Only that parts that are changed will be updated.

#### Restoring state
```reset-from-ls!``` resets the atom to the state stored in Local Storage, given that there is any.

```Clojure
(keep-updated! base-key an-atom)
```



## License

Copyright © 2014 Henrik Eneroth

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
