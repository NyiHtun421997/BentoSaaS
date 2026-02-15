import LoadingSpinner from "./LoadingSpinner";

type PageLoadingProps = {
  text?: string;
};

const PageLoading = ({ text = "Loading..." }: PageLoadingProps) => {
  return (
    <div className="flex min-h-[300px] flex-col items-center justify-center gap-4">
      <p className="text-sm font-semibold text-slate-600">{text}</p>
      <LoadingSpinner size="xl" />
    </div>
  );
};

export default PageLoading;
