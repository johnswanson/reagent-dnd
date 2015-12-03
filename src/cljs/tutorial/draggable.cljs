(ns tutorial.draggable
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
          {:opacity
           (if (:dragging? @drag-state) 0.5 1)}}
   "♘"])

(defn knight []
  (let [drag-state (reagent/atom {})]
    [dnd/drag-source
     :type :knight
     :state drag-state
     :child [knight-span drag-state]]))

(defn k [s]
  (fn []
    [dnd/drag-source
     :type :knight
     :state s
     :child [knight-span s]]))

(defcard-rg draggable-knight
  "This knight is draggable."
  (fn [state _]
    [(dnd/with-drag-drop-context
       dnd/html5-backend
       (k state))])
  (reagent/atom {})
  {:inspect-data true})
