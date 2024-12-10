[<img src="resources/fw_logo.jpg" alt="fw" width="200px">](https://fw.pat.baby)

# fw
Simple file-watcher for babashka/jvm.    
Uses [NextJournal's Beholder](https://github.com/nextjournal/beholder) on the JVM.   
Uses [Babashka's FS-Watcher Pod](https://github.com/babashka/pod-babashka-fswatcher) on BB.

## Installation

```baby.pat/jessica-spano {:mvn/version "0.1.0"}```

## Usage

```clojure
(require '[baby.pat.fw :as fw])
(def watcher (fw/watch "src" prn))
(fw/kill-watcher! watcher)
```
