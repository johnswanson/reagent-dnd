(ns examples.views.drag-around-naive
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

(defn draggable [drag-state]
  (let [pos (subscribe [:drag-around-naive :position])]
    (fn [drag-state]
      (let [[left top] @pos]
        [:div {:style {:background-color :white
                       :cursor           :move
                       :display          (if (:dragging? @drag-state)
                                           :none :block)
                       :border           "1px dashed black"
                       :padding          ".5rem 1rem"
                       :top              top
                       :left             left
                       :position         :absolute}}
         "Drag Me Around"]))))

(defn source [drag-state]
  (let [pos (subscribe [:drag-around-naive :position])]
    (fn [drag-state]
      [:div {:style {:position :relative}}
       [dnd/drag-source
        :child [draggable drag-state]
        :type :box
        :begin-drag (constantly @pos)
        :state drag-state]])))

(defn target [drag-state drop-state]
  [dnd/drop-target
   :child [:div {:style {:height "400px"
                         :width "400px"
                         :border "2px solid black"}}
           [source drag-state]]
   :types [:box]
   :state drop-state
   :drop (fn [args]
           (let [[x y]   (:item args)
                 [dx dy] (:difference-from-initial-offset args)]
             (dispatch [:move-naive-handler [(+ x dx)
                                             (+ y dy)]])))])

(defn view []
  (let [drag-state (r/atom {})
        drop-state (r/atom {})]
    (fn []
      [card
       :content (document
                 [target drag-state drop-state])
       :state {:drag-state drag-state
               :drop-state drop-state}])))

