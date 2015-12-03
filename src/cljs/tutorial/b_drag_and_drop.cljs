(ns tutorial.b-drag-and-drop
  (:require [reagent.core :as reagent]
            [reagent-dnd.core :as dnd]
            [devcards.core :as dc])
  (:require-macros
   [reagent.ratom :refer [reaction]]
   [devcards.core
    :as dc
    :refer [defcard defcard-doc defcard-rg deftest]]))

(defonce state
  (reagent/atom {:knight-position [0 0]}))

(defn knight-at? [position]
  (reaction (= (:knight-position @state)
               position)))

(defn can-move-knight? [position]
  (reaction
   (let [[kx ky] (:knight-position @state)
         [tx ty] position
         dx (Math/abs (- kx tx))
         dy (Math/abs (- ky ty))]
     (or
      (and (= dx 2) (= dy 1))
      (and (= dy 2) (= dx 1))))))

(defn knight-span [drag-state]
  [:span {:style
          {:cursor :move
           :opacity
           (if (:dragging? @drag-state) 0.5 1)}}
   "♘"])

(defn knight []
  (let [drag-state (reagent/atom {})]
    [dnd/drag-source
     :type :knight
     :state drag-state
     :child [knight-span drag-state]]))

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

(defn move-knight-to [position]
  (swap! state assoc :knight-position position))

(defn board-square [& {:keys [position]}]
  (let [drag-state (reagent/atom {})
        [x y]  position
        black? (odd? (+ x y))]
    [:div {:style {:height "12.5%"
                   :width  "12.5%"}}
     [:div {:style {:position :relative :width "100%" :height "100%"}}
      [dnd/drop-target
       :types [:knight]
       :state drag-state
       :drop #(move-knight-to position)
       :can-drop? (fn [] @(can-move-knight? position))
       :child [square
               :drag-state drag-state
               :black? black?
               :piece (when @(knight-at? position)
                        [knight])]]]]))

(defn position [i]
  [(quot i 8)
   (rem i 8)])

(defn board []
  [:div {:style {:width     "60vw"
                 :height    "60vw"
                 :display   :flex
                 :flex-wrap :wrap}}
   (for [i (range 64)
         :let [p (position i)]]
     ^{:key (str "square-" i)}
     [board-square
      :position p])])

(def context
  (dnd/with-drag-drop-context
   dnd/html5-backend
   board))

(defcard-doc
  "# Drag & Drop
  Before getting to implementing drag & drop, let's quickly
  teach our code something about how chess works. The knight
  can't move anywhere, it can only move to certain positions based on its
  current position. So we define a function, `can-move-knight`:"
  (dc/mkdn-pprint-source can-move-knight?)
  "Now, let's add the drag-drop functionality. We need three things:
  ## with-drag-drop-context
  This sets up the shared state for our drag-drop operations, and chooses
  a backend to use. The only backend, currently, is
  the HTML5
  [backend](https://gaearon.github.io/react-dnd/docs-drag-drop-context.html)
  provided by the ReactDnD library."
  (dc/mkdn-pprint-source context)
  "*(Note that the second argument to 
  `with-drag-drop-context` must be a function of zero arguments, returning
  a reagent component. If you need to pass properties to the
  underlying component, use a higher-order function that closes over them
  and returns a function of zero args.)*"
  "## drag-source
  Now we make the knight a drag-source."
  (dc/mkdn-pprint-source knight)
  "- `:type` tells reagent-dnd what kind of object this is. Later, we'll 
  define drop-targets that can accept one or more types.
  - `:state` provides reagent-dnd with a ratom to place the drag-state in.
  This tells us, among other things,
  whether a drag-operation on this element in progress.
  - `:child` is the actual draggable element: knight-span."
  (dc/mkdn-pprint-source knight-span)
  "This element checks `(:dragging? @drag-state)` and dims the
  element during a drag operation."
  "## drop-target
  Finally, we make the board-squares into drop-targets. Note that we
  no longer set up the `on-click` handler--the knight can only be moved
  via drag-and-drop."
  (dc/mkdn-pprint-source board-square)
  "- `:types` tells reagent-dnd what kind of objects this drop-target
  accepts.
  - `:state` provides reagent-dnd with a ratom to place the drag-state in.
  In this case, the drag-state will contain things like whether the user
  is hovering over the square.
  - `:drop` is what happens when something is dropped onto this target.
  - `:can-drop?` tells reagent-dnd whether this target can handle what is
  currently being dragged.
  - `:child` is the droppable element: the square. This has also been
  modified to look at the drag-state:"
  (dc/mkdn-pprint-source square)
  "The square now looks at `(:is-over? @drag-state)` and `(:can-drop?
  @drag-state)`, and highlights the square according to whether the
  user is dragging something over it, or whether the square could
  accept the drag operation."
  "Here's the finished product--what we see when we render the
  entire board in the HTML5 context. Drag and drop around!")

(defcard-rg context
  (fn [state _]
    [context])
  state
  {:inspect-data true
   :watch-atom   false})
