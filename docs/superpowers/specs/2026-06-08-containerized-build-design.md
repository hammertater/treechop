# Containerized Build — Design

**Date:** 2026-06-08
**Status:** Approved

## Goal

Produce the mod's loader jars (NeoForge + Fabric) via a single Docker command,
without installing Java/Gradle on the host drive, in a reproducible way.

## Non-goals

- No full dev/IDE environment inside the container.
- No CI/release pipeline (may reuse the image later, but not in scope now).

## Approach

Docker Compose with a `build` service. The build image provides only the JDK 21
toolchain; the project source is bind-mounted at run time (builds the current
working tree, not a baked git ref). Gradle itself comes from the committed
`gradlew` wrapper, keeping the Gradle version pinned by the repo.

## Components

### `Dockerfile`
- Base: `eclipse-temurin:21-jdk` (pinned tag).
- Sets `GRADLE_USER_HOME=/gradle-home`.
- Working dir `/workspace`.
- No source copied in; minimal toolchain image.

### `compose.yaml`
- Service `build`:
  - Bind mount project root → `/workspace`.
  - Named volume `treechop-gradle-cache` → `/gradle-home` (persists downloads).
  - Bind mount `./build-output` → `/output`.
  - Command: run `./gradlew build`, then copy the final loader jars
    (`neoforge/build/libs/*.jar`, `fabric/build/libs/*.jar`, excluding
    `*-sources.jar`/`*-dev*.jar` as appropriate) into `/output`.

### `.dockerignore`
- Exclude `.git`, `build/`, `.gradle/`, IDE dirs, `build-output/` from the
  build context.

### `.gitignore`
- Add `build-output/`.

### `README.md`
- Document `docker compose run --rm build`.

## Output flow

Gradle writes jars to `neoforge/build/libs` and `fabric/build/libs`
(archivesName `TreeChop-1.21.1-<loader>`). The service copies only the final
release jars into the host `./build-output/`, so the host receives finished
artifacts only — no build intermediates.

## Reproducibility

- Gradle version pinned via wrapper (`gradle-8.9`).
- JDK pinned via image tag.
- Named cache only accelerates repeat builds; Gradle verifies artifacts, so the
  cache does not compromise reproducibility.

## User-facing UX

```
docker compose run --rm build
# => ./build-output/TreeChop-1.21.1-neoforge-*.jar
# => ./build-output/TreeChop-1.21.1-fabric-*.jar
```
