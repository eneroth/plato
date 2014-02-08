(ns plato.plato
  (:require [clojure.string :as string]
            [cljs.reader :refer [read-string]]))



(defn keyify
  [base-key m]
  (into {} (for [[k v] m]
             (let [built-key (str base-key k)]
               (if (map? v)
                 (keyify built-key v)
                 {built-key (pr-str v)})))))

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

(defn unkeyify
  "Takes a base-key and a map of entries on format
  {com.example:a 1
  com.example:nested:c 2}
  and returns a map such that the base-key is removed from the beginning
  of each key, and the rest is parsed into a map structure. The example above
  should return
  {:a 1 :nested {:c 2}}"
  [base-key keyified-map]
  (let [unkeyified-map (map #(let [[k v] %] [(split-key (clean-key base-key k)) (read-string v)]) keyified-map)]
    (reduce #(apply assoc-in %1 %2) {} unkeyified-map)))


(defn put
  [base-key state]
  (let [serialized-state (keyify base-key state)]
    (doall
     (map #(js/localStorage.setItem (key %) (val %)) serialized-state))))


(defn filter-our-keys
  "From a list of all keys, return a list with the keys that
  contains our base-key only."
  [base-key all-keys]
  (let [base-pattern (re-pattern (str "^" base-key))]
    (filter #(re-find base-pattern %) all-keys)))

(defn get
  [base-key]
  (let [all-keys (js/Object.keys js/localStorage)
        our-keys (filter-our-keys base-key all-keys)
        all-data (reduce #(assoc %1 %2 (aget js/localStorage %2)) {} our-keys)]
    (unkeyify base-key all-data)))

(defn reset!
  [base-key state-atom]
  (clojure.core/reset! state-atom (get base-key)))

;; Tests

;(= @app-state (unkeyify "test" (keyify "test" @app-state)))
