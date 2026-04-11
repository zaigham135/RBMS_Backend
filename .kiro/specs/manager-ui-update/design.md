# Design Document: Manager UI Update

## Overview

This document describes the technical design for the Manager UI Update feature in the TaskMan application. The goal is to replace the current NexusUI-based manager pages with a production-quality, data-driven UI that covers four pages: Manager Dashboard, Manager Projects, Manager Tasks, and Manager Team.

The redesign introduces:
- A consistent `ManagerTopBar` component with breadcrumb, search, notification bell, and user profile
- A cleaned-up Sidebar with exactly four manager nav items and a live task count badge
- Reusable `StatCard`, `ProgressBar`, `WorkloadLabel`, `ViewToggle` components
- Recharts-based `TaskCompletionTrendChart` and `WorkloadDistributionChart`
- New table designs for projects, tasks, and team roster
- Kanban/Board view toggles for projects and tasks
- Five new backend endpoints under `/api/manager/` for dashboard stats, activity, task completion trend, paginated tasks with filters, and team stats
- New TypeScript types, a `managerDashboardService`, and a `useManagerDashboard` hook

All data flows from the Java Spring Boot backend through Axios-based services, into custom React hooks, and down to page components via props.

---

## Architecture

### High-Level Data Flow

```
Spring Boot Backend
  └── ManagerController (new endpoints)
        └── ManagerDashboardService (new Java service)
              └── existing repositories (ProjectRepository, TaskRepository, UserRepository, ActivityLogRepository)

Next.js Frontend
  └── managerDashboardService.ts  (HTTP calls via api.ts Axios instance)
        └── useManagerDashboard.ts  (React hook, aggregates all dashboard data)
              └── /manager/page.tsx  (Manager Dashboard page)

  └── existing services (projectService, taskService, userService)
        └── existing hooks (useProjects, useTasks, useUsers)
              └── /manager/projects/page.tsx
              └── /manager/tasks/page.tsx
              └── /manager/team/page.tsx
```

### Component Hierarchy

```
ManagerLayout (layout.tsx)
  ├── Sidebar (updated: 4 manager nav items + task badge)
  └── main
        ├── ManagerTopBar (new: breadcrumb + search + bell + profile)
        └── page content
              ├── /manager          → ManagerDashboard
              ├── /manager/projects → ManagerProjectsPage
              ├── /manager/tasks    → ManagerTasksPage
              └── /manager/team     → ManagerTeamPage
```

### Shared Component Tree (new/updated)

```
components/
  common/
    Sidebar.tsx              ← updated managerNav (4 items) + task badge
    StatusBadge.tsx          ← updated color mapping (project + task statuses)
    PriorityBadge.tsx        ← color mapping already correct, minor cleanup
    ManagerTopBar.tsx        ← NEW
    StatCard.tsx             ← NEW (replaces NexusUI StatCard for manager pages)
    ProgressBar.tsx          ← NEW standalone (extracted from NexusUI)
    WorkloadLabel.tsx        ← NEW
    ViewToggle.tsx           ← NEW
  charts/
    TaskCompletionTrendChart.tsx  ← NEW (recharts LineChart)
    WorkloadDistributionChart.tsx ← NEW (recharts BarChart)
  manager/
    ProjectsTable.tsx        ← NEW
    KanbanBoard.tsx          ← NEW
    TasksTable.tsx           ← NEW
    BoardView.tsx            ← NEW
    TeamRosterTable.tsx      ← NEW
```

---

## Components and Interfaces

### ManagerTopBar

Replaces the per-page `NexusTopbar` usage on manager pages with a single consistent component.

```tsx
interface ManagerTopBarProps {
  pageName: string;           // e.g. "Dashboard", "My Projects"
  searchPlaceholder?: string;
  hasUnreadNotifications?: boolean;
}
```

Renders: `Management > {pageName}` breadcrumb (hidden on mobile < 768px), search input, notification bell with optional red dot, user avatar + name + role linking to `/manager/account`.

Reads `name`, `profilePhoto` from `useAuth()` internally.

### Sidebar (updated)

The `managerNav` array is replaced with exactly four items:

```ts
const managerNav: NavItem[] = [
  { label: "Manager Dashboard", href: "/manager",          icon: <Grid2x2 /> },
  { label: "Manager Projects",  href: "/manager/projects", icon: <FolderKanban /> },
  { label: "Manager Tasks",     href: "/manager/tasks",    icon: <ClipboardList />, badge: true },
  { label: "Manager Team",      href: "/manager/team",     icon: <Users /> },
];
```

