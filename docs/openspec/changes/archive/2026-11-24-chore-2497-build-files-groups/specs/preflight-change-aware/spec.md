## ADDED Requirements

### Requirement: Build files select the preflight groups they affect

`workspace/sdlc/preflight_affected.py` SHALL map `docker-compose*.yml` and `workspace/stack/` to the `stack`
group, the root `pom.xml` to `backend` and `stack`, and each `Dockerfile` to the groups of its module. No path SHALL
select every group; only a missing comparison base does.

#### Scenario: A compose change runs only the server-backed group
- **WHEN** a branch changes only `docker-compose.prod.yml`
- **THEN** the affected groups are `stack`

#### Scenario: The root POM runs the backend
- **WHEN** a branch changes only `pom.xml`
- **THEN** the affected groups are `backend` and `stack`
