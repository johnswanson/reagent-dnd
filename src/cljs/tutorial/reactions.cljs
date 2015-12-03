(ns tutorial.reactions
  (:require [reagent.core :as reagent]
            [reagent-dnd.core :as dnd]
            [devcards.core :as dc])
  (:require-macros
   [reagent.ratom :refer [reaction]]
   [devcards.core
    :as dc
    :refer [defcard defcard-doc defcard-rg deftest]]))

(defn new-random-numbers []
  (vec (repeatedly 20 #(rand-int 100))))

(defonce state (reagent/atom {:time (.now js/Date)
                              :numbers (new-random-numbers)}))

(defonce sort-state (reagent/atom {:sorted false
                                   :numbers false}))

(defn sorted-indicator []
  [:div
   [:div.com-rigsomelight-devcards_rendered-card.com-rigsomelight-devcards-markdown.code
    {:style
     {:width           "50%"
      :display         :flex
      :align-content   :flex-end
      :justify-content :space-between}}
    [:code "(reaction (:numbers @state))"]
    [:div {:style {:height           "30px"
                   :width            "30px"
                   :border-radius    "3px"
                   :transition       (if (:numbers @sort-state)
                                       "background-color 0s"
                                       "background-color .5s")
                   :background-color (if (:numbers @sort-state)
                                       :yellow
                                       :grey)}}]]
   [:div.com-rigsomelight-devcards_rendered-card.com-rigsomelight-devcards-markdown.code{:style
          {:width "50%"
           :display :flex
           :align-content :flex-end
           :justify-content :space-between}}
    [:code
      "(reaction (sort @numbers))"]
    [:div {:style {:height           "30px"
                   :width            "30px"
                   :border-radius    "3px"
                   :transition       (if (:sorted @sort-state)
                                       "background-color 0s"
                                       "background-color .5s")
                   :background-color (if (:sorted @sort-state)
                                       :yellow
                                       :grey)}}]]])

(defn sorted-numbers []
  (let [numbers (reaction (:numbers @state))]
    (reaction
     (sort @numbers))))

(defn sorted-numbers* []
  (let [numbers (reaction
                 (swap! sort-state assoc :numbers true)
                 (.setTimeout js/window #(swap! sort-state assoc :numbers false))
                 (:numbers @state))]
    (reaction
     (swap! sort-state assoc :sorted true)
     (.setTimeout js/window #(swap! sort-state assoc :sorted false) 100)
     (sort @numbers))))

(defn numbers []
  (reaction
   (:numbers @state)))

(defn card [state]
  (let [interval (atom nil)]
    (reagent/create-class
     {:component-will-mount
      (fn [this]
        (reset!
         interval
         (.setInterval js/window
                       (fn []
                         (swap! state assoc :time (.now js/Date)))
                       2000)))
      :component-will-unmount
      (fn [this]
        (when @interval
          (.clearInterval js/window @interval)))
      :reagent-render
      (fn [state]
        (let [nums (sorted-numbers*)]
          @nums
          [:div
           [:div [sorted-indicator]]
           [:button {:on-click #(swap! state assoc :numbers (new-random-numbers))}
            "Regenerate Numbers"]]))})))

(defn naive-sorted-numbers []
  (reaction (sort (:numbers @state))))

(defcard-doc
  "# More on Reactions
  Let's say you have a data structure that is changing often. You want
  to perform some computation on a subset of this data, but you don't want
  to rerun this computation every time *any* data changes.

  As an example, let's create a state that contains the current time
  (updated at some set, frequent interval) in one key, and a list of
  numbers in another key. It might look like this:"
  @state
  "Now imagine we wanted a reagent component to render a sorted list of
  the numbers at the `:numbers` key. We might write the following:"
  (dc/mkdn-pprint-source naive-sorted-numbers)
  "But notice the problem here--when `:time` changes, `@state` changes.
  When `@state` changes, the reaction runs. And when the reaction runs,
  we do a cpu-intensive sort operation. Every time. Not great!
  ## Chained Reactions to the rescue
  To ensure that our expensive sort only runs when it *needs* to, we
  just feed one reaction to another. First, we create a simple reaction
  to extract the numbers from the state. This one will run every time
  the state changes, but that's fine--it's cheap.

  Then, we'll deref *that* reaction inside a new one. The new one will
  perform the sort."
  (dc/mkdn-pprint-source sorted-numbers)
  "The effect is this: when @state changes, the inner reaction (`(:numbers @state)`)
  runs. **IF AND ONLY IF** *that* changes, the outer reaction (`(sort @numbers)`)
  runs."
  "Let's check out an example. The lights below indicate when each
  reaction runs. The cheap one on top runs every time the state changes at all
  to extract the numbers. The expensive one on bottom runs only when the
  numbers change--if you click the button.")

(defcard-rg reaction-inspector
  (fn [state _]
    [card state])
  state
  {:inspect-data true
   :heading      false})

