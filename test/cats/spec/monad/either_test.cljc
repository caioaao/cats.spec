(ns cats.spec.monad.either-test
  #?@(:clj
      [(:require [cats.monad.either :as m.either]
                 [cats.spec.monad.either :as s.either]
                 [clojure.spec.alpha :as s]
                 [clojure.spec.gen.alpha :as gen]
                 [clojure.test :as t])]

      :cljs
      [(:require [cats.monad.either :as m.either]
                 [cats.spec.monad.either :as s.either :include-macros true]
                 [clojure.spec :as s]
                 [clojure.test.check.generators]
                 [cljs.spec.impl.gen :as gen]
                 [cljs.test :as t])]))

(s/def :either.player/name string?)
(s/def :either.player/gender #{:male :female})
(s/def :either.player/age (s/and int? pos? #(< % 120)))
(s/def :either.player/player (s/keys :req [:either.player/name :either.player/gender :either.player/age]))

(s/def :either.move/delta int?)
(s/def :either.move/player :either.player/player)
(s/def :either.move/move (s/keys :req [:either.move/delta :either.move/player]))

(s/def :either.move/error string?)

(s/def :either/move-or-error (s.either/either :either.move/move :either.move/error))

(t/deftest either-spec-test
  (t/testing "Works with clojure.spec api"
    (doseq [v (gen/sample (s/gen :either/move-or-error))]
      (t/is (s/valid? :either/move-or-error v))
      (t/is (m.either/either? v))
      (t/is (= v
               (s/conform :either/move-or-error v)
               (s/unform :either/move-or-error v)
               (s/unform :either/move-or-error (s/conform :either/move-or-error v))))
      (t/is (or (and (m.either/right? v) (s/valid? :either.move/move @v))
                (and (m.either/left? v) (s/valid? :either.move/error @v)))))

    (t/is (= (s/describe :either/move-or-error) '(either :either.move/move :either.move/error))))

  (t/testing "Explain works for `either?` pred"
    (t/is (-> (s/explain-data :either/move-or-error 1)
              ::s/problems
              (= {:path [], :pred `m.either/either?, :val 1, :via [:either/move-or-error], :in []}))))

  (t/testing "Explain works for any side of the branch"
    (let [right-v (gen/generate (s/gen :either.move/move))
          left-v (gen/generate (s/gen :either.move/error))]
      (t/is (-> (s/explain-data :either/move-or-error (m.either/right left-v))
                ::s/problems
                first
                (= {:path [:either/right], :pred 'map?, :val left-v, :via [:either/move-or-error], :in []})))

      (t/is (-> (s/explain-data :either/move-or-error (m.either/left right-v))
                ::s/problems
                first
                (= {:path [:either/left], :pred 'string?, :val right-v, :via [:either/move-or-error], :in []}))))))
