(ns front-boiler.components.drawings
  (:require [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer close!]]
            [clojure.string :as str]
            [front-boiler.async :refer [put!]]
            [front-boiler.state :as state]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]])
  (:require-macros [front-boiler.utils :refer [defrender]]))

(def draw-nav
  (html
   [:g.draw-nav
    [:rect {:height "1000" :width "100"}]
    [:circle {:r "20" :cy "950" :cx "50"}]
    [:circle {:r "20" :cy "350" :cx "50"}]
    [:circle {:r "20" :cy "250" :cx "50"}]
    [:circle {:r "20" :cy "150" :cx "50"}]
    [:path {:d "M50,45.2c2.6,0,4.8,2.1,4.8,4.8s-2.1,4.8-4.8,4.8c-2.6,0-4.8-2.1-4.8-4.8S47.4,45.2,50,45.2z M50,30c-9.3,0-17.1,6.4-19.4,15c0,0.1,0,0.2,0,0.2c0,0.5,0.4,1,1,1h8.1c0.4,0,0.7-0.2,0.9-0.6c0,0,0,0,0,0 c1.7-3.6,5.3-6.1,9.5-6.1c5.8,0,10.5,4.7,10.5,10.5c0,5.8-4.7,10.5-10.5,10.5c-4.2,0-7.8-2.5-9.5-6.1c0,0,0,0,0,0 c-0.2-0.3-0.5-0.6-0.9-0.6h-8.1c-0.5,0-1,0.4-1,1c0,0.1,0,0.2,0,0.2c2.2,8.6,10.1,15,19.4,15c11,0,20-9,20-20S61,30,50,30z"}]]))

(def draw-menu
  (html
   [:g.draw-menu
    [:line {:x1 "350" :y1 "0" :x2 "350" :y2 "1000"}]
    [:g
     [:text {:transform "matrix(1 0 0 1 125 130)"} "aside-nav-v2"]
     [:polyline {:points "318.6,127.3 321.4,130 329.4,122"}]]
    [:g
     [:text {:transform "matrix(1 0 0 1 125 155)"} "brand-new-nav"]
     [:polyline {:points "318.6,152.3 321.4,155 329.4,147"}]]
    [:g
     [:text {:transform "matrix(1 0 0 1 125 180)"} "build-switcher-styles"]
     [:polyline {:points "318.6,177.3 321.4,180 329.4,172"}]]
    [:g
     [:text {:transform "matrix(1 0 0 1 125 205)"} "build-top-queue-fixes"]
     [:polyline {:points "318.6,202.3 321.4,205 329.4,197"}]]
    [:g
     [:text {:transform "matrix(1 0 0 1 125 230)"} "customer-story-styles"]
     [:polyline {:points "318.6,227.3 321.4,230 329.4,222"}]]
    [:g
     [:text {:transform "matrix(1 0 0 1 125 255)"} "design-listing-rewrite"]
     [:polyline {:points "318.6,252.3 321.4,255 329.4,247"}]]
    [:g
     [:text {:transform "matrix(1 0 0 1 125 280)"} "master"]
     [:polyline {:points "318.6,277.3 321.4,280 329.4,272"}]]
    [:line {:x1 "100" :y1 "100" :x2 "350" :y2 "100"}]
    [:text {:transform "matrix(1 0 0 1 145.3517 55)"} "Your Branch Activity"]]))

