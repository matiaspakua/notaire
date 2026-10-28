# Change records

One Markdown file per change: `aisdlc change new <kebab-name> --issue <n> --title "…"`.
`aisdlc change check` (the `plan` stage) fails while a required section holds only its template
comment, when no issue is cited, or when the tracker cannot resolve the cited issue.

A change record describes **only that change**. Permanent documentation stays the source of truth;
link to it instead of copying it. Projects with a spec engine keep it and call its validator from
a `plan` gate instead.
