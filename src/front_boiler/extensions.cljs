(ns front-boiler.extensions)

(extend-type js/HTMLCollection
  ISeqable
  (-seq [collection] (array-seq collection 0)))