The `badge: true` item fetches the active task count from `GET /api/manager/tasks?status=TODO&status=IN_PROGRESS&size=1` on mount (using the `totalElements` from the paginated response). Displays the count as a pill; shows `"99+"` when count > 99.

### StatCard (new standalone)

```tsx
interface StatCardProps {
  icon: LucideIcon;
  label: string;
  value: string | number;
  trend?: string;           // e.g. "+2 this month"
  trendPositive?: boolean;  // controls trend color (green vs red)
  isLoading?: boolean;
}
```

### ProgressBar (new standalone)

```tsx
interface ProgressBarProps {
  value: number;            // 0–100, clamped internally
  colorByValue?: boolean;   // if true: green >= 75, amber 40-74, red < 40
  className?: string;
  showLabel?: boolean;
}
```

### WorkloadLabel

```tsx
interface WorkloadLabelProps {
  workload: number;  // 0–100
}
// Returns: "High" (>= 75), "Optimal" (40–74), "Low" (< 40)
// Color:   red,            amber,              green
```

### ViewToggle

```tsx
type ViewMode = "list" | "kanban" | "board";

interface ViewToggleProps {
  mode: ViewMode;
  options: { value: ViewMode; label: string; icon: LucideIcon }[];
  onChange: (mode: ViewMode) => void;
}
```

### TaskCompletionTrendChart

```tsx
interface TaskCompletionTrendChartProps {
  data: TaskCompletionTrendPoint[];  // 30 entries, one per day
  isLoading?: boolean;
}
```

Uses `recharts` `LineChart` with `XAxis` (date labels), `YAxis` (task count), `Tooltip`, `CartesianGrid`, and a single `Line` in `#1557d6`.

### WorkloadDistributionChart

```tsx
interface WorkloadDistributionChartProps {
  data: { name: string; workload: number }[];
  isLoading?: boolean;
}
```

Uses `recharts` `BarChart` with a `ReferenceLine` at y=75 (the "High" threshold).

### ProjectsTable

```tsx
interface ProjectsTableProps {
  projects: ProjectWithStats[];
  isLoading?: boolean;
  onEdit: (project: ProjectWithStats) => void;
  onViewDetails: (project: ProjectWithStats) => void;
  onArchive: (project: ProjectWithStats) => void;
}
```

Columns: PROJECT NAME | STATUS | HEALTH/PROGRESS | TEAM | ACTIONS

### KanbanBoard

```tsx
interface KanbanBoardProps {
  projects: ProjectWithStats[];
  onEdit: (project: ProjectWithStats) => void;
}
```

Groups projects by `status` into columns: Planned | Active | In Review | On Hold.

### TasksTable

```tsx
interface TasksTableProps {
  tasks: Task[];
  isLoading?: boolean;
  onView: (task: Task) => void;
  onEdit: (task: Task) => void;
  onDelete: (task: Task) => void;
}
```

Columns: ☐ | TASK NAME | PROJECT | STATUS | PRIORITY | ASSIGNEE | DUE DATE | ACTIONS

### BoardView

```tsx
interface BoardViewProps {
  tasks: Task[];
  onView: (task: Task) => void;
  onEdit: (task: Task) => void;
}
```

Groups tasks into three columns: TODO | IN_PROGRESS | DONE.

### TeamRosterTable

```tsx
interface TeamRosterTableProps {
  members: UserWithWorkload[];
  activeTab: "all" | "design" | "engineering";
  onTabChange: (tab: "all" | "design" | "engineering") => void;
  onAssign: (member: UserWithWorkload) => void;
  isLoading?: boolean;
}
```

Columns: MEMBER | ROLE | ACTIVE PROJECTS | WORKLOAD | ACTIONS

---

## Data Models

### New TypeScript Types (`src/types/dashboard.ts` additions)

