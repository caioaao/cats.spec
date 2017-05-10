(ns cats.spec.monad.maybe
  #?@(:clj
      [(:require [cats.monad.maybe :as m.maybe]
                 [clojure.spec.alpha :as s]
                 [clojure.spec.gen.alpha :as gen])]
      :cljs
      [(:require [cats.monad.maybe :as m.maybe]
                 [clojure.spec :as s]
                 [cljs.spec.impl.gen :as gen])]))

(defn maybe-impl
  [form pred]
  (let [spec (delay (s/specize* pred form))]
    (reify
      s/Specize
      (specize* [s] s)
      (specize* [s _] s)

      s/Spec
      (conform* [_ x]
        (cond
          (not (m.maybe/maybe? x)) ::s/invalid
          (m.maybe/nothing? x) x
          :else (m.maybe/just (s/conform* @spec @x))))

      (unform* [_ x]
        (cond
          (not (m.maybe/maybe? x)) ::s/invalid
          (m.maybe/nothing? x) x
          :else (m.maybe/just (s/unform* @spec @x))))

      (explain* [_ path via in x]
        (cond
          (not (m.maybe/maybe? x)) {:path path :pred `m.maybe/maybe? :val x :via via :in in}
          (m.maybe/just? x) (s/explain* @spec (conj path :maybe/just)
                                        via in @x)))

      (gen* [this overrides path rmap]
        (if-let [gfn (:gfn this)]
          (gfn)
          (gen/frequency
           [[1 (gen/return (m.maybe/nothing))]
            [9 (gen/fmap m.maybe/just (s/gen* @spec overrides path rmap))]])))

      (with-gen* [this gfn] (assoc this :gfn gfn))

      (describe* [_]
        `(maybe ~form)))))

(defmacro maybe
  [pred]
  `(maybe-impl '~pred ~pred))
