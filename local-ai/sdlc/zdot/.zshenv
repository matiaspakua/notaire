# ZDOTDIR for the worker's `zsh -lc`: the user's startup files, then the crawl_guard shims
[ -f "$HOME/.zshenv" ] && . "$HOME/.zshenv"
