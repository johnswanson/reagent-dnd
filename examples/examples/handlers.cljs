(ns examples.handlers
  (:require [re-frame.core :as re-frame]))

(def nested-drop-targets-dummy
  {:root1 {:children [:a]}
   :a     {:children [:b]}
   :b     {:children [:c]}
   :c     {:children [:d]}
   :d     {:children [:e]}
   :e     {:children [:f]}
   :f     {:children [:g]}
   :g     {:children []}})

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
              :nested-drag-sources nested-drag-sources-dummy
              :nested-drop-targets nested-drop-targets-dummy}})

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
 (fn [db [_ [x y]]]
   (update-in db [:examples :drag-around-naive] assoc :position [x y])))

(re-frame/register-handler
 :toggle-forbid-drag-source
 (fn [db [_ id]]
   (update-in db [:examples :nested-drag-sources id :forbidden?] not)))

(re-frame/register-handler
 :nested-drag-source-dropped
 (fn [db [_ id]]
   db))

(re-frame/register-handler
 :toggle-greedy-drop-target
 (fn [db [_ id]]
   (update-in db [:examples :nested-drop-targets id :greedy?] not)))

(re-frame/register-handler
 :dropped-on-nested-drop-target
 [re-frame/debug]
 (fn [db [_ id & {:keys [dropped?]}]]
   (js/console.log dropped?)
   (let [greedy? (get-in db [:examples :nested-drop-targets id :greedy?])]
     (if (and dropped? (not greedy?)) ;; handled already
       db
       (update-in db [:examples :nested-drop-targets id]
                  assoc
                  :dropped? true
                  :dropped-on-child? dropped?)))))
(re-frame/register-handler
 :initialize-nested-drop-targets
 (fn [db]
   (assoc-in db [:examples :nested-drop-targets] nested-drop-targets-dummy)))
