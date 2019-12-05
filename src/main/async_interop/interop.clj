(ns async-interop.interop)

(defmacro <p!
  "Takes the value of a promise resolution. The value of a rejected promise 
  will be thrown wrapped in a instance of ExceptionInfo, acessible via ex-cause."
  [exp]
  `(async-interop.interop/throw-err (cljs.core.async/<! (async-interop.interop/p->c ~exp))))