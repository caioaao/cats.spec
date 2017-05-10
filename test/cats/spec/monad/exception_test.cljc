(ns cats.spec.monad.exception-test
  #?@(:clj
      [(:require [cats.spec.monad.exception :as s.exc]
                  [clojure.test :as t]
                  [clojure.spec.alpha :as s]
                  [clojure.spec.gen.alpha :as gen]
                  [cats.monad.exception :as m.exc]
                  [cats.protocols :as p])]

      :cljs
      [(:require [cats.monad.exception :as m.exc]
                 [cats.protocols :as p :include-macros true]
                 [cats.spec.monad.exception :as s.exc :include-macros true]
                 [cljs.spec.impl.gen :as gen]
                 [cljs.test :as t]
                 [clojure.spec :as s]
                 [clojure.test.check.generators])]))

(s/def :exc/success int?)
(s/def :exc/failure (s/with-gen #(instance? #?(:clj clojure.lang.ExceptionInfo
                                               :cljs cljs.core.ExceptionInfo) %)
                      #(gen/return (ex-info "Wat" {}))))

(s/def :exc/computation-result (s.exc/exception :exc/success :exc/failure))

(t/deftest exception-spec-test
  (t/testing "Works with clojure.spec api"
    (doseq [v (gen/sample (s/gen :exc/computation-result))]
      (t/is (s/valid? :exc/computation-result v))
      (t/is (m.exc/exception? v))
      (t/is (= v
               (s/conform :exc/computation-result v)
               (s/unform :exc/computation-result v)
               (s/unform :exc/computation-result (s/conform :exc/computation-result v))))
      (t/is (or (and (m.exc/success? v) (s/valid? :exc/success @v))
                (and (m.exc/failure? v) (s/valid? :exc/failure (p/-extract v))))))

    (t/is (= (s/describe :exc/computation-result) '(either :exc/success :exc/failure))))

  (t/testing "Explain works for `exception?` pred"
    (t/is (-> (s/explain-data :exc/computation-result 1)
              ::s/problems
              (= {:path [], :pred `m.exc/exception?, :val 1, :via [:exc/computation-result], :in []}))))

  (t/testing "Explain works for any side of the branch"
    (t/is (= (s/explain-data :exc/computation-result (m.exc/success "123"))
             {::s/problems [{:path [:either/right]
                             :pred 'int?
                             :val "123"
                             :via [:exc/computation-result]
                             :in []}]}))

    (let [exception (#?(:clj Exception. :cljs js/Error) "123")
          explain-failure (s/explain-data :exc/computation-result (m.exc/failure exception))]
      (t/is (= explain-failure
               {::s/problems [{:path [:either/left]
                               :pred ::s/unknown
                               :val exception
                               :via [:exc/computation-result]
                               :in []}]})))))
