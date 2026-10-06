#!/usr/bin/env python3
"""Run the worker agent (Codex or OpenCode) for one phase, with the harness guards (#1107).

  worker.py AGENT WT PROFILE MODEL HOOKS LAST PROMPT_FILE

Exit status is the agent's. LAST receives the agent's final message, for both agents.
"""
import json
import os
import subprocess
import sys

AGENTS = ("codex", "opencode")


def command(agent, wt, profile, model, hooks, last, prompt, here):
    if agent == "codex":
        return ["codex", "exec", "--profile", profile, "--skip-git-repo-check", "-C", wt,
                # WORKER.md is the worker's brief; AGENTS.md cost ~1.7K tokens and contradicts it
                "-c", "project_doc_max_bytes=0",
                # the Codex shell snapshot restores the PATH captured at startup, undoing the ZDOTDIR shims
                "-c", "features.shell_snapshot=false",
                "-c", f'shell_environment_policy.set.ZDOTDIR="{here}/zdot"',
                "-c", 'shell_environment_policy.set.GIT_CONFIG_COUNT="1"',
                "-c", 'shell_environment_policy.set.GIT_CONFIG_KEY_0="core.hooksPath"',
                "-c", f'shell_environment_policy.set.GIT_CONFIG_VALUE_0="{hooks}"',
                "-o", last, prompt]
    if agent == "opencode":
        return ["opencode", "run", "--pure", "--auto", "--format", "json", "--dir", wt, "-m", model, prompt]
    raise ValueError(f"unknown worker agent {agent!r} (one of {', '.join(AGENTS)})")


def environment(agent, base, here, hooks):
    """Codex gets its guards from -c flags; OpenCode's bash tool inherits this environment."""
    env = dict(base)
    if agent != "opencode":
        return env
    home = env.get("HOME", os.path.expanduser("~"))
    env.update({
        # the repo's opencode.json loads AGENTS.md, the rules and every skill: far beyond a 64K window
        "OPENCODE_CONFIG_DIR": os.path.join(os.path.dirname(here), "opencode"),
        "OPENCODE_DISABLE_PROJECT_CONFIG": "1",
        "OPENCODE_DISABLE_CLAUDE_CODE": "1",
        "OPENCODE_DISABLE_EXTERNAL_SKILLS": "1",
        "OPENCODE_DISABLE_AUTOUPDATE": "1",
        # the user's global config starts MCP servers; gh keeps its own config dir
        "XDG_CONFIG_HOME": os.path.join(os.path.dirname(here), "opencode", "xdg"),
        "GH_CONFIG_DIR": os.path.join(home, ".config", "gh"),
        "GIT_CONFIG_COUNT": "1",
        "GIT_CONFIG_KEY_0": "core.hooksPath",
        "GIT_CONFIG_VALUE_0": hooks,
        # a non-login `$SHELL -c`: no path_helper reorders PATH, so the shims can go first directly
        "PATH": os.path.join(here, "bin", "shims") + os.pathsep + env.get("PATH", ""),
    })
    return env


def last_message(stream):
    """The final `text` part of an OpenCode JSON event stream."""
    text = ""
    for line in stream.splitlines():
        try:
            event = json.loads(line)
        except ValueError:
            continue
        part = event.get("part") or {}
        if part.get("type") == "text" and part.get("text", "").strip():
            text = part["text"].strip()
    return text


def run(agent, wt, profile, model, hooks, last, prompt_file):
    here = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    prompt = open(prompt_file).read()
    argv = command(agent, wt, profile, model, hooks, last, prompt, here)
    env = environment(agent, os.environ, here, hooks)
    # stdin from /dev/null: both agents otherwise wait on a non-TTY stdin forever
    if agent == "codex":
        return subprocess.run(argv, env=env, stdin=subprocess.DEVNULL).returncode
    done = subprocess.run(argv, env=env, stdin=subprocess.DEVNULL, stdout=subprocess.PIPE, text=True)
    sys.stdout.write(done.stdout)
    with open(last, "w") as f:
        f.write(last_message(done.stdout) + "\n")
    return done.returncode


if __name__ == "__main__":
    if len(sys.argv) != 8:
        sys.exit(__doc__)
    sys.exit(run(*sys.argv[1:]))
