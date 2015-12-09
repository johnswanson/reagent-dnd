(ns examples.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [examples.handlers]
            [examples.subs]
            [examples.views :as views]
            [examples.routes :as routes]
            [devcards.core :as dc])
  (:require-macros
   [reagent.ratom :refer [reaction]]
   [devcards.core
    :as dc
    :refer [defcard defcard-doc defcard-rg deftest]]))

(defn mount-root []
  (reagent/render [views/page]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (routes/app-routes)
  (re-frame/dispatch-sync [:initialize-db])
  (js/setInterval #(re-frame/dispatch [:rearrange-stress-test]) 1000)
  (mount-root))


