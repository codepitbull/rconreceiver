--control.lua

-- Item ids: https://wiki.factorio.com/Data.raw#item

local json = require ("lib.dkjson")

commands.add_command("find", "Prints elements with given name to the console.", function(command)
  local surface = game.surfaces[1]  -- Assuming the main surface is used; replace with the actual surface if needed
  local entity_name = command.parameter or "stone-furnace"  -- Replace with the name of the entity you're looking for

  local entities = surface.find_entities_filtered{name=entity_name}

  for _, entity in pairs(entities) do
        -- Perform operations on each entity

      local tbl = {
        n = entity_name,
        id = entity.unit_number,
        x = entity.position.x,
        y = entity.position.y
      }

      game.print(json.encode (tbl, { indent = false }))
      rcon.print(json.encode (tbl, { indent = false }))
  end
end)

commands.add_command("comb", "Prints constant combinator to the console.", function(command)
    local surface = game.surfaces[1]

    local parameters, pos, err = json.decode (command.parameter, 1, nil)
    if err then
      game.print("Error: " .. err)
      do return end
    end

    if parameters.id == nil then
        game.print("Error: id is missing")
        do return end
    end
    if parameters.x == nil then
        game.print ("Error: x is missing")
        do return end
    end
    if parameters.y == nil then
        game.print ("Error: y is missing")
        do return end
    end
    if parameters.command == nil then
        game.print ("Error: command is missing")
        do return end
    end

    local entity_id = parameters.entityId

    local constant_combinator = surface.find_entity("constant-combinator", {parameters.x, parameters.y})

    if constant_combinator == nil then
       game.print ("Error: no constant-combinator at that location")
       do return end
   end

    local control_behavior = constant_combinator.get_or_create_control_behavior()
    local sections = control_behavior.sections
    local signals = {}
    for i, section in pairs(sections) do
        local state = {
            id = constant_combinator.unit_number,
            x = constant_combinator.position.x,
            y = constant_combinator.position.y,
            slot = section.get_slot(1), -- section.get_slot(1).min => current value, section.get_slot(1).value.name => signal name,
            -- Full example of value: name:"signal-A", type: "virtual", quality:"normal", comparator: "="
        }
        game.print(json.encode (state, { indent = false }))
    end
end)


script.on_event(defines.events.on_player_changed_position,
  function(event)
    local surface = game.surfaces[1]  -- Assuming the main surface is used; replace with the actual surface if needed
    local entity_name = "stone-furnace"  -- Replace with the name of the entity you're looking for

    local entities = surface.find_entities_filtered{name=entity_name}

    for _, entity in pairs(entities) do
        game.print("Found entity at position: " .. entity.position.x .. ", " .. entity.position.y)
    end
end)

