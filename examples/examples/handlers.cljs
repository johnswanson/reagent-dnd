(ns examples.handlers
  (:require [re-frame.core :as re-frame]))

(defn initialize-db
  []
  {})

(re-frame/register-handler
 :initialize-db
 initialize-db)

(defn set-viewing
  [db [_ panel]]
  (assoc db :active-panel panel))

(re-frame/register-handler
 :set-viewing
 set-viewing)

