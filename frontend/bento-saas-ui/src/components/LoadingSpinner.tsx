import type { HTMLAttributes } from "react";

type SpinnerSize = "xs" | "sm" | "md" | "lg" | "xl";

const sizeClass: Record<SpinnerSize, string> = {
  xs: "h-3 w-3 border-2",
  sm: "h-4 w-4 border-2",
  md: "h-6 w-6 border-2",
  lg: "h-10 w-10 border-4",
  xl: "h-14 w-14 border-4",
};

type LoadingSpinnerProps = HTMLAttributes<HTMLSpanElement> & {
  size?: SpinnerSize;
};

const LoadingSpinner = ({
  size = "md",
  className = "",
  ...rest
}: LoadingSpinnerProps) => {
  return (
    <span
      aria-hidden="true"
      className={`${sizeClass[size]} animate-spin rounded-full border-slate-300 border-t-emerald-600 ${className}`}
      {...rest}
    />
  );
};

export default LoadingSpinner;
