(ns reagent-dnd.react-dnd
  (:require [reagent.core :as r]
            [cljsjs.react-dnd]
            [cljsjs.react-dnd-html5-backend]))

(def drag-drop-context
  (.-DragDropContext js/ReactDnD))

(def drag-layer
  (.-DragLayer js/ReactDnD))

(def drag-source
  (.-DragSource js/ReactDnD))

(def drop-target
  (.-DropTarget js/ReactDnD))

(def html5-backend js/ReactDnDHTML5Backend)

(def get-empty-image (.getEmptyImage html5-backend))

(defn with-drag-drop-context [backend]
  (fn [component]
    (r/adapt-react-class
     ((drag-drop-context backend)
      (r/reactify-component component)))))
