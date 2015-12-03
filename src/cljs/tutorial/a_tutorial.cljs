(ns tutorial.a-tutorial
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

(defn knight-position []
  (reaction (:knight-position @state)))

(defn knight-at?* [position]
  (let [k (knight-position)]
    (reaction (= @k position))))

(defn knight []
  [:span "♘"])

(defn square [& {:keys [black? piece]}]
  [:div {:style {:width            "100%"
                 :height           "100%"
                 :background-color (if black? :black :white)
                 :color            (if black? :white :black)
                 :font-size        "32px"}}
   [:span piece]])

(defn move-knight-to [position]
  (swap! state assoc :knight-position position))

(defn board-square [& {:keys [position]}]
  (let [[x y]  position
        black? (odd? (+ x y))]
    [:div {:style {:height "12.5%"
                   :width  "12.5%"}}
     [:div {:style {:position :relative :width "100%" :height "100%"}
            :on-click #(move-knight-to position)}
      [square
       :black? black?
       :piece (when @(knight-at? position)
                [knight])]]]))

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


(defcard-doc
  "#State
  One of the fundamentally awesome things about React and Reagent is the
  ability to define UI declaratively: if the state says a menu is open,
  render that menu open. If the state says that render is closed, render
  that menu closed.
  There are many ways to use state in Reagent, but my favorite (stolen
  from [re-frame](https://github.com/Day8/re-frame)) is to maintain a
  single source of truth, in a single *ratom*, along with views into that ratom
  through *reaction*s.
  ## Ratoms
  The first piece of the state puzzle is ratoms.
  Ratoms are like regular Clojure atoms, except they keep track of when
  they're derefed. This allows components to be automatically re-rendered
  when their state changes (rendered to the virtual DOM--if the output
  doesn't change, the real DOM won't be changed).

  We're going to build a chess board with a single knight. Our state should
  keep track of where the knight is located on the board. It will look like
  this:"
  (dc/mkdn-pprint-source state)
  "## Reactions
  Reactions allow you to peek inside a larger atom/ratom, compute with it,
  and return a new, read-only ratom. For example, you might have a reaction
  that does something like `(get-in state [:some :deep :location])`. But
  there's more to reactions than that--they allow you to do *computation*
  with the data inside the ratom, and that computation is run *if and only
  if* that data changed. (For more on reactions and how to use them, see
  the [re-frame](https://github.com/Day8/re-frame) docs.)

  So what do our UI components need to know about the app state? Just one thing:
  whether the knight is at a given position. So, we'll write a reaction
  to check:
  "
  (dc/mkdn-pprint-source knight-at?)
  "`knight-at?` is a function of position. It returns a reaction, which is *used*
  like a read-only ratom. But the value of this ratom can only be `true` or `false`.
  The ratom contains data *computed* from the original `state` ratom--not (necessarily)
  the data in that ratom itself.

  If you're careful, you might notice a problem here: if the computation was
  expensive, and our state were bigger (like if it contained every chess piece
  instead of a single knight), we wouldn't want that computation to run whenever
  *any part* of the state changed--only when the part we're interested in, the
  knight position in this case, changes. In that case, we can chain reactions!"
  (dc/mkdn-pprint-source knight-position)
  (dc/mkdn-pprint-source knight-at?*)
  "The computation in `knight-at?*` is only run when the `:knight-position` of the
  state changes. If the state contained a changing `:bishop-position`, changing
  it would not result in more computation from `knight-at?*`."
  "Reactions allow us to use one large ratom to represent the entire state of 
  our application, without having to provide that ratom to every component
  that needs to access state. This architecture helps keep different components
  in sync--there is exactly **one** source of truth, with many views
  into it")

(defcard-doc
  "# Components
  Now that we've defined our state and seen how components will look into it,
  let's define our UI components.
  ## Knight
  Let's start with the
  knight. The knight can be completely stateless--it's just a `<span>`
  containing a knight symbol. Easy!"
  (dc/mkdn-pprint-source knight)
  "Here's how that knight looks when rendered:")

(defcard-rg knight
  [knight]
  state
  {:inspect-data true})

(defcard-doc
  "## Square
  Next, we need to define the `square` component. This component has two
  properties. `:black?` is a boolean determining what color the square is.
  `:piece` is an optional child react component, like `[knight]`"
  (dc/mkdn-pprint-source square)
  "This is what the square looks like: `[square :black? true :piece [knight]]`")

(defcard-rg square
  (fn [state _]
    [:div {:style {:height "100px"}}
     [square
     :black? true
     :piece [knight]]])
  state
  {:inspect-data true})

(defcard-doc
  "##Board-Square
  Now we define a `board-square` component. Why have a `board-square` *and* a
  `square` component? Well, `square` knows only whether it's black or white and
  what piece it contains. It always expands to fill all available area.
  `board-square`, on the other hand, knows its position on the board, and
  knows that it should be exactly 12.5% of the available height/width.
  Because it knows its own position, it can set up a callback to move the knight."
  (dc/mkdn-pprint-source board-square)
  "The `move-knight-to` function just updates the state to contain the new knight-position"
  (dc/mkdn-pprint-source move-knight-to)
  "Because of reagent's declarative nature, changing the state immediately
  changes the DOM. Awesome.")

(defcard-rg board-square
  "Here's what the rendered board-square looks like, for position `[1 0]` (near
  the top left of the board). It's a black square. It looks a little funny, of course,
  because it knows to only take up 12.5% of the available area, and there aren't
  other squares to fill it all up. It's rendered on a grey background for visibility.
  Notice that its position on its background is \"wrong\" because there aren't
  other board-squares present to push it into position.

  The knight starts at `[0 0]`, but you can move the knight down and up
  with the buttons below, to bring it to
  this board-square. Or, since clicking a board-square changes the
  knight-position in the state, you can just click the square to bring the knight
  there!"
  (fn [state _]
    [:div
     [:div {:style {:width     "200px"
                    :height    "200px"
                    :background-color "lightgrey"
                    :display   :flex
                    :flex-wrap :wrap}}
      [board-square
       :position [1 0]]]
      [:button {:on-click #(swap! state update-in [:knight-position 0] inc)}
       "Move Knight Down"]
      [:button {:on-click #(swap! state update-in [:knight-position 0] dec)}
       "Move Knight Up"]])
  state
  {:inspect-data true})

(defcard-doc
  "##Board
  Now it's time to define the whole `board`. We'll just render 64 `board-squares`,
  with a helper function that turns an integer 0-63 to `[x y]` coordinates."
  (dc/mkdn-pprint-source position)
  (dc/mkdn-pprint-source board))

(defcard-rg board
  "And here's what `[board]` looks like:"
  board
  state
  {:inspect-data true})

(defcard-doc
  "Clicking any square should move the knight to it.")

(defcard-doc
  "# Source
  Here's all the source in one place:"
  (dc/mkdn-pprint-source state)
  (dc/mkdn-pprint-source knight)
  (dc/mkdn-pprint-source knight-at?)
  (dc/mkdn-pprint-source move-knight-to)
  (dc/mkdn-pprint-source square)
  (dc/mkdn-pprint-source board-square)
  (dc/mkdn-pprint-source position)
  (dc/mkdn-pprint-source board))