```ts
// Manager dashboard summary stats
export interface ManagerDashboardStats {
  activeProjects: number;
  pendingTasks: number;        // tasks with status TODO
  completedThisWeek: number;   // tasks with status DONE in current ISO week
  teamMembers: number;
  activeProjectsTrend: string; // e.g. "+2 this month"
  pendingTasksTrend: string;
  completedThisWeekTrend: string;
  teamMembersTrend: string;
}

// Single data point for the 30-day trend chart
export interface TaskCompletionTrendPoint {
  date: string;   // "yyyy-MM-dd"
  count: number;
}

// Activity feed entry (mirrors existing AdminDashboardActivity)
export interface ManagerDashboardActivity {
  id: number;
  userName: string;
  userPhoto?: string | null;
  action: string;
  entityType: string;
  entityId: number;
  entityName: string;
  projectId?: number | null;
  projectName?: string | null;
  createdAt: string;
}

// Team-level aggregate stats
export interface TeamStats {
  totalMembers: number;
  avgWorkload: number;          // 0–100
  tasksCompletedThisMonth: number;
  overloadedMembersCount: number; // workload >= 85%
  totalMembersTrend: string;
}
```

### New TypeScript Types (`src/types/project.ts` additions)

```ts
export type ProjectStatus = "ACTIVE" | "IN_REVIEW" | "PLANNED" | "ON_HOLD";

export interface ProjectWithStats extends Project {
  status: ProjectStatus;
  progress: number;          // 0–100, computed from DONE/total tasks
  totalTasks: number;
  doneTasks: number;
  teamMembers: Pick<User, "id" | "name" | "profilePhoto">[];
}
```

### New TypeScript Types (`src/types/user.ts` additions)

```ts
export type Department = "DESIGN" | "ENGINEERING" | "OTHER";

export interface UserWithWorkload extends User {
  workload: number;           // 0–100 percentage
  activeProjects: number;
  activeProjectNames: string[];
  department: Department;
}
```

### New Backend DTOs

**ManagerDashboardStatsResponse.java**
```java
public class ManagerDashboardStatsResponse {
    private int activeProjects;
    private long pendingTasks;
    private long completedThisWeek;
    private int teamMembers;
    private String activeProjectsTrend;
    private String pendingTasksTrend;
    private String completedThisWeekTrend;
    private String teamMembersTrend;
}
```

**TaskCompletionTrendPoint.java**
```java
public class TaskCompletionTrendPoint {
    private String date;   // "yyyy-MM-dd"
    private long count;
}
```

**TeamStatsResponse.java**
```java
public class TeamStatsResponse {
    private int totalMembers;
    private double avgWorkload;
    private long tasksCompletedThisMonth;
    private int overloadedMembersCount;
    private String totalMembersTrend;
}
```

**UserWithWorkloadResponse.java** (extends UserResponse)
```java
public class UserWithWorkloadResponse extends UserResponse {
    private int workload;
    private int activeProjects;
    private List<String> activeProjectNames;
    private String department;
}
```

**ProjectWithStatsResponse.java** (extends ProjectResponse)
```java
public class ProjectWithStatsResponse extends ProjectResponse {
    private String status;
    private int progress;
    private int totalTasks;
    private int doneTasks;
    private List<TeamMemberSummary> teamMembers;

    @Data
    public static class TeamMemberSummary {
        private Long id;
        private String name;
        private String profilePhoto;
    }
}
```

---

## New Backend Endpoints

All endpoints are added to `ManagerController` and secured with `ROLE_MANAGER` via Spring Security.

### GET /api/manager/dashboard/stats

Returns `ManagerDashboardStatsResponse`. The backend:
1. Fetches the authenticated manager's projects via `projectService.getProjectsForManager()`
2. Counts active projects (all returned projects are considered active)
3. Counts pending tasks: `taskRepository.countByProjectIdInAndStatus(projectIds, TaskStatus.TODO)`
4. Counts completed this week: `taskRepository.countByProjectIdInAndStatusAndCreatedAtBetween(projectIds, DONE, weekStart, weekEnd)`
5. Counts team members: `userService.getAllEmployees().size()`
6. Computes trend strings by comparing to the previous period (previous week/month)

### GET /api/manager/dashboard/activity

Query param: `limit` (default 10). Returns `List<ActivityLogResponse>` filtered to the manager's project IDs, ordered by `createdAt DESC`.

### GET /api/manager/tasks/completion-trend

Returns `List<TaskCompletionTrendPoint>` for the last 30 calendar days. The backend queries `taskRepository.countByProjectIdInAndStatusAndDueDateBetween(projectIds, DONE, start, end)` grouped by date, then fills in zeros for missing days.

