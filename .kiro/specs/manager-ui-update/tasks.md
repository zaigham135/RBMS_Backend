# Implementation Tasks — Manager UI Update

## Tasks

- [ ] 1. Backend: New DTOs and Entity Update
  - [ ] 1.1 Add `department` field to `User` entity
  - [ ] 1.2 Create `ManagerDashboardStatsResponse` DTO
  - [ ] 1.3 Create `TaskCompletionTrendPoint` DTO
  - [ ] 1.4 Create `ManagerTeamStatsResponse` DTO
  - [ ] 1.5 Create `EmployeeWithWorkloadResponse` DTO

- [ ] 2. Backend: New Service Methods
  - [ ] 2.1 Add `getManagerDashboardStats()` to `ProjectService` / new manager service
  - [ ] 2.2 Add `getManagerDashboardActivity(int limit)` to `ActivityLogService`
  - [ ] 2.3 Add `getTaskCompletionTrend(int days)` to `TaskService`
  - [ ] 2.4 Add `getManagerTasks(page, size, filters)` to `TaskService`
  - [ ] 2.5 Add `getManagerTeamStats()` to `UserService`
  - [ ] 2.6 Update `getAllEmployees()` to return `EmployeeWithWorkloadResponse`

- [ ] 3. Backend: Update `ManagerController` with new endpoints
  - [ ] 3.1 Add `GET /api/manager/dashboard/stats`
  - [ ] 3.2 Add `GET /api/manager/dashboard/activity`
  - [ ] 3.3 Add `GET /api/manager/tasks/completion-trend`
  - [ ] 3.4 Add `GET /api/manager/tasks` (manager-scoped paginated tasks)
  - [ ] 3.5 Add `GET /api/manager/team/stats`
  - [ ] 3.6 Update `GET /api/manager/employees` to return extended DTO

- [ ] 4. Frontend: New TypeScript Types
  - [ ] 4.1 Create `types/managerDashboard.ts`
  - [ ] 4.2 Create `types/managerTeam.ts`
  - [ ] 4.3 Add `ProjectWithStats` to `types/project.ts`

- [ ] 5. Frontend: New Service and Hook
  - [ ] 5.1 Create `services/managerDashboardService.ts`
  - [ ] 5.2 Create `hooks/useManagerDashboard.ts`

- [ ] 6. Frontend: Shared Component Updates
  - [ ] 6.1 Update `Sidebar.tsx` — clean manager nav (4 items) + task count badge
  - [ ] 6.2 Update `StatusBadge.tsx` — new color mappings
  - [ ] 6.3 Update `PriorityBadge.tsx` — new color mappings
  - [ ] 6.4 Create `components/common/WorkloadBar.tsx`

- [ ] 7. Frontend: New Manager Components
  - [ ] 7.1 Create `components/manager/ManagerTopBar.tsx`
  - [ ] 7.2 Create `components/manager/StatCard.tsx`
  - [ ] 7.3 Create `components/manager/QuickActionsPanel.tsx`
  - [ ] 7.4 Create `components/manager/AssignedProjectsSection.tsx`
  - [ ] 7.5 Create `components/manager/TaskCompletionTrendChart.tsx`
  - [ ] 7.6 Create `components/manager/TeamWorkloadSection.tsx`
  - [ ] 7.7 Create `components/manager/RecentActivityFeed.tsx`
  - [ ] 7.8 Create `components/manager/ViewToggle.tsx`
  - [ ] 7.9 Create `components/manager/ProjectsTable.tsx`
  - [ ] 7.10 Create `components/manager/ProjectKanbanBoard.tsx`
  - [ ] 7.11 Create `components/manager/TasksTable.tsx`
  - [ ] 7.12 Create `components/manager/TaskBoardView.tsx`
  - [ ] 7.13 Create `components/manager/TeamRosterTable.tsx`
  - [ ] 7.14 Create `components/manager/WorkloadDistributionChart.tsx`

- [ ] 8. Frontend: Redesign Manager Pages
  - [ ] 8.1 Redesign `app/(dashboard)/manager/page.tsx` (Dashboard)
  - [ ] 8.2 Redesign `app/(dashboard)/manager/projects/page.tsx`
  - [ ] 8.3 Redesign `app/(dashboard)/manager/tasks/page.tsx`
  - [ ] 8.4 Redesign `app/(dashboard)/manager/team/page.tsx`
