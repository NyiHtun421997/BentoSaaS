// BaseModal.tsx
import React, { useEffect, useMemo, useRef, useState } from "react";

type BaseModalProps = {
  open: boolean; // parent controls open/close
  onClose: () => void; // called after close animation finishes
  animMs?: number;

  // UI options
  backdropClassName?: string;
  dialogClassName?: string;
  panelClassName?: string;

  // render prop so "extensions" can access close()
  children: (api: { close: () => void; isClosing: boolean }) => React.ReactNode;
};

export default function BaseModal({
  open,
  onClose,
  animMs = 300,
  backdropClassName = "backdrop:bg-black/40",
  dialogClassName = "",
  panelClassName = "",
  children,
}: BaseModalProps) {
  const modalRef = useRef<HTMLDialogElement | null>(null);

  const [isClosing, setIsClosing] = useState(false);
  const [isVisible, setIsVisible] = useState(false);

  const close = () => {
    if (isClosing) return;
    setIsClosing(true);
    setIsVisible(false);

    window.setTimeout(() => {
      modalRef.current?.close();
      setIsClosing(false);
      onClose(); // parent flips open=false here
    }, animMs);
  };

  // open/close based on prop
  useEffect(() => {
    const dialog = modalRef.current;
    if (!dialog) return;

    if (open) {
      if (!dialog.open) dialog.showModal();

      // animate in
      setIsVisible(false);
      requestAnimationFrame(() => setIsVisible(true));
    } else {
      // if parent sets open=false, animate out (if it's currently open)
      if (dialog.open && !isClosing) close();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open]);

  // ESC / native cancel should animate too
  useEffect(() => {
    const dialog = modalRef.current;
    if (!dialog) return;

    const onCancel = (e: Event) => {
      e.preventDefault();
      close();
    };

    dialog.addEventListener("cancel", onCancel);
    return () => dialog.removeEventListener("cancel", onCancel);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const dialogClasses = useMemo(
    () =>
      [
        "absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 z-50 bg-transparent p-0",
        backdropClassName,
        "[&:not([open])]:hidden",
        "flex items-center justify-center",
        dialogClassName,
      ].join(" "),
    [backdropClassName, dialogClassName],
  );

  const panelClasses = useMemo(
    () =>
      [
        "min-h-24 rounded-md bg-white shadow-xl border-stone-800",
        "transition-all ease-in-out",
        isVisible
          ? "opacity-100 scale-100 translate-y-0"
          : "opacity-0 scale-95 translate-y-1",
        panelClassName,
      ].join(" "),
    [isVisible, panelClassName],
  );

  // If open is false, don't render anything (prevents accidental focus traps)
  if (!open) return null;

  return (
    <dialog ref={modalRef} className={dialogClasses}>
      <div
        style={{ transitionDuration: `${animMs}ms` }}
        className={panelClasses}
      >
        {children({ close, isClosing })}
      </div>
    </dialog>
  );
}
