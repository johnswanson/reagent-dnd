(ns examples.views.nested-drop-targets
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

(declare drop-target-for-id drop-target)

(defn drop-target-from-id [state id]
  (let [target (subscribe [:nested-drop-target id])]
    (fn [state id]
      [:div {:style {:padding "1em"
                     :color "#ccc"
                     :background-color (if (or
                                            (:shallow-over? @state)
                                            (and (:over? @state) (:greedy? @target)))
                                         "#444"
                                         "#888")
                     :border "1px solid black"}}
       [:label
        [:input {:type :checkbox
                 :checked (:greedy? @target)
                 :on-change #(dispatch [:toggle-greedy-drop-target id])}]
        [:span "greedy"]
        [:span (when (:dropped? @target)
                 [:span " (Dropped)"])]
        [:span (when (:dropped-on-child? @target)
                 [:span " (on child) "])]]
       (for [child (:children @target)]
         ^{:key (name child)}
         [:div {:style {:margin-left "5px"}}
          [drop-target child]])])))

(defn greedy? [id]
  (:greedy? @(subscribe [:nested-drop-target id])))

(defn drop-target [id]
  (let [state (r/atom {})]
    (fn [id]
      [dnd/drop-target
       :drop
       (fn [monitor]
         (js/console.log (name id) (clj->js (select-keys monitor [:dropped?])))
         (dispatch [:dropped-on-nested-drop-target id
                    :dropped? (:dropped? monitor)])
         true)
       :types [:box]
       :state state
       :child [drop-target-from-id state id]])))

(defn box [state]
  [:div {:style {:border  "1px solid black"
                 :padding "1em"
                 :opacity (if (:dragging? @state) 0.5 1)}
         :on-click #(dispatch [:initialize-nested-drop-targets])}
   [:span "Drag Me (or click to clear)"]])

(defn drag-source []
  (let [state (r/atom {})]
    [dnd/drag-source
     :state state
     :type :box
     :child [box state]]))

(defn view []
  [:div
   [:div {:style {:display         :flex
                  :flex-wrap       :wrap
                  :justify-content :space-around}}
    [:div {:style {:flex "0 1 auto"}}
     [drop-target :root1]]]
   [:div {:style {:display         :flex
                  :margin          "2em"
                  :flex-wrap       :wrap
                  :justify-content :center}}
    [drag-source]]])