(def draw-main
  (html
   [:g.draw-main
    [:g.draw-tr
     [:line {:x1 "350" :y1 "100" :x2 "1600" :y2 "100"}]
     [:text.draw-th {:transform "matrix(1 0 0 1 400 55.0535)"} "Build"]
     [:text.draw-th {:transform "matrix(1 0 0 1 637.1372 55)"} "Author"]
     [:text.draw-th {:transform "matrix(1 0 0 1 896.2832 55)"} "Log"]
     [:text.draw-th {:transform "matrix(1 0 0 1 1331.788 55)"} "Length"]
     [:text.draw-th {:transform "matrix(1 0 0 1 1525.047 55)"} "Status"]]
    [:g.draw-tr
     [:line {:x1 "350" :y1 "150" :x2 "1600" :y2 "150"}]
     [:path {:d "M1574.5,137.5h-50c-6.9,0-12.5-5.6-12.5-12.5v0c0-6.9,5.6-12.5,12.5-12.5h50 c6.9,0,12.5,5.6,12.5,12.5v0C1587,131.9,1581.4,137.5,1574.5,137.5z"}]
     [:text {:transform "matrix(1 0 0 1 400 130.3062)"} "100273"]
     [:text {:transform "matrix(1 0 0 1 637.1372 130.3062)"} "Danny King"]
     [:text {:transform "matrix(1 0 0 1 896.2832 130.3062)"} "prevent project settings from getting clipped"]
     [:text {:transform "matrix(1 0 0 1 1337.8501 130.3062)"} "2:50"]]
    [:g.draw-tr
     [:line {:x1 "350" :y1 "200" :x2 "1600" :y2 "200"}]
     [:path {:d "M1574.5,187.5h-50c-6.9,0-12.5-5.6-12.5-12.5v0c0-6.9,5.6-12.5,12.5-12.5h50c6.9,0,12.5,5.6,12.5,12.5v0 C1587,181.9,1581.4,187.5,1574.5,187.5z"}]
     [:text {:transform "matrix(1 0 0 1 400 180.3062)"} "100236"]
     [:text {:transform "matrix(1 0 0 1 637.1372 180.3062)"} "Kevin Bell"]
     [:text {:transform "matrix(1 0 0 1 896.2832 180.3062)"} "Turn on markdown rendering"]
     [:text {:transform "matrix(1 0 0 1 1337.8501 180.3062)"} "2:34"]]
    [:g.draw-tr
     [:line {:x1 "350" :y1 "250" :x2 "1600" :y2 "250"}]
     [:path {:d "M1574.5,237.5h-50c-6.9,0-12.5-5.6-12.5-12.5v0c0-6.9,5.6-12.5,12.5-12.5h50c6.9,0,12.5,5.6,12.5,12.5v0 C1587,231.9,1581.4,237.5,1574.5,237.5z"}]
     [:text {:transform "matrix(1 0 0 1 400 230.3062)"} "100224"]
     [:text {:transform "matrix(1 0 0 1 637.1372 230.3062)"} "Daniel Woelfel"]
     [:text {:transform "matrix(1 0 0 1 896.2832 230.3062)"} "ask if the user is empty"]
     [:text {:transform "matrix(1 0 0 1 1337.8501 230.3062)"} "2:32"]]
    [:g.draw-tr
     [:line {:x1 "350" :y1 "300" :x2 "1600" :y2 "300"}]
     [:path {:d "M1574.5,287.5h-50c-6.9,0-12.5-5.6-12.5-12.5l0,0c0-6.9,5.6-12.5,12.5-12.5h50c6.9,0,12.5,5.6,12.5,12.5 l0,0C1587,281.9,1581.4,287.5,1574.5,287.5z"}]
     [:text {:transform "matrix(1 0 0 1 400 280.3062)"} "100120"]
     [:text {:transform "matrix(1 0 0 1 637.1372 280.3062)"} "Nick Gottlieb"]
     [:text {:transform "matrix(1 0 0 1 896.2832 280.3062)"} "serve images from cdn"]
     [:text {:transform "matrix(1 0 0 1 1337.8501 280.3062)"} "2:39"]]
    [:g.draw-tr
     [:line {:x1 "350" :y1 "350" :x2 "1600" :y2 "350"}]
     [:path {:d "M1574.5,337.5h-50c-6.9,0-12.5-5.6-12.5-12.5l0,0c0-6.9,5.6-12.5,12.5-12.5h50c6.9,0,12.5,5.6,12.5,12.5 l0,0C1587,331.9,1581.4,337.5,1574.5,337.5z"}]
     [:text {:transform "matrix(1 0 0 1 400 330.3062)"} "100032"]
     [:text {:transform "matrix(1 0 0 1 637.1372 330.3062)"} "Gordon Syme"]
     [:text {:transform "matrix(1 0 0 1 896.2832 330.3062)"} "Fight the good fight against auto-keywordisation"]
     [:text {:transform "matrix(1 0 0 1 1337.8501 330.3062)"} "2:46"]]
    [:g.draw-tr
     [:line {:x1 "350" :y1 "400" :x2 "1600" :y2 "400"}]
     [:path {:d "M1574.5,387.5h-50c-6.9,0-12.5-5.6-12.5-12.5l0,0c0-6.9,5.6-12.5,12.5-12.5h50c6.9,0,12.5,5.6,12.5,12.5 l0,0C1587,381.9,1581.4,387.5,1574.5,387.5z"}]
     [:text {:transform "matrix(1 0 0 1 400 380.3062)"} "99915"]
     [:text {:transform "matrix(1 0 0 1 637.1372 380.3062)"} "Emile Snyder"]
     [:text {:transform "matrix(1 0 0 1 896.2832 380.3062)"} "Add webdriver test, make non-existent build pages fake 404."]
     [:text {:transform "matrix(1 0 0 1 1337.8501 380.3062)"} "2:22"]]
    [:g.draw-tr
     [:line {:x1 "350" :y1 "450" :x2 "1600" :y2 "450"}]
     [:path {:d "M1574.5,437.5h-50c-6.9,0-12.5-5.6-12.5-12.5l0,0c0-6.9,5.6-12.5,12.5-12.5h50c6.9,0,12.5,5.6,12.5,12.5 l0,0C1587,431.9,1581.4,437.5,1574.5,437.5z"}]
     [:text {:transform "matrix(1 0 0 1 400 430.3062)"} "99901"]
     [:text {:transform "matrix(1 0 0 1 637.1372 430.3062)"} "Brandon Bloom"]
     [:text {:transform "matrix(1 0 0 1 896.2832 430.3062)"} "Validate email address form fields."]
     [:text {:transform "matrix(1 0 0 1 1337.8501 430.3062)"} "2:54"]]
    [:g.draw-tr.draw-new
     [:circle.draw-filler {:cx "1574.5" :cy "475" :r "12.5"}]
     [:circle.draw-filler {:cx "1524.5" :cy "475" :r "12.5"}]
     [:rect.draw-filler {:x "1524.5" :y "462.5" :width "50" :height "25"}]
     [:g.draw-status
      [:g.draw-status-background
       [:circle.background-left  {:cx "1524.5" :cy "475" :r "12.5"}]
       [:rect.background-center  {:x "1524.5" :y "462.5" :width "50" :height "25"}]
       [:circle.background-right {:cx "1574.5" :cy "475" :r "12.5"}]]
      [:g.draw-status-icon
       [:g.draw-status-turn
        [:path {:d "M1524.5,467c-3.7,0-6.9,2.5-7.7,6c0,0,0,0.1,0,0.1c0,0.2,0.2,0.4,0.4,0.4h3.2 c0.2,0,0.3-0.1,0.3-0.2c0,0,0,0,0,0c0.7-1.4,2.1-2.4,3.8-2.4c2.3,0,4.2,1.9,4.2,4.2c0,2.3-1.9,4.2-4.2,4.2c-1.7,0-3.1-1-3.8-2.4 c0,0,0,0,0,0c-0.1-0.1-0.2-0.2-0.3-0.2h-3.2c-0.2,0-0.4,0.2-0.4,0.4c0,0,0,0.1,0,0.1c0.9,3.5,4,6,7.7,6c4.4,0,8-3.6,8-8 C1532.5,470.6,1528.9,467,1524.5,467z"}]
        [:circle {:cx "1524.5" :cy "475" :r "8"}]]
       [:path.draw-status-circle {:d "M1524.5,473.1c1.1,0,1.9,0.9,1.9,1.9c0,1.1-0.9,1.9-1.9,1.9c-1.1,0-1.9-0.9-1.9-1.9 C1522.6,473.9,1523.4,473.1,1524.5,473.1z"}]
       [:path.draw-status-check  {:d "M1526.9,474.2l-2.2,2.2l-0.5,0.5c-0.1,0.1-0.4,0.1-0.5,0l-0.5-0.5l-1.1-1.1 c-0.1-0.1-0.1-0.4,0-0.5l0.5-0.5c0.1-0.1,0.4-0.1,0.5,0l0.8,0.8l1.9-1.9c0.1-0.1,0.4-0.1,0.5,0l0.5,0.5 C1527.1,473.8,1527.1,474,1526.9,474.2z"}]]
      [:g.draw-status-text
       [:path {:d "M1537,479.1v-8h3.5c1.7,0,2.6,1.2,2.6,2.5c0,1.4-0.9,2.5-2.6,2.5h-2.1v3H1537z M1541.7,473.6 c0-0.8-0.6-1.3-1.4-1.3h-1.9v2.5h1.9C1541.1,474.9,1541.7,474.4,1541.7,473.6z"}]
       [:path {:d "M1549,479.1l-0.6-1.5h-3.7l-0.6,1.5h-1.6l3.1-8h1.8l3.1,8H1549z M1546.6,472.5l-1.4,3.8h2.9L1546.6,472.5z"}]
       [:path {:d "M1550.8,478l0.8-1.1c0.6,0.6,1.4,1.1,2.5,1.1c1.2,0,1.6-0.6,1.6-1.1c0-1.7-4.7-0.6-4.7-3.6 c0-1.3,1.2-2.3,2.9-2.3c1.2,0,2.3,0.4,3,1.1l-0.8,1c-0.6-0.6-1.5-0.9-2.3-0.9c-0.8,0-1.4,0.4-1.4,1c0,1.5,4.7,0.6,4.7,3.6 c0,1.3-0.9,2.5-3.1,2.5C1552.6,479.3,1551.5,478.7,1550.8,478z"}]
       [:path {:d "M1558,478l0.8-1.1c0.6,0.6,1.4,1.1,2.5,1.1c1.2,0,1.6-0.6,1.6-1.1c0-1.7-4.7-0.6-4.7-3.6 c0-1.3,1.2-2.3,2.9-2.3c1.2,0,2.3,0.4,3,1.1l-0.8,1c-0.6-0.6-1.5-0.9-2.3-0.9c-0.8,0-1.4,0.4-1.4,1c0,1.5,4.7,0.6,4.7,3.6 c0,1.3-0.9,2.5-3.1,2.5C1559.7,479.3,1558.7,478.7,1558,478z"}]
       [:path {:d "M1565.6,479.1v-8h5.5v1.2h-4.1v2.1h4v1.2h-4v2.2h4.1v1.2H1565.6z"}]
       [:path {:d "M1572.5,479.1v-8h3c2.5,0,4.2,1.7,4.2,4c0,2.4-1.7,4-4.2,4H1572.5z M1578.3,475.1c0-1.5-1-2.8-2.7-2.8 h-1.6v5.5h1.6C1577.2,477.9,1578.3,476.6,1578.3,475.1z"}]]]
     [:line {:x1 "350" :y1 "500" :x2 "1600" :y2 "500"}]
     [:text {:transform "matrix(1 0 0 1 400 480.3062)"} "99899"]
     [:text {:transform "matrix(1 0 0 1 637.1372 480.3062)"} "Danny King"]
     [:text {:transform "matrix(1 0 0 1 896.2832 480.3062)"} "use selector interpolation from less v1.7"]
     [:text {:transform "matrix(1 0 0 1 1337.8501 480.3062)"} "2:34"]]
    [:g.draw-tr
     [:line {:x1 "350" :y1 "550" :x2 "1600" :y2 "550"}]
     [:path {:d "M1574.5,537.5h-50c-6.9,0-12.5-5.6-12.5-12.5l0,0c0-6.9,5.6-12.5,12.5-12.5h50c6.9,0,12.5,5.6,12.5,12.5 l0,0C1587,531.9,1581.4,537.5,1574.5,537.5z"}]
     [:text {:transform "matrix(1 0 0 1 400 530.3062)"} "99895"]
     [:text {:transform "matrix(1 0 0 1 637.1372 530.3062)"} "Gordon Syme"]
     [:text {:transform "matrix(1 0 0 1 896.2832 530.3062)"} "Render account balances correctly"]
     [:text {:transform "matrix(1 0 0 1 1337.8501 530.3062)"} "2:06"]]
    [:g.draw-tr
     [:line {:x1 "350" :y1 "600" :x2 "1600" :y2 "600"}]
     [:path {:d "M1574.5,587.5h-50c-6.9,0-12.5-5.6-12.5-12.5l0,0c0-6.9,5.6-12.5,12.5-12.5h50c6.9,0,12.5,5.6,12.5,12.5 l0,0C1587,581.9,1581.4,587.5,1574.5,587.5z"}]
     [:text {:transform "matrix(1 0 0 1 400 580.3062)"} "99585"]
     [:text {:transform "matrix(1 0 0 1 637.1372 580.3062)"} "Sean Grove"]
     [:text {:transform "matrix(1 0 0 1 896.2832 580.3062)"} "Add basic behavior for adding heroku api-key"]
     [:text {:transform "matrix(1 0 0 1 1337.8501 580.3062)"} "2:42"]]
    [:g.draw-tr
     [:line {:x1 "350" :y1 "650" :x2 "1600" :y2 "650"}]
     [:path {:d "M1574.5,637.5h-50c-6.9,0-12.5-5.6-12.5-12.5l0,0c0-6.9,5.6-12.5,12.5-12.5h50c6.9,0,12.5,5.6,12.5,12.5 l0,0C1587,631.9,1581.4,637.5,1574.5,637.5z"}]
     [:text {:transform "matrix(1 0 0 1 400 630.3062)"} "99557"]
     [:text {:transform "matrix(1 0 0 1 637.1372 630.3062)"} "Danny King"]
     [:text {:transform "matrix(1 0 0 1 896.2832 630.3062)"} "fix iconpiles animation"]
     [:text {:transform "matrix(1 0 0 1 1337.8501 630.3062)"} "2:56"]]
    [:g.draw-tr
     [:line {:x1 "350" :y1 "700" :x2 "1600" :y2 "700"}]
     [:path {:d "M1574.5,687.5h-50c-6.9,0-12.5-5.6-12.5-12.5l0,0c0-6.9,5.6-12.5,12.5-12.5h50c6.9,0,12.5,5.6,12.5,12.5 l0,0C1587,681.9,1581.4,687.5,1574.5,687.5z"}]
     [:text {:transform "matrix(1 0 0 1 400 680.3062)"} "99542"]
     [:text {:transform "matrix(1 0 0 1 637.1372 680.3062)"} "Ian Duncan"]
     [:text {:transform "matrix(1 0 0 1 896.2832 680.3062)"} "Use monospace fonts for config textareas. Fixes #2784"]
     [:text {:transform "matrix(1 0 0 1 1337.8501 680.3062)"} "2:22"]]
    [:g.draw-tr
     [:line {:x1 "350" :y1 "750" :x2 "1600" :y2 "750"}]
     [:path {:d "M1574.5,737.5h-50c-6.9,0-12.5-5.6-12.5-12.5l0,0c0-6.9,5.6-12.5,12.5-12.5h50c6.9,0,12.5,5.6,12.5,12.5 l0,0C1587,731.9,1581.4,737.5,1574.5,737.5z"}]
     [:text {:transform "matrix(1 0 0 1 400 730.3062)"} "99536"]
     [:text {:transform "matrix(1 0 0 1 637.1372 730.3062)"} "Daniel Woelfel"]
     [:text {:transform "matrix(1 0 0 1 896.2832 730.3062)"} "dedup build messages"]
     [:text {:transform "matrix(1 0 0 1 1337.8501 730.3062)"} "2:08"]]
    [:g.draw-tr
     [:line {:x1 "350" :y1 "800" :x2 "1600" :y2 "800"}]
     [:path {:d "M1574.5,787.5h-50c-6.9,0-12.5-5.6-12.5-12.5l0,0c0-6.9,5.6-12.5,12.5-12.5h50c6.9,0,12.5,5.6,12.5,12.5 l0,0C1587,781.9,1581.4,787.5,1574.5,787.5z"}]
     [:text {:transform "matrix(1 0 0 1 400 780.3062)"} "99535"]
     [:text {:transform "matrix(1 0 0 1 637.1372 780.3062)"} "Michal Vyšinský"]
     [:text {:transform "matrix(1 0 0 1 896.2832 780.3062)"} "Fix api url typo [circle.com -> circleci.com]"]
     [:text {:transform "matrix(1 0 0 1 1337.8501 780.3062)"} "2:53"]]
    [:g.draw-tr
     [:line {:x1 "350" :y1 "850" :x2 "1600" :y2 "850"}]
     [:path {:d "M1574.5,837.5h-50c-6.9,0-12.5-5.6-12.5-12.5l0,0c0-6.9,5.6-12.5,12.5-12.5h50c6.9,0,12.5,5.6,12.5,12.5 l0,0C1587,831.9,1581.4,837.5,1574.5,837.5z"}]
     [:text {:transform "matrix(1 0 0 1 400 830.3062)"} "99528"]
     [:text {:transform "matrix(1 0 0 1 637.1372 830.3062)"} "David Lowe"]
     [:text {:transform "matrix(1 0 0 1 896.2832 830.3062)"} "remove dubious branch linkifying"]
     [:text {:transform "matrix(1 0 0 1 1337.8501 830.3062)"} "2:39"]]
    [:g.draw-tr
     [:line {:x1 "350" :y1 "900" :x2 "1600" :y2 "900"}]
     [:path {:d "M1574.5,887.5h-50c-6.9,0-12.5-5.6-12.5-12.5l0,0c0-6.9,5.6-12.5,12.5-12.5h50c6.9,0,12.5,5.6,12.5,12.5 l0,0C1587,881.9,1581.4,887.5,1574.5,887.5z"}]
     [:text {:transform "matrix(1 0 0 1 400 880.3062)"} "99526"]
     [:text {:transform "matrix(1 0 0 1 637.1372 880.3062)"} "Gordon Syme"]
     [:text {:transform "matrix(1 0 0 1 896.2832 880.3062)"} "Clean the build before loading another route"]
     [:text {:transform "matrix(1 0 0 1 1337.8501 880.3062)"} "2:38"]]
    [:g.draw-tr
     [:line {:x1 "350" :y1 "950" :x2 "1600" :y2 "950"}]
     [:path {:d "M1574.5,937.5h-50c-6.9,0-12.5-5.6-12.5-12.5l0,0c0-6.9,5.6-12.5,12.5-12.5h50c6.9,0,12.5,5.6,12.5,12.5 l0,0C1587,931.9,1581.4,937.5,1574.5,937.5z"}]
     [:text {:transform "matrix(1 0 0 1 400 930.3062)"} "99520"]
     [:text {:transform "matrix(1 0 0 1 637.1372 930.3062)"} "Daniel Woelfel"]
     [:text {:transform "matrix(1 0 0 1 896.2832 930.3062)"} "get converted trailing output from the terminal converter"]
     [:text {:transform "matrix(1 0 0 1 1337.8501 930.3062)"} "2:39"]]
    [:g.draw-tr
     [:path {:d "M1574.5,987.5h-50c-6.9,0-12.5-5.6-12.5-12.5l0,0c0-6.9,5.6-12.5,12.5-12.5h50c6.9,0,12.5,5.6,12.5,12.5 l0,0C1587,981.9,1581.4,987.5,1574.5,987.5z"}]
     [:text {:transform "matrix(1 0 0 1 400 980.3062)"} "99519"]
     [:text {:transform "matrix(1 0 0 1 637.1372 980.3062)"} "Danny King"]
     [:text {:transform "matrix(1 0 0 1 896.2832 980.3062)"} "re-enable cascade delay"]
     [:text {:transform "matrix(1 0 0 1 1337.8501 980.3062)"} "2:54"]]]))

