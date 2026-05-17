// =============================================================================
// Job DSL: notaire-ci-pipeline
// =============================================================================
// Defines the "Notaire CI Pipeline" job in Jenkins via Configuration as Code
// (JCasC) + the Job DSL plugin.
//
// Pipeline source:
//   Git repo at /workspace/notaire/.git — this is the local project directory
//   mounted into the Jenkins container via docker-compose.yml.
//   Jenkinsfile is read from the repository root.
// =============================================================================

pipelineJob('notaire-ci-pipeline') {

    displayName('Notaire CI Pipeline')
    description(
        'Build, test, and publish code coverage for the Notaire project.\n' +
        'Uses the Jenkins Coverage Plugin (https://github.com/jenkinsci/coverage-plugin) ' +
        'for trend charts, file-level annotations, and quality gates.'
    )

    // ---------------------------------------------------------------------------
    // Pipeline definition
    // ---------------------------------------------------------------------------
    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('/workspace/notaire/.git')
                    }
                    branches('*/main')
                    extensions {
                        cleanBeforeCheckout()
                    }
                }
            }
            scriptPath('Jenkinsfile')
            lightweight(false)
        }
    }

    // ---------------------------------------------------------------------------
    // Triggers — every 30 minutes
    // ---------------------------------------------------------------------------
    triggers {
        cron('H/30 * * * *')
    }

    // ---------------------------------------------------------------------------
    // Build discarder
    // ---------------------------------------------------------------------------
    logRotator {
        numToKeep(20)
        daysToKeep(30)
        artifactNumToKeep(5)
    }

    // ---------------------------------------------------------------------------
    // Properties (coverage quality gates are handled inside the Jenkinsfile via
    // the Coverage Plugin's pipeline step)
    // ---------------------------------------------------------------------------
    properties {
        pipelineTriggers {
            triggers {
                cron {
                    spec('H/30 * * * *')
                }
            }
        }
    }
}
