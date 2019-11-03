(ns async-interop.interop
  (:require [cljs.core.async :refer [chan close! put!]]))

(defn p->c
  [p]
  (let [c (chan)]
    (.then p
           (fn [res]
             (if (nil? res)
               (close! c)
               (put! c res)))
           (fn [err]
             (put! c
                   (ex-info "Promise error"
                            {:error :promise-error} 
                            err))))
    c))
