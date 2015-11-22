(ns reagent-dnd.core
  (:require [reagent.core :as r]
            [reagent-dnd.react-dnd :as react-dnd]))

(def drag-drop-context react-dnd/drag-drop-context)

(def html5-backend react-dnd/html5-backend)

(defn with-drag-drop-context [backend component]
  (r/adapt-react-class
   ((drag-drop-context backend)
    (r/reactify-component component))))

(defn drop-target
  [& {:keys [child types drop state can-drop?]
      :or {drop      (constantly nil)
           can-drop? (constantly true)}}]
  (let [wrapper (react-dnd/drop-target (clj->js types)
                             #js{:drop (fn [props monitor]
                                         (clj->js (drop)))
                                 :canDrop (fn [props]
                                            (can-drop?))}
                             (fn [connect monitor]
                               #js{:connect-drop-target (.dropTarget connect)
                                   :can-drop?           (.canDrop monitor)
                                   :is-over?            (.isOver monitor)}))]
    [(r/adapt-react-class
      (wrapper
       (r/reactify-component
        (r/create-class
         {:component-will-update
          (fn [this [_ next-props] _]
            (let [{:keys [is-over? can-drop?]} next-props]
              (swap! state assoc
                     :is-over? is-over?
                     :can-drop? can-drop?)))
          :render
          (fn [this]
            (let [this                (r/current-component)
                  props               (r/props this)
                  connect-drop-target (:connect-drop-target props)]
              (connect-drop-target
               (r/as-element child))))}))))]))

(defn drag-source
  [& {:keys [child types state begin-drag]
      :or {begin-drag (constantly {})}}]
  (let [wrapper (react-dnd/drag-source
                 (clj->js types)
                 #js{:beginDrag (fn [props]
                                  (clj->js (begin-drag)))}
                 (fn [connect monitor]
                   #js{:connect-drag-source (.dragSource connect)
                       :is-dragging?        (.isDragging monitor)}))]
    [(r/adapt-react-class
      (wrapper
       (r/reactify-component
        (r/create-class
         {:component-will-update
          (fn [this [_ next-props] _]
            (let [{:keys [is-dragging?]} next-props]
              (swap! state assoc
                     :is-dragging? is-dragging?)))
          :render
          (fn [this]
            (let [props               (r/props (r/current-component))
                  connect-drag-source (:connect-drag-source props)]
              (connect-drag-source
               (r/as-element child))))}))))]))
