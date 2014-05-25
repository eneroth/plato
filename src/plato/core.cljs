(ns plato.core
  (:require [clojure.string :as string]
            [cljs.reader :refer [read-string]]))


;; Diffing algorithm.
;; Please improve and/or benchmark!
;; ----------------------------------------------------------------

(defn- map-two [f col1 col2]
  "Takes the first value from each collection, then applies the 
  function f to them, with col1 being the first argument to the function."
  (map (fn [x] (apply f x)) (partition 2 (interleave col1 col2))))

(defn- assoc-nil [x y]
  "Associates x with y, given that y is not nil."
  (when-not (nil? y) (assoc {} x y)))

(defn diff-states [old-state new-state]
  "Takes a map representing an old state, and
  a map representing a new state and returns
  a vector representing the difference between the two.
  The first item in the vector details what has been removed
  and the second what has been added or changed."
  (when-not (= old-state new-state)
    (if-not (and (map? old-state) (map? new-state))
      [(when (nil? new-state) old-state) new-state]
      (let [all-keys    (-> (into (keys old-state) (keys new-state)) set vec)
            substates   (map #(diff-states (get old-state %) (get new-state %)) all-keys)
            old-vals    (map-two assoc-nil all-keys (map first substates))
            new-vals    (map-two assoc-nil all-keys (map second substates))]
        [(apply merge old-vals) 
         (apply merge new-vals)]))))

(log-str (diff-states {:a 1 
                       :b {:c 2 :d [1 2 3]} 
                       :e "same" 
                       :f "removed"} 
                      {:a 2 
                       :b {:c 3 :d [4 5 6]} 
                       :e "same"}))


;; Key mutation
;; ----------------------------------------------------------------

(defn- split-key
  "Returns a list of keys as split from a nested key string."
  [nested-key]
  (vec (map keyword (string/split nested-key #":"))))

(defn- clean-key
  "Removes the base-key from the nested-key.
  Note: The : is added to base-key to avoid getting a trailing :
  when the nested key is turned into a collection"
  [base-key nested-key]
  (string/replace-first nested-key (str base-key ":") ""))

(defn- pathify
  "Takes an arbitrarily nested map and returns a list
  vectors, where each vectors is a tuple of a path
  describing, as well as a value.
  
  For example, called with the base-vector [\"com.example\"],
  and the map {:a 1 :b {:c 2 :d 3}}, the function returns
  ([[\"com.example\" :a] 1]
  [[\"com.example\" :b :c] 2]
  [[\"com.example\" :b :d] 3])"
  [base-vector m]
  (apply concat (for [[k v] m]
                  (let [built-vector (conj base-vector k)]
                    (if (map? v)
                      (pathify built-vector v)
                      [[built-vector (pr-str v)]])))))

(defn- unkeyify
  "Takes a base-key and a map of entries on format
  {com.example:a 1
  com.example:nested:c 2}
  and returns a map such that the base-key is removed from the beginning
  of each key, and the rest is parsed into a map structure. The example above
  should return
  {:a 1 :nested {:c 2}}"
  [base-key keyified-map]
  (let [unkeyified-map (map (fn [x] 
                              (let [[k v] x]
                                [(split-key (clean-key base-key k))
                                 (read-string v)]))
                            keyified-map)]
    (reduce #(apply assoc-in %1 %2) {} unkeyified-map)))


(defn- to-string
  [base-key path-vector]
  (str base-key (apply str path-vector)))


;; API
;; ----------------------------------------------------------------


;; Put-functions
;; ----------------------------------------------------------------

(defn store!
  "Updates a particular key stored in local storage.
  For example, (store! \"com.test\" [:foo :bar] \"Hello World!\")
  will update the key com.test:foo:bar to have value \"Hello world\"
  in local storage."
  [base-key path-vector value]
  (let [the-key (to-string base-key path-vector)]
    (js/localStorage.setItem the-key value)))

(defn store-many!
  "Stores a collection of path vectors in local storage.
  The path vectors should be on format:
  ([[:a] 1]
  [[:b :c] 2]
  [[:b :d] 3])"
  [base-key path-vectors]
  (let [put (partial store! base-key)]
    (doall
      (map #(apply put %) path-vectors))))

(defn store-state!
  "Takes hash-map and stores it in local storage."
  [base-key state]
  (store-many! base-key (pathify [] state)))

(defn store-atom!
  "Takes an atom and stores the state it contains in local storage."
  [base-key an-atom]
  (store-state! base-key @an-atom))


;; Get-functions
;; ----------------------------------------------------------------

(defn- filter-our-keys
  "From a list of all keys, return a list with the keys that
  contains our base-key only."
  [base-key all-keys]
  (let [base-pattern (re-pattern (str "^" base-key))]
    (filter #(re-find base-pattern %) all-keys)))

(defn- get-by-string [path-string]
  "Get the value associated with the given path-string."
  (aget js/localStorage path-string))

(defn- get-our-keys [base-key]
  (let [all-keys (js/Object.keys js/localStorage)]
    (filter-our-keys base-key all-keys)))

(defn restore [base-key path-vector]
  "Get the value associated with the specified base-key"
  (get-by-string (to-string base-key path-vector)))

(defn restore-state
  "Get all localStorage entries beginning with the given base-key."
  [base-key]
  (let [our-keys (get-our-keys base-key)
        all-data (reduce #(assoc %1 %2 (get-by-string %2)) {} our-keys)]
    (unkeyify base-key all-data)))

(defn restore-atom!
  "Get stored state from local storage and reset the given atom with it."
  [base-key an-atom]
  (clojure.core/reset! an-atom (restore-state base-key)))


;; Remove-functions
;; ----------------------------------------------------------------
(defn- erase-string!
  "Erase an entry from local storage given the supplied string."
  [a-string]
  (js/localStorage.removeItem a-string))

(defn erase!
  "Removes a value from local storage."
  [base-key path-vector]
  (let [the-key (to-string base-key path-vector)]
    (erase-string! the-key)))

(defn erase-many!
  "Remove all keys that belonging to the given base-key
  from local storage."
  [base-key path-vectors]
  (let [remove (partial remove base-key)]
    (doall
      (map #(apply erase! %) path-vectors))))

(defn erase-all!
  "Remove all data belonging to the supplied base-key."
  [base-key]
  (doall
    (map erase-string! (get-our-keys base-key))))

;; Functions for automatic state synchronisation
;; ----------------------------------------------------------------

;; For formating log messages
(defn- added-to-strings
  [pathified]
  (apply str (interpose ", " (map #(str (first %) " to " (second %)) pathified))))

(defn- removed-to-strings
  [pathified]
  (apply str (interpose ", " (map #(str (first %)) pathified))))


;; The actual API
(defn keep-updated!
  "Updates local storage with all changes made to an atom.
  Call with true as third arg to switch on logging."
  ([base-key an-atom]
   (keep-updated! base-key an-atom false))
  ([base-key an-atom log-updates]
   (add-watch an-atom :a-key
              (fn [a-key the-reference old-state new-state]
                (let [the-diff      (diff-states old-state new-state)
                      added         (pathify [] (second the-diff))
                      removed       (pathify [] (first the-diff))]
                  (when-not (empty? added)
                    (when log-updates (js/console.log "Updating in localStorage" (added-to-strings added)))
                    (store-many! base-key added))
                  (when-not (empty? removed)
                    (when log-updates (js/console.log "Removing in localStorage" (removed-to-strings removed)))
                    (erase-many! base-key removed)))))
   an-atom))