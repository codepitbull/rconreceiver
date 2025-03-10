/Users/jochenmader/Library/Application Support/factorio



https://www.reddit.com/r/factorio/comments/upsopx/how_do_i_bulk_change_constant_combinator_settings/

ent.get_or_create_control_behavior().set_signal(1,{signalid = {type="fluid"/"item"/"virtual", name="nameofsignal"}, count=1})


```lua
-- Define the entity ID and surface name
local entity_id = "constant-combinator-12345"  -- Replace with the actual entity ID
local surface_name = "nauvis"  -- Usually "nauvis" for the default surface

-- Retrieve the entity
local surface = game.surfaces[surface_name]
local constant_combinator = surface.find_entity(entity_id)

-- Check if the entity exists and is a constant combinator
if constant_combinator and constant_combinator.name == "constant-combinator" then
    -- Access the control behavior of the combinator
    local control_behavior = constant_combinator.get_or_create_control_behavior()

    -- Modify the signal for A from 0 to 1
    local parameters = control_behavior.parameters
    for i, signal in pairs(parameters) do
        if signal.signal and signal.signal.name == "signal-A" and signal.count == 0 then
            -- Update the count to 1
            control_behavior.set_signal(i, {signal = signal.signal, count = 1})
            game.print("Updated signal A from 0 to 1.")
            break
        end
    end
else
    game.print("Entity not found or not a constant combinator.")
end
```


## Setup

The mod in **factorioserver/mods/rconreceiver** must be linked into your game installations mod folder so these remain synced:

```bash
 ln -s /Users/jmader/Development/0_playground/rconjava/factorioserver/mods/rconreceiver/ /Users/jmader/Library/Application\ Support/factorio/mods/rconreceiver
```

Beyond that you must edited the **mod-list.json** available in both directories to look something like this:

```json
{
  "mods":
  [

    {
      "name": "base",
      "enabled": true
    },

    {
      "name": "elevated-rails",
      "enabled": true
    },

    {
      "name": "quality",
      "enabled": true
    },

    {
      "name": "space-age",
      "enabled": true
    },

    {
      "name": "rconreceiver",
      "enabled": true
    }
  ]
}
```

This file must be identical for the server and your game installation so that both have the same active mods.


### Accelerated Research
```bash
game.player.cheat_mode=true
game.player.force.research_all_technologies()
```