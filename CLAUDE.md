# Code Style Guidelines

## Imports
- Always use proper imports at the top of the file
- Never use fully qualified class names in code (e.g., `com.badlogic.gdx.scenes.scene2d.Touchable.enabled`)
- Use short class names with imports instead (e.g., `Touchable.enabled`)

## Comments
- Do not leave redundant comments that reference removed features or historical context
- Comments should be clear to maintainers without knowledge of previous implementation attempts
- Avoid comments like "disabled in favor of X" or "removed because Y" that suggest alternative approaches existed
