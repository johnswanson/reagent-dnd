(ns examples.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as re-frame :refer [register-sub]]))

(defn active-panel
  [db _]
  (reaction (:active-panel @db)))

(register-sub
 :active-panel
 active-panel)

(defn example
  [db _]
  (reaction (:example @db)))

(register-sub
 :example
 example)

(defn examples [db]
  (reaction (:examples @db)))

(defn stress-test [db]
  (reaction (:stress-test @(examples db))))

(defn stress-test-drop-targets
  [db _]
  (let [stress-test (stress-test db)]
    (reaction (:drop-targets @stress-test))))

(defn stress-test-drag-sources
  [db _]
  (let [stress-test (stress-test db)]
    (reaction (:drag-sources @stress-test))))

(register-sub
 :stress-test-drop-targets
 stress-test-drop-targets)

(register-sub
 :stress-test-drag-sources
 stress-test-drag-sources)

(register-sub
 :drag-around-naive
 (fn [db [_ v]]
   (reaction (get-in @db [:examples :drag-around-naive v]))))

(defn nested-drag-sources [db]
  (reaction (:nested-drag-sources @(examples db))))

(defn nested-drop-targets [db]
  (reaction (:nested-drop-targets @(examples db))))

(register-sub
 :nested-drag-source
 (fn [db [_ id]]
   (reaction (id @(nested-drag-sources db)))))

(register-sub
 :nested-drop-target
 (fn [db [_ id]]
   (reaction (id @(nested-drop-targets db)))))

(register-sub
 :nested-drag-source-root
 (fn [db _]
   (reaction (:root @(nested-drag-sources db)))))

(defn expand-tree [db node]
  (let [children (map (fn [id]
                        (expand-tree
                         db
                         (id db)))
                      (:children node))]
    (assoc node :children children)))

(register-sub
 :nested-drag-source-tree
 (fn [db _]
   (let [drag-sources  @(nested-drag-sources db)
         root          (:root drag-sources)
         expanded-root (expand-tree drag-sources root)]
     (reaction expanded-root))))
