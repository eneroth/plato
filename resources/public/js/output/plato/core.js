// Compiled by ClojureScript 0.0-2138
goog.provide('plato.core');
goog.require('cljs.core');
goog.require('clojure.data');
goog.require('cljs.reader');
goog.require('clojure.data');
goog.require('cljs.reader');
goog.require('clojure.string');
goog.require('clojure.string');
plato.core.compare_keys = (function compare_keys(old_state,new_state){var all_keys = cljs.core.set.call(null,cljs.core.into.call(null,cljs.core.keys.call(null,old_state),cljs.core.keys.call(null,new_state)));var k = cljs.core.first.call(null,all_keys);var rest_keys = all_keys;var removed = null;var added = null;while(true){
if(cljs.core.empty_QMARK_.call(null,rest_keys))
{return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [removed,added], null);
} else
{var old_val = k.call(null,old_state);var new_val = k.call(null,new_state);var in_old_QMARK_ = cljs.core.contains_QMARK_.call(null,old_state,k);var in_new_QMARK_ = cljs.core.contains_QMARK_.call(null,new_state,k);var vec__4819 = plato.core.compare_states.call(null,old_val,new_val,in_old_QMARK_,in_new_QMARK_);var sub_removed = cljs.core.nth.call(null,vec__4819,0,null);var sub_added = cljs.core.nth.call(null,vec__4819,1,null);{
var G__4820 = cljs.core.first.call(null,rest_keys);
var G__4821 = cljs.core.disj.call(null,rest_keys,k);
var G__4822 = ((!((sub_removed == null)))?cljs.core.assoc.call(null,removed,k,sub_removed):removed);
var G__4823 = ((!((sub_added == null)))?cljs.core.assoc.call(null,added,k,sub_added):added);
k = G__4820;
rest_keys = G__4821;
removed = G__4822;
added = G__4823;
continue;
}
}
break;
}
});
plato.core.compare_states = (function compare_states(old_state,new_state,old_has_key_QMARK_,new_has_key_QMARK_){var old_is_map_QMARK_ = cljs.core.map_QMARK_.call(null,old_state);var new_is_map_QMARK_ = cljs.core.map_QMARK_.call(null,new_state);if(cljs.core.truth_((function (){var and__3396__auto__ = old_has_key_QMARK_;if(cljs.core.truth_(and__3396__auto__))
{return cljs.core.not.call(null,new_has_key_QMARK_);
} else
{return and__3396__auto__;
}
})()))
{return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [old_state,null], null);
} else
{if(cljs.core.truth_((function (){var and__3396__auto__ = cljs.core.not.call(null,old_has_key_QMARK_);if(and__3396__auto__)
{return new_has_key_QMARK_;
} else
{return and__3396__auto__;
}
})()))
{return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [null,new_state], null);
} else
{if(cljs.core._EQ_.call(null,old_state,new_state))
{return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [null,null], null);
} else
{if((old_is_map_QMARK_) && (!(new_is_map_QMARK_)))
{return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [old_state,new_state], null);
} else
{if(!(old_is_map_QMARK_))
{return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [null,new_state], null);
} else
{if(new cljs.core.Keyword(null,"else","else",1017020587))
{return plato.core.compare_keys.call(null,old_state,new_state);
} else
{return null;
}
}
}
}
}
}
});
/**
* Takes a map representing an old state, and
* a map representing a new state and returns
* a vector representing the difference between the two.
* The first item in the vector details what has been removed
* and the second what has been added or changed.
*/
plato.core.diff_states = (function diff_states(old_state,new_state){return plato.core.compare_states.call(null,old_state,new_state,true,true);
});
plato.core.diff_states.call(null,new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"a","a",1013904339),1,new cljs.core.Keyword(null,"b","b",1013904340),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"c","c",1013904341),2], null)], null),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"a","a",1013904339),2,new cljs.core.Keyword(null,"b","b",1013904340),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"c","c",1013904341),3], null)], null));
/**
* Returns a list of keys as split from a nested key string.
*/
plato.core.split_key = (function split_key(nested_key){return cljs.core.vec.call(null,cljs.core.map.call(null,cljs.core.keyword,clojure.string.split.call(null,nested_key,/:/)));
});
/**
* Removes the base-key from the nested-key.
* Note: The : is added to base-key to avoid getting a trailing :
* when the nested key is turned into a collection
*/
plato.core.clean_key = (function clean_key(base_key,nested_key){return clojure.string.replace_first.call(null,nested_key,[cljs.core.str(base_key),cljs.core.str(":")].join(''),"");
});
/**
* Takes an arbitrarily nested map and returns a list
* vectors, where each vectors is a tuple of a path
* describing, as well as a value.
* 
* For example, called with the base-vector ["com.example"],
* and the map {:a 1 :b {:c 2 :d 3}}, the function returns
* ([["com.example" :a] 1]
* [["com.example" :b :c] 2]
* [["com.example" :b :d] 3])
*/
plato.core.pathify = (function pathify(base_vector,m){return cljs.core.apply.call(null,cljs.core.concat,(function (){var iter__4119__auto__ = (function iter__4832(s__4833){return (new cljs.core.LazySeq(null,(function (){var s__4833__$1 = s__4833;while(true){
var temp__4092__auto__ = cljs.core.seq.call(null,s__4833__$1);if(temp__4092__auto__)
{var s__4833__$2 = temp__4092__auto__;if(cljs.core.chunked_seq_QMARK_.call(null,s__4833__$2))
{var c__4117__auto__ = cljs.core.chunk_first.call(null,s__4833__$2);var size__4118__auto__ = cljs.core.count.call(null,c__4117__auto__);var b__4835 = cljs.core.chunk_buffer.call(null,size__4118__auto__);if((function (){var i__4834 = 0;while(true){
if((i__4834 < size__4118__auto__))
{var vec__4838 = cljs.core._nth.call(null,c__4117__auto__,i__4834);var k = cljs.core.nth.call(null,vec__4838,0,null);var v = cljs.core.nth.call(null,vec__4838,1,null);cljs.core.chunk_append.call(null,b__4835,(function (){var built_vector = cljs.core.conj.call(null,base_vector,k);if(cljs.core.map_QMARK_.call(null,v))
{return pathify.call(null,built_vector,v);
} else
{return new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [built_vector,cljs.core.pr_str.call(null,v)], null)], null);
}
})());
{
var G__4840 = (i__4834 + 1);
i__4834 = G__4840;
continue;
}
} else
{return true;
}
break;
}
})())
{return cljs.core.chunk_cons.call(null,cljs.core.chunk.call(null,b__4835),iter__4832.call(null,cljs.core.chunk_rest.call(null,s__4833__$2)));
} else
{return cljs.core.chunk_cons.call(null,cljs.core.chunk.call(null,b__4835),null);
}
} else
{var vec__4839 = cljs.core.first.call(null,s__4833__$2);var k = cljs.core.nth.call(null,vec__4839,0,null);var v = cljs.core.nth.call(null,vec__4839,1,null);return cljs.core.cons.call(null,(function (){var built_vector = cljs.core.conj.call(null,base_vector,k);if(cljs.core.map_QMARK_.call(null,v))
{return pathify.call(null,built_vector,v);
} else
{return new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [built_vector,cljs.core.pr_str.call(null,v)], null)], null);
}
})(),iter__4832.call(null,cljs.core.rest.call(null,s__4833__$2)));
}
} else
{return null;
}
break;
}
}),null,null));
});return iter__4119__auto__.call(null,m);
})());
});
/**
* Takes a base-key and a map of entries on format
* {com.example:a 1
* com.example:nested:c 2}
* and returns a map such that the base-key is removed from the beginning
* of each key, and the rest is parsed into a map structure. The example above
* should return
* {:a 1 :nested {:c 2}}
*/
plato.core.unkeyify = (function unkeyify(base_key,keyified_map){var unkeyified_map = cljs.core.map.call(null,(function (p1__4841_SHARP_){var vec__4845 = p1__4841_SHARP_;var k = cljs.core.nth.call(null,vec__4845,0,null);var v = cljs.core.nth.call(null,vec__4845,1,null);return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [plato.core.split_key.call(null,plato.core.clean_key.call(null,base_key,k)),cljs.reader.read_string.call(null,v)], null);
}),keyified_map);return cljs.core.reduce.call(null,(function (p1__4842_SHARP_,p2__4843_SHARP_){return cljs.core.apply.call(null,cljs.core.assoc_in,p1__4842_SHARP_,p2__4843_SHARP_);
}),cljs.core.PersistentArrayMap.EMPTY,unkeyified_map);
});
plato.core.to_string = (function to_string(base_key,path_vector){return [cljs.core.str(base_key),cljs.core.str(cljs.core.apply.call(null,cljs.core.str,path_vector))].join('');
});
/**
* Updates a particular key stored in local storage.
* For example, (put "com.test" [:foo :bar] "Hello World!")
* will update the key com.test:foo:bar to have value "Hello world"
* in local storage.
*/
plato.core.put = (function put(base_key,path_vector,value){var the_key = plato.core.to_string.call(null,base_key,path_vector);return localStorage.setItem(the_key,value);
});
plato.core.put_all = (function put_all(base_key,path_vectors){var put = cljs.core.partial.call(null,plato.core.put,base_key);return cljs.core.doall.call(null,cljs.core.map.call(null,(function (p1__4846_SHARP_){return cljs.core.apply.call(null,put,p1__4846_SHARP_);
}),path_vectors));
});
/**
* Removes a value from local storage.
*/
plato.core.remove = (function remove(base_key,path_vector){var the_key = plato.core.to_string.call(null,base_key,path_vector);return localStorage.removeItem(the_key);
});
plato.core.remove_all = (function remove_all(base_key,path_vectors){var remove = cljs.core.partial.call(null,plato.core.remove,base_key);return cljs.core.doall.call(null,cljs.core.map.call(null,(function (p1__4847_SHARP_){return cljs.core.apply.call(null,remove,p1__4847_SHARP_);
}),path_vectors));
});
plato.core.put_state = (function put_state(base_key,state){return plato.core.put_all.call(null,base_key,plato.core.pathify.call(null,cljs.core.PersistentVector.EMPTY,state));
});
/**
* From a list of all keys, return a list with the keys that
* contains our base-key only.
*/
plato.core.filter_our_keys = (function filter_our_keys(base_key,all_keys){var base_pattern = cljs.core.re_pattern.call(null,[cljs.core.str("^"),cljs.core.str(base_key)].join(''));return cljs.core.filter.call(null,(function (p1__4848_SHARP_){return cljs.core.re_find.call(null,base_pattern,p1__4848_SHARP_);
}),all_keys);
});
plato.core.get_all = (function get_all(base_key){var all_keys = Object.keys(localStorage);var our_keys = plato.core.filter_our_keys.call(null,base_key,all_keys);var all_data = cljs.core.reduce.call(null,((function (all_keys,our_keys){
return (function (p1__4849_SHARP_,p2__4850_SHARP_){return cljs.core.assoc.call(null,p1__4849_SHARP_,p2__4850_SHARP_,(localStorage[p2__4850_SHARP_]));
});})(all_keys,our_keys))
,cljs.core.PersistentArrayMap.EMPTY,our_keys);return plato.core.unkeyify.call(null,base_key,all_data);
});
/**
* Sets the state of an atom to be that of the state
* retrieved from local storage.
*/
plato.core.reset_BANG_ = (function reset_BANG_(base_key,state_atom){return cljs.core.reset_BANG_.call(null,state_atom,plato.core.get_all.call(null,base_key));
});
plato.core.added_to_strings = (function added_to_strings(pathified){return cljs.core.apply.call(null,cljs.core.str,cljs.core.interpose.call(null,", ",cljs.core.map.call(null,(function (p1__4851_SHARP_){return [cljs.core.str(cljs.core.first.call(null,p1__4851_SHARP_)),cljs.core.str(" to "),cljs.core.str(cljs.core.second.call(null,p1__4851_SHARP_))].join('');
}),pathified)));
});
plato.core.removed_to_strings = (function removed_to_strings(pathified){return cljs.core.apply.call(null,cljs.core.str,cljs.core.interpose.call(null,", ",cljs.core.map.call(null,(function (p1__4852_SHARP_){return [cljs.core.str(cljs.core.first.call(null,p1__4852_SHARP_))].join('');
}),pathified)));
});
/**
* Updates local storage with all changes made to an atom.
* Call with true as second arg to switch on logging.
*/
plato.core.keep_updated_BANG_ = (function() {
var keep_updated_BANG_ = null;
var keep_updated_BANG___2 = (function (base_key,an_atom){return keep_updated_BANG_.call(null,base_key,an_atom,false);
});
var keep_updated_BANG___3 = (function (base_key,an_atom,log_updates){return cljs.core.add_watch.call(null,an_atom,new cljs.core.Keyword(null,"a-key","a-key",1104932453),(function (a_key,the_reference,old_state,new_state){var the_diff = plato.core.diff_states.call(null,old_state,new_state);var added = plato.core.pathify.call(null,cljs.core.PersistentVector.EMPTY,cljs.core.second.call(null,the_diff));var removed = plato.core.pathify.call(null,cljs.core.PersistentVector.EMPTY,cljs.core.first.call(null,the_diff));if(cljs.core.empty_QMARK_.call(null,added))
{} else
{if(cljs.core.truth_(log_updates))
{console.log("Updating in localStorage",plato.core.added_to_strings.call(null,added));
} else
{}
plato.core.put_all.call(null,base_key,added);
}
if(cljs.core.empty_QMARK_.call(null,removed))
{return null;
} else
{if(cljs.core.truth_(log_updates))
{console.log("Removing in localStorage",plato.core.removed_to_strings.call(null,removed));
} else
{}
return plato.core.remove_all.call(null,base_key,removed);
}
}));
});
keep_updated_BANG_ = function(base_key,an_atom,log_updates){
switch(arguments.length){
case 2:
return keep_updated_BANG___2.call(this,base_key,an_atom);
case 3:
return keep_updated_BANG___3.call(this,base_key,an_atom,log_updates);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
keep_updated_BANG_.cljs$core$IFn$_invoke$arity$2 = keep_updated_BANG___2;
keep_updated_BANG_.cljs$core$IFn$_invoke$arity$3 = keep_updated_BANG___3;
return keep_updated_BANG_;
})()
;

//# sourceMappingURL=core.js.map