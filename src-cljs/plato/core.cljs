(ns plato.core
  (:require [clojure.string :as string]
            [cljs.reader :refer [read-string]]
            [clojure.data :refer [diff]]))



(declare compare-states)

(defn compare-keys
  [old-state new-state]
  (let [all-keys (set (into (keys old-state) (keys new-state)))]
    (loop [k         (first all-keys)
           rest-keys all-keys
           removed   nil
           added     nil]
      (if (empty? rest-keys)
        [removed added]
        (let [old-val (k old-state)
              new-val (k new-state)
              in-old? (contains? old-state k)
              in-new? (contains? new-state k)
              [sub-removed sub-added] (compare-states old-val new-val in-old? in-new?)]
          (recur (first rest-keys)
                 (disj rest-keys k)
                 (if-not (nil? sub-removed)
                   (assoc removed k sub-removed)
                   removed)
                 (if-not (nil? sub-added)
                   (assoc added k sub-added)
                   added)))))))

(defn compare-states
  [old-state new-state old-has-key? new-has-key?]
  (let [old-is-map? (map? old-state)
        new-is-map? (map? new-state)]
    (cond
     (and old-has-key? (not new-has-key?)) [old-state nil]
     (and (not old-has-key?) new-has-key?) [nil new-state]
     (= old-state new-state)               [nil nil]
     (and old-is-map? (not new-is-map?))   [old-state new-state]
     (not old-is-map?)                     [nil new-state]
     :else                                 (compare-keys old-state new-state))))

(defn diff-states
  "Takes a map representing an old state, and
  a map representing a new state and returns
  a vector representing the difference between the two.
  The first item in the vector details what has been removed
  and the second what has been added or changed."
  [old-state new-state]
  (compare-states old-state new-state true true))

(diff-states {:a 1 :b {:c 2}} {:a 2 :b {:c 3}})

(defn split-key
  "Returns a list of keys as split from a nested key string."
  [nested-key]
  (vec (map keyword (string/split nested-key #":"))))

(defn clean-key
  "Removes the base-key from the nested-key.
  Note: The : is added to base-key to avoid getting a trailing :
  when the nested key is turned into a collection"
  [base-key nested-key]
  (string/replace-first nested-key (str base-key ":") ""))

(defn pathify
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

(defn unkeyify
  "Takes a base-key and a map of entries on format
  {com.example:a 1
  com.example:nested:c 2}
  and returns a map such that the base-key is removed from the beginning
  of each key, and the rest is parsed into a map structure. The example above
  should return
  {:a 1 :nested {:c 2}}"
  [base-key keyified-map]
  (let [unkeyified-map (map #(let [[k v] %]
                               [(split-key (clean-key base-key k))
                                (read-string v)])
                            keyified-map)]
    (reduce #(apply assoc-in %1 %2) {} unkeyified-map)))


(defn to-string
  [base-key path-vector]
  (str base-key (apply str path-vector)))

(defn put
  "Updates a particular key stored in local storage.
  For example, (put \"com.test\" [:foo :bar] \"Hello World!\")
  will update the key com.test:foo:bar to have value \"Hello world\"
  in local storage."
  [base-key path-vector value]
  (let [the-key (to-string base-key path-vector)]
    (js/localStorage.setItem the-key value)))

(defn put-all
  [base-key path-vectors]
  (let [put (partial put base-key)]
    (doall
     (map #(apply put %) path-vectors))))

(defn remove
  "Removes a value from local storage."
  [base-key path-vector]
  (let [the-key (to-string base-key path-vector)]
    (js/localStorage.removeItem the-key)))

(defn remove-all
  [base-key path-vectors]
  (let [remove (partial remove base-key)]
    (doall
     (map #(apply remove %) path-vectors))))

(defn put-state
  [base-key state]
  (put-all base-key (pathify [] state)))


(defn filter-our-keys
  "From a list of all keys, return a list with the keys that
  contains our base-key only."
  [base-key all-keys]
  (let [base-pattern (re-pattern (str "^" base-key))]
    (filter #(re-find base-pattern %) all-keys)))

(defn get-all
  [base-key]
  (let [all-keys (js/Object.keys js/localStorage)
        our-keys (filter-our-keys base-key all-keys)
        all-data (reduce #(assoc %1 %2 (aget js/localStorage %2)) {} our-keys)]
    (unkeyify base-key all-data)))

(defn reset!
  "Sets the state of an atom to be that of the state
  retrieved from local storage."
  [base-key state-atom]
  (clojure.core/reset! state-atom (get-all base-key)))


(defn added-to-strings
  [pathified]
  (apply str (interpose ", " (map #(str (first %) " to " (second %)) pathified))))


(defn removed-to-strings
  [pathified]
  (apply str (interpose ", " (map #(str (first %)) pathified))))


(defn keep-updated!
  "Updates local storage with all changes made to an atom.
  Call with true as second arg to switch on logging."
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
                      (put-all base-key added))
                  (when-not (empty? removed)
                      (when log-updates (js/console.log "Removing in localStorage" (removed-to-strings removed)))
                      (remove-all base-key removed)))))))

;; Tests

;(= @game-state (unkeyify "test" (keyify "test" @game-state)))