(def draw-build
  (html
   [:g.draw-build
    [:g.draw-crumbs
     [:line {:x1 "350" :y1 "100" :x2 "1600" :y2 "100"}]
     [:text {:transform "matrix(1 0 0 1 375.0002 55.0532)"} "circleci / circle / oss-feature-flag-ui / 100812"]
     [:path {:d "M1562.4,48.8c-0.1-0.6-0.5-1-1.1-1.1 c-0.7-0.1-2.1-0.3-2.1-0.3c-0.6-0.2-1.1-0.7-1.1-1.4c0-0.3,0.1-0.5,0.2-0.7c0,0,1.3-1.7,1.3-1.7c0.4-0.4,0.3-1.1,0-1.5 c-0.5-0.6-1.1-1.2-1.7-1.7c-0.4-0.4-1.1-0.4-1.5,0c-0.6,0.4-1.7,1.3-1.7,1.3c-0.2,0.1-0.5,0.2-0.8,0.2c-0.7,0-1.2-0.5-1.4-1.1 l-0.3-2.1c-0.1-0.6-0.5-1-1.1-1.1c-0.8-0.1-1.6-0.1-2.4,0c-0.6,0.1-1,0.5-1.1,1.1l-0.3,2.1c-0.1,0.7-0.7,1.1-1.4,1.1 c-0.3,0-0.6-0.1-0.8-0.3l-1.6-1.3c-0.4-0.4-1.1-0.3-1.5,0c-0.6,0.5-1.2,1.1-1.7,1.7c-0.4,0.4-0.4,1.1,0,1.5 c0.4,0.5,1.2,1.6,1.2,1.6c0.2,0.2,0.3,0.5,0.3,0.8c0,0.7-0.5,1.3-1.2,1.4l-2,0.3c-0.6,0.1-1,0.5-1.1,1.1c-0.1,0.8-0.1,1.6,0,2.4 c0.1,0.6,0.5,1,1.1,1.1l2,0.3c0.7,0.1,1.2,0.7,1.2,1.4c0,0.3-0.1,0.6-0.3,0.8l-1.2,1.6c-0.4,0.4-0.3,1.1,0,1.5 c0.5,0.6,1.1,1.2,1.7,1.7c0.2,0.2,0.5,0.3,0.8,0.3c0.3,0,0.5-0.1,0.8-0.3l1.6-1.3c0.2-0.2,0.5-0.3,0.8-0.3c0.7,0,1.2,0.5,1.4,1.1 c0,0,0.3,2.1,0.3,2.1c0.1,0.6,0.5,1,1.1,1.1c0.4,0,0.8,0.1,1.2,0.1c0.4,0,0.8,0,1.2-0.1c0.6-0.1,1-0.5,1.1-1.1l0.3-2.1 c0.1-0.7,0.7-1.1,1.4-1.1c0.3,0,0.6,0.1,0.8,0.2l1.7,1.3c0.2,0.2,0.5,0.3,0.8,0.3c0.3,0,0.5-0.1,0.8-0.3c0.6-0.5,1.2-1.1,1.7-1.7 c0.4-0.4,0.4-1.1,0-1.5c-0.4-0.6-1.3-1.7-1.3-1.7c-0.2-0.2-0.2-0.5-0.2-0.8c0-0.7,0.5-1.2,1.1-1.4c0,0,1.4-0.2,2.1-0.3 c0.6-0.1,1-0.5,1.1-1.1C1562.5,50.4,1562.5,49.6,1562.4,48.8z"}]
     [:path {:d "M1550.1,50 c0,0.1-0.1,0.1-0.1,0.1s-0.1-0.1-0.1-0.1s0.1-0.1,0.1-0.1S1550.1,49.9,1550.1,50"}]]
    [:g.draw-stats
     [:text {:transform "matrix(1 0 0 1 375.0002 130.0532)"} "Author"]
     [:text {:transform "matrix(1 0 0 1 500.0002 130.0532)"} "Paul Biggar"]
     [:text {:transform "matrix(1 0 0 1 375.0002 155.0529)"} "Duration"]
     [:text {:transform "matrix(1 0 0 1 500.0002 155.0529)"} "21:34"]
     [:text {:transform "matrix(1 0 0 1 375.0002 180.0528)"} "Pull Request"]
     [:text {:transform "matrix(1 0 0 1 500.0002 180.0528)"} "#3254"]
     [:text {:transform "matrix(1 0 0 1 375.0002 205.0531)"} "Commit Log"]
     [:text {:transform "matrix(1 0 0 1 500.0002 205.0531)"} "Refactoring some old components."]
     [:text {:transform "matrix(1 0 0 1 375.0002 230.0529)"} "Build Artifacts"]
     [:text {:transform "matrix(1 0 0 1 500.0002 230.0528)"} "View All"]]
    [:g.draw-buttons
     [:text {:transform "matrix(1 0 0 1 1280.2092 180.0529)"} "rebuild"]
     [:path {:d "M1342.9,194.4H1279c-10.4,0-18.8-8.4-18.8-18.8v0 c0-10.4,8.4-18.8,18.8-18.8h63.9V194.4z"}]
     [:text {:transform "matrix(1 0 0 1 1464.7092 180.0529)"} "& enable ssh"]
     [:path {:d "M1556.2,194.4h-101.4v-37.7h101.4c10.4,0,18.8,8.4,18.8,18.8v0 C1575,186,1566.6,194.4,1556.2,194.4z"}]
     [:text {:transform "matrix(1 0 0 1 1352.9592 180.0529)"} "& clear cache"]
     [:line {:x1 "1342.9" :y1 "156.7" :x2 "1454.7" :y2 "156.7"}]
     [:line {:x1 "1342.9" :y1 "194.4" :x2 "1454.7" :y2 "194.4"}]]
    [:line {:x1 "375" :y1 "250" :x2 "375" :y2 "700"}]
    [:line {:x1 "1575" :y1 "250" :x2 "1575" :y2 "700"}]
    [:g.draw-action
     [:text {:transform "matrix(1 0 0 1 885.3306 254.4905)"} "INFRASTRUCTURE ACTIONS"]
     [:text {:transform "matrix(1 0 0 1 400.0002 280.0541)"} "starting the build"]
     [:text {:transform "matrix(1 0 0 1 1397.9369 280.054)"} "config"]
     [:text {:transform "matrix(1 0 0 1 1466.1526 280.0543)"} "00:00"]
     [:line {:x1 "375" :y1 "250" :x2 "883.7" :y2 "250"}]
     [:line {:x1 "1066.3" :y1 "250" :x2 "1575" :y2 "250"}]
     [:polyline {:points "1534,271 1542,279 1550,271"}]]
    [:g.draw-action
     [:text {:transform "matrix(1 0 0 1 400.0002 330.0543)"} "start container"]
     [:text {:transform "matrix(1 0 0 1 1397.9369 330.0543)"} "config"]
     [:text {:transform "matrix(1 0 0 1 1468.3386 330.0545)"} "00:01"]
     [:line {:x1 "375" :y1 "300" :x2 "1575" :y2 "300"}]
     [:polyline {:points "1534,321 1542,329 1550,321"}]]
    [:g.draw-action
     [:text {:transform "matrix(1 0 0 1 906.897 354.4902)"} "CHECKOUT ACTIONS"]
     [:text {:transform "matrix(1 0 0 1 400.0002 380.0543)"} "restore source cache"]
     [:text {:transform "matrix(1 0 0 1 1399.0409 380.0545)"} "cache"]
     [:text {:transform "matrix(1 0 0 1 1470.3561 380.0543)"} "00:11"]
     [:line {:x1 "375" :y1 "350" :x2 "905.5" :y2 "350"}]
     [:line {:x1 "1044.5" :y1 "350" :x2 "1575" :y2 "350"}]
     [:polyline {:points "1534,371 1542,379 1550,371"}]]
    [:g.draw-action
     [:text {:transform "matrix(1 0 0 1 400.0002 430.0545)"} "checkout using deploy key"]
     [:text {:transform "matrix(1 0 0 1 1397.9369 430.0543)"} "config"]
     [:text {:transform "matrix(1 0 0 1 1466.5947 430.0545)"} "00:03"]
     [:line {:x1 "375" :y1 "400" :x2 "1575" :y2 "400"}]
     [:polyline {:points "1534,421 1542,429 1550,421"}]]
    [:g.draw-action
     [:text {:transform "matrix(1 0 0 1 913.1832 454.4904)"} "MACHINE ACTIONS"]
     [:text {:transform "matrix(1 0 0 1 400.0002 480.054)"} "configure the build"]
     [:text {:transform "matrix(1 0 0 1 1399.0409 480.0543)"} "cache"]
     [:text {:transform "matrix(1 0 0 1 1466.3464 480.0543)"} "00:02"]
     [:line {:x1 "375" :y1 "450" :x2 "911.7" :y2 "450"}]
     [:line {:x1 "1038.2" :y1 "450" :x2 "1574.9" :y2 "450"}]
     [:polyline {:points "1534,471 1542,479 1550,471"}]]
    [:g.draw-action
     [:text {:transform "matrix(1 0 0 1 400.0002 530.0543)"} "restore cache"]
     [:text {:transform "matrix(1 0 0 1 1399.0409 530.0541)"} "cache"]
     [:text {:transform "matrix(1 0 0 1 1466.4031 530.0546)"} "00:08"]
     [:line {:x1 "375" :y1 "500" :x2 "1575" :y2 "500"}]
     [:polyline {:points "1534,521 1542,529 1550,521"}]]
    [:g.draw-action
     [:text {:transform "matrix(1 0 0 1 898.8965 554.4904)"} "DEPENDENCY ACTIONS"]
     [:text {:transform "matrix(1 0 0 1 400.0002 580.0543)"} "exporting RAILS_ENV, RACK_ENV"]
     [:text {:transform "matrix(1 0 0 1 1375.1698 580.0543)"} "inference"]
     [:text {:transform "matrix(1 0 0 1 1466.1545 580.0543)"} "00:00"]
     [:line {:x1 "375" :y1 "550" :x2 "897.3" :y2 "550"}]
     [:line {:x1 "1052.7" :y1 "550" :x2 "1575" :y2 "550"}]
     [:polyline {:points "1534,571 1542,579 1550,571"}]]
    [:g.draw-action
     [:text {:transform "matrix(1 0 0 1 400.0002 630.0546)"} "bundle install"]
     [:text {:transform "matrix(1 0 0 1 1375.1698 630.0541)"} "inference"]
     [:text {:transform "matrix(1 0 0 1 1468.3394 630.0546)"} "00:01"]
     [:line {:x1 "375" :y1 "600" :x2 "1575" :y2 "600"}]
     [:polyline {:points "1534,621 1542,629 1550,621"}]]
    [:g.draw-action
     [:text {:transform "matrix(1 0 0 1 927.7149 654.4903)"} "TEST ACTIONS"]
     [:text {:transform "matrix(1 0 0 1 400.0002 680.0541)"} "parallel rspec"]
     [:text {:transform "matrix(1 0 0 1 1375.1698 680.0546)"} "inference"]
     [:text {:transform "matrix(1 0 0 1 1466.7795 680.0546)"} "02:40"]
     [:line {:x1 "375" :y1 "650" :x2 "926" :y2 "650"}]
     [:line {:x1 "1024" :y1 "650" :x2 "1575" :y2 "650"}]
     [:polyline {:points "1550,679 1542,671 1534,679"}]]
    [:g.draw-output
     [:rect {:x "375" :y "700" :width "1200" :height "300"}]
     [:text {:transform "matrix(1 0 0 1 400.0002 730.0546)"} "bundle exec rspec --color"]
     [:text {:transform "matrix(1 0 0 1 400.0002 755.0544)"} "........................................................................................................................................."]
     [:text {:transform "matrix(1 0 0 1 400.0002 780.0542)"} "........................................................................................................................................."]
     [:text {:transform "matrix(1 0 0 1 400.0002 805.0540)"} "........................................................................................................................................."]
     [:text {:transform "matrix(1 0 0 1 400.0002 830.0538)"} "........................................................................................................................................."]
     [:text {:transform "matrix(1 0 0 1 400.0002 855.0538)"} "........................................................................................................................................."]
     [:text {:transform "matrix(1 0 0 1 400.0002 880.0536)"} "........................................................................................................................................."]
     [:text {:transform "matrix(1 0 0 1 400.0002 905.0540)"} "........................................................................................................................................."]
     [:text {:transform "matrix(1 0 0 1 400.0002 930.0549)"} "............................................."]
     [:text {:transform "matrix(1 0 0 1 400.0002 955.0545)"} "Finished in 2 minutes 30.1 seconds"]
     [:text {:transform "matrix(1 0 0 1 400.0002 980.0548)"} "884 examples, 0 failures"]]]))

