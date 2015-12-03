(ns tutorial.droppable
  (:require [reagent.core :as reagent]
            [reagent-dnd.core :as dnd]
            [devcards.core :as dc])
  (:require-macros
   [reagent.ratom :refer [reaction]]
   [devcards.core
    :as dc
    :refer [defcard defcard-doc defcard-rg deftest]]))

(defn knight-span [drag-state]
  [:span {:style
          {:font-size "100px"
           :opacity
           (if (:dragging? @drag-state) 0.5 1)}}
   "♘"])

(defn square [& {:keys [black? piece drag-state]}]
  [:div {:style {:position         :relative
                 :width            "100%"
                 :height           "100%"
                 :background-color (if black? :black :white)
                 :color            (if black? :white :black)
                 :font-size        "32px"}}
   piece
   (when (:is-over? @drag-state)
     [:div {:style {:width            "100%"
                    :height           "100%"
                    :position         :absolute
                    :z-index          "1"
                    :opacity          "0.5"
                    :background-color :red}}])
   (when (:can-drop? @drag-state)
     [:div {:style {:width            "100%"
                    :height           "100%"
                    :position         :absolute
                    :z-index          "2"
                    :opacity          "0.5"
                    :background-color :green}}])])

(defn k [drop-state]
  (fn []
    (let [drag-state (reagent/atom {})]
      [:div [dnd/drag-source
             :type :knight
             :state drag-state
             :child [knight-span drag-state]]
       [:div {:style {:height "300px"
                      :width "300px"}}
        [dnd/drop-target
         :types :knight
         :state drop-state
         :child [square
                 :drag-state drop-state
                 :black? true
                 :piece nil]]]])))

(defcard-rg draggable-knight
  "Drag the knight around, and observe how the `drop-state` ratom
  changes."
  (fn [state _]
    [(dnd/with-drag-drop-context
       dnd/html5-backend
       (k state))])
  (reagent/atom {})
  {:inspect-data true})
