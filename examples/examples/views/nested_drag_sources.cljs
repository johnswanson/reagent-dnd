(ns examples.views.nested-drag-sources
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

(declare drag-source draggable-drag-source drag-source*)

(defn drag-source [id]
  (let [me (subscribe [:nested-drag-source id])]
    (fn [id]
      [:div {:style {:padding-left "10px"}}
       [draggable-drag-source
        :id id
        :forbidden? (:forbidden? @me)
        :color (:color @me)
        :children (:children @me)]])))

(defn draggable-drag-source [& {:keys [id color children forbidden?]}]
  (let [drag-state (r/atom {})]
    [dnd/drag-source
     :can-drag? #(not forbidden?)
     :child [drag-source* drag-state
             :id id
             :color color
             :children children
             :forbidden? forbidden?]
     :type :box
     :state drag-state
     :begin-drag (fn [] {:id id})]))


(defn drag-source* [drag-state & {:keys [id color children forbidden?]}]
  [:div {:style {:background-color (if (= color :blue)
                                     "#88f" "#ff8")
                 :cursor           :move
                 :opacity          (if (:dragging? @drag-state) 0.5 1)
                 :border           "1px solid black"
                 :padding          "10px"}}
   [:h4 (name color)]
   [:label [:input {:type      :checkbox
                    :checked   forbidden?
                    :on-change (fn []
                                 (dispatch [:toggle-forbid-drag-source id]))}]
    [:span "Forbid Drag"]]
   [:div
    (for [child children]
      ^{:key (name child)}
      [drag-source child])]])


(defn drag-source-root []
  (let [root (subscribe [:nested-drag-source-root])]
    (fn []
      [draggable-drag-source
       :id :root
       :color (:color @root)
       :children (:children @root)
       :forbidden? (:forbidden? @root)])))

(defn drop-target* [state]
  [:div {:style {:background-color (if (:over? @state)
                                     :grey
                                     :white)
                 :padding "100px"
                 :margin "1px"
                 :border "2px solid black"}}
   [:span "Drop Here!"]])

(defn drop-target []
  (let [drag-state (r/atom {})]
    [dnd/drop-target
     :child [drop-target* drag-state]
     :types [:box]
     :drop (fn [{:keys [item]}]
             (dispatch [:nested-drag-source-dropped item]))
     :state drag-state]))

(defn content []
  (document
   [:div {:style {:display :flex}}
    [:div {:style {:flex "0 1 auto"}}
     [drag-source-root]]
    [:div {:style {:flex "0 1 auto"}}
     [drop-target]]]))

(defn view []
  [card
   :content [content]
   :state (array-map
           :tree (subscribe [:nested-drag-source-tree]))])
