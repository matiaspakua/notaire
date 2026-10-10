# Design — fix-1357-personas-search-debounce

- Debounce the whole criteria object (one timer) rather than each input, so changing two fields quickly is still one request.
- Each settled criteria set is its own cache entry; TanStack Query only renders the data of the current key, so an earlier response can never overwrite a later one.
- `keepPreviousData` keeps the last results visible while the next search loads; the DataTable shows its fetching state instead of a skeleton.
- Clearing all criteria disables the query and the table shows the already cached server page, with no search request.
