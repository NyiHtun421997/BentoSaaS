import "./ForbiddenPage.css";

export default function ForbiddenPage() {
  return (
    <div className="page-shell">
      <h1 className="page-title">Forbidden</h1>
      <p className="page-subtitle">
        You do not have access to this page.
      </p>
      <div className="forbidden-panel">
        <p className="text-sm text-slate-500">
          If you think this is a mistake, please contact support.
        </p>
      </div>
    </div>
  );
}
