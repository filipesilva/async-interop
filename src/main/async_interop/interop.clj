(ns async-interop.interop)

(defmacro <p!
  [exp]
  `(let [v# (cljs.core.async/<! (async-interop.interop/p->c ~exp))]
     (if (and 
          (instance? cljs.core/ExceptionInfo v#)
          (= (:error (ex-data v#)) :promise-error))
       (throw (ex-cause v#))
       v#)))
