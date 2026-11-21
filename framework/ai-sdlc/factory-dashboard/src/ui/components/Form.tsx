import { type FormEvent, type ReactNode, useState } from "react";

import { ApiError } from "../api.ts";
import { ErrorNote } from "./State.tsx";

let counter = 0;

export function Field({
  label,
  hint,
  children,
}: {
  label: string;
  hint?: string;
  children: (id: string, describedBy?: string) => ReactNode;
}) {
  const [id] = useState(() => `f${++counter}`);
  return (
    <div className="field">
      <label htmlFor={id}>{label}</label>
      {children(id, hint ? `${id}-hint` : undefined)}
      {hint && (
        <p className="field-hint" id={`${id}-hint`}>
          {hint}
        </p>
      )}
    </div>
  );
}

/** A form that disables itself while submitting and shows the kit's problem in place. */
export function ActionForm({
  onSubmit,
  submitLabel,
  busyLabel,
  disabled,
  children,
  footer,
}: {
  onSubmit: () => Promise<void>;
  submitLabel: string;
  busyLabel?: string;
  disabled?: boolean;
  children: ReactNode;
  footer?: ReactNode;
}) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<ApiError>();
  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setBusy(true);
    setError(undefined);
    try {
      await onSubmit();
    } catch (failure) {
      setError(failure instanceof ApiError ? failure : new ApiError(0, "unknown", String(failure)));
    } finally {
      setBusy(false);
    }
  };
  return (
    <form className="form" onSubmit={submit} aria-busy={busy}>
      <fieldset disabled={disabled || busy}>{children}</fieldset>
      {error && <ErrorNote error={error} />}
      <div className="form-actions">
        {footer}
        <button type="submit" className="btn btn-primary" disabled={disabled || busy}>
          {busy && busyLabel ? busyLabel : submitLabel}
        </button>
      </div>
    </form>
  );
}
