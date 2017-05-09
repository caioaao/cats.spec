(ns cats.spec.monad.identity-test
  (:require [cats.monad.identity :as m.identity]
            [cats.spec.monad.identity :as s.identity]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.test :as t]))

(s/def :unq/identity (s.identity/identity int?))

(t/deftest identity-spec-test
  (t/testing "Works with clojure.spec api"
    (let [v (gen/generate (s/gen :unq/identity))]
      (t/is (s/valid? :unq/identity v))
      (t/is (m.identity/identity? v))
      (t/is (= v
               (s/conform :unq/identity v)
               (s/unform :unq/identity v)
               (s/unform :unq/identity (s/conform :unq/identity v))))
      (t/is (s/valid? int? @v))))

  (t/testing "explain-data"
    (t/is (= (s/explain-data :unq/identity (m.identity/identity "str"))
             {::s/problems [{:path [:identity/self], :pred 'int?, :val "str", :via [:unq/identity], :in []}]}))))

