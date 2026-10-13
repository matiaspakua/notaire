// semgrep fixture — notaire-regex-unescaped-input must fire on this file.
// The pattern below only escapes forward-slashes, leaving all other
// regex metacharacters (. * + ? ^ $ { } ( ) [ ] | \) unescaped.
// Use escapeRegExp() from the project utility instead.

// ruleid: notaire-regex-unescaped-input
const pattern = new RegExp(someUrl.replace(/[.*+?^${}()|[\]\\]/g, "\\$&"));
