(ns loicb.client.core.db
  "State management using re-frame.
   
   ## Naming convention (inspired by Ivan Fedorov)
   :evt.domain/evt-id for events
   :subs.domain/sub-id for subs
   :domain/key-id for db keys
   :fx.domain/fx-id for effects
   :cofx.domain/cofx-id for coeffects"
  (:require [loicb.client.core.db.event]
            [loicb.client.core.db.fx]
            [loicb.client.core.db.sub]))

