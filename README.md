# frontend boiler

## Overview

Stripped down version of circleci's frontend
(https://github.com/circleci/frontend)


## Setup

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL.


## Notes from 'Building CircleCI's Front end With Om' --
https://www.youtube.com/watch?v=LNtQPSUi1iQ

#### Client Stack
- ClojureScript
- c.c/atom - Handles client state
- core.async - async communication
- Secretary - client-side router
- Om - interface to Facebook's React
- Sablono - Lisp/Hiccup style templating for Facebook's React
- Less - css styling
- Stefon - An asset pipeline for clojure

#### Client Architecture

[ Server ] <=> [ Controllers ] - swap! -> [ App State (Atom) ] - render -> [ Views ] - put! ->
[ Controllers ] <=> [ Server ] ...

- Controllers process data and network requests, talk to server, operate on a queue, various
  controllers 'swap' into a big global app state (Atom)
- Render the Atom with views
- Views 'put' actions back on to the controller queue and cycle repeats

#### Layout
- Controllers: where all the events are processed. They also coordinate network requests
- Models: contains all the data functions that operate on information requested
  by the API and controllers
- Components: views!

#### App State a.k.a. The Atom
Pros
- debuggable: state data is printable, serializable.
- simple: what you put in is what you get out.
- consolidates app state

Cons
- Anti-modular: all views must agree on the data shape
- Complex: Couples API requests to views. Requires some discipline to scale.
- Imperative State Schema Management: Need to sync the Atom's schema with what shows up in the views
- Fetch vs render performance conflict: denormalization is the norm: carefult not to give too much data to a view because it
  might cause unnessary re-renders.

#### Controllers
- `navigation.cljs`: request, subscribe, render. App Entry point. Everything is
  driven by the Navigation controller. Responsible for requesting data from server. Subscribing to various real-time update
    channels. Execute inital renders.
- `api.cljs`: Collate server responses. Handles various server responses and collect them into the big atom which causes re-renders to happen.
- `controls.cljs`: handle user input. Controls put messages on a queue. They
  will also process the messages and update the big atom accordingly which
  causes view renders
- `ws.clj and ws.cljs`: websocket controller. When a page renders, it will setup
  subscriptions that listen for incremental changes in order to update the app
  state to trigger renders.

#### App start up sequence of events
1. Issue a bunch of requests.
2. setup and coordinate using core.asyc all the moving parts
3. render page
4. handle events after page rendering e.g., button clicks, ect.,

#### An example of a Component's lifetime
1. navigate to the page
2. put message on API channel saying 'go get the build data'
3. subscribe to build updates channel (server side push notification channel)
4. set some optimistic state: breadcrumbs, id values for uncleared
   information,.... Optimistic updates provide for a good UX loading process.
5. In the API controller, when the event comes into the API queue saying that
   the data has been received, it will `assoc` that data in the key places where
   there were temporary data.
6. As things happen on the server side, changes are pushed down to WS channels
   that will update the state too.

#### An example of the Control's controller flow
- Handle user interactions. Explictly define all actions that can happen from
  a User Interaction. Actions put a message on the api channel. You tell the
  user that the action has been queued. Then respond back with success or
  failure

#### Controllers
##### page centric
- requests & subscriptions are triggered by navigation
- code improperly organized by technology not page
- won't scale up to a large number of pages or app-like UX
- works well for limited number of views.

##### centralized
- Global logging / easy audits BUT no component isolation e.g.,
  here are all the requests coming from X,Y,... Helpful for site optization
- harder to make complex reusable components

##### Non-deterministic
- Tame network non-determinism with core-async
- inadvertently introduce UI non-determinism
- Network latency is a source of client non-determinism.
- core-async acts as a coordinator will make sure that things come in the right order
- Do not use core-async in the view layer or callbacks. source of bugs. Views
  are deterministic and core-async is a tool for handling non-determinism

#### More info on the View's lifecycle
1. get data
2. reify om interfaces (reify: creating an individual, unique object that implements
   the desired protocol )
3. Pull information out of data
4. calc derived values
5. render template
6. hook up event handlers to DOM node - use a side effect function called 'raise!' - queues message fire and forget.
   returns Nil. Enables Global logging.
   ** NOTE: another way to do this would be to add logging to the channels a la
   RX logging.

#### Principles of Pure Components
- assume controllers pre-fetch all data
- minimize component local state
- limit callbacks to message queues
- encapsulate violations of above

#### Impurities
- Component local state is for view concerns
- need it for UI transient state
- encapsulate state and related callbacks to purpose built components or narrow
  interfaces
- components have local state that is mounted to the local Atom. Allows us to
  serialize everything include component local state. Useful for debugging
  components
- limit use. keep to building pure functions that input data to views.

### Conclusion
- Despite disadvantages of CircleCI's architecture, it does provide these
  benefits:
  - externalized state - easy debugging
  - events as queued messages
  - immutability
  - inspectable states
