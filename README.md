# Dashboard Starter Template — Angular + Spring Boot + MySQL

A ready-to-run starter so you can begin building your dashboard immediately
instead of setting up the project from scratch.

**What's already done for you:**
- Angular 21 app with the Cranes Varsity dashboard theme wired up (header, sidebar, footer)
- A login page (no validation — click Sign In and you're in)
- An **Overview** page with dummy stat cards and a dummy table
- A Spring Boot API with a working `Student` CRUD example + MySQL
- CORS already configured between the two

---

## 1. What you need installed

| Tool | Version | Check with |
|---|---|---|
| Node.js | 20+ | `node -v` |
| Java JDK | 17+ | `java -version` |
| MySQL | 8.x (**not 5.5**) | `mysql --version` |

**You do NOT need to install Maven.** This project includes the Maven Wrapper (`mvnw.cmd`),
which downloads Maven for you on first run.

> ### ⚠ You must set `JAVA_HOME`
> `mvnw` fails with *"JAVA_HOME not found in your environment"* if this isn't set.
>
> **Windows (permanent):** Start → search *"Edit the system environment variables"* → **Environment Variables**
> → under *System variables* click **New**:
> - Variable name: `JAVA_HOME`
> - Variable value: your JDK folder, e.g. `C:\Program Files\Java\jdk-17.0.18`
>
> Then **open a new terminal** and check: `echo %JAVA_HOME%`
>
> **Just for the current PowerShell window (quick test):**
> ```powershell
> $env:JAVA_HOME = 'C:\Program Files\Java\jdk-17.0.18'
> ```

---

## 2. Run the backend (Spring Boot)

```bash
cd backend
```

**a) Create the database** (once):
```bash
mysql -u root -p < db/init.sql
```

**b) Set your MySQL username/password** in
`src/main/resources/application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=root      <-- change to your MySQL password
```

**c) Start it** (use the wrapper — no Maven install needed):
```powershell
# Windows
.\mvnw.cmd spring-boot:run
```
```bash
# Mac / Linux
./mvnw spring-boot:run
```

> Prefer an IDE? Open the `backend` folder in **IntelliJ IDEA** and click ▶ on
> `TemplateApplication.java`. IntelliJ bundles its own Maven, so this works too.

The API runs at **http://localhost:8080/api**

Test it in the browser: <http://localhost:8080/api/students> — you should see 5 dummy students
(they are seeded automatically on first run).

---

## 3. Run the frontend (Angular)

Open a **second terminal**:

```bash
cd frontend
npm install
npm start
```

Open **http://localhost:4200** → you land on the login page → click **Sign In** → you see the Overview dashboard.

---

## 4. Project structure

```
intern-template/
├── frontend/                     Angular app
│   ├── src/index.html            <-- theme data-* attributes live here (DO NOT DELETE)
│   ├── src/environments/         API URL config (dev + prod)
│   └── src/app/
│       ├── app.routes.ts         <-- ADD YOUR PAGES HERE
│       ├── layout/
│       │   ├── header/           top purple bar + logo
│       │   ├── sidebar/          <-- ADD YOUR MENU ITEMS HERE
│       │   ├── footer/
│       │   └── main-layout/      shell = header + sidebar + footer
│       └── modules/
│           ├── auth/login/       login page (no validation)
│           └── dashboard/overview/   <-- sample page with dummy data
│
└── backend/                      Spring Boot API
    ├── db/init.sql               creates the database
    └── src/main/
        ├── resources/application.properties   <-- DB settings
        └── java/com/cranesvarsity/template/
            ├── TemplateApplication.java       entry point
            ├── config/CorsConfig.java         allows localhost:4200
            ├── config/DataSeeder.java         inserts dummy rows (delete later)
            ├── model/Student.java             <-- COPY THIS for your entities
            ├── repository/StudentRepository.java
            ├── service/StudentService.java
            └── controller/StudentController.java   <-- COPY THIS for your endpoints
```

The backend layering is **Controller → Service → Repository → Database**. Keep controllers thin.

---

## 5. How to add a new page (the thing you'll do most)

**Step 1 — create the component** in `frontend/src/app/modules/`:

`students/students.ts`
```ts
import { Component } from '@angular/core';

@Component({
  selector: 'app-students',
  imports: [],
  templateUrl: './students.html',
})
export class Students {}
```

**Step 2 — register the route** in `frontend/src/app/app.routes.ts`, as a child of `MainLayout`:
```ts
{ path: 'students', component: Students },
```

**Step 3 — add the sidebar link** in `frontend/src/app/layout/sidebar/sidebar.html`:
```html
<li routerLinkActive="mm-active">
  <a routerLink="/students">
    <i class="bi bi-people"></i>
    <span class="nav-text">Students</span>
  </a>
</li>
```

That's it — the page now appears in the menu with the theme applied.

---

## 6. How to call the API from Angular

See `modules/dashboard/overview/overview.ts` for a complete working example.

```ts
constructor(private http: HttpClient) {}

load(): void {
  this.loading = true;

  this.http.get<Student[]>(`${environment.apiUrl}/students`).subscribe({
    next: (data) => {
      this.students = data;      // the screen updates automatically
      this.loading = false;
    },
    error: (err) => {
      console.error(err);
      this.loading = false;
    },
  });
}
```

Two things to remember:

1. **Always handle the `error` case.** If the backend is down, `next` never runs — without an
   `error` handler your page silently stays on "Loading…".
2. **`subscribe()` is when the request actually fires.** An HTTP call with no `subscribe()`
   does nothing at all.

This project uses standard Angular change detection (zone.js), so the screen refreshes on its own
after HTTP calls, timers and click events. You do **not** need to call `detectChanges()`.

---

## 7. Common problems

| Symptom | Cause / Fix |
|---|---|
| Logo missing in top-left | The `data-*` attributes were removed from `<body>` in `src/index.html`. Put them back. |
| "CORS error" / `0 Unknown Error` | The HTTP method isn't allowed in `CorsConfig.java`, or the backend isn't running. |
| Page stuck on "Loading…" | The request failed and you have no `error` handler in `subscribe()`. Check the browser Network tab. |
| `JAVA_HOME not found in your environment` | Set the `JAVA_HOME` variable — see section 1. |
| `Communications link failure` on backend start | MySQL isn't running, or wrong password in `application.properties`. |
| `Unsupported server version` / driver errors | Your MySQL is older than 5.6. The driver needs **MySQL 8**. |
| `Unknown database 'intern_template'` | You skipped `db/init.sql`. |
| Sidebar menu won't expand | Only happens for `has-arrow` submenus — the theme JS handles this; make sure you didn't remove the scripts in `angular.json`. |

---

## 8. Ground rules

- **Don't edit** `src/assets/dashboard/**` — that's the purchased theme. Put your own CSS in `src/styles.css` or the component's own `.css`.
- Keep the layering: Controller → Service → Repository. Don't put SQL in controllers.
- Delete `DataSeeder.java` and the dummy arrays in `overview.ts` once you have real data.
- Commit early, commit often. Ask before changing anything in `layout/`.
