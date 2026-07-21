# What changed in this update

## New design system
`styles/theme.css` — shared across all dashboard pages (sidebar layout, cards,
tables, buttons, chat UI, schedule grid, AI-flavored panels). Each page keeps
a small page-specific CSS file (`director.css`, `student.css`,
`professor.css`) just for accent colors — everything structural lives in
`theme.css` so the three dashboards look consistent.

`admin.html`/`admin.css`/`login.html` were **not touched** — still your
original files. Worth migrating to `theme.css` next for full consistency,
happy to do that when you're ready.

## Director dashboard (`director.html`)
Sidebar + 5 sections: Overview (stat cards), Filières (live, calls your real
`/admin/filiere` endpoint), Professors, Students, and an **AI & ML Insights**
panel — this is the "what to build next" roadmap from your project spec,
rendered as toggle cards (all disabled/greyed out, since nothing's wired to
an LLM yet). Purely visual placeholders you can flip on as you build each
feature.

## Student dashboard (`student.html`)
Sidebar + 4 sections: Dashboard (quick stats), **Emploi du temps** (the
Mon–Sat × Morning/Afternoon grid we designed earlier, reads from
`getWeeklyScheduleAsMap()`'s shape), **Modules & Résumés** (grades table per
module + a `courseSummary` panel, matching your `Module`/`Grade` entities),
and **Assistant IA** — a real chat UI (thread list + message bubbles + input)
wired to call your `Conversation`/`Message` entities, currently showing a
"not connected yet" message since there's no LLM behind it.

## Professor dashboard (`professor.html`)
Sidebar + 4 sections: My Modules, Grades entry, Attendance, and an AI Tools
panel (course outline generator, quiz generator, PDF summarizer — all
labeled "bientôt", matching your spec's Phase 7).

---

# Backend endpoints that still need to be built

The frontend calls these — right now they'll fail gracefully (empty-state
message shown instead of a crash) since they don't exist yet:

| Endpoint | Used by | Notes |
|---|---|---|
| `GET /api/v1/admin/professor` | Director → Professors | List all professors |
| `GET /api/v1/admin/student` | Director → Students | List all students |
| `GET /api/v1/student/schedule/{filiereId}` | Student → Emploi du temps | Should return the shape from `Filiere.getWeeklyScheduleAsMap()` |
| `GET /api/v1/student/modules` | Student → Modules, AI assistant | Each module + the logged-in student's `Grade` for it |
| `GET /api/v1/student/conversations/{moduleId}` | Student → Assistant IA | List of `Conversation` for this student+module |
| `POST /api/v1/student/conversations/{moduleId}/ask` | Student → Assistant IA | Body: `{conversationTitle, question}` → should call FastAPI/LLM, save via `Conversation.addMessage()`, return `{response}` |
| `GET /api/v1/professor/modules` | Professor → Mes modules | Modules where `teacher` = logged-in professor |

All the URLs/shapes above are guesses based on your existing patterns
(`/admin/filiere`) — adjust `utile.js`'s helper functions once you decide the
real paths.

## One more thing worth doing soon
None of these new routes are protected by role in `SecurityConfiguration`
yet beyond the general `authenticated()` catch-all. Once you build the
controllers above, add the `hasAuthority(...)` matchers we discussed earlier
so a Student can't hit `/admin/**` and a Director can't hit `/student/**`.