### GET /api/manager/tasks

Query params: `page`, `size`, `projectId`, `status`, `priority`, `assigneeId`. Returns `Page<TaskResponse>`. This is a manager-scoped version of the existing `/api/tasks` endpoint — it filters to only tasks belonging to the authenticated manager's projects.

### GET /api/manager/team/stats

Returns `TeamStatsResponse`. Workload is computed as: `(activeTasks / capacity) * 100` where capacity is a configurable constant (default 10 tasks = 100%). Overloaded threshold is >= 85%.

### GET /api/manager/employees (updated)

The existing endpoint is updated to return `List<UserWithWorkloadResponse>` instead of `List<UserResponse>`, adding workload, activeProjects, activeProjectNames, and department fields.

---

## New Frontend Services

### managerDashboardService.ts

```ts
// src/services/managerDashboardService.ts
export const managerDashboardService = {
  getStats: (): Promise<ApiResponse<ManagerDashboardStats>> =>
    api.get("/api/manager/dashboard/stats").then(r => r.data),

  getActivity: (limit = 10): Promise<ApiResponse<ManagerDashboardActivity[]>> =>
    api.get("/api/manager/dashboard/activity", { params: { limit } }).then(r => r.data),

  getCompletionTrend: (): Promise<ApiResponse<TaskCompletionTrendPoint[]>> =>
    api.get("/api/manager/tasks/completion-trend").then(r => r.data),

  getManagerTasks: (filters: ManagerTaskFilters): Promise<ApiResponse<PaginatedResponse<Task>>> =>
    api.get("/api/manager/tasks", { params: filters }).then(r => r.data),

  getTeamStats: (): Promise<ApiResponse<TeamStats>> =>
    api.get("/api/manager/team/stats").then(r => r.data),
};
```

---

## New Frontend Hooks

### useManagerDashboard.ts

Aggregates all dashboard data into a single hook to avoid prop drilling and multiple `useEffect` calls in the page component.

```ts
// src/hooks/useManagerDashboard.ts
export function useManagerDashboard() {
  const [stats, setStats] = useState<ManagerDashboardStats | null>(null);
  const [activity, setActivity] = useState<ManagerDashboardActivity[]>([]);
  const [trend, setTrend] = useState<TaskCompletionTrendPoint[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const load = useCallback(async () => {
    setIsLoading(true);
    const results = await Promise.allSettled([
      managerDashboardService.getStats(),
      managerDashboardService.getActivity(),
      managerDashboardService.getCompletionTrend(),
    ]);
    // Each result is handled independently — a failure in one
    // does not prevent the others from rendering.
    // Failed fetches leave the previous state (or null) in place.
    setIsLoading(false);
  }, []);

  useEffect(() => { load(); }, [load]);

  return { stats, activity, trend, isLoading, errors, reload: load };
}
```

The hook uses `Promise.allSettled` so a failure in one fetch does not block the others (satisfying Requirement 1.6).

---

## Page Designs

### Manager Dashboard (`/manager/page.tsx`)

Layout (desktop):
```
ManagerTopBar
─────────────────────────────────────────────────────
[StatCard: Active Projects] [StatCard: Pending Tasks]
[StatCard: Completed Week]  [StatCard: Team Members]
─────────────────────────────────────────────────────
[Quick Actions Panel]
─────────────────────────────────────────────────────
[Assigned Projects (up to 4 cards)]        [Team Workload (up to 5 members)]
─────────────────────────────────────────────────────
[TaskCompletionTrendChart (30 days)]       [Recent Activity Feed]
```

Stat cards use a 2-col grid on `< lg`, 4-col on `>= lg`.

### Manager Projects (`/manager/projects/page.tsx`)

```
ManagerTopBar
─────────────────────────────────────────────────────
Page title + "+ New Project" button
[ViewToggle: List | Kanban]  [Filter]  [Sort]
─────────────────────────────────────────────────────
List mode:   ProjectsTable
Kanban mode: KanbanBoard (columns: Planned | Active | In Review | On Hold)
```

### Manager Tasks (`/manager/tasks/page.tsx`)

```
ManagerTopBar
─────────────────────────────────────────────────────
Page title + "+ Add Task" button
[ViewToggle: List | Board]  [Filter]  [Sort]
─────────────────────────────────────────────────────
List mode:  TasksTable (paginated, page size 10)
Board mode: BoardView (columns: TODO | IN_PROGRESS | DONE)
```

