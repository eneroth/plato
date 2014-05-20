# Plato

> Our object in the construction of the state is the greatest happiness of the whole, and not that of any one class. 
> 
> *Plato*

This is why we should try to keep the state separate from the rest of the code.


## Purpose
The purpose of Plato is to store and retrieve the state of an atom to (and from) Local Storage, automatically or, if so desired, manually.

This library grew out of the need to keep storage of application state separate from the application logic. I noticed that I interspersed my application logic with a lot of storage related function calls. Storage and persistence is arguably orthogonal to application logic, and therefore those functions were extracted and put in a separate library.

Plato parses arbitrarily nested hash-maps in atoms and constructs local storage keys from their relative paths.

For example, given the ```base-key``` (base-keys are explained below) "myproject", the hash-map ```{:a 1 :b {:c 2 :d 3}``` is stored in local storage as:
```
  Key: myproject:a,   value: 1
  Key: myproject:b:c, value: 2
  Key: myproject:b:d, value: 3
```

Due to limitations in the local storage API, everything except hash-maps is stored as strings. Hash-maps are stored in the formatting of the keys, as shown above.

## Usage
Almost all Plato functions take a ```base-key``` as the first argument. The reason for this is to make sure that there are no collisions between keys stored in local storage. Make sure you use a different base-key for every atom you intend to persist.

If you're only using a single atom, and wish to omit the base-key, you can use ```partial```, like this:

```clojure
(ns myproject.core
  (:require [plato.core :as plato]))

(def base-key "myproject")

(def put-atom! (partial plato/put-atom! base-key))
```

### Storing and restoring state
The two main functions of Plato is ```keep-updated!``` and ```get-atom!```. They are used to store and restore state, respectively. There are also a number of functions that can be used to store data more manually. In particular, you may need to run ```put-atom!``` once before running ```keep-updated!```, since ```keep-updated!``` only will persist the *changes* made to the atom.

#### Storing state

```keep-updated!``` adds a watch to the atom in question that reacts to changes in the atom and updates Local Storage accordingly. Only that parts that are changed will be updated.

```clojure
(plato/keep-updated! base-key an-atom)
```

If you wish to see when and what it is being written to local storage, you can supply a third argument (being a boolean) to ```keep-updated!``` in order to turn on console logging. Use it thusly:

```clojure
(def my-atom (atom {:coords {:x 0
                             :y 0}}))

(plato/keep-updated! "myproject" my-atom true)
```

```keep-updated!``` will only write to local storage once the atom changes, so if you want to store everything currently in the atom, you have to use ```put-atom!```. Something like this would do,

```clojure
(def my-atom (atom {:coords {:x 0
                             :y 0}}))
                             
(def my-base-key "myproject")                       

(plato/put-atom! my-base-key my-atom)

(plato/keep-updated! my-base-key my-atom true)
```


#### Restoring state
```get-atom!``` resets the atom to the state stored in Local Storage, given that there is any.

```clojure
(plato/get-atom! base-key an-atom)
```

For example,

```clojure
(def my-atom (atom {:coords {:x 0
                             :y 0}}))

(plato/get-atom! "myproject" my-atom) ;; Will overwrite current atom content
```

## Full function list

### Putting

**put-key!**
```clojure
(put-key! base-key path-vectors)
```
Updates a particular key stored in local storage. For example, 

```clojure 
(put-key! "com.example.my-atom" [:foo :bar] "Hello World!")
``` 

will update the key ```"com.test:foo:bar"``` to have value ```"Hello world"``` in local storage.

**put-many!**
```clojure
(put-many! base-key path-vectors)
```

Stores a collection of path vectors in local storage. The path vectors should be on format:

```clojure
[[:a :b :c] 1]
```
  
For example,

```clojure
(def path-vectors [[[:a] 1]
                   [[:b :c] 2]
                   [[:b :d] 3]])

(put-many! "com.example.my-atom" path-vectors)
```

**put-state!**
```clojure
(put-state! base-key state)
```
Takes an atom state and stores it in local storage. For example,

```clojure
(put-state! "com.example.my-atom" @my-atom)
```

**put-atom!**
```clojure
(put-state! base-key state)
```
Same as ```put-state!```, but it does the ```deref for you```. For example,

```clojure
(put-state! "com.example.my-atom" my-atom)
```

### Getting

**get-key**
```clojure
(get-key base-key path-vector)
```

Get the value associated with the specified base-key from local storage. For example,

```clojure
(get-key "com.example.my-atom" [:a :b :c])
```

**get-all**
```clojure
(get-all base-key)
```

Get all localStorage entries beginning with the given base-key. For example,

```clojure
(get-key "com.example.my-atom")
```


**get-atom!**
```clojure
(get-atom! base-key an-atom)
```

Get stored state from local storage and reset the given atom with it. For example,

```clojure
(get-atom! "com.example.my-atom" my-atom)
```

### Removing
**remove-key!**
```clojure
(remove-key! base-key path-vector)
```

Removes a key and corresponding value from local storage. For example,

```clojure
(remove-key! "com.example.my-atom" [:a :b :c])
```

**remove-many!**
```clojure
(remove-many! base-key path-vectors)
```
Remove all keys that belonging to the given base-key from local storage. For example,
```clojure
(remove-many! "com.example.my-atom" [[:a :b :c]
                                     [:d :e]])
```

### Maintaing state in sync
**keep-updated!**
```clojure
(keep-updated! base-key an-atom log-update)
```

Updates local storage with all changes made to an atom. Call with ```true``` as third arg to switch on logging. For example,

```clojure
(keep-updated! "com.example.my-atom" my-atom)
(keep-updated! "com.example.my-atom" my-atom true) ;; Console logging turned on
```
### Diffing

**diff-states**
```clojure
(diff-states old-state new-state)
```
Takes a map representing an old state, and a map representing a new state and returns a vector representing the difference between the two. The first item in the vector details what has been removed and the second what has been added or changed.


## License

Copyright © 2014 Henrik Eneroth

Distributed under the Eclipse Public License.