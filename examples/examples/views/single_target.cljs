(ns examples.views.single-target
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

(defn target [state last-dropped]
  [:div {:style {:height           "100px"
                 :width            "100px"
                 :background-color (cond
                                     (:is-over? @state) :black
                                     (:can-drop? @state) :grey
                                     :default :white)
                 :color            (cond
                                     (:is-over? @state) :white
                                     (:can-drop? @state) :white
                                     :default :black)}}
   [:div "Drag a box here"]])

(defn drop-target [drop-state last-dropped]
  [dnd/drop-target
   :types [:glass :banana :paper]
   :state drop-state
   :child [target drop-state last-dropped]
   :drop (fn [state]
           (reset! last-dropped (:item state)))])

(defn box [id state]
  [:span
   {:style {:border     "1px dashed black"
            :text-align :center
            :padding    "10px"
            :margin     "10px"
            :cursor     :move}}
   (make-title id)])

(defn draggable [id state]
  [dnd/drag-source
   :type id
   :state state
   :child [box id state]
   :begin-drag (constantly id)])

(defn single-target-example []
  (let [last-dropped      (r/atom nil)
        drag-state-glass  (r/atom {})
        drag-state-banana (r/atom {})
        drag-state-paper  (r/atom {})
        drop-state        (r/atom {})]
    [card
     :content [:div
               [:div.drop-targets
                [drop-target drop-state last-dropped]]
               [:div.drag-sources
                [draggable :glass drag-state-glass][draggable :banana drag-state-banana][draggable :paper drag-state-paper]]


               ]
     :state {:drag-state-glass  drag-state-glass
             :drag-state-banana drag-state-banana
             :drag-state-paper  drag-state-paper
             :drop-state        drop-state
             :last-dropped      last-dropped}]))

(defn view []
  (document
   "# Single Target
This is as simple as it gets."
   [single-target-example]
   (dc/mkdn-pprint-source box)
   (dc/mkdn-pprint-source draggable)
   (dc/mkdn-pprint-source target)
   (dc/mkdn-pprint-source drop-target)
   (dc/mkdn-pprint-source single-target-example)))
