(ns clojure-doc-jp.extract
  (:require [clojure.pprint :as pp]
            [clojure.java.io :as io]
            clojure.data
            clojure.inspector
            clojure.xml))

(def target-ns
  "Clojure標準のnamespace列挙"
  '[
   clojure.core
   clojure.data
   clojure.edn
   clojure.inspector
   clojure.instant
   clojure.java.browse
   clojure.java.io
   clojure.java.javadoc
   clojure.java.shell
   clojure.main
   clojure.pprint
   clojure.reflect
   clojure.repl
   clojure.set
   clojure.stacktrace
   clojure.string
   clojure.template
   clojure.test
   clojure.walk
   clojure.xml
   clojure.zip ])

(defn- ns-meta [ns]
  "名前空間を受け取り、その名前空間の全てのpublicなVarを、シンボルとメタデータの組にしたシーケンスを返す。"
  (for [[k v] (ns-publics (the-ns ns))] [k (meta v)]))

(defn- make-doc-map [sym-meta-pairs]
  "シンボルとメタデータの組をドキュメント用の雛形となるTreeMapに変換して返す。"
  (into
   (sorted-map)
   (map (fn [[k v]]
          (vector (keyword k)
                  (sorted-map
                   :brief "TODO"
                   :docjp (:doc v)
                   :tips  "TODO"))))
   sym-meta-pairs))

(defn- doc-jp-extract []
  "各名前空間と特殊形式からドキュメントの雛形を抽出し、TreeMapに束ねたものを返す。"
  (into (sorted-map)
        (conj
         ;各名前空間
         (for [ns target-ns
               :let [doc-map (-> ns ns-meta make-doc-map)]]
           [ns doc-map])
         ;特殊形式
         (-> 'clojure.repl ns-map ('special-doc-map) var-get make-doc-map
             (as-> m ['special-form m])))))

(defn output-base!
  "各名前空間ドキュメントの雛形をそれぞれ個別のedn形式ファイルとして出力する。"
  ([] (output-base! "./docjp/"))
  ([out-target] (output-base! out-target (doc-jp-extract)))
  ([out-target src]
   (clojure.java.io/make-parents (str out-target "_"))
   (run! #(spit
           (str out-target (symbol (key %)) ".edn")
           (with-out-str
             (clojure.pprint/pprint (val %))))
         src)))

