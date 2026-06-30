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

## Upgrading Minecraft / Dependency Versions

All versions live in `gradle.properties`. When targeting a new Minecraft version, update every entry below. The Architectury Loom plugin version in `build.gradle` must also be checked — a mismatch causes a `binarypatcher ConsoleTool not found` error at the NeoForge patching step.

### Where to find each version

| Property | Source |
|----------|--------|
| `minecraft_version` | Target release, e.g. `1.21.3` |
| `minecraft_version_range` | Use the `game_versions` array returned by the Architectury API Modrinth query — set range to `[lowest, highest]` of those versions, e.g. `[1.21.2,1.21.3]` |
| `architectury_api_version` | Modrinth API: `https://api.modrinth.com/v2/project/lhGA9TYQ/version?version_number={version}` → `game_versions` array |
| `architectury_version_range` | `[{architectury_api_version},)` |
| `fabric_loader_version` | `https://meta.fabricmc.net/v2/versions/loader/{mc_version}?limit=1` → `loader.version` |
| `fabric_api_version` | Modrinth API: `https://api.modrinth.com/v2/project/P7dR8mSH/version?game_versions=["{mc_version}"]&version_type=release&loaders=["fabric"]` → first result `version_number` |
| `neoforge_api_version` | `https://maven.neoforged.net/api/maven/latest/version/releases/net%2Fneoforged%2Fneoforge?filter={mc_major}.{mc_minor}.` |
| `neoforge_version_range` | `[{mc_major}.{mc_minor},)` e.g. `[21.3,)` for MC `1.21.3` |
| `dev.architectury.loom` (in `build.gradle`) | `https://raw.githubusercontent.com/architectury/architectury-api/{lowest_game_version}/build.gradle` → find the `dev.architectury.loom` version string (branch name is the lowest MC version from `game_versions`) |

### Checklist

1. Query the Architectury API version on Modrinth (`lhGA9TYQ`) to get the `game_versions` it supports.
2. Set `minecraft_version` to the highest of those game versions, and `minecraft_version_range` to `[lowest,highest]`.
3. Update `architectury_api_version` and set `architectury_version_range` to `[{version},)`.
4. Update `fabric_loader_version` and `fabric_api_version` (version suffix must match MC version, e.g. `+1.21.3`).
5. Update `neoforge_api_version` and `neoforge_version_range` (NeoForge major/minor tracks MC, e.g. `[21.3,)` for MC `1.21.3`).
6. Fetch `https://raw.githubusercontent.com/architectury/architectury-api/{lowest_game_version}/build.gradle` and copy the `dev.architectury.loom` version into `build.gradle`.

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