### Manager Team (`/manager/team/page.tsx`)

```
ManagerTopBar
─────────────────────────────────────────────────────
[StatCard: Total Members] [StatCard: Avg Workload]
[StatCard: Tasks Completed] [StatCard: Overloaded]
─────────────────────────────────────────────────────
TeamRosterTable (tabs: All Members | Design | Engineering)
─────────────────────────────────────────────────────
WorkloadDistributionChart (bar chart, reference line at 75%)
```

---

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system — essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Stat card graceful degradation on fetch failure

*For any* stat card whose data fetch fails, the displayed value should be a non-negative number (either the last successfully fetched value or zero) — never `undefined`, `NaN`, or an error string.

**Validates: Requirements 1.6**

---

### Property 2: Workload label is deterministic and exhaustive

*For any* workload percentage value in the range [0, 100], the `getWorkloadLabel` function should return exactly one of "High", "Optimal", or "Low" — specifically "High" when value >= 75, "Optimal" when 40 <= value < 75, and "Low" when value < 40.

**Validates: Requirements 5.4**

---

### Property 3: Workload sort order

*For any* list of team members with workload values, after sorting by workload descending, every member's workload should be greater than or equal to the workload of the next member in the list.

**Validates: Requirements 5.3**

---

### Property 4: Trend chart always has 30 data points

*For any* valid completion trend API response, the normalized data array passed to `TaskCompletionTrendChart` should have exactly 30 entries — one per calendar day — with missing days filled in as zero.

**Validates: Requirements 4.2, 4.5**

---

### Property 5: Progress computation is clamped

*For any* non-negative `doneTasks` count and non-negative `totalTasks` count, `computeProgress(doneTasks, totalTasks)` should return a value in [0, 100]. When `totalTasks` is 0, the result should be 0.

**Validates: Requirements 9.5, 9.6**

---

### Property 6: StatusBadge color mapping is deterministic

*For any* status value in the defined set (`TODO`, `IN_PROGRESS`, `DONE`, `ACTIVE`, `IN_REVIEW`, `PLANNED`, `ON_HOLD`), the `StatusBadge` component should always return the same color class — never a fallback or undefined class.

**Validates: Requirements 9.4, 11.4, 16.1, 16.3**

---

### Property 7: PriorityBadge color mapping is deterministic

*For any* priority value in the defined set (`HIGH`, `MEDIUM`, `LOW`), the `PriorityBadge` component should always return the same color class.

**Validates: Requirements 11.5, 16.2, 16.4**

---

### Property 8: Sidebar task badge displays correct value

*For any* non-negative active task count `n`, the sidebar badge should display `String(n)` when `n <= 99`, and `"99+"` when `n > 99`.

**Validates: Requirements 7.3, 7.4**

---

### Property 9: Sidebar active nav item matches current route

*For any* manager route pathname, exactly one nav item should have the active CSS class, and it should be the item whose `href` matches the current pathname (using exact match for `/manager` and prefix match for sub-routes).

**Validates: Requirements 7.2**

---

### Property 10: Breadcrumb format is always "Management > [Page Name]"

*For any* manager page route, the rendered breadcrumb text should match the pattern `"Management > [non-empty page name]"`.

**Validates: Requirements 8.1**

---

### Property 11: Project filter predicate is sound

*For any* filter criteria (status, due date range) and any list of projects, every project in the filtered result should satisfy all active filter predicates — no project that fails a filter criterion should appear in the output.

**Validates: Requirements 10.7**

---

### Property 12: Kanban grouping is correct

*For any* list of projects, the kanban grouping function should produce groups such that every project in a given group has the same `status` value as the group's key, and every project from the input appears in exactly one group.

**Validates: Requirements 10.2**

---

### Property 13: Activity feed renders at most 10 entries

*For any* activity log array of length `n`, the rendered feed should display exactly `min(n, 10)` entries.

**Validates: Requirements 6.1**

---

### Property 14: Avg workload computation is correct

*For any* non-empty list of team members with workload values, `computeAvgWorkload(members)` should equal `Math.round(sum(workloads) / members.length)`.

**Validates: Requirements 13.4**

---

### Property 15: Overloaded member count uses correct threshold

*For any* list of team members, `countOverloaded(members)` should equal the count of members whose `workload` is >= 85.

