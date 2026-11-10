import { LANG_KEY, THEME_KEY } from "./preferences";

/**
 * Runs from the document head before paint. Sets data-theme and lang from
 * localStorage, otherwise from the system. Does not send anything off the page.
 */
export const PREFERENCE_BOOT = `(function(){try{var t=localStorage.getItem("${THEME_KEY}")||"system";if(t!=="light"&&t!=="dark"&&t!=="system")t="system";var dark=t==="dark"||(t!=="light"&&window.matchMedia("(prefers-color-scheme: dark)").matches);var root=document.documentElement;root.dataset.theme=dark?"dark":"light";root.dataset.themeChoice=t;var l=localStorage.getItem("${LANG_KEY}")||"system";if(l!=="en"&&l!=="es"&&l!=="system")l="system";var lang=(navigator.language||"");var es=l==="es"||(l==="system"&&lang.toLowerCase().indexOf("es")===0);root.lang=es?"es":"en";root.dataset.langChoice=l;var meta=document.querySelector('meta[name="theme-color"]');if(meta)meta.setAttribute("content",dark?"#0b1220":"#ffffff");}catch(e){}})();`;
