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