**Validates: Requirements 13.6**

---

### Property 16: Department tab filter is sound

*For any* list of team members and any department tab selection other than "All Members", every member displayed in the roster table should have a `department` value matching the selected tab.

**Validates: Requirements 14.3**

---

### Property 17: Workload chart data mirrors roster data

*For any* team data response, the data array passed to `WorkloadDistributionChart` should have the same length as the member list passed to `TeamRosterTable`, and each entry's `workload` value should equal the corresponding member's `workload`.

**Validates: Requirements 15.2, 15.5**

---

### Property 18: Assigned projects section count matches API response

*For any* non-empty list of projects returned by `/api/manager/my-projects`, the number of rendered project cards in the Assigned Projects section should equal `min(projects.length, 4)`, and a "View All" link should be present if and only if `projects.length > 4`.

**Validates: Requirements 3.1, 3.3**

---

## Error Handling

### Frontend

- All service calls are wrapped in `try/catch`. Errors are stored in hook state as `string | null`.
- The `useManagerDashboard` hook uses `Promise.allSettled` so partial failures degrade gracefully — each stat card independently shows a zero placeholder if its fetch failed.
- Loading states: each section shows a skeleton UI (gray animated placeholder blocks) while data is in flight.
- Toast notifications (via `sonner`) are shown for mutation errors (create/update/delete task or project).
- Empty states: dedicated empty state components are rendered when API returns an empty array (Assigned Projects, Recent Activity, Team Roster).

### Backend

- All new endpoints return `ApiResponse<T>` with `status: "success" | "error"` and a `message` field.
- `@ControllerAdvice` global exception handler catches `AccessDeniedException`, `EntityNotFoundException`, and generic `Exception`, returning appropriate HTTP status codes (403, 404, 500).
- The manager's project scope is enforced by fetching the authenticated user's ID from `SecurityContextHolder` and filtering all queries to that manager's projects — preventing cross-manager data leakage.

---

## Testing Strategy

### Unit Tests

Focus on pure utility functions and component rendering with specific examples:

- `getWorkloadLabel(value)` — test all three branches with boundary values (39, 40, 74, 75)
- `computeProgress(done, total)` — test with (0,0), (5,10), (10,10), (11,10)
- `computeAvgWorkload(members)` — test with single member, multiple members
- `countOverloaded(members)` — test with threshold boundary (84, 85, 86)
- `normalizeTrendData(raw, 30)` — test that missing days are filled with 0
- `StatusBadge` — render with each status value, assert correct class
- `PriorityBadge` — render with each priority value, assert correct class
- `WorkloadLabel` — render with boundary values, assert correct label and color
- `ManagerTopBar` — render with a route, assert breadcrumb format
- `Sidebar` — render with MANAGER role, assert exactly 4 nav items

### Property-Based Tests

Using **fast-check** (already compatible with Jest/Vitest in Next.js projects). Each property test runs a minimum of 100 iterations.

```
// Feature: manager-ui-update, Property 2: Workload label is deterministic and exhaustive
fc.assert(fc.property(fc.integer({ min: 0, max: 100 }), (workload) => {
  const label = getWorkloadLabel(workload);
  return ["High", "Optimal", "Low"].includes(label);
}));

// Feature: manager-ui-update, Property 5: Progress computation is clamped
fc.assert(fc.property(
  fc.nat(), fc.nat(),
  (done, total) => {
    const result = computeProgress(done, total);
    return result >= 0 && result <= 100;
  }
));

// Feature: manager-ui-update, Property 8: Sidebar task badge
fc.assert(fc.property(fc.nat(), (count) => {
  const badge = formatTaskBadge(count);
  return count <= 99 ? badge === String(count) : badge === "99+";
}));
```

Full property test suite covers Properties 1–18 as defined above.

### Integration Tests

- Mock `/api/manager/dashboard/stats` and verify `useManagerDashboard` populates all four stat values
- Mock `/api/manager/tasks` with `status=TODO` and verify pending task count is set
- Mock `/api/manager/dashboard/activity` and verify activity feed renders correct number of entries
- Mock `/api/manager/tasks/completion-trend` and verify chart receives 30 data points

### Visual / Responsive Tests

Responsive layout requirements (Requirement 17) are verified via manual testing at 375px, 768px, and 1280px viewport widths, and optionally via Playwright screenshot comparisons.