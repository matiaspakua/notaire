[ -f "$HOME/.zlogin" ] && . "$HOME/.zlogin"
# last, after /etc/zprofile's path_helper, which would move a PATH prefix behind /usr/bin
PATH="${ZDOTDIR:h}/bin/shims:$PATH"
