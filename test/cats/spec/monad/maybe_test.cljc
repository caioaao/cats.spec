(ns cats.spec.monad.maybe-test
  #?@(:clj
      [(:require [cats.monad.maybe :as m.maybe]
                 [cats.spec.monad.maybe :as s.maybe]
                 [clojure.spec.alpha :as s]
                 [clojure.spec.gen.alpha :as gen]
                 [clojure.test :as t])]

      :cljs
      [(:require [cats.monad.maybe :as m.maybe]
                 [cats.spec.monad.maybe :as s.maybe :include-macros true]
                 [clojure.spec :as s]
                 [clojure.test.check.generators]
                 [cljs.spec.impl.gen :as gen]
                 [cljs.test :as t])]))

(s/def :maybe/int (s.maybe/maybe int?))

(t/deftest maybe-spec-test
  (t/testing "Works with clojure.spec api"
    (doseq [v (gen/sample (s/gen :maybe/int))]
      (t/is (s/valid? :maybe/int v))
      (t/is (m.maybe/maybe? v))
      (t/is (or (and (m.maybe/just? v) (int? @v))
                (m.maybe/nothing? v)))
      (t/is (= v
               (s/conform :maybe/int v)
               (s/unform :maybe/int v)
               (s/unform :maybe/int (s/conform :maybe/int v)))))

    (t/is (= (s/describe :maybe/int) '(maybe int?))))

  (t/testing "Explain works"
    (t/is (= (s/explain-data :maybe/int :any)
             {::s/problems {:path []
                            :pred `m.maybe/maybe?
                            :val :any
                            :via [:maybe/int]
                            :in []}}))
    (t/is (= (s/explain-data :maybe/int (m.maybe/just "str"))
             {::s/problems [{:path [:maybe/just]
                             :pred 'int?
                             :val "str"
                             :via [:maybe/int]
                             :in []}]}))))
