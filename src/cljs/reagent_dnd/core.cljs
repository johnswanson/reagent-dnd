(ns reagent-dnd.core
  (:require [reagent.core :as r]
            [reagent-dnd.react-dnd :as react-dnd]
            [reagent-dnd.validate :refer [string-or-hiccup?]]
            [cljs.reader :refer [read-string]])
  (:require-macros [reagent-dnd.validate :refer [validate-args-macro]]))

(def drag-drop-context react-dnd/drag-drop-context)

(def html5-backend react-dnd/html5-backend)

(defn serialize [x]
  #js {"data" (pr-str x)})
(defn unserialize [o]
  (try (read-string (.-data o))
       (catch js/Error e
         false)))
(defn serializable? [x]
  (try (= (unserialize (serialize x))
          x)
       (catch js/Error e
         false)))

(defn with-drag-drop-context [backend component]
  (r/adapt-react-class
   ((drag-drop-context backend)
    (r/reactify-component component))))

(def drop-target-args-desc
  [{:name :child
    :required true
    :type "string | hiccup"
    :validate-fn string-or-hiccup?
    :description "the thing to drop onto"}
   {:name :state
    :required true
    :type "atom | ratom"
    :description "an atom/ratom to hold state, e.g. can-drop, is-over"}
   {:name :types
    :required true
    :type "keyword | [keyword]"
    :description "the type of thing to be dropped. e.g. :card or :list"}
   {:name :drop
    :required false
    :type "-> serializable"
    :validate-fn fn?
    :description "function that returns the 'drop result'"}
   {:name :hover
    :required false
    :type "-> any"
    :validate-fn fn?
    :description "function that is called whenever an item is hovered"}
   {:name :can-drop?
    :required false
    :type "(dragged-item) -> boolean"
    :validate-fn fn?
    :description "function that indicates whether the item can be accepted by this drop zone"}])

(defn drop-target-options [{:keys [drop can-drop?]
                            :or {drop (constantly nil)}}]
  (let [options #js{}]
    (aset options "drop" (fn [props monitor]
                           (let [result (drop (unserialize
                                               (.getItem monitor)))]
                             (assert (serializable? result)
                                     (str "Not Serializable: " (pr-str result)))
                             (serialize result))))
    (when can-drop?
      (aset options "canDrop" (fn [props monitor]
                                (can-drop? (.getItem monitor)))))
    options))

(defn drop-target
  [& {:as args
      :keys [child types drop state can-drop?]}]
  {:pre [(validate-args-macro drop-target-args-desc args "drop-target")]}
  (let [wrapper (react-dnd/drop-target
                 (clj->js types)
                 (drop-target-options args)
                 (fn [connect monitor]
                   #js{:connect-drop-target (.apply (aget connect "dropTarget") connect)
                       :can-drop?           (.apply (aget monitor "canDrop") monitor)
                       :is-over?            (.apply (aget monitor "isOver") monitor)}))]
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

(def drag-source-args-desc
  [{:name :child :required true :type "string | hiccup" :validate-fn string-or-hiccup? :description "the thing to be dragged"}
   {:name :type :required true :type "keyword" :validate-fn keyword? :description "the type of thing to be dragged. e.g. :card or :list"}
   {:name :state :required true :type "atom" :description "an atom (or ratom) to contain the drag-state"}
   {:name :begin-drag :required false :type "-> serializable" :validate-fn fn? :description "a function returning the 'dragged item'"}
   {:name :dragging? :required false :type "-> boolean" :validate-fn fn? :description "a function identifying whether this represents the currently dragged component (e.g. if a kanban card moves as you drag it)"}
   {:name :end-drag :required false :type "(did-drop?, drop-result) -> any" :validate-fn fn? :description "a function called when dragging stops, guaranteed to be called once for every begin-drag call. did-drop? is true if drop-target handled this drop, drop-result is the result of the drop target's :drop, if any"}
   {:name :can-drag? :required false :type "-> boolean" :validate-fn fn? :description "a function returning a boolean indicating whether this component can be dragged"}])

(defn drag-source-options [{:keys [begin-drag dragging? end-drag can-drag?]
                            :or {begin-drag (constantly {})}}]
  (let [options #js{}]
    (aset options "beginDrag" (fn [props]
                                (let [result (begin-drag)]
                                  (assert (serializable? result)
                                          (str "Not Serializable: " (pr-str result)))
                                  (serialize result))))
    (when can-drag?
      (aset options "canDrag" (fn [props monitor]
                                (can-drag?))))
    (when end-drag
      (aset options "endDrag" (fn [props monitor component]
                                (end-drag
                                 (.apply (aget monitor "didDrop") monitor)
                                 (unserialize
                                  (.apply (aget monitor "getDropResult") monitor))))))

    (when dragging?
      (aset options "isDragging" (fn [props monitor]
                                   (let [data (unserialize (.getItem monitor))]
                                     (dragging? data)))))
    options))

(defn drag-source
  [& {:keys [child type state begin-drag dragging? end-drag can-drag?]
      :as args}]
  {:pre [(validate-args-macro drag-source-args-desc args "drag-source")]}
  (let [options (drag-source-options (select-keys args [:begin-drag
                                                        :dragging?
                                                        :end-drag
                                                        :can-drag?]))
        wrapper (react-dnd/drag-source
                 (clj->js type)
                 options
                 (fn [connect monitor]
                   #js{:connect-drag-source (.apply (aget connect "dragSource") connect)
                       :dragging?        (.apply (aget monitor "isDragging") monitor)}))]
    [(r/adapt-react-class
      (wrapper
       (r/reactify-component
        (r/create-class
         {:component-will-update
          (fn [this [_ next-props] _]
            (let [{:keys [dragging?]} next-props]
              (swap! state assoc :dragging? dragging?)))
          :render
          (fn [this]
            (let [props               (r/props (r/current-component))
                  connect-drag-source (:connect-drag-source props)]
              (connect-drag-source
               (r/as-element child))))}))))]))
