(ns async-interop.interop-tests
  (:require
   [cljs.core.async :refer [go]]
   [async-interop.interop :refer-macros [<p!]]
   [cljs.test :refer-macros [deftest is async]]))

; Native JS promise tests
(deftest native-resolve
  (async done
         (.finally
          (.then (js/Promise.resolve 42)
                 #(is (= %1 42))
                 #(is false "Should not reject"))
          (done))))

(deftest native-multiple-resolve
  (async done
         (let [total (atom 0)]
           (-> (js/Promise.resolve 1)
               (.then #(swap! total + %))
               (.then #(js/Promise.resolve 2))
               (.then #(swap! total + %))
               (.then #(js/Promise.resolve 3))
               (.then #(swap! total + %))
               (.finally
                #(do (is (= @total 6))
                     (done)))))))

(deftest native-catch
  (async done
         (.finally
          (let [err (js/Error. "Rejected")]
            (.then (js/Promise.reject err)
                   #(is false "Should not resolve")
                   #(is (= err %))))
          (done))))

; This is similar to the puppeteer example in https://github.com/athos/kitchen-async#example.
(deftest native-nested
  (async done
         (let [total (atom 0)]
           (-> (js/Promise.resolve 1)
               (.then (fn [first-res]
                        (-> (js/Promise.resolve 2)
                            (.then (fn [second-res]
                                     (-> (js/Promise.resolve 3)
                                         (.then #(swap! total + %))
                                         (.then #(js/Promise.resolve 5))
                                         (.then #(swap! total + %))
                                         (.then #(swap! total + first-res))
                                         (.then #(swap! total + second-res))
                                         (.finally
                                          #(do (is (= @total 11))
                                               (done)))))))))))))

; Equivalent async-interop tests
(deftest interop-resolve
  (async done
         (go
           (is (= (<p! (js/Promise.resolve 42)) 42))
           (done))))

(deftest interop-multiple-resolve
  (async done
         (go
           (let [total (atom 0)]
             (swap! total + (<p! (js/Promise.resolve 1)))
             (swap! total + (<p! (js/Promise.resolve 2)))
             (swap! total + (<p! (js/Promise.resolve 3)))
             (is (= @total 6))
             (done)))))

(deftest interop-catch
  (async done
         (let [err (js/Error. "Rejected")]
           (go
             (is (= err
                    (ex-cause 
                     (is (thrown?
                          js/Error
                          (<p! (js/Promise.reject err)))))))
             (done)))))

(deftest interop-catch-non-error
  (async done
         (let [err "Rejected"]
           (go
             (is (= err
                    (ex-cause
                     (is (thrown?
                          js/Error
                          (<p! (js/Promise.reject err)))))))
             (done)))))

(deftest interop-nested
  (async done
         (go
           (let [total (atom 0)
                 first-res (<p! (js/Promise.resolve 1))
                 second-res (<p! (js/Promise.resolve 2))]
             (swap! total + (<p! (js/Promise.resolve 3)))
             (swap! total + (<p! (js/Promise.resolve 5)))
             (swap! total + first-res)
             (swap! total + second-res)
             (is (= @total 11))
             (done)))))

(deftest interop-multiple-consumer
  (async done
         (go
          (let [p (js/Promise.resolve 42)]
            (is (= (<p! p) 42))
            (is (= (<p! p) 42))
            (done)))))