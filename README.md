# Plato

> Our object in the construction of the state is the greatest happiness of the whole, and not that of any one class. 
> 
> *Plato*

This is why we should try to keep the state separate from the rest of the code.


## Purpose
The purpose of Plato is to store and restore data to (and from) Local Storage. There are also a couple of quite useful functions for persisting state held within atoms.

This library grew out of the need to keep storage of application state separate from the application logic. I noticed that I interspersed my application logic with a lot of storage related function calls. Storage and persistence is arguably orthogonal to application logic, and therefore those functions were extracted and put in a separate library.

Plato parses arbitrarily nested hash-maps in atoms and constructs local storage keys from their relative paths.

For example, given the ```base-key``` (base-keys are explained below) "myproject", the hash-map ```{:a 1 :b {:c 2 :d 3}``` is stored in local storage as:
```
  Key: "myproject:a",   value: "1"
  Key: "myproject:b:c", value: "2"
  Key: "myproject:b:d", value: "3"
```

Due to limitations in the local storage API, everything except hash-maps is stored as strings in the value field, including vectors and so on. Hash-maps are stored in the formatting of the keys, as shown above.

## Usage
Add the following to your project.clj dependencies:
```clojure
[plato "0.1.10"]
```

Almost all Plato functions take a ```base-key``` as the first argument. The reason for this is to make sure that there are no collisions between keys stored in local storage. Make sure you use a different base-key for every atom you intend to persist.

If you're only using a single atom, and wish to omit the base-key, you can use ```partial```, like this:

```clojure
(ns myproject.core
  (:require [plato.core :as plato]))

(def base-key "myproject")

(def store-atom! (partial plato/store-atom! base-key))
```

### Storing and restoring state
Two of the main functions in Plato are ```keep-updated!``` and ```restore-atom!```. They are used to store and restore the state of an atom, respectively. There are also a number of functions that can be used to store data more manually. In particular, you *may* need to run ```store-atom!``` once before running ```keep-updated!```, if you have state in the atom that is not currently in Local Storage. This is due to the fact that ```keep-updated!``` works incrementally, and only will persist the *changes* made to the atom.


#### Storing state

```keep-updated!``` adds a watch to an atom. This watch reacts to changes in the atom and updates Local Storage accordingly. Only those parts that are changed will be updated. The size of the state held in the atom is irrelevant, only the parts that are changed will be written to Local Storage. If large parts of the state held in the atom is changed, then a larger number of writes to Local Storage will take place.

```clojure
(plato/keep-updated! base-key an-atom)
```

If you wish to see when and what it is being written to local storage, you can supply a third argument (being a boolean) to ```keep-updated!``` in order to turn on console logging. Use it thusly:

```clojure
(def my-atom (atom {:coords {:x 0
                             :y 0}}))

(plato/keep-updated! "myproject" my-atom true)
```

```keep-updated!``` will only write to local storage once the atom changes, so if you want to store everything currently in the atom, you have to use ```store-atom!```. Something like this would do,

```clojure
(def my-atom (atom {:coords {:x 0
                             :y 0}}))
                             
(def my-base-key "myproject")                       

(plato/store-atom! my-base-key my-atom)

(plato/keep-updated! my-base-key my-atom true)
```


#### Restoring state
```restore-atom!``` resets the atom to the state stored in Local Storage, given that there is any.

```clojure
(plato/restore-atom! base-key an-atom)
```

For example,

```clojure
(def my-atom (atom {:coords {:x 0
                              :y 0}}))

(plato/restore-atom! "myproject" my-atom) ;; Will overwrite current atom content
```

## Full function list

### Storing

**store!**
```clojure
(store! base-key path-vector value)
```
Updates a particular key stored in local storage. For example, 

```clojure 
(store! "com.example.my-atom" [:foo :bar] "Hello World!")
``` 

will update the key ```"com.example.my-atom"``` to have value ```"Hello World!"``` in local storage.

**store-many!**
```clojure
(store-many! base-key path-vectors)
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

(store-many! "com.example.my-atom" path-vectors)
```

**store-state!**
```clojure
(store-state! base-key state)
```
Takes a hash-map and stores it in local storage. For example,

```clojure
(store-state! "com.example.my-state" {:a {:b {:c 1}}})
```

**store-atom!**
```clojure
(store-atom! base-key an-atom)
```
Stores the contents of an atom (typically a hash-map) in local storage. For example,

```clojure
(store-atom! "com.example.my-atom" my-atom)
```

### Retrieving and restoring

**restore**
```clojure
(restore base-key path-vector)
```

Get the value associated with the specified base-key from local storage. For example,

```clojure
(restore "com.example.my-atom" [:a :b :c])
```

**restore-state**
```clojure
(restore-state base-key)
```

Get all localStorage entries beginning with the given base-key. For example,

```clojure
(restore-state "com.example.my-state")
```


**restore-atom!**
```clojure
(restore-atom! base-key an-atom)
```

Get stored state from local storage and reset the given atom with it. For example,

```clojure
(restore-atom! "com.example.my-atom" my-atom)
```

### Erasing
**erase!**
```clojure
(erase! base-key path-vector)
```

Remove a key and corresponding value from local storage. For example,

```clojure
(erase! "com.example.my-state" [:a :b :c])
```

**erase-many!**
```clojure
(erase-many! base-key path-vectors)
```
Remove all entries as specified by path vectors, belonging to the given base-key, from local storage. For example,
```clojure
(erase-many! "com.example.my-atom" [[:a :b :c]
                                     [:d :e]])
```

**erase-all!**
```clojure
(erase-all! base-key)
```
Remove all keys belonging to the given base-key from local storage. For example,
```clojure
(erase-all! "com.example.my-atom")
```

### Maintaining state in sync
**keep-updated!**
```clojure
(keep-updated! base-key an-atom log-update)
```

Updates local storage with all changes made to an atom. Call with ```true``` as third arg to switch on logging. For example,

```clojure
(keep-updated! "com.example.my-atom" my-atom)
(keep-updated! "com.example.my-atom" my-atom true) ;; Console logging turned on
```

Example of logging output when logging is turned on,
```
…
Updating in localStorage [:coords :x] to 561
Updating in localStorage [:coords :y] to 174, [:coords :x] to 570
…
```


### Diffing

**diff-states**
```clojure
(diff-states old-state new-state)
```
Takes a map representing an old state, and a map representing a new state and returns a vector representing the difference between the two. The first item in the vector details what has been removed and the second what has been added or changed.

As opposed to [clojure.data/diff](http://clojuredocs.org/clojure_core/clojure.data/diff), this diffing algorithm considers everything that is not a map to be a value. This is in order to prepare the data for Local Storage, which is just a simple string based key-value store.

For example,
 
```clojure
(diff-states {:a 1 
              :b {:c 2 
                  :d [1 2 3]} 
              :e "same" 
              :f "removed"} 
             {:a 2 
              :b {:c 3 
                  :d [4 5 6]} 
              :e "same"})
```

Outputs:

```clojure
[{:f "removed"} 
 {:a 2
  :b {:c 3 
      :d [4 5 6]}}] 
```


## License

Copyright © 2014 Henrik Eneroth

Distributed under the Eclipse Public License.