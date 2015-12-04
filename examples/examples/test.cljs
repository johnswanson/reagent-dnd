(ns examples.test
  (:require-macros [devcards.core :as dc :refer [defcard-rg]])
  (:require [devcards.core :as dc]))

(defn rg-test [] [:div "hello world"])


(defcard-rg hello-world [rg-test])
