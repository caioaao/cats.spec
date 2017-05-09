(ns cats.spec.monad.either-test
  (:require [cats.monad.either :as m.either]
            [cats.spec.monad.either :as s.either]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.test :as t]))

(s/def :player/name string?)
(s/def :player/gender #{:male :female})
(s/def :player/age (s/and int? pos? #(< % 120)))
(s/def :player/player (s/keys :req [:player/name :player/gender :player/age]))

(s/def :move/delta int?)
(s/def :move/player :player/player)
(s/def :move/move (s/keys :req [:move/delta :move/player]))

(s/def :move/error string?)

(s/def :move/move-or-error (s.either/either :move/move :move/error))


(t/deftest either-spec-test
  (t/testing "Works with clojure.spec api"
    (doseq [v (gen/sample (s/gen :move/move-or-error))]
      (t/is (s/valid? :move/move-or-error v))
      (t/is (m.either/either? v))
      (t/is (= v
               (s/conform :move/move-or-error v)
               (s/unform :move/move-or-error v)
               (s/unform :move/move-or-error (s/conform :move/move-or-error v))))
      (t/is (or (and (m.either/right? v) (s/valid? :move/move @v))
                (and (m.either/left? v) (s/valid? :move/error @v)))))

    (t/is (= (s/describe :move/move-or-error) '(either :move/move :move/error))))

  (t/testing "Explain works for `either?` pred"
    (t/is (-> (s/explain-data :move/move-or-error 1)
              ::s/problems
              (= {:path [], :pred `m.either/either?, :val 1, :via [:move/move-or-error], :in []}))))

  (t/testing "Explain works for any side of the branch"
    (let [right-v (gen/generate (s/gen :move/move))
          left-v (gen/generate (s/gen :move/error))]
      (t/is (-> (s/explain-data :move/move-or-error (m.either/right left-v))
                ::s/problems
                first
                (= {:path [:either/right], :pred 'map?, :val left-v, :via [:move/move-or-error], :in []})))

      (t/is (-> (s/explain-data :move/move-or-error (m.either/left right-v))
                ::s/problems
                first
                (= {:path [:either/left], :pred 'string?, :val right-v, :via [:move/move-or-error], :in []}))))))
