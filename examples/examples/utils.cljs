(ns examples.utils
  (:require [clojure.string :as str]
            [clojure.walk]
            [medley.core :refer [map-vals]]
            [markdown.core :refer [md->html]]))

(defn make-title [id]
  (-> id
      name
      (str/split #"-")
      (#(map str/capitalize %))
      (#(str/join " " %))))

(defn make-url [category id]
  (str "#/examples/" (name category) "/" (name id)))

(declare show show-state)

(defn document [& args]
  [:div
   (for [arg args]
     ^{:key (pr-str arg)}
     [:div {:style
            {:margin "50px 0"}}
      (show arg)])])

(defn card [& {:keys [content state]}]
  [:div.card
   content
   (show-state state)])

(defmulti show #(type %))
(defmethod show js/String [s]
  [:div {:dangerouslySetInnerHTML
      {:__html (md->html s)}}])
(defmethod show PersistentVector [s]
  s)
(defmethod show :default [s]
  s)

(defn atom? [thing] (implements? IDeref thing))

(defn show-state [state]
  (devcards.util.edn-renderer/html-edn
   (clojure.walk/prewalk
    (fn [form]
      (if (atom? form)
        @form
        form))
    state)))
