import { useEffect, useRef, useState } from "react";
import { languageLabels, supportedLanguages, type Language } from "../i18n/translations";
import { useLanguage } from "../contexts/LanguageContext";

function LanguageSelector({ mode = "ui" }: { mode?: "ui" | "narration" }) {
  const {
    language,
    setLanguage,
    narrationLanguage,
    setNarrationLanguage,
    t,
  } = useLanguage();
  const selectedLanguage = mode === "ui" ? language : narrationLanguage;
  const [isOpen, setIsOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!isOpen) return;
    const handlePointerDown = (event: PointerEvent) => {
      if (!containerRef.current?.contains(event.target as Node)) setIsOpen(false);
    };
    document.addEventListener("pointerdown", handlePointerDown);
    return () => document.removeEventListener("pointerdown", handlePointerDown);
  }, [isOpen]);

  return (
    <div className="language-selector" ref={containerRef}>
      <button
        className="language-selector-button"
        type="button"
        aria-haspopup="listbox"
        aria-expanded={isOpen}
        onClick={() => setIsOpen((open) => !open)}
      >
        {selectedLanguage.toUpperCase()} <span aria-hidden="true">⌄</span>
      </button>
      {isOpen && (
        <div className="language-menu" role="listbox" aria-label={t("language", "selectorLabel")}>
          {supportedLanguages.map((option) => (
            <button
              className={option === selectedLanguage ? "is-selected" : ""}
              key={option}
              type="button"
              role="option"
              aria-selected={option === selectedLanguage}
              onClick={() => {
                if (mode === "ui") {
                  setLanguage(option as Language);
                } else {
                  setNarrationLanguage(option as Language);
                }
                setIsOpen(false);
              }}
            >
              <span>{option.toUpperCase()}</span>
              <span>{languageLabels[option]}</span>
            </button>
          ))}
        </div>
      )}
    </div>
  );
}

export default LanguageSelector;
