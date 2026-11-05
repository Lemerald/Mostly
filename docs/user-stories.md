# User Stories — Mostly (student timetable/calendar/to-do app)

## Decisions (fixed, don't relitigate without user sign-off)

- **Stack:** Kotlin + Jetpack Compose. Native Android.
- **Data / sync:** Events live in the Android **CalendarProvider** (`CalendarContract`). CalDAV synchronization is delegated to **DAVx5** (separate sync app). The app must not implement its own CalDAV layer or duplicate event storage. The app creates and manages its own local calendar(s) in the provider.
- **Tasks storage:** RFC 5545 defines `VTODO`, and CalDAV + DAVx5 can sync it (via the Tasks.org provider). Sync is **supported, not required**: tasks' source of truth is the app's own Room DB so the app works fully offline; a DAVx5-backed sync bridge (via the Tasks.org provider) is a backlog story (US-5.3). `.ics` exports include `VTODO`s.
- **Timetable model: terms only.** One schedule set per term (school year/semester), plus a free-day list. **No A/B / alternating weeks.**
- **MVP scope:** Epic 1 (Calendar) + Epic 2 (To-Dos, incl. course tasks) + Epic 3-basic (courses & timetable basics).
- **App views:** exactly these top-level views — `Calendar` (default, events + due tasks), `To-dos` (task list grouped by due-date buckets), `Timetable` (static weekly grid of course slots).

## Tooling/Syntax used

- **Story format:** As a student, I want … so that …
- **Priorities:** Must = MVP; Should = post-MVP, strongly wanted; Could = nice-to-have; Won't (now) = explicitly out of scope.
- **Acceptance criteria:** written as Given/When/Then, testable.

## Resolved former open questions

1. **Task storage:** own Room DB as source of truth; DAVx5/Tasks.org sync as optional bridge (US-5.3); `VTODO` in exports. Strictly local/offline always possible.
2. **Min SDK:** decided during scaffold (see AGENTS.md toolchain section) — target covers notification channels + Compose floor.
3. **Editing generated events:** covered as a Must story (US-3.6) — warn + redirect offer, user keeps agency, standard recurrence edit semantics otherwise.

---

## Epic 1 — Calendar core

### US-1.1 (Must) View calendar by day/week/month
As a student, I want to switch between day, week, and month views so that I can see what's ahead at the granularity I need.
- Given a week view, When I swipe, Then the view moves to the adjacent week.
- Given any view, When I tap "today", Then the view scrolls to today.
- Given the month view, When I tap a day, Then I see that day's events (day view) with times.

### US-1.2 (Must) Show all selected on-device calendars
As a student, I want to see combined events from all on-device calendars (including my CalDAV-synced ones via DAVx5) so that one app shows everything.
- Given the user has a CalDAV account synced by DAVx5, When I open the calendar, Then its events appear alongside local events.
- Given I deselect a calendar in settings, When I re-open the calendar view, Then that calendar's events are hidden from all views.

### US-1.3 (Must) Create, edit, delete events
As a student, I want to create, edit, and delete events with title, date, time, location, and note so that my schedule is accurate.
- Given an event form, When I save without a title, Then save is rejected with a visible hint.
- Given an existing event, When I long-press it in the day view, Then I get edit/delete options.
- When deleting, Then the app asks for confirmation.

### US-1.4 (Must) Recurring events
As a student, I want recurring events (e.g. "every Monday") so that I don't re-enter them.
- Given I choose "weekly on Monday", Then one event with a weekly RRULE is stored, not 15 copies.
- Given a recurring series, When I edit one instance, Then I can choose "this event only" or "whole series".

### US-1.5 (Must) Reminders and notifications
As a student, I want event reminders so that I don't miss classes or deadlines.
- Given an event has a 10-minute reminder, When the time arrives, Then a notification appears (with notification-permission handling on first launch).
- Given the device restarts, When a reminder time comes around, Then the reminder still fires.

### US-1.6 (Should) All-day events, visually distinct
As a student, I want all-day events without start/end times, shown distinctly and without crowding out timed events.
- Given an all-day event, When rendered in day/week/month view, Then it uses a distinct style (no time label, separate top band) that must not occupy the whole day area.
- Given a day with 2 all-day and 3 timed events, When viewed, Then all timed events are fully visible and interactive alongside the all-day band.

---

## Epic 2 — To-Dos

### US-2.1 (Must) Create tasks with due dates
As a student, I want to create a task with a title and an optional due date (and time) so that deadlines are tracked.
- Given a task without due date, When created, Then it appears under the "No due date" group (see US-2.4).

### US-2.2 (Must) Complete, hide-by-default, and reveal completed tasks
As a student, I want to check off tasks, with completed ones hidden by default and a non-default "show completed" toggle so that my to-do views stay clean but history is reachable.
- Given a task freshly completed, When I look at the To-dos view or any due-date group, Then the task is hidden by default.
- Given the "show completed" toggle is on, When I look again, Then completed tasks appear (sorted by completion date), grouped under their original due-date bucket.
- Given the default state, When I open the To-dos view, Then the "show completed" toggle is off.

### US-2.3 (Must) Tasks appear in the default Calendar view
As a student, I want tasks with due dates shown in the calendar views so that I see deadlines next to events.
- Given a task due Oct 12, When I view Oct 12, Then the task appears in that day's list, visually distinct from events (and colored by course if assigned, see US-3.1).

### US-2.4 (Must) Dedicated To-dos view grouped by due-date buckets
As a student, I want a separate To-dos view listing upcoming tasks grouped by due date — Today, Tomorrow, End of week, Later, No due date — so that I can plan my workload without reading a calendar.
- Given tasks due today, tomorrow, within the current week, later than this week, and without a due date, When I open the To-dos view, Then each appears under its correct bucket header.
- Given a task due tomorrow at 08:00, When today passes, Then it automatically appears under "Today" on the next day.
- Given the "show completed" toggle in this view, When off (default), Then no completed tasks are shown in any bucket.

