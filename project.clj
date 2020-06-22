(defproject com.caioaao/cats.spec "0.1.0"
  :description "specs for funcool cats monads"
  :url "http://github.com/caioaao/cats.spec"
  :license {:name "BSD (2 Clause)"
            :url  "http://opensource.org/licenses/BSD-2-Clause"}
  :manifest {"GIT_COMMIT"   ~(System/getenv "GIT_COMMIT")
             "BUILD_NUMBER" ~(System/getenv "BUILD_NUMBER")}
  :scm "https://github.com/caioaao/tank"
  :dependencies [[org.clojure/clojure "1.10.0" :scope "provided"]
                 [org.clojure/clojurescript "1.9.521" :scope "provided"]
                 [org.clojure/spec.alpha "0.1.94" :scope "provided"]
                 [org.clojure/test.check "0.9.0" :scope "provided"]
                 [funcool/cats "2.0.0" :scope "provided"]]
  :profiles {:dev {:dependencies []
                   :plugins      [[lein-cljfmt "0.6.7"]]
                   :global-vars  {*warn-on-reflection* true}}}
  :source-paths ["src"]
  :test-paths ["test"]
  :release-tasks [["deploy" "clojars"]]
  :deploy-repositories [["clojars" {:url           "https://clojars.org/repo"
                                    :sign-releases false
                                    :username      :env/clojars_username
                                    :password      :env/clojars_password}]])
