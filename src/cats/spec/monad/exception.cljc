(ns cats.spec.monad.exception
  (:require [clojure.spec.alpha :as s]
            [cats.monad.exception :as m.exc]
            [cats.protocols :as p]
            [clojure.spec.gen.alpha :as gen]))

;; TODO Assert if `pred-failure` is a pred for an exception
(defn exception-impl
  [form-success pred-success form-failure pred-failure]
  (let [spec-success (delay (s/specize* pred-success form-success))
        spec-failure (delay (s/specize* pred-failure form-failure))]
    (reify
      s/Specize
      (specize* [s] s)
      (specize* [s _] s)

      s/Spec
      (conform* [_ x]
        (let [conformed-v (delay (s/conform* (if (m.exc/success? x) @spec-success @spec-failure) (p/-extract x)))]
          (cond
            (or (not (m.exc/exception? x))
                (= @conformed-v ::s/invalid)
                (and (m.exc/failure? x) (not (instance? Exception @conformed-v))))
            ::s/invalid

            (m.exc/success? x)
            (m.exc/success @conformed-v)

            (m.exc/failure? x)
            (m.exc/failure @conformed-v))))

      (unform* [_ x]
        (let [unformed-v (delay (s/unform* (if (m.exc/success? x) @spec-success @spec-failure) (p/-extract x)))]
          (cond
            (or (not (m.exc/exception? x))
                (= @unformed-v ::s/invalid)
                (and (m.exc/failure? x) (not (instance? Exception @unformed-v))))
            ::s/invalid

            (m.exc/success? x)
            (m.exc/success @unformed-v)

            (m.exc/failure? x)
            (m.exc/failure @unformed-v))))

      (explain* [_ path via in x]
        (cond
          (not (m.exc/exception? x)) {:path path :pred `m.exc/exception? :val x :via via :in in}
          (m.exc/success? x) (s/explain* @spec-success
                                         (conj path :either/right)
                                         via in (p/-extract x))
          (m.exc/failure? x) (s/explain* @spec-failure
                                         (conj path :either/left)
                                         via in (p/-extract x))))

      (gen* [this overrides path rmap]
        (if-let [gfn (:gfn this)]
          (gfn)
          (gen/frequency
           [[1 (gen/fmap m.exc/success (s/gen* @spec-success overrides path rmap))]
            [1 (gen/fmap m.exc/failure (s/gen* @spec-failure overrides path rmap))]])))

      (with-gen* [this gfn] (assoc this :gfn gfn))

      (describe* [_]
        `(either ~form-success ~form-failure)))))

(defmacro exception
  [pred-success pred-failure]
   `(exception-impl '~pred-success ~pred-success '~pred-failure ~pred-failure))
