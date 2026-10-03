import type { TipoDeDocumento } from "@/types";

/** Defaults for the admin create form (CU27 / #800). */
export const EMPTY_DOCUMENT_TYPE: Partial<TipoDeDocumento> = {
  name: "",
  expires: false,
  dueDays: null,
  deliveredBy: "",
  enabled: true,
  returned: false,
};
