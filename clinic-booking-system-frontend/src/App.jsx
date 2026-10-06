const services = [
  { title: 'Find a doctor', description: 'Browse our care team and find the right specialist.', icon: '✚' },
  { title: 'Book an appointment', description: 'Choose a time that works for you and book online.', icon: '▦' },
  { title: 'Manage your visits', description: 'Keep your upcoming appointments in one place.', icon: '♡' },
]

function App() {
  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">
      <header className="border-b border-slate-200 bg-white">
        <nav className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4" aria-label="Main navigation">
          <a className="flex items-center gap-2 text-lg font-bold tracking-tight" href="/">
            <span className="grid h-9 w-9 place-items-center rounded-xl bg-brand-600 text-white" aria-hidden="true">+</span>
            CarePoint
          </a>
          <a className="rounded-lg bg-brand-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-brand-700" href="#appointments">
            Book a visit
          </a>
        </nav>
      </header>

      <main>
        <section className="mx-auto grid max-w-6xl gap-10 px-6 py-16 md:grid-cols-[1.2fr_0.8fr] md:items-center md:py-24">
          <div>
            <p className="mb-4 text-sm font-semibold uppercase tracking-[0.16em] text-brand-700">Care that fits your life</p>
            <h1 className="max-w-2xl text-4xl font-bold tracking-tight sm:text-5xl">Your health, made easier.</h1>
            <p className="mt-5 max-w-xl text-lg leading-8 text-slate-600">
              Find trusted care, book appointments, and stay connected with your clinic—all in one place.
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <a className="rounded-lg bg-brand-600 px-5 py-3 font-semibold text-white transition hover:bg-brand-700" href="#appointments">Get started</a>
              <a className="rounded-lg border border-slate-300 bg-white px-5 py-3 font-semibold text-slate-700 transition hover:bg-slate-100" href="#services">Explore services</a>
            </div>
          </div>
          <div className="rounded-3xl bg-brand-100 p-8 sm:p-10">
            <div className="rounded-2xl bg-white p-6 shadow-sm">
              <div className="flex items-start justify-between gap-4">
                <div>
                  <p className="text-sm font-medium text-slate-500">A little reminder</p>
                  <h2 className="mt-2 text-xl font-semibold">Your care starts here</h2>
                </div>
                <span className="grid h-11 w-11 place-items-center rounded-full bg-brand-50 text-xl text-brand-700" aria-hidden="true">♡</span>
              </div>
              <p className="mt-4 leading-7 text-slate-600">Set up a visit with our care team in just a few simple steps.</p>
              <a className="mt-6 inline-flex font-semibold text-brand-700 hover:text-brand-800" href="#appointments">See appointment options <span className="ml-2" aria-hidden="true">→</span></a>
            </div>
          </div>
        </section>

        <section id="services" className="border-t border-slate-200 bg-white">
          <div className="mx-auto max-w-6xl px-6 py-16">
            <div className="max-w-xl">
              <p className="text-sm font-semibold uppercase tracking-[0.16em] text-brand-700">Here for you</p>
              <h2 className="mt-3 text-3xl font-bold tracking-tight">A simpler way to get care</h2>
            </div>
            <div className="mt-9 grid gap-5 md:grid-cols-3">
              {services.map((service) => (
                <article key={service.title} className="rounded-2xl border border-slate-200 p-6">
                  <span className="grid h-11 w-11 place-items-center rounded-xl bg-brand-50 text-xl font-bold text-brand-700" aria-hidden="true">{service.icon}</span>
                  <h3 className="mt-5 text-lg font-semibold">{service.title}</h3>
                  <p className="mt-2 leading-7 text-slate-600">{service.description}</p>
                </article>
              ))}
            </div>
          </div>
        </section>

        <section id="appointments" className="mx-auto max-w-6xl px-6 py-16">
          <div className="rounded-3xl bg-slate-900 px-7 py-10 text-white sm:px-10">
            <h2 className="text-2xl font-bold">Ready to get started?</h2>
            <p className="mt-2 text-slate-300">Appointment booking will be available here soon.</p>
          </div>
        </section>
      </main>

      <footer className="border-t border-slate-200 bg-white">
        <div className="mx-auto max-w-6xl px-6 py-6 text-sm text-slate-500">© {new Date().getFullYear()} CarePoint Clinic</div>
      </footer>
    </div>
  )
}

export default App
