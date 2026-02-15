import "./NotFoundPage.css";

const NotFoundPage = () => {
  return (
    <div className="page-shell">
      <h1 className="page-title">Not Found</h1>
      <p className="page-subtitle">
        The page you requested does not exist.
      </p>
      <div className="notfound-panel">
        <p className="text-sm text-slate-500">
          Check the URL or use the navigation to get back on track.
        </p>
      </div>
    </div>
  );
};

export default NotFoundPage;
