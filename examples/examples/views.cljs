(ns examples.views
  (:require-macros [devcards.core :as dc])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [devcards.core :as dc]
            [devcards.util.edn-renderer]
            [reagent.core :as r]
            [reagent-dnd.core :as dnd]
            [reagent.ratom]
            [examples.utils :refer [make-title make-url document card]]
            [examples.views.single-target]
            [examples.views.multiple-targets]
            [examples.views.stress-test]
            [examples.views.drag-around-naive]
            [examples.views.drag-around-custom]
            [examples.views.nested-drag-sources]))

(defn link [page url]
  [:a {:href url} page])

(defn link-col [page & [url]]
  (let [url (or url (str "#/" page))]
    [:div.col [link page url]]))

(defn header []
  [:div
   [:div.grid-center.title
    [link-col "reagent-dnd" "#/"]]
   [:div.grid
    [link-col "about"]
    [link-col "docs"]
    [link-col "tutorial"]
    [link-col "examples"]]])

(defn knight [state]
  [:span {:style {:opacity          (if (:dragging? @state) 0.1 1)
                  :cursor           :move
                  :background-color (if (:dragging? @state)
                                      :white
                                      :grey)
                  :font-size        "64px"}}
   "♘"])

(defn draggable
  [state]
  (fn []
    [dnd/drag-source
     :type       :knight
     :end-drag   (fn [state]
                   (js/console.log "dropped on: " (clj->js (:drop-result state))))
     :child      [knight state]
     :begin-drag (fn [] {:id 1})
     :state      state]))

(defn square [state]
  [:div {:style {:height           "64px"
                 :width            "64px"
                 :background-color (cond
                                     (:is-over? @state) "#555"
                                     (:can-drop? @state) "#777"
                                     :default "#999")}}])

(defn droppable
  [state]
  (fn []
    [dnd/drop-target
     :types [:knight]
     :drop (fn [state]
             (js/console.log "dropped item: " (clj->js (:item state)))
             {:id 5})
     :child [square state]
     :state state]))



(defn about-text [drag-state drop-state]
  (fn []
    (document
    "
# Reagent-dnd: Simple Drag & Drop for Reagent
Reagent-dnd is a set of [Reagent](https://reagent-project.github.io/) 
components that help you create drag-and-drop
interfaces that don't directly manipulate the DOM. It's a very thin wrapper 
around the excellent [ReactDnD](https://gaearon.github.io/react-dnd/) library.

I built it because I had trouble finding a good way to use drag-and-drop from
ClojureScript. I wanted something that:

Some libraries manipulated the DOM, which made them unsuitable
for use with React or Reagent. Using the
[disastrous](http://www.quirksmode.org/blog/archives/2009/09/the_html5_drag.html)
HTML5 drag & drop API was painful and bug-prone. ReactDnD was a breath of fresh
air--a simple drag & drop library that focused on how the operation changed
your *data* without changing the DOM state directly. I wanted a simple way to
use it with Reagent.
  
## What does it look like?
"
     (dc/mkdn-pprint-source knight)
     (dc/mkdn-pprint-source draggable)
     (dc/mkdn-pprint-source square)
     (dc/mkdn-pprint-source droppable)
     [draggable drag-state]
     [droppable drop-state])))


(defn about []
  (let [drag-state (r/atom {})
        drop-state (r/atom {})]
    (fn []
      [card
       :content [about-text drag-state drop-state]
       :state {:drag-state drag-state
               :drop-state drop-state}])))

(defn link-to [category id]
  (let [txt (make-title id)
        url (make-url category id)]
    [:a {:href url} txt]))


(def examples-components
  (array-map
   :dustbin (array-map
             :single-target [examples.views.single-target/view]
             :multiple-targets [examples.views.multiple-targets/view]
             :stress-test [examples.views.stress-test/view])
   :drag-around (array-map
                 :naive [examples.views.drag-around-naive/view]
                 :custom [examples.views.drag-around-custom/view])
   :nested (array-map
            :drag-sources [examples.views.nested-drag-sources/view])))

(defn examples-sidebar []
  [:div
   (for [[ex-name m] examples-components]
     ^{:key (str "sidebar-example-" ex-name)}
     [:div.sidebar-group
      [:h4.sidebar-group (make-title ex-name)]
      [:ul
       (for [[id _] m]
         ^{:key (str "sidebar-example-" id)}
         [:li (link-to ex-name id)])]])])

(defn api-docs []
  [:div "api docs"])

(defn tutorial []
  [:div "Tutorial"])

(defn examples-main []
  (let [example (subscribe [:example])]
    (fn []
      (if-let [component (and @example (get-in examples-components @example))]
        component
        [examples.views.single-target/view]))))

(defn examples []
  [:div.grid
   [:div.col-3 [examples-sidebar]]
   [:div.col [examples-main]]])

(defmulti panels identity)
(defmethod panels :about []
  [about])

(defmethod panels :api-docs []
  [api-docs])
(defmethod panels :tutorial []
  [tutorial])
(defmethod panels :examples []
  [examples])
(defmethod panels :default []
  [:div])

(defn page []
  (let [active-panel (subscribe [:active-panel])
        page (fn []
               [:div
                [header]
                [:div.grid
                 [:div.col-1_sm-12]
                 [:div.col-10_sm-12
                  [:div (panels @active-panel)]]
                 [:div.col-1_sm-12]]])]
    [(dnd/with-drag-drop-context
       dnd/html5-backend
       page)]))
