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

## Adding Sprites for Models

### Overview
Sprites are configured in `assets/sprite-config.json`. Each entry maps one or more model classes
to a set of named texture images. The default `LibgdxSprite` handles static rendering (alpha,
hurt/interact flash). A custom sprite class is only needed when that default behavior is
insufficient.

### Step 1 — Add an entry to `sprite-config.json`

```json
{
  "modelClasses": ["com.noiprocs.gameplay.model.SomeModel"],
  "images": {
    "default": {
      "imagePath": "textures/some_model.png",
      "offsetX": -30,
      "offsetY": -40,
      "scaleX": 0.07,
      "scaleY": 0.07
    }
  }
}
```

- `offsetX` / `offsetY` shift the image relative to the model's isometric screen position.
  Tune by visual inspection in-game.
- `flippedOffsetX` / `flippedOffsetY` are optional overrides used when the texture is
  horizontally flipped (e.g. mirrored directional sprites).
- Multiple model classes can share one entry (e.g. `GoblinModel` and `RangeGoblinModel`).

### Step 2 — Create a sprite class only when needed

You **do not** need a sprite class if the model always shows one static image. The default
`LibgdxSprite` is used automatically when `spriteClass` is omitted from the config entry.

Create a custom class (extending `LibgdxSprite`) when the model requires:

| Requirement | Example |
|---|---|
| Different texture per direction or state | `WallTrapSprite` — switches between `waiting`/`fire` textures based on model state |
| Custom rendering (rotation, animation) | `FlyingDartSprite` — rotates the texture to match the projectile's movement direction |
| Multiple textures loaded at class init | Any sprite with more than one named image key it switches between |

Place the class under `core/src/main/java/com/noiprocs/ui/libgdx/sprite/` in the sub-package
matching the model's domain (e.g. `mob/`, `environment/`, `plant/`). Load textures as
`static final` fields using `loadTexture(MODEL_CLASS, "key")`, and override `getTexture(Model)`
to select among them. Reference the class in the config entry's `spriteClass` field.

## Bug Fixing Principles

### Fix at the source
Always trace a bug to its root cause and fix it there. Never wrap or convert to work around a wrong type/value downstream (e.g. wrapping a HashMap in a ConcurrentHashMap) — change the original declaration to the correct type directly.
