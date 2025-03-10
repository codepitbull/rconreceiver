--control.lua

-- Item ids: https://wiki.factorio.com/Data.raw#item

local json = require ("lib.dkjson")

commands.add_command("find", "Get elements of specified name via RCON.", function(command)
  local surface = game.surfaces[1]  -- Assuming the main surface is used; replace with the actual surface if needed
  local entity_name = command.parameter or "stone-furnace"  -- Replace with the name of the entity you're looking for

  local entities = surface.find_entities_filtered{name=entity_name}

  local found = {}

  for i, entity in pairs(entities) do
        -- Perform operations on each entity
      found[i] = {
        n = entity_name,
        id = entity.unit_number,
        x = entity.position.x,
        y = entity.position.y
      }
  end
  rcon.print(json.encode (found, { indent = false }))
end)

commands.add_command("get_combinators_signals", "Get constant combinators via RCON.", function(command)
    local surface = game.surfaces[1]

    local parameters, pos, err = json.decode (command.parameter, 0, nil)
    if err then
      log("Error: " .. err)
      do return end
    end
    local states = {}
    for stateIndex, combCoords in ipairs(parameters) do
        if combCoords.id == nil then
            log("Error: id is missing")
            do return end
        end
        if combCoords.x == nil then
            log("Error: x is missing")
            do return end
        end
        if combCoords.y == nil then
            log("Error: y is missing")
            do return end
        end
        if combCoords.s == nil then
            log("Error: s (slot id) is missing")
            do return end
        end
        if combCoords.sn == nil then
            log("Error: sn (signal name) is missing")
            do return end
        end
        local entity_id = combCoords.entityId

        local constant_combinator = surface.find_entity("constant-combinator", {combCoords.x, combCoords.y})

        if constant_combinator == nil then
           game.print ("Error: no constant-combinator at that location")
           do return end
        end

        local control_behavior = constant_combinator.get_or_create_control_behavior()
        local section = control_behavior.get_section(1)
        if section == nil then
            log("No section with id 1" )
            do return end
        end
        local slot = section.get_slot(combCoords.s)
        if slot == nil then
            log("No slot with id " ..combCoords.s)
            do return end
        end
        local name = slot.value.name
        if name ~= combCoords.sn then
            log("Signal names didn't match. Requested " ..combCoords.sn.. " got " ..name)
            do return end
        end

        states[stateIndex] = {
            min = slot.min
          }
    end
    rcon.print(json.encode (states, { indent = false }))
end)

commands.add_command("set_combinators_signals", "Set constant combinators via RCON.", function(command)
    local surface = game.surfaces[1]

    local parameters, pos, err = json.decode (command.parameter, 0, nil)
    if err then
      log("Error: " .. err)
      do return end
    end

    for stateIndex, combCoords in ipairs(parameters) do
        if combCoords.id == nil then
            log("Error: id is missing")
            do return end
        end
        if combCoords.x == nil then
            log("Error: x is missing")
            do return end
        end
        if combCoords.y == nil then
            log("Error: y is missing")
            do return end
        end
        if combCoords.s == nil then
            log("Error: s (slot id) is missing")
            do return end
        end
        if combCoords.sn == nil then
            log("Error: sn (signal name) is missing")
            do return end
        end
        if combCoords.min == nil then
            log("Error: min is missing")
            do return end
        end
        local entity_id = combCoords.entityId

        local constant_combinator = surface.find_entity("constant-combinator", {combCoords.x, combCoords.y})

        if constant_combinator == nil then
            log("No combiner found" )
           do return end
        end

        local control_behavior = constant_combinator.get_or_create_control_behavior()
        local section = control_behavior.get_section(1)
        if section == nil then
            log("No section with id 1" )
            do return end
        end
        local slot = section.get_slot(combCoords.s)
        if slot == nil then
            log("No slot with id " ..combCoords.s)
            do return end
        end
        local name = slot.value.name
        if name ~= combCoords.sn then
            log("Signal names didn't match. Requested " ..combCoords.sn.. " got " ..name)
            do return end
        end
        slot.min = combCoords.min
        section.set_slot(1, slot)
    end
end)