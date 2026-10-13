<!-- Governed by CONSTITUTION.md. CU76, issue #1591. Evidence: the v0.2.0 release has no assets. -->

## Purpose

Every `vX.Y.Z` GitHub Release must carry the five release assets, the pipeline
must fail loudly when it cannot attach them, and earlier releases must be
repairable without touching tags, notes, or existing assets.

## ADDED Requirements

### Requirement: A release-please tag starts the delivery pipeline

The release workflow SHALL start the delivery pipeline on the tag it creates,
because a tag created with the repository token does not start a tag-triggered
workflow by itself. The delivery pipeline SHALL still start on a manually
pushed `v*` or legacy `notaire-v*` tag.

#### Scenario: Release creation dispatches delivery on the tag

- **WHEN** release-please reports that it created a release with a tag name
- **THEN** the release workflow dispatches the delivery workflow with that tag as the ref, using only the repository token and an `actions: write` permission

#### Scenario: No release created, no dispatch

- **WHEN** release-please only opens or updates its pull request
- **THEN** no delivery dispatch happens

#### Scenario: Manually pushed tags still publish

- **WHEN** the delivery workflow triggers are read
- **THEN** both the `v*` and the `notaire-v*` tag filters remain

### Requirement: Attachment fails closed on a partial asset set

The release job SHALL verify that all five assets exist before upload, SHALL
fail when any is missing instead of attaching fewer, SHALL refuse unmatched
upload patterns, and SHALL verify after upload that the Release lists exactly
the five asset names. Concurrent runs for the same ref SHALL NOT overwrite
each other's upload mid-flight.

#### Scenario: Missing image bill stops the upload

- **WHEN** the backend image bill is absent from the downloaded artifacts
- **THEN** the release job fails before any upload

#### Scenario: Unmatched pattern fails the step

- **WHEN** a listed asset file does not exist at upload time
- **THEN** the upload step fails rather than skipping it

#### Scenario: Post-condition lists exactly five assets

- **WHEN** the upload finishes
- **THEN** the job reads the Release assets and fails unless the names are exactly the five documented ones

#### Scenario: Same-ref runs are serialised

- **WHEN** the release job definition is read
- **THEN** it declares a concurrency group per ref that does not cancel an in-progress run

### Requirement: Image delivery is verified before release assets

Release assets SHALL be attached only after the identity check and both image
builds succeed for the same tag, and the images SHALL be tagged with the
semver of the tag.

#### Scenario: Identity check gates the build

- **WHEN** the delivery workflow is read
- **THEN** the image build depends on the identity check job, and the release job depends on the image build

#### Scenario: Incoherent tag publishes nothing

- **WHEN** the pushed tag does not equal the tree version
- **THEN** the identity check fails and no image is built, signed or attached

### Requirement: The v0.2.0 backfill is non-destructive and image-faithful

The backfill runbook SHALL attach the five assets to `v0.2.0` using the image
digests already published for the tagged commit, SHALL NOT rebuild images,
SHALL NOT move or recreate any tag, SHALL NOT edit or delete the Release,
SHALL NOT overwrite an existing asset, and SHALL show a before/after proof that
the tag commit, the publish timestamp and the notes are unchanged. It MAY add
the `0.2.0` image tag to the existing digest because adding a tag alters no
published digest.

#### Scenario: Runbook forbids destructive commands

- **WHEN** the runbook text is scanned
- **THEN** it contains no forced tag move, no forced push, no release edit or delete command, and no overwrite flag on asset upload

#### Scenario: Uploads skip assets already present

- **WHEN** the runbook is followed on a release that already holds some of the five assets
- **THEN** only the missing assets are uploaded and the present ones are left byte-identical

#### Scenario: Before and after proof

- **WHEN** the runbook completes for `v0.2.0`
- **THEN** it records the tag commit, the publish timestamp and the target commit before and after, and they are equal, and the Release lists five assets

#### Scenario: Image bills come from the published digest

- **WHEN** the runbook generates the image bills
- **THEN** it resolves the backend and frontend digests from the images tagged with the tagged commit SHA, and the signature of that digest remains verifiable

### Requirement: Legacy releases receive only what they can prove

For `notaire-v0.1.1` and `notaire-v0.1.2` the runbook SHALL attach the database
bill generated from the tagged tree and an image bill only when an image
published for the tagged commit exists. It SHALL NOT attach a product bill or a
product record for those tags, because their Maven version was a snapshot that
never equalled the tag. The runbook SHALL record each omission and its reason.

#### Scenario: Legacy product bill is not fabricated

- **WHEN** the runbook is followed for a legacy tag
- **THEN** no `sbom-notaire.cdx.json` and no `product-release.json` is attached

#### Scenario: Legacy gap is recorded

- **WHEN** a legacy release ends with fewer than five assets
- **THEN** the runbook's legacy table states which assets are absent and why
