(require '[cljs.build.api :as b])

(b/watch (b/inputs "test" "src")
  {:main 'cats.spec.runner
   :target :nodejs
   :output-to "out/tests.js"
   :output-dir "out"
   :verbose true})
