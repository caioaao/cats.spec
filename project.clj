(defproject cats.spec "0.1.0-SNAPSHOT"
  :description "specs for funcool cats monads"
  :url "http://github.com/caioaao/cats.spec"
    :license {:name "BSD (2 Clause)"
            :url  "http://opensource.org/licenses/BSD-2-Clause"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha16" :scope "provided"]
                 [org.clojure/clojurescript "1.9.521" :scope "provided"]
                 [org.clojure/spec.alpha "0.1.94" :scope "provided"]
                 [org.clojure/test.check "0.9.0" :scope "provided"]
                 [funcool/cats "2.1.1" :scope "provided"]]
  :source-paths ["src"]
  :test-paths ["test"])
