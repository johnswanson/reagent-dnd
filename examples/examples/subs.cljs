(ns examples.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as re-frame :refer [register-sub]]))

(defn active-panel
  [db _]
  (reaction (:active-panel @db)))

(register-sub
 :active-panel
 active-panel)
