import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from "react";
import {
  supportedLanguages,
  translations,
  type Language,
  type TranslationDictionary,
} from "../i18n/translations";

const UI_STORAGE_KEY = "linguaflow_ui_lang";
const NARRATION_STORAGE_KEY = "linguaflow_narration_lang";
const LEGACY_STORAGE_KEY = "linguaflow_lang";

function readLanguage(key: string): Language {
  if (typeof window === "undefined") return "vi";
  try {
    const stored =
      window.localStorage.getItem(key) ??
      (key === UI_STORAGE_KEY ? window.localStorage.getItem(LEGACY_STORAGE_KEY) : null);
    return stored && supportedLanguages.includes(stored as Language) ? (stored as Language) : "vi";
  } catch {
    return "vi";
  }
}

function interpolate(value: string, variables?: Record<string, string | number>): string {
  if (!variables) return value;
  return value.replace(/\{(\w+)\}/g, (_, key: string) => String(variables[key] ?? `{${key}}`));
}

interface LanguageContextValue {
  language: Language;
  setLanguage: (language: Language) => void;
  uiLanguage: Language;
  narrationLanguage: Language;
  setUiLanguage: (language: Language) => void;
  setNarrationLanguage: (language: Language) => void;
  t: <K extends keyof TranslationDictionary>(
    section: K,
    key: keyof TranslationDictionary[K],
    variables?: Record<string, string | number>,
  ) => string;
}

const LanguageContext = createContext<LanguageContextValue | null>(null);

export function LanguageProvider({ children }: { children: ReactNode }) {
  const [uiLanguage, setUiLanguageState] = useState<Language>(() => readLanguage(UI_STORAGE_KEY));
  const [narrationLanguage, setNarrationLanguageState] = useState<Language>(() =>
    readLanguage(NARRATION_STORAGE_KEY),
  );

  const setUiLanguage = useCallback((nextLanguage: Language) => {
    setUiLanguageState(nextLanguage);
    try {
      window.localStorage.setItem(UI_STORAGE_KEY, nextLanguage);
      window.localStorage.setItem(LEGACY_STORAGE_KEY, nextLanguage);
    } catch {
      // The selected language remains active for the current session.
    }
  }, []);

  const setNarrationLanguage = useCallback((nextLanguage: Language) => {
    setNarrationLanguageState(nextLanguage);
    try {
      window.localStorage.setItem(NARRATION_STORAGE_KEY, nextLanguage);
    } catch {
      // The selected language remains active for the current session.
    }
  }, []);

  const t = useCallback<LanguageContextValue["t"]>(
    (section, key, variables) => {
      const value = translations[uiLanguage][section][key];
      return interpolate(String(value), variables);
    },
    [uiLanguage],
  );

  const value = useMemo(
    () => ({
      language: uiLanguage,
      setLanguage: setUiLanguage,
      uiLanguage,
      narrationLanguage,
      setUiLanguage,
      setNarrationLanguage,
      t,
    }),
    [uiLanguage, narrationLanguage, setUiLanguage, setNarrationLanguage, t],
  );
  return <LanguageContext.Provider value={value}>{children}</LanguageContext.Provider>;
}

export function useLanguage(): LanguageContextValue {
  const context = useContext(LanguageContext);
  if (!context) throw new Error("useLanguage must be used within LanguageProvider");
  return context;
}
