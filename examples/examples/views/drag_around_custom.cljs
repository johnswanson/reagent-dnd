(ns examples.views.drag-around-custom
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

(defn box [& {:keys [title]}]
  [:div {:style {:background-color :white
                 :cursor           :move
                 :border           "1px dashed grey"
                 :padding          "0.5rem 1rem"}}
   title])

(defn box-drag-preview [& {:keys [title going-left?]}]
  [:div {:style {:display :inline-block
                 :transform (if going-left?
                              "rotate(-7deg)"
                              "rotate( 7deg)")}}
   [box :title title]])

(defn draggable [drag-state]
  (let [pos (subscribe [:drag-around-naive :position])]
    (fn [drag-state]
      (let [[left top] @pos]
        [:div {:style {:transform (str "translate3d(" left "px, " top "px, 0)")
                       :position  :absolute
                       :opacity   (if (:dragging? @drag-state)
                                    0
                                    1)}}
         [box
          :title "Drag Me Around"]]))))

(defn source [drag-state]
  (let [pos (subscribe [:drag-around-naive :position])]
    (fn [drag-state]
      [:div {:style {:position :relative}}
       [dnd/drag-source
        :child [draggable drag-state]
        :drag-preview dnd/get-empty-image
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

(defn item-styles [state]
  (let [[x y] (:source-client-offset @state)]
    (if (and x y)
      {:transform (str "translate(" x "px, " y "px)")}
      {:display :none})))

(defn going-left? [state]
  (let [[dx _] (:difference-from-initial-offset @state)]
    (neg? dx)))

(defn drag-layer [state]
  [:div {:style {:position       :fixed
                 :pointer-events :none
                 :z-index        100
                 :top            0
                 :left           0
                 :width          "100%"
                 :height         "100%"}}
   [:div {:style (item-styles state)}
    [box-drag-preview
     :title "**** ** ******"
     :going-left? (going-left? state)]]])

(defn custom-drag-layer [state]
  (fn [state]
    [dnd/drag-layer
     :state state
     :child [drag-layer state]]))

(defn view []
  (let [drag-state  (r/atom {})
        drop-state  (r/atom {})
        layer-state (r/atom {})]
    (fn []
      [card
       :content (document
                 [target drag-state drop-state]
                 [custom-drag-layer layer-state])
       :state {:drag-state drag-state
               :drop-state drop-state
               :layer-state layer-state}])))
