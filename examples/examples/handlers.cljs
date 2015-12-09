(ns examples.handlers
  (:require [re-frame.core :as re-frame]))

(def nested-drag-sources-dummy
  {:root {:color    :blue
          :children [:b :c]}
   :b    {:color    :yellow
          :children [:d :e]}
   :c    {:color    :blue
          :children [:f]}
   :d    {:color :yellow}
   :e    {:color :blue}
   :f    {:color :yellow}})

(def dummy-db
  {:examples {:stress-test       {:drag-sources [:glass :banana :pepper]
                                  :drop-targets [:glass :banana :pepper]}
              :drag-around-naive {:position [80 80]}
              :nested-drag-sources nested-drag-sources-dummy}})

(re-frame/register-handler
 :initialize-db
 (fn [] dummy-db))

(defn set-viewing
  [db [_ panel]]
  (assoc db :active-panel panel))

(re-frame/register-handler
 :set-viewing
 set-viewing)

(defn set-viewing-examples
  [db [_ example]]
  (assoc db
         :active-panel :examples
         :example example))

(re-frame/register-handler
 :set-viewing-examples
 set-viewing-examples)

(re-frame/register-handler
 :rearrange-stress-test
 (fn [db _]
   (-> db
       (update-in [:examples :stress-test :drop-targets] shuffle)
       (update-in [:examples :stress-test :drag-sources] shuffle))))

(re-frame/register-handler
 :move-naive-handler
 [re-frame/debug]
 (fn [db [_ [x y]]]
   (update-in db [:examples :drag-around-naive] assoc :position [x y])))

(re-frame/register-handler
 :toggle-forbid-drag-source
 [re-frame/debug]
 (fn [db [_ id]]
   (update-in db [:examples :nested-drag-sources id :forbidden?] not)))

(re-frame/register-handler
 :nested-drag-source-dropped
 (fn [db [_ id]]
   db))
