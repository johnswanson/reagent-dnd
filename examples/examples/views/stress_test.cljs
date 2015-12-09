(ns examples.views.stress-test
  (:require-macros [devcards.core :as dc])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :as str]
            [devcards.core :as dc]
            [devcards.util.edn-renderer]
            [reagent.core :as r]
            [reagent-dnd.core :as dnd]
            [reagent.ratom]
            [markdown.core :refer [md->html]]
            [cljsjs.highlight]
            [cljsjs.highlight.langs.clojure]
            [medley.core :refer [map-vals]]
            [examples.utils :refer [make-title make-url document card]]))

(defn bin [state type]
  [:div {:style {:border (if (:can-drop? @state)
                           "1px solid black"
                           "1px dashed black")
                 :float :left
                 :background-color (cond
                                     (:over? @state) :purple
                                     (:can-drop? @state) :yellow
                                     :default :white)
                 :height "100px"
                 :width  "100px"}}
   (make-title type)])

(defn droppable [type state]
  [dnd/drop-target
   :types [type]
   :child [bin state type]
   :state state])

(defn box [id state]
  [:span
   {:style {:border           "1px dashed black"
            :background-color (if (:dragging? @state) :grey :white)
            :display          (if (:dragging? @state) :none :inline)
            :text-align       :center
            :padding          "10px"
            :margin           "10px"
            :cursor           :move}}
   (make-title id)])

(defn draggable [type state]
  [dnd/drag-source
   :type type
   :state state
   :child [box type state]
   :begin-drag (constantly type)])

(defn drag-sources [states]
  (let [types (subscribe [:stress-test-drag-sources])]
    (fn [states]
      [:div
       (for [type (or @types [])
             :let [state (type states)]]
         ^{:key (str "drag-" type)}
         [draggable type state])])))

(defn drop-targets [states]
  (let [types (subscribe [:stress-test-drop-targets])]
    (fn [states]
      [:div
       (for [type @types
             :let [state (type states)]]
         ^{:key (str "drop-" type)}
         [droppable type state])])))

(defn view []
  (let [types [:glass :banana :pepper]
        drag-states (apply array-map
                           (interleave
                            types
                            (repeatedly #(r/atom {}))))
        drop-states (apply array-map
                           (interleave
                            types
                            (repeatedly #(r/atom {}))))]
    (fn []
      (document
       "# Stress Test"
       [card
        :content [:div
                  [drop-targets drop-states]
                  [drag-sources drag-states]]
        :state (array-map
                :drag-sources drag-states
                :drop-targets drop-states)]))))
