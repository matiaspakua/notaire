# patched-dependencies Specification

## Purpose
Runtime dependencies stay at or above the patched release of each known HIGH/CRITICAL CVE.
## Requirements
### Requirement: Patched runtime libraries

The backend runtime classpath SHALL use releases at or above the fixed version of every known HIGH/CRITICAL CVE on the release line in use, for embedded Tomcat and Jackson 2 and 3.

#### Scenario: Patched Tomcat

- **WHEN** the backend classpath is resolved
- **THEN** tomcat-embed-core reports 11.0.25 or later on the 11.0 line

#### Scenario: Patched Jackson

- **WHEN** the backend classpath is resolved
- **THEN** jackson-core and jackson-databind report at least 2.21.7 (2.21 line) or 2.22.3 (2.22 line), and 3.1.7 (3.1 line) or 3.2.3 (3.2 line)

### Requirement: No known deserialization gadget

commons-collections 2.x/3.x SHALL NOT be on the backend classpath.

#### Scenario: No commons-collections gadget

- **WHEN** the backend classpath is inspected
- **THEN** `org.apache.commons.collections.functors.InvokerTransformer` cannot be loaded

