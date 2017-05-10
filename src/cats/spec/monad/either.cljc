(ns cats.spec.monad.either
  #?@(:clj
      [(:require [cats.monad.either :as m.either]
                 [clojure.spec.alpha :as s]
                 [clojure.spec.gen.alpha :as gen])]
      :cljs
      [(:require [cats.monad.either :as m.either]
                 [clojure.spec :as s]
                 [cljs.spec.impl.gen :as gen])]))

(declare either)

(defn either-impl
  [form-r pred-r form-l pred-l]
  (let [spec-r (delay (s/specize* pred-r form-r))
        spec-l (delay (s/specize* pred-l form-l))]
    (reify
      s/Specize
      (specize* [s] s)
      (specize* [s _] s)

      s/Spec
      (conform* [_ x]
        (let [conformed-v (delay (s/conform* (if (m.either/right? x) @spec-r @spec-l) @x))]
          (cond
            (or (not (m.either/either? x)) (= @conformed-v ::s/invalid)) ::s/invalid
            (m.either/right? x) (m.either/right @conformed-v)
            (m.either/left? x) (m.either/left @conformed-v))))

      (unform* [_ x]
        (let [unformed-v (delay (s/conform* (if (m.either/right? x) @spec-r @spec-l) @x))]
          (cond
            (or (not (m.either/either? x)) (= @unformed-v ::s/invalid)) ::s/invalid
            (m.either/right? x) (m.either/right @unformed-v)
            (m.either/left? x) (m.either/left @unformed-v))))

      (explain* [_ path via in x]
        (cond
          (not (m.either/either? x)) {:path path :pred `m.either/either? :val x :via via :in in}
          (m.either/right? x) (s/explain* @spec-r
                                          (conj path :either/right)
                                          via in @x)
          (m.either/left? x) (s/explain* @spec-l
                                         (conj path :either/left)
                                         via in @x)))

      (gen* [this overrides path rmap]
        (if-let [gfn (:gfn this)]
          (gfn)
          (gen/frequency
           [[1 (gen/fmap m.either/right (s/gen* @spec-r overrides path rmap))]
            [1 (gen/fmap m.either/left (s/gen* @spec-l overrides path rmap))]])))

      (with-gen* [this gfn] (assoc this :gfn gfn))

      (describe* [_]
        `(either ~form-r ~form-l)))))

(defmacro either
  [pred-r pred-l]
  `(either-impl '~pred-r ~pred-r '~pred-l ~pred-l))
