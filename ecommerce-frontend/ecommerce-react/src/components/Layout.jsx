import Header from "./Header";
import Footer from "./Footer";

export default function Layout({ children }) {
  return (
    <div className="page">
      <Header />
      <main className="page-body">{children}</main>
      <Footer />
    </div>
  );
}
