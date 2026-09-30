# Tool call shape

- Your shell tool is `exec_command`. Its arguments are one JSON object whose `cmd`
  is ONE string: `{"cmd": "sed -n 1,80p slug.py"}`. Never a list, never
  `["bash", "-lc", ...]`.
- For you `apply_patch` DOES work, but only as a command inside `exec_command`
  (there is no separate `apply_patch` tool). Edit a file with it:

  ```text
  {"cmd": "apply_patch <<'PATCH'\n*** Begin Patch\n*** Update File: slug.py\n@@\n-old line\n+new line\n*** End Patch\nPATCH"}
  ```

  Create a file with `*** Add File: <path>` and every line prefixed with `+`.
- Inside the JSON string escape every `"` as `\"` and every backslash as `\\`.
