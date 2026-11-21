/**
 * Factory Dashboard — Control bar component.
 *
 * Transport-agnostic control surface: play, pause, resume, stop, and skip.
 * Sends commands via WebSocket (real-time) or REST API (fallback).
 * Accessible from the production line view and station detail panel.
 */
import { useCallback } from "react";
import { ControlState } from "@/types";

interface ControlBarProps {
  controlState: ControlState;
  runId: string | null;
  onControl: (command: "play" | "pause" | "resume" | "stop" | "skip", reason?: string) => void;
}

export function ControlBar({ controlState, runId, onControl }: ControlBarProps) {
  const handlePlay = useCallback(() => onControl("play"), [onControl]);
  const handlePause = useCallback(() => onControl("pause"), [onControl]);
  const handleResume = useCallback(() => onControl("resume"), [onControl]);
  const handleStop = useCallback(() => onControl("stop"), [onControl]);

  const canPlay = controlState === "idle" || controlState === "stopped";
  const canPause = controlState === "playing";
  const canResume = controlState === "paused";
  const canStop = controlState === "playing" || controlState === "paused";

  return (
    <div className="control-bar" role="toolbar" aria-label="Factory controls">
      {/* Play */}
      <button
        className={`control-btn control-btn-play ${canPlay ? "" : "disabled"}`}
        onClick={handlePlay}
        disabled={!canPlay}
        title="Start factory run"
        aria-label="Play"
      >
        <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
          <path d="M4 2.5v11l9-5.5z" />
        </svg>
        <span className="control-btn-label">Play</span>
      </button>

      {/* Pause */}
      <button
        className={`control-btn control-btn-pause ${canPause ? "" : "disabled"}`}
        onClick={handlePause}
        disabled={!canPause}
        title="Pause factory run"
        aria-label="Pause"
      >
        <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
          <rect x="3" y="2" width="3.5" height="12" rx="1" />
          <rect x="9.5" y="2" width="3.5" height="12" rx="1" />
        </svg>
        <span className="control-btn-label">Pause</span>
      </button>

      {/* Resume */}
      <button
        className={`control-btn control-btn-resume ${canResume ? "" : "disabled"}`}
        onClick={handleResume}
        disabled={!canResume}
        title="Resume paused factory run"
        aria-label="Resume"
      >
        <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
          <path d="M4 2.5v11l9-5.5z" />
        </svg>
        <span className="control-btn-label">Resume</span>
      </button>

      {/* Stop */}
      <button
        className={`control-btn control-btn-stop ${canStop ? "" : "disabled"}`}
        onClick={handleStop}
        disabled={!canStop}
        title="Stop factory run"
        aria-label="Stop"
      >
        <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
          <rect x="3" y="3" width="10" height="10" rx="2" />
        </svg>
        <span className="control-btn-label">Stop</span>
      </button>

      {/* State indicator */}
      <div className="control-state-indicator">
        <span className={`control-state-dot ${controlState}`} />
        <span className="control-state-label">{controlState}</span>
        {runId && <span className="control-run-id">{runId}</span>}
      </div>
    </div>
  );
}
