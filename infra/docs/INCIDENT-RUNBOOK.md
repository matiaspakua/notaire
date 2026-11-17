# Incident runbook

Use this when the notarial stack is up in the wrong way: health fails, Postgres
is unreachable, or Grafana panels are empty. Day-to-day start and stop stay in
[OPERATION.md](OPERATION.md). This page does not define an on-call rota. No
on-call rota is configured. The repository owner is the escalation until one
exists. A practice exercise has not been recorded here.

## Scenarios

### Application health is not OK

1. `curl http://localhost:8080/actuator/health`
2. If that is not HTTP 200, read the latest backend log: `docker logs notary-backend --tail 200`
3. Bring the application back with `bash workspace/stack/start.sh`
4. Repeat the health curl. Then run `bash infra/scripts/check-infra.sh`

### Postgres is not accepting connections

1. `docker ps --filter name=notary-postgres`
2. Read `docker logs notary-postgres --tail 200`
3. If the container is stopped, start the application stack again: `bash workspace/stack/start.sh`
4. Confirm the backend health curl above returns 200. Backups are tracked in #256; this runbook does not restore a dump.

### Grafana panels are empty

1. Confirm infra is up: `bash infra/scripts/check-infra.sh`
2. `bash infra/scripts/verify-signals.sh --with-grafana-panels`
3. A failing panel name in that output is the next place to look. Do not treat a green script as a claim that every dashboard in the UI was opened by hand.

## Escalation

No on-call rota is configured. Tell the repository owner what failed, which
command showed it, and the time. Do not page a vendor from this document.

## Communication

Copy this note. Fill the blanks. Do not add a cause you have not seen.

```text
Incident: <one line>
Seen at: <time>
Command: <the command and its result>
Impact: <who cannot work>
Next check: <the recovery command you will run>
```

## Post-incident

After the service is back, write a short note:

- What the operator saw
- Which command showed the failure
- What was restarted or left alone
- What is still open (including #256 if data recovery was in question)

Do not describe a practice exercise as finished unless the owner records one
elsewhere.

## Recovery checklist

- [ ] `curl http://localhost:8080/actuator/health` returns 200
- [ ] `bash infra/scripts/check-infra.sh` exits 0
- [ ] If panels were the symptom, `bash infra/scripts/verify-signals.sh --with-grafana-panels` exits 0
- [ ] The owner has the communication note
