(ns examples.views
  (:require-macros [reagent.ratom :refer [reaction]]
                   [devcards.system :as system]
                   [devcards.core :as dc :refer [defcard-rg]])
  (:require [re-frame.core :as re-frame :refer [subscribe dispatch]]
            [cljs.pprint]
            [examples.test]
            [devcards.core :as dc]
            [devcards.util.edn-renderer]
            [devcards.system :as system]
            [reagent.core :as r]
            [reagent-dnd.core :as dnd]
            [markdown.core :refer [md->html]]
            [reagent-dnd.core :as dnd]
            [reagent.ratom]
            [cljsjs.highlight]
            [cljsjs.highlight.langs.clojure]
            [medley.core :refer [map-vals]]))

(defn pprint [obj]
  (with-out-str (cljs.pprint/pprint obj)))

(defn show-state [state]
  (devcards.util.edn-renderer/html-edn
   (cond
     (instance? PersistentVector state) (map deref state)
     (instance? PersistentArrayMap state) (map-vals
                                           deref
                                           state)
     (instance? reagent.ratom/RAtom state) @state
     :default @state)))

(defn highlight [thing]
  (fn [thing]
    [:pre
     [:code
      {:dangerouslySetInnerHTML
       {:__html (.-value (.highlight js/hljs "clojure" thing))}}]]))

(defn card [& {:keys [content state]}]
  [:div.card
   content
   (show-state state)])

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
     :end-drag   (fn [handled? result]
                   (swap! state
                          assoc
                          :drag-result
                          result))
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
     :drop (fn [data]
             (swap! state assoc :last-dropped-item data)
             {:id 5})
     :child [square state]
     :state state]))

(defmulti show #(type %))
(defmethod show js/String [s]
  [:div {:dangerouslySetInnerHTML
      {:__html (md->html s)}}])
(defmethod show PersistentVector [s]
  s)
(defmethod show :default [s]
  s)

(defn document [& args]
  [:div
   (for [arg args]
     ^{:key (pr-str arg)}
     [:div {:style
            {:margin "50px 0"}}
      (show arg)])])

(defn about-text [drag-state drop-state]
  (fn []
    (document
    "# Reagent-dnd: Simple Drag & Drop for Reagent
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

(defn api-docs []
  [:div "api docs"])

(defn tutorial []
  [:div "Tutorial"])

(defn examples []
  [:div "Examples"])

(defmulti panels identity)
(defmethod panels :about []
  [(dnd/with-drag-drop-context
    dnd/html5-backend
    about)])
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