(def draw-build-large
  (html
   [:svg.drawing {:version "1.1" :x "0px" :y "0px" :viewBox "0 0 1600 1000" :enableBackground "new 0 0 1600 1000"}
    [:g.draw-build
     [:g.draw-crumbs
      [:text {:transform "matrix(1 0 0 1 50 55.0532)"} "circleci / circle / oss-feature-flag-ui / 100812"]
      [:path {:d "M1549.9,48.8c-0.1-0.6-0.5-1-1.1-1.1c-0.7-0.1-2.1-0.3-2.1-0.3 c-0.6-0.2-1.1-0.7-1.1-1.4c0-0.3,0.1-0.5,0.2-0.7c0,0,1.3-1.7,1.3-1.7c0.4-0.4,0.3-1.1,0-1.5c-0.5-0.6-1.1-1.2-1.7-1.7 c-0.4-0.4-1.1-0.4-1.5,0c-0.6,0.4-1.7,1.3-1.7,1.3c-0.2,0.1-0.5,0.2-0.8,0.2c-0.7,0-1.2-0.5-1.4-1.1l-0.3-2.1 c-0.1-0.6-0.5-1-1.1-1.1c-0.8-0.1-1.6-0.1-2.4,0c-0.6,0.1-1,0.5-1.1,1.1l-0.3,2.1c-0.1,0.7-0.7,1.1-1.4,1.1c-0.3,0-0.6-0.1-0.8-0.3 l-1.6-1.3c-0.4-0.4-1.1-0.3-1.5,0c-0.6,0.5-1.2,1.1-1.7,1.7c-0.4,0.4-0.4,1.1,0,1.5c0.4,0.5,1.2,1.6,1.2,1.6 c0.2,0.2,0.3,0.5,0.3,0.8c0,0.7-0.5,1.3-1.2,1.4l-2,0.3c-0.6,0.1-1,0.5-1.1,1.1c-0.1,0.8-0.1,1.6,0,2.4c0.1,0.6,0.5,1,1.1,1.1 l2,0.3c0.7,0.1,1.2,0.7,1.2,1.4c0,0.3-0.1,0.6-0.3,0.8l-1.2,1.6c-0.4,0.4-0.3,1.1,0,1.5c0.5,0.6,1.1,1.2,1.7,1.7 c0.2,0.2,0.5,0.3,0.8,0.3c0.3,0,0.5-0.1,0.8-0.3l1.6-1.3c0.2-0.2,0.5-0.3,0.8-0.3c0.7,0,1.2,0.5,1.4,1.1c0,0,0.3,2.1,0.3,2.1 c0.1,0.6,0.5,1,1.1,1.1c0.4,0,0.8,0.1,1.2,0.1c0.4,0,0.8,0,1.2-0.1c0.6-0.1,1-0.5,1.1-1.1l0.3-2.1c0.1-0.7,0.7-1.1,1.4-1.1 c0.3,0,0.6,0.1,0.8,0.2l1.7,1.3c0.2,0.2,0.5,0.3,0.8,0.3c0.3,0,0.5-0.1,0.8-0.3c0.6-0.5,1.2-1.1,1.7-1.7c0.4-0.4,0.4-1.1,0-1.5 c-0.4-0.6-1.3-1.7-1.3-1.7c-0.2-0.2-0.2-0.5-0.2-0.8c0-0.7,0.5-1.2,1.1-1.4c0,0,1.4-0.2,2.1-0.3c0.6-0.1,1-0.5,1.1-1.1 C1550,50.4,1550,49.6,1549.9,48.8z"}]
      [:path {:d "M1537.6,50c0,0.1-0.1,0.1-0.1,0.1s-0.1-0.1-0.1-0.1 s0.1-0.1,0.1-0.1S1537.6,49.9,1537.6,50z"}]
      [:line {:x1 "0" :y1 "100" :x2 "1600" :y2 "100"}]]
     [:g.draw-stats
      [:text {:transform "matrix(1 0 0 1 50 129.364)"} "Author"]
      [:text {:transform "matrix(1 0 0 1 175 129.5)"} "Paul Biggar"]
      [:text {:transform "matrix(1 0 0 1 50 154.5)"} "Duration"]
      [:text {:transform "matrix(1 0 0 1 175 154.5)"} "21:34"]
      [:text {:transform "matrix(1 0 0 1 50 179.5)"} "Pull Request"]
      [:text {:transform "matrix(1 0 0 1 175 179.5)"} "#3254"]
      [:text {:transform "matrix(1 0 0 1 50 204.5)"} "Commit Log"]
      [:text {:transform "matrix(1 0 0 1 175 204.5)"} "Refactoring some old components."]
      [:text {:transform "matrix(1 0 0 1 50 229.5)"} "Build Artifacts"]
      [:text {:transform "matrix(1 0 0 1 175 229.492)"} "View All"]]
     [:g.draw-buttons
      [:text {:transform "matrix(1 0 0 1 1254.2971 179.52)"} "rebuild"]
      [:path {:d "M1316,194h-64.2c-10.4,0-18.8-8.4-18.8-18.8v-0.3 c0-10.4,8.4-18.8,18.8-18.8h64.2V194z"}]
      [:text {:transform "matrix(1 0 0 1 1438.7131 179.52)"} "& enable ssh"]
      [:path {:d "M1531.2,194H1429v-38h102.2c10.4,0,18.8,8.4,18.8,18.8v0.3 C1550,185.6,1541.6,194,1531.2,194z"}]
      [:text {:transform "matrix(1 0 0 1 1327.1379 179.52)"} "& clear cache"]
      [:line {:x1 "1316" :y1 "156" :x2 "1429" :y2 "156"}]
      [:line {:x1 "1316" :y1 "194" :x2 "1429" :y2 "194"}]]
     [:line.draw-border {:x1 "50" :y1 "300" :x2 "50" :y2 "700"}]
     [:line.draw-border {:x1 "1550" :y1 "300" :x2 "1550" :y2 "700"}]
     [:g.draw-actions
      [:g.draw-action
       [:g [:text {:transform "matrix(1 0 0 1 75 329.492)"} "start container"]]
       [:text {:transform "matrix(1 0 0 1 1372.937 329.4918)"} "config"]
       [:text {:transform "matrix(1 0 0 1 1443.3387 329.492)"} "00:01"]
       [:line {:x1 "50" :y1 "300" :x2 "1550" :y2 "300"}]
       [:polyline.chevron-down {:points "1509,321 1517,329 1525,321  "}]]
      [:g.draw-action
       [:text {:transform "matrix(1 0 0 1 731.897 353.9304)"} "CHECKOUT ACTIONS"]
       [:g [:text {:transform "matrix(1 0 0 1 75 379.5)"} "restore source cache"]]
       [:text {:transform "matrix(1 0 0 1 1374.041 379.5)"} "cache"]
       [:text {:transform "matrix(1 0 0 1 1445.3562 379.4918)"} "00:11"]
       [:line {:x1 "50" :y1 "350" :x2 "730.5" :y2 "350"}]
       [:line {:x1 "869.5" :y1 "350" :x2 "1550" :y2 "350"}]
       [:polyline.chevron-down {:points "1509,371 1517,379 1525,371  "}]]
      [:g.draw-action
       [:g [:text {:transform "matrix(1 0 0 1 75 429.4918)"} "checkout using deploy key"]]
       [:text {:transform "matrix(1 0 0 1 1372.937 429.4918)"} "config"]
       [:text {:transform "matrix(1 0 0 1 1441.5948 429.492)"} "00:03"]
       [:line {:x1 "50" :y1 "400" :x2 "1550" :y2 "400"}]
       [:polyline.chevron-down {:points "1509,421 1517,429 1525,421  "}]]
      [:g.draw-action
       [:text {:transform "matrix(1 0 0 1 738.1831 453.9306)"} "MACHINE ACTIONS"]
       [:g [:text {:transform "matrix(1 0 0 1 75 479.4918)"} "configure the build"]]
       [:text {:transform "matrix(1 0 0 1 1374.041 479.5)"} "cache"]
       [:text {:transform "matrix(1 0 0 1 1441.3466 479.5)"} "00:02"]
       [:line {:x1 "50" :y1 "450" :x2 "736.7" :y2 "450"}]
       [:line {:x1 "863.2" :y1 "450" :x2 "1549.9" :y2 "450"}]
       [:polyline.chevron-down {:points "1509,471 1517,479 1525,471  "}]]
      [:g.draw-action
       [:g [:text {:transform "matrix(1 0 0 1 75 529.4918)"} "restore cache"]]
       [:text {:transform "matrix(1 0 0 1 1374.041 529.5)"} "cache"]
       [:text {:transform "matrix(1 0 0 1 1441.4032 529.4926)"} "00:08"]
       [:line {:x1 "50" :y1 "500" :x2 "1550" :y2 "500"}]
       [:polyline.chevron-down {:points "1509,521 1517,529 1525,521  "}]]
      [:g.draw-action
       [:text {:transform "matrix(1 0 0 1 723.8965 553.9303)"} "DEPENDENCY ACTIONS"]
       [:g [:text {:transform "matrix(1 0 0 1 75 579.4918)"} "exporting RAILS_ENV, RACK_ENV"]]
       [:text {:transform "matrix(1 0 0 1 1350.1699 579.4918)"} "inference"]
       [:text {:transform "matrix(1 0 0 1 1441.1547 579.4918)"} "00:00"]
       [:line {:x1 "50" :y1 "550" :x2 "722.3" :y2 "550"}]
       [:line {:x1 "877.7" :y1 "550" :x2 "1550" :y2 "550"}]
       [:polyline.chevron-down {:points "1509,571 1517,579 1525,571  "}]]
      [:g.draw-action
       [:g [:text {:transform "matrix(1 0 0 1 75 629.5)"} "bundle install"]]
       [:text {:transform "matrix(1 0 0 1 1350.1699 629.5)"} "inference"]
       [:text {:transform "matrix(1 0 0 1 1443.3395 629.4926)"} "00:01"]
       [:line {:x1 "50" :y1 "600" :x2 "1550" :y2 "600"}]
       [:polyline.chevron-down {:points "1509,621 1517,629 1525,621  "}]]
      [:g.draw-action
       [:text {:transform "matrix(1 0 0 1 752.7148 653.9302)"} "TEST ACTIONS"]
       [:g [:text {:transform "matrix(1 0 0 1 75 679.51)"} "parallel rspec"]]
       [:text {:transform "matrix(1 0 0 1 1350.1699 679.51)"} "inference"]
       [:text {:transform "matrix(1 0 0 1 1441.7797 679.51)"} "02:40"]
       [:line {:x1 "50" :y1 "650" :x2 "751" :y2 "650"}]
       [:line {:x1 "849" :y1 "650" :x2 "1550" :y2 "650"}]
       [:polyline.chevron-down {:points "1525,679 1517,671 1509,679  "}]]]
     [:g.draw-output
      [:rect {:x "50" :y "700" :width "1500" :height "250"}]
      [:g [:text {:transform "matrix(1 0 0 1 75 728.2069)"} "bundle exec rspec --color"]]
      [:g [:text {:transform "matrix(1 0 0 1 75 753.2067)"} "............................................................................................................................................................................."]]
      [:g [:text {:transform "matrix(1 0 0 1 75 778.2065)"} "............................................................................................................................................................................."]]
      [:g [:text {:transform "matrix(1 0 0 1 75 803.2068)"} "............................................................................................................................................................................."]]
      [:g [:text {:transform "matrix(1 0 0 1 75 828.2067)"} "............................................................................................................................................................................."]]
      [:g [:text {:transform "matrix(1 0 0 1 75 853.2067)"} "............................................................................................................................................................................."]]
      [:g [:text {:transform "matrix(1 0 0 1 75 878.207)"} "..............."]]
      [:g [:text {:transform "matrix(1 0 0 1 75 903.2067)"} "Finished in 2 minutes 30.1 seconds"]]
      [:g [:text {:transform "matrix(1 0 0 1 75 928.207)"} "884 examples, 0 failures"]]]]]))

(def drawing-dashboard
  (html
   [:svg.drawing {:xmlns "http://www.w3.org/2000/svg" :x "0px" :y "0px" :viewBox "0 0 1600 1000" :enableBackground "new 0 0 1600 1000"}
    draw-nav
    draw-menu
    draw-main]))

(def drawing-build
  (html
   [:svg.drawing {:xmlns "http://www.w3.org/2000/svg" :x "0px" :y "0px" :viewBox "0 0 1600 1000" :enableBackground "new 0 0 1600 1000"}
    draw-nav
    draw-menu
    draw-build]))
