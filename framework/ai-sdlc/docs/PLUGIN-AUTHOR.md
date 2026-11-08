# Plugin author notes

A plugin is a TOML manifest. The kit reads it. It does not exec it during `plugin list` or `config validate`.

```toml
id = "example-method"
kind = "method"          # method | harness | tracker | gate | evidence
version = "1.0.0"
status = "example"       # free text; planned files stay out of [plugins] allow
capabilities = ["plan"]
command = ["example-method", "plan", "{change}"]   # argv, never a shell string
```

The profile lists the ids it trusts:

```toml
[plugins]
allow = ["example-method"]

[[plugins.entries]]
id = "example-method"
kind = "method"
version = "1.0.0"
manifest = "plugins/example-method/plugin.toml"
trusted = true
```

`trusted = true` without the id in `allow` fails `config validate`. `trusted = false` is listed and is not available. Its capabilities are not offered.

`{change}` is replaced only when every value is a single token. The result is still not run.

A manifest path must stay inside the project. That is not a sandbox. Permissions written in a manifest are not enforced isolation, because this slice does not enforce them.

`auto_trust_hooks = true` fails `config validate` (#2124). Plugins that could install
harness hooks set `auto_trust_hooks = false` (or omit the key).

Planned manifests under `plugins/planned/` are documentation. They are not entries.
