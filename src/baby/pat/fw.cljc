(ns baby.pat.fw
  (:require [clojure.string]
            [babashka.fs :as fs]
            #?(:bb [babashka.pods :as pods]
               :clj [nextjournal.beholder :as beholder])))

#?(:bb (pods/load-pod 'org.babashka/fswatcher "0.0.5"))
#?(:bb (require '[pod.babashka.fswatcher :as fw]))

;; This is excellent work, great ideas. Copy more when in doubt.
;; https://github.com/filipesilva/fdb/blob/master/src/fdb/watcher.clj
(def default-ignore-list
  [".DS_Store" ".git" ".gitignore" ".obsidian" ".vscode" "node_modules" "target" ".cpcache"])

(defn file-dir-relative
  [file-or-dir]
  (let [[file dir] (if (fs/directory? file-or-dir)
                     [nil file-or-dir]
                     [file-or-dir (fs/parent file-or-dir)])
        relative   #(-> dir (fs/relativize %) str)]
    [file dir relative]))

(def re-escape-cmap
  (->> "()&^%$#!?*."
      (map (fn [c] [c (str \\ c)]))
      (into {})))

(defn ignore-re
  [ignore-list]
  (let [ignore-ors (->> ignore-list
                        (map #(clojure.string/escape % re-escape-cmap))
                        (clojure.string/join "|"))]
    (re-pattern (str "(?:^|\\/)(?:" ignore-ors ")(?:\\/|$)"))))

(def memo-ignore-re (memoize ignore-re))

(defn ignore?
  [ignore-list path]
  (boolean (re-find (memo-ignore-re ignore-list) path)))

(defn *watch [path-str f]
  #?(:bb  (fw/watch path-str f {:recursive true})
     :clj (beholder/watch f path-str)))

(defn watch
  "Watch a file or directory for changes and call update-fn with their relative paths.
  File move shows up as two update calls, in no deterministic order.
  Returns a closeable that stops watching when closed."
  [path-str f]
  (let [[_ _ relative] (file-dir-relative path-str)]
    (*watch path-str (fn [{:keys [path type]}]
                      (let [path' (str (relative path))]
                        (when-not (ignore? default-ignore-list path')
                          (case type
                            (:create :modify :delete) (f path')
                            :overflow                 (println "overflow"))))))))

(defn watch-many
  [watch-spec]
  (->> watch-spec (map (partial apply watch)) doall))

(defn kill-watcher! [watcher]
  #?(:bb (fw/unwatch watcher)
     :clj (beholder/stop watcher)))

#_(def a (watch-path "foss" prn))
#_(kill-watcher! a)

