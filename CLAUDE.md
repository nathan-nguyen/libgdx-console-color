# Code Style Guidelines

## Imports
- Always use proper imports at the top of the file
- Never use fully qualified class names in code (e.g., `com.badlogic.gdx.scenes.scene2d.Touchable.enabled`)
- Use short class names with imports instead (e.g., `Touchable.enabled`)

## Comments
- Do not leave redundant comments that reference removed features or historical context
- Comments should be clear to maintainers without knowledge of previous implementation attempts
- Avoid comments like "disabled in favor of X" or "removed because Y" that suggest alternative approaches existed

## Architecture

### Coordinate System
- `model.position` and hitbox dimensions from `hitboxManager.getHitboxDimension()` are both in
  **world units**. Divide by `Config.WORLD_SCALE` to get tile coordinates.

### Isometric Rendering
- Render depth is sorted by the **center of the object's footprint**:
  `2*(position.x + position.y) + dim.x + dim.y`. The factor of 2 is intentional —
  it equals `near_corner_depth + far_corner_depth`, which is `2 × center_depth`.
- Encapsulated in `IsometricRenderPolicy.isoDepth()`.

### Direction → Screen Mapping
World directions (from `Direction.java`) map to isometric screen directions as follows:
- `NORTH (-1, 0)` → NE on screen
- `SOUTH (+1, 0)` → SW on screen
- `WEST (0, -1)` → NW on screen
- `EAST (0, +1)` → SE on screen

## Bug Fixing Principles

### Fix at the source
Always trace a bug to its root cause and fix it there. Never wrap or convert to work around a wrong type/value downstream (e.g. wrapping a HashMap in a ConcurrentHashMap) — change the original declaration to the correct type directly.
