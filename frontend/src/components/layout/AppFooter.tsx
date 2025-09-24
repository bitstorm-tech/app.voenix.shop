import { Facebook, Factory, Instagram, Mail, Phone, RefreshCcw, ShieldCheck, Truck, Youtube } from 'lucide-react';

const topHighlights = [
  {
    icon: Truck,
    title: 'Versandkostenfrei ab 60 €**',
    description: 'Versandkosten Deutschland ab 5,95 €* · Versand UK ab 9,90 €*',
  },
  {
    icon: RefreshCcw,
    title: 'Umtausch & Rückgabe innerhalb 14 Tagen',
    description: 'Einfach & unkompliziert – wir kümmern uns darum',
  },
  {
    icon: Factory,
    title: 'Direkt aus Deutschland',
    description: 'Produziert innerhalb von 24h · mit Zufriedenheitsgarantie',
  },
  {
    icon: ShieldCheck,
    title: 'Sichere & schnelle Zahlung',
    description: 'PayPal, Klarna, Überweisung, Kreditkarte, Apple Pay',
  },
];

export function AppFooter() {
  return (
    <footer className="bg-white text-gray-700">
      <div className="border-t border-gray-200 bg-gray-100">
        <div className="mx-auto max-w-6xl px-4 py-8">
          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
            {topHighlights.map(({ icon: Icon, title, description }) => (
              <div key={title} className="flex gap-4">
                <div className="flex h-12 w-12 flex-none items-center justify-center rounded-full bg-white text-orange-500 shadow-sm">
                  <Icon className="h-6 w-6" strokeWidth={1.5} />
                </div>
                <div>
                  <p className="font-semibold tracking-wide text-gray-900 uppercase">{title}</p>
                  <p className="mt-2 text-sm leading-relaxed text-gray-600">{description}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="mx-auto max-w-6xl px-4 pt-12 pb-16">
        <div className="grid gap-12 lg:grid-cols-[1.1fr,0.9fr,0.9fr,1.1fr]">
          <section className="space-y-6">
            <header>
              <p className="text-xs font-semibold tracking-[0.3em] text-gray-500 uppercase">Kontakt</p>
            </header>
            <div className="space-y-3 text-sm leading-relaxed">
              <p className="text-2xl font-bold text-orange-500 uppercase">Voenix Shop</p>
              <p className="text-sm font-medium tracking-[0.3em] text-gray-400 uppercase">Moments made magical</p>
              <p className="pt-2 text-gray-600">
                Münchner Straße 37
                <br />
                82067 Schäftlarn
                <br />
                Deutschland
              </p>
            </div>
            <div className="flex flex-col gap-3 text-sm text-gray-600">
              <div className="flex items-center gap-3">
                <span className="flex h-10 w-10 items-center justify-center rounded-full border border-gray-200 bg-white text-orange-500">
                  <Phone className="h-5 w-5" strokeWidth={1.5} />
                </span>
                <div>
                  <p className="text-xs tracking-[0.25em] text-gray-500 uppercase">Telefon</p>
                  <p className="font-medium text-gray-800">+49 (0) 89 1234 5678</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <span className="flex h-10 w-10 items-center justify-center rounded-full border border-gray-200 bg-white text-orange-500">
                  <Mail className="h-5 w-5" strokeWidth={1.5} />
                </span>
                <div>
                  <p className="text-xs tracking-[0.25em] text-gray-500 uppercase">E-Mail</p>
                  <p className="font-medium text-gray-800">support@voenix.shop</p>
                </div>
              </div>
            </div>
          </section>

          <section className="space-y-6">
            <p className="text-xs font-semibold tracking-[0.3em] text-gray-500 uppercase">Menükonto</p>
            <ul className="space-y-3 text-sm text-gray-600">
              {['Mein Konto', 'Bestellverlauf', 'Wunschliste', 'Newsletter verwalten'].map((item) => (
                <li key={item} className="transition-colors hover:text-gray-900">
                  {item}
                </li>
              ))}
            </ul>
          </section>

          <section className="space-y-6">
            <p className="text-xs font-semibold tracking-[0.3em] text-gray-500 uppercase">Information</p>
            <ul className="space-y-3 text-sm text-gray-600">
              {[
                'Kontaktformular & FAQ',
                'Zahlung',
                'Versand & Lieferung',
                'Widerruf & Rückgabe',
                'Datenschutz',
                'Cookie Richtlinie',
                'Impressum',
              ].map((item) => (
                <li key={item} className="transition-colors hover:text-gray-900">
                  {item}
                </li>
              ))}
            </ul>
          </section>

          <section className="space-y-8">
            <div className="space-y-4">
              <p className="text-xs font-semibold tracking-[0.3em] text-gray-500 uppercase">Social Media</p>
              <div className="flex gap-4 text-gray-600">
                {[Facebook, Instagram, Youtube].map((Icon, index) => (
                  <span
                    key={Icon.displayName ?? Icon.name ?? index}
                    className="flex h-10 w-10 items-center justify-center rounded-full border border-gray-200 bg-white text-gray-700 transition-colors hover:border-orange-400 hover:text-orange-500"
                  >
                    <Icon className="h-5 w-5" strokeWidth={1.5} />
                  </span>
                ))}
              </div>
            </div>

            <div className="space-y-4">
              <p className="text-xs font-semibold tracking-[0.3em] text-gray-500 uppercase">Newsletter</p>
              <p className="text-sm text-gray-600">Verpasse keine VOENIX News: Erhalte Infos zu neuen Highlights, Aktionen & Events.</p>
              <form className="flex flex-col gap-3 sm:flex-row">
                <input
                  type="email"
                  placeholder="E-Mail Adresse"
                  className="h-12 w-full rounded-full border border-gray-300 bg-gray-50 px-4 text-sm transition outline-none focus:border-orange-500 focus:bg-white"
                />
                <button
                  type="button"
                  className="h-12 shrink-0 rounded-full bg-orange-500 px-6 text-sm font-semibold tracking-widest text-white uppercase transition-colors hover:bg-orange-600"
                >
                  Abonnieren
                </button>
              </form>
            </div>
          </section>
        </div>
      </div>

      <div className="border-t border-gray-200 bg-gray-900">
        <div className="mx-auto max-w-6xl px-4 py-4">
          <p className="text-center text-xs tracking-[0.4em] text-gray-300 uppercase">Copyright ©2025 VOENIXSHOP</p>
        </div>
      </div>
    </footer>
  );
}
