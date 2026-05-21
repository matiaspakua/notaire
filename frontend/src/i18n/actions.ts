"use server";

import { cookies } from "next/headers";
import { isSupportedLocale, DEFAULT_LOCALE } from "./request";

const ONE_YEAR_SECONDS = 365 * 24 * 60 * 60;

export async function setLocale(locale: string): Promise<void> {
  const safeLocale = isSupportedLocale(locale) ? locale : DEFAULT_LOCALE;
  const cookieStore = await cookies();
  cookieStore.set("NEXT_LOCALE", safeLocale, {
    maxAge: ONE_YEAR_SECONDS,
    path: "/",
    sameSite: "lax",
  });
}