### US-2.5 (Must) Tasks can belong to a course (homework aid)
As a student, I want to assign a task to a course when creating it, with smart due-date presets, so that tracking homework is quick.
- Given a course exists, When I create a task, Then I can assign the course; the task then uses the course color.
- Given an assigned course with slots, When I pick a due date, Then I can choose any course slot as reference, with deadline presets "finishing before the slot starts" or "the day before the slot".
- Alongside the presets, I can always enter an arbitrary date/time.
- Given a task assigned to a course, When it is shown (To-dos, Calendar), Then it is visually colored/tagged with that course.

---

## Epic 3 — Courses & timetable (basic = MVP)

### US-3.1 (Must) Define courses
As a student, I want to define a course with name, teacher, room, and color so that it can be reused across terms.
- Given an existing course, When I set its color, Then all events generated for it **and all tasks assigned to it** (US-2.5) use that color.

### US-3.2 (Must) Courses connect to terms; slots are defined per term
As a student, I want to connect a course to one or more terms and define its weekly time slots **per term** (weekday + start/end time) so that schedules follow the term structure.
- Given a course connected to zero terms, When I try to add a slot, Then it is not possible — the app asks me to first connect the course to a term.
- Given a course connected to terms T1 and T2, When I add a slot, Then I must pick at least one of (T1, T2) for that slot.
- Given the same course with different slot patterns in T1 and T2, When I save, Then both term-specific patterns exist independently.

### US-3.3 (Must) Terms with start/end dates
As a student, I want to define a term (start and end date) so that a new term replaces the old schedule without losing past events.
- Given term "2026/27" from 2026-08-10 to 2027-07-31, When I generate its schedule, Then generated events stay inside that date range.
- Given a past term, When I generate a new term's schedule, Then past generated events are not deleted or altered.

### US-3.4 (Must) Free days (holidays/breaks), before or after generation
As a student, I want to mark free days within a term at any time so that no timetable events are generated for those days — including retroactively after events already exist.
- Given a free day set before generation, When generating, Then no slot events exist on that day.
- Given events already generated, When I add a free day, Then regeneration reports (preview) deleting the generated events on that day, requiring confirmation before deletion.
- Given a free day is set, When I remove it, Then regeneration offers to create the suppressed events again (only future ones or with explicit confirmation for past dates).

### US-3.5 (Must) Generate calendar events from slots (preview-based regeneration)
As a student, I want to materialize timetable slots as calendar events for a term (with preview) so that my calendar is populated without manual entry.
- Given 1 course × 2 slots × a 10-month term, When I generate, Then events occur only on matching weekdays within term range, minus free days.
- Given I trigger generation, Then a preview lists what would be created/changed/deleted before anything is written to the provider.
- Given generated events, Then they live in the app's own managed calendar so they can be identified and regenerated later.

### US-3.6 (Must) Editing generated events: warn, offer redirect, respect user choice
As a student, I want to be warned when I edit a generated timetable event and offered a redirect to the relevant course/slot, while keeping full editing agency.
- Given I start editing a generated event, Then the app warns that it belongs to the timetable and offers to open the responsible course/slot editor instead.
- Given I decline the redirect, When I edit, Then normal semantics apply — for recurring/serialized events, "this occurrence" vs "whole series" handling per US-1.4.
- Given user-made edits to generated events, When I later regenerate, Then the preview shows them and regeneration never silently discards explicit user edits.

### US-3.7 (Should) Timetable as a dedicated view
As a student, I want a static weekly grid view of my course slots as one of the app's top-level views so that I can glance at my timetable, separate from the actual calendar.
- Given a term with slots, When I open the Timetable view, Then the week grid shows slots grouped by course color even on holidays (it's data-based, not event-based).

---

## Epic 5 — Data safety

### US-5.1 (Should) Export as .ics including VTODOs (+ app-specific JSON)
As a student, I want to export my data in standard `.ics` format — containing both `VEVENT` and `VTODO` components — plus optionally an app-specific JSON for course/slot/term structures, so that I can back up or migrate.
- Given exported .ics, When opened in another calendar app, Then events and tasks are standard RFC 5545 components.
- Given the app-specific JSON is exported, When re-imported, Then courses, term links, slots and free days are restored.
- Given only the .ics is exported, When imported anywhere, Then nothing references courses (plain events/tasks).

### US-5.2 (Should) Import .ics (VEVENT + VTODO)
As a student, I want to import `.ics` files so that I can bring in external calendars.
- Given an .ics with VEVENTs and VTODOs, When imported, Then they become regular events and tasks — not linked to any course ("non-school" data).
- Given a VTODO component, When imported, Then it appears as a task with the mapped due date.

### US-5.3 (Should) DAVx5-backed task sync bridge
As a student, I want my Room-stored tasks pushed to the Tasks.org content provider so that DAVx5 can sync them as CalDAV VTODOs — while the app still works fully offline without it.
- Given Tasks.org provider is present, When I create/update/complete a task, Then it is reflected in the provider for DAVx5 to sync.
- Given Tasks.org is absent, When I use the app, Then all task features work purely locally with no degradation.

---

## Explicitly out of scope for now (Won't)

- Own CalDAV implementation or own sync engine — DAVx5 owns sync.
- A/B-week / alternating schedules (model is terms only).
- Accounts, own backend, Firebase/Supabase.
- Grades/homework-and-exam *management* beyond course-tagged tasks (E4) — revisit after MVP.
- Google Calendar *API* integration — power users can just add the Google account on device; DAVx5 handles standard CalDAV.
