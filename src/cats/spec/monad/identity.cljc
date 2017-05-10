(ns cats.spec.monad.identity
  #?@(:clj
      [(:require [clojure.spec.alpha :as s]
                 [cats.monad.identity :as m.identity]
                 [clojure.spec.gen.alpha :as gen])]

      :cljs
      [(:require [clojure.spec :as s]
                 [cats.monad.identity :as m.identity]
                 [clojure.spec.impl.gen :as gen])]))

(defn identity?
  [v]
  (instance? cats.monad.identity.Identity v))

(defn identity-impl
  [form pred]
  (let [spec (delay (s/specize* pred form))]
    (reify
      s/Specize
      (specize* [s] s)
      (specize* [s _] s)

      s/Spec
      (conform* [_ x]
        (if (not (identity? x))
          ::s/invalid
          (m.identity/identity (s/conform* @spec @x))))

      (unform* [_ x]
        (if (not (identity? x))
          ::s/invalid
          (m.identity/identity (s/unform* @spec @x))))

      (explain* [_ path via in x]
        (if (not (identity? x))
          {:path path :pred `identity? :val x :via via :in in}
          (s/explain* @spec (conj path :identity/self)
                     via in @x)))

      (gen* [this overrides path rmap]
        (if-let [gfn (:gfn this)]
          (gfn)
          (gen/fmap m.identity/identity (s/gen* @spec overrides path rmap))))

      (with-gen* [this gfn] (assoc this :gfn gfn))

      (describe* [_]
        `(identity ~form)))))

(defmacro identity
  [pred]
  `(identity-impl '~pred ~pred))
