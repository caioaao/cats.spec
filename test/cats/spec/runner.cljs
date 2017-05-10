(ns cats.spec.runner
  (:require [clojure.string :as str]
            [cljs.test :as test]
            [cats.spec.monad.identity-test]
            [cats.spec.monad.maybe-test]
            [cats.spec.monad.either-test]
            [cats.spec.monad.exception-test]))

(enable-console-print!)

(defn -main []
  (test/run-tests
   (test/empty-env)
   'cats.spec.monad.identity-test
   'cats.spec.monad.maybe-test
   'cats.spec.monad.either-test
   'cats.spec.monad.exception-test))

(defmethod test/report [:cljs.test/default :end-run-tests] [m]
  (when-not (test/successful? m)
    ((aget js/process "exit") 1)))

(set! *main-cli-fn* -main)
