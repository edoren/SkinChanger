# SkinChanger

Minecraft mod (Fabric + NeoForge) built with Java 21 and Gradle 8.8 via Architectury.

## Building

### Local

```bash
./gradlew build
```

Outputs land in `fabric/build/libs/` and `neoforge/build/libs/`.

### Docker

```bash
# First time — build the image
docker compose build

# Run a build
docker compose run --rm build
```

## Commits

This project uses [Conventional Commits](https://www.conventionalcommits.org/).

Format: `type(scope): description`

| Type | When to use |
|------|-------------|
| `feat` | New user-facing feature |
| `fix` | Bug fix |
| `build` | Build system or dependency changes |
| `ci` | CI/CD configuration changes |
| `docs` | Documentation only |
| `refactor` | Code change that is neither a fix nor a feature |
| `chore` | Maintenance tasks that don't fit elsewhere |

`scope` is optional and refers to the subproject: `common`, `fabric`, `neoforge`.

Examples:
```
feat(fabric): add skin preview in inventory screen
fix(common): handle null skin response from server
build: add Docker environment for local builds
ci: pin temurin JDK version in workflow
```
