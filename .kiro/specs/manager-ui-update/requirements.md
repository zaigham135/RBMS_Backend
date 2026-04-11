# Requirements Document

## Introduction

This feature updates the UI for the Manager role pages in the TaskMan application to match a new design system. The update covers four pages: Manager Dashboard, Manager Projects, Manager Tasks, and Manager Team. The redesign introduces richer data visualizations (charts, progress bars, workload indicators), a consistent top bar with breadcrumb navigation, an updated sidebar with role-prefixed labels and task count badges, and improved data tables with status/priority badges, team avatars, and action menus. The frontend is built with Next.js 14, TypeScript, and Tailwind CSS. All data is fetched from an existing Java Spring Boot backend.

## Glossary

- **Manager_Dashboard**: The overview page at `/manager`, showing summary stats, quick actions, assigned projects, a task completion trend chart, team workload, and recent activity.
- **Manager_Projects_Page**: The page at `/manager/projects`, showing the manager's assigned projects in a table or kanban view.
- **Manager_Tasks_Page**: The page at `/manager/tasks`, showing all tasks across the manager's projects in a table or board view.
- **Manager_Team_Page**: The page at `/manager/team`, showing the team roster with workload metrics and a workload distribution chart.
- **Sidebar**: The left navigation panel shared across all manager pages, containing nav links, the TaskMan logo, and user controls.
- **Top_Bar**: The horizontal bar at the top of each manager page, containing a breadcrumb, search input, notification bell, and user profile.
- **Stat_Card**: A summary card displaying a single metric with a label, numeric value, and optional trend indicator.
- **Status_Badge**: A colored pill component that visually represents a project or task status (e.g., Active, In Review, Planned, In Progress, Pending, Completed).
- **Priority_Badge**: A colored pill component that visually represents a task priority level (High, Medium, Low).
- **Progress_Bar**: A horizontal bar component showing a percentage value between 0 and 100.
- **Workload_Label**: A text label (e.g., "High", "Optimal", "Low") assigned to a team member based on their workload percentage threshold.
- **Task_Completion_Trend_Chart**: A line chart showing the number of tasks completed per day over the last 30 days.
- **Workload_Distribution_Chart**: A bar chart showing the workload percentage per team member.
- **Quick_Actions_Panel**: A panel on the Manager Dashboard containing shortcut buttons for common actions.
- **Team_Roster_Table**: The table on the Manager Team page listing team members with their role, active projects, workload, and actions.
- **View_Toggle**: A UI control allowing the user to switch between two layout modes (e.g., List View / Kanban, or List View / Board).

---

## Requirements

### Requirement 1: Manager Dashboard — Stats Cards

**User Story:** As a manager, I want to see key metrics at a glance on my dashboard, so that I can quickly assess the state of my projects and team.

#### Acceptance Criteria

1. THE Manager_Dashboard SHALL display four Stat_Cards: "Active Projects", "Pending Tasks", "Completed This Week", and "Team Members".
2. WHEN the Manager_Dashboard loads, THE Manager_Dashboard SHALL fetch the Active Projects count from the `/api/manager/my-projects` endpoint.
3. WHEN the Manager_Dashboard loads, THE Manager_Dashboard SHALL fetch the Team Members count from the `/api/manager/employees` endpoint.
4. WHEN the Manager_Dashboard loads, THE Manager_Dashboard SHALL fetch the Pending Tasks count from the `/api/manager/tasks` endpoint filtered by status `TODO`.
5. WHEN the Manager_Dashboard loads, THE Manager_Dashboard SHALL fetch the Completed This Week count from the `/api/manager/tasks` endpoint filtered by status `DONE` and the current calendar week.
6. IF a data fetch fails, THEN THE Manager_Dashboard SHALL display the last successfully fetched value or a zero placeholder for the affected Stat_Card.
7. EACH Stat_Card SHALL display a trend indicator (e.g., "+2 this month") sourced from the API response or computed from historical data.

---

### Requirement 2: Manager Dashboard — Quick Actions Panel

**User Story:** As a manager, I want quick access to common actions from my dashboard, so that I can perform frequent tasks without navigating away.

#### Acceptance Criteria

1. THE Manager_Dashboard SHALL display a Quick_Actions_Panel containing three action buttons: "Create Task", "Update Status", and "Message Team".
2. WHEN the "Create Task" button is clicked, THE Manager_Dashboard SHALL open the task creation modal.
3. WHEN the "Update Status" button is clicked, THE Manager_Dashboard SHALL navigate the user to the Manager_Tasks_Page.
4. WHEN the "Message Team" button is clicked, THE Manager_Dashboard SHALL navigate the user to the Manager_Team_Page.

---

### Requirement 3: Manager Dashboard — Assigned Projects Section

**User Story:** As a manager, I want to see my assigned projects with their status and progress on the dashboard, so that I can monitor project health at a glance.

#### Acceptance Criteria

1. THE Manager_Dashboard SHALL display an "Assigned Projects" section showing project cards for all projects returned by `/api/manager/my-projects`.
2. EACH project card SHALL display the project name, a Status_Badge, a Progress_Bar with a percentage value, a task count, and team member avatars (up to 4, with overflow count).
3. WHEN the project list contains more than 4 projects, THE Manager_Dashboard SHALL display a "View All" link navigating to the Manager_Projects_Page.
4. IF no projects are assigned to the manager, THEN THE Manager_Dashboard SHALL display an empty state message within the Assigned Projects section.

---

### Requirement 4: Manager Dashboard — Task Completion Trend Chart

**User Story:** As a manager, I want to see a trend chart of task completions over the last 30 days, so that I can identify productivity patterns.

#### Acceptance Criteria

1. THE Manager_Dashboard SHALL display a Task_Completion_Trend_Chart rendered as a line chart.
2. THE Task_Completion_Trend_Chart SHALL display data points for each of the last 30 calendar days, where each data point represents the count of tasks with status `DONE` on that day.
3. WHEN the Manager_Dashboard loads, THE Manager_Dashboard SHALL fetch task completion data from the backend to populate the Task_Completion_Trend_Chart.
4. THE Task_Completion_Trend_Chart SHALL display labeled axes: the x-axis showing dates and the y-axis showing task count.
5. IF no completion data exists for a given day, THEN THE Task_Completion_Trend_Chart SHALL render that day's data point as zero.

---

### Requirement 5: Manager Dashboard — Team Workload Sidebar

**User Story:** As a manager, I want to see my team members' workload on the dashboard, so that I can identify overloaded or underutilized members.

#### Acceptance Criteria

1. THE Manager_Dashboard SHALL display a Team Workload section listing up to 5 team members with their avatar, name, and a Progress_Bar showing their workload percentage.
2. WHEN the Manager_Dashboard loads, THE Manager_Dashboard SHALL fetch team member data from `/api/manager/employees` to populate the Team Workload section.
3. THE Manager_Dashboard SHALL sort team members in the workload section by workload percentage in descending order.
4. EACH team member entry SHALL display a Workload_Label: "High" for workload >= 75%, "Optimal" for workload between 40% and 74%, and "Low" for workload < 40%.
5. WHEN the Team Workload section contains more than 5 members, THE Manager_Dashboard SHALL display a "View All" link navigating to the Manager_Team_Page.

---

### Requirement 6: Manager Dashboard — Recent Activity Feed

**User Story:** As a manager, I want to see a feed of recent activity on my dashboard, so that I can stay informed about changes made by my team.

#### Acceptance Criteria

1. THE Manager_Dashboard SHALL display a Recent Activity feed showing the 10 most recent activity log entries scoped to the manager's projects.
2. EACH activity entry SHALL display the acting user's avatar, the user's name, a description of the action, and a relative timestamp (e.g., "10 mins ago").
3. WHEN the Manager_Dashboard loads, THE Manager_Dashboard SHALL fetch activity data from the backend activity log endpoint.
4. IF no activity exists, THEN THE Manager_Dashboard SHALL display an empty state message within the Recent Activity feed.

---

### Requirement 7: Sidebar Navigation — Updated Labels and Structure

**User Story:** As a manager, I want the sidebar navigation to clearly identify manager-specific pages, so that I can navigate the application without confusion.

#### Acceptance Criteria

1. THE Sidebar SHALL display exactly four primary navigation items for the manager role: "Manager Dashboard" (href `/manager`), "Manager Projects" (href `/manager/projects`), "Manager Tasks" (href `/manager/tasks`), and "Manager Team" (href `/manager/team`).
2. WHILE the user is on a manager page, THE Sidebar SHALL highlight the navigation item whose href matches the current route as the active item.
3. THE Sidebar SHALL display a numeric badge on the "Manager Tasks" navigation item showing the count of active (non-`DONE`) tasks assigned to the manager's projects.
4. WHEN the task count exceeds 99, THE Sidebar SHALL display "99+" in the badge instead of the exact number.
5. THE Sidebar SHALL display the TaskMan logo and application name at the top of the navigation panel.

---

### Requirement 8: Top Bar — Breadcrumb, Search, and User Profile

**User Story:** As a manager, I want a consistent top bar on every page showing my current location, a search input, and my profile, so that I can orient myself and access global features quickly.

#### Acceptance Criteria

1. THE Top_Bar SHALL display a breadcrumb showing the current page path in the format "Management > [Page Name]" (e.g., "Management > Dashboard", "Management > My Projects").
2. THE Top_Bar SHALL display a search input with placeholder text relevant to the current page.
3. THE Top_Bar SHALL display a notification bell icon with a red indicator dot when unread notifications exist.
4. THE Top_Bar SHALL display the authenticated user's avatar, full name, and role (e.g., "Monica Hall — Project Manager").
5. WHEN the user's avatar or name is clicked, THE Top_Bar SHALL navigate to the manager account settings page at `/manager/account`.

---

### Requirement 9: Manager Projects Page — Projects Table

**User Story:** As a manager, I want to view all my assigned projects in a structured table, so that I can monitor their status, progress, and team composition.

#### Acceptance Criteria

1. THE Manager_Projects_Page SHALL display a table with the following columns: PROJECT NAME, STATUS, HEALTH/PROGRESS, TEAM, and ACTIONS.
2. EACH table row SHALL display the project icon, project name, client or project type label, a Status_Badge, a Progress_Bar with milestone label and percentage, up to 4 team member avatars with overflow count, and an action menu button ("...").
3. WHEN the Manager_Projects_Page loads, THE Manager_Projects_Page SHALL fetch project data from `/api/manager/my-projects`.
4. THE Status_Badge SHALL render with the following color mappings: "Active" as green, "In Review" as yellow/amber, "Planned" as gray, and "On Hold" as red.
5. THE Progress_Bar value SHALL be computed as the percentage of `DONE` tasks out of total tasks for the project, clamped between 0 and 100.
6. IF a project has zero tasks, THEN THE Manager_Projects_Page SHALL display 0% progress for that project.
7. WHEN the action menu ("...") is clicked for a project row, THE Manager_Projects_Page SHALL display options: "Edit", "View Details", and "Archive".

---

### Requirement 10: Manager Projects Page — View Toggle and Filters

**User Story:** As a manager, I want to switch between list and kanban views and filter my projects, so that I can work with the project list in the format most useful to me.

#### Acceptance Criteria

1. THE Manager_Projects_Page SHALL display a View_Toggle allowing the user to switch between "List View" and "Kanban" layout modes.
2. WHEN the user selects "Kanban" in the View_Toggle, THE Manager_Projects_Page SHALL render project cards grouped by status in a kanban board layout.
3. WHEN the user selects "List View" in the View_Toggle, THE Manager_Projects_Page SHALL render the projects table.
4. THE Manager_Projects_Page SHALL display a "Filter" button that opens a filter panel allowing filtering by status and due date range.
5. THE Manager_Projects_Page SHALL display a "Sort" button that allows sorting by project name, due date, or progress percentage.
6. THE Manager_Projects_Page SHALL display a "+ New Project" button that opens the project creation modal.
7. WHEN a filter is applied, THE Manager_Projects_Page SHALL update the displayed project list to show only projects matching all active filter criteria.

---

### Requirement 11: Manager Tasks Page — Tasks Table

**User Story:** As a manager, I want to view all tasks across my projects in a structured table, so that I can track task status, priority, and assignments.

#### Acceptance Criteria

1. THE Manager_Tasks_Page SHALL display a table with the following columns: TASK NAME, PROJECT, STATUS, PRIORITY, ASSIGNEE, DUE DATE, and ACTIONS.
2. EACH table row SHALL display a checkbox, the task name, the associated project name, a Status_Badge, a Priority_Badge, the assignee's avatar and name, the due date, and an action menu button ("...").
3. WHEN the Manager_Tasks_Page loads, THE Manager_Tasks_Page SHALL fetch task data from the existing tasks API endpoint with pagination (page size of 10).
4. THE Status_Badge for tasks SHALL render with the following mappings: "In Progress" (IN_PROGRESS) as blue, "Pending" (TODO) as gray, and "Completed" (DONE) as green.
5. THE Priority_Badge SHALL render with the following mappings: "High" as red, "Medium" as yellow/amber, and "Low" as green.
6. WHEN the action menu ("...") is clicked for a task row, THE Manager_Tasks_Page SHALL display options: "View Details", "Edit", and "Delete".
7. WHEN the "Delete" option is selected, THE Manager_Tasks_Page SHALL display a confirmation dialog before deleting the task.

---

### Requirement 12: Manager Tasks Page — View Toggle and Filters

**User Story:** As a manager, I want to switch between list and board views and filter tasks, so that I can manage tasks in the format most useful to me.

#### Acceptance Criteria

1. THE Manager_Tasks_Page SHALL display a View_Toggle allowing the user to switch between "List View" and "Board" layout modes.
2. WHEN the user selects "Board" in the View_Toggle, THE Manager_Tasks_Page SHALL render tasks grouped by status (TODO, IN_PROGRESS, DONE) in a kanban board layout.
3. WHEN the user selects "List View" in the View_Toggle, THE Manager_Tasks_Page SHALL render the tasks table.
4. THE Manager_Tasks_Page SHALL display a "Filter" button that opens a filter panel allowing filtering by project, status, priority, and assignee.
5. THE Manager_Tasks_Page SHALL display a "Sort" button that allows sorting by due date, priority, or status.
6. THE Manager_Tasks_Page SHALL display an "+ Add Task" button that opens the task creation modal.

---

### Requirement 13: Manager Team Page — Stats Cards

**User Story:** As a manager, I want to see team-level metrics at the top of the team page, so that I can quickly assess team capacity and performance.

#### Acceptance Criteria

1. THE Manager_Team_Page SHALL display four Stat_Cards: "Total Members", "Avg Workload", "Tasks Completed", and "Overloaded Members".
2. WHEN the Manager_Team_Page loads, THE Manager_Team_Page SHALL fetch team data from `/api/manager/employees` to populate the Stat_Cards.
3. THE "Total Members" Stat_Card SHALL display the total count of employees under the manager and a trend indicator showing the change from the previous month (e.g., "+2 this month").
4. THE "Avg Workload" Stat_Card SHALL display the average workload percentage across all team members.
5. THE "Tasks Completed" Stat_Card SHALL display the total count of `DONE` tasks assigned to team members within the current month.
6. THE "Overloaded Members" Stat_Card SHALL display the count of team members whose workload percentage is >= 85%.

---

### Requirement 14: Manager Team Page — Team Roster Table

**User Story:** As a manager, I want to view my team members in a filterable roster table, so that I can see their roles, active projects, and workload at a glance.

#### Acceptance Criteria

1. THE Manager_Team_Page SHALL display a Team_Roster_Table with the following columns: MEMBER (avatar, name, email), ROLE, ACTIVE PROJECTS, WORKLOAD, and ACTIONS.
2. THE Team_Roster_Table SHALL display tabs for filtering members by department: "All Members", "Design", and "Engineering".
3. WHEN a department tab is selected, THE Team_Roster_Table SHALL display only members whose department matches the selected tab.
4. EACH table row SHALL display the member's avatar, full name, and email in the MEMBER column; their role label in the ROLE column; up to 3 active project name tags in the ACTIVE PROJECTS column; a Progress_Bar with percentage and Workload_Label in the WORKLOAD column; and an "Assign" button in the ACTIONS column.
5. WHEN the "Assign" button is clicked for a team member, THE Manager_Team_Page SHALL open the task assignment modal pre-populated with that team member as the assignee.
6. THE Manager_Team_Page SHALL display a "Filter" button that opens a filter panel for filtering by role or workload range.

---

### Requirement 15: Manager Team Page — Workload Distribution Chart

**User Story:** As a manager, I want to see a workload distribution chart at the bottom of the team page, so that I can visually compare workload across all team members.

#### Acceptance Criteria

1. THE Manager_Team_Page SHALL display a Workload_Distribution_Chart rendered as a bar chart at the bottom of the page.
2. THE Workload_Distribution_Chart SHALL display one bar per team member, where the bar height represents the member's workload percentage (0–100%).
3. THE Workload_Distribution_Chart SHALL label each bar with the team member's name on the x-axis.
4. THE Workload_Distribution_Chart SHALL display a horizontal reference line at 75% to indicate the "High" workload threshold.
5. WHEN the Manager_Team_Page loads, THE Manager_Team_Page SHALL use the same team member data fetched for the Team_Roster_Table to populate the Workload_Distribution_Chart.

---

### Requirement 16: Status Badge and Priority Badge — Color Mapping Consistency

**User Story:** As a manager, I want status and priority badges to use consistent colors across all pages, so that I can recognize states at a glance without re-learning the color scheme on each page.

#### Acceptance Criteria

1. THE Status_Badge SHALL apply the same color style for a given status value on every page where it appears (Manager_Dashboard, Manager_Projects_Page, Manager_Tasks_Page, Manager_Team_Page).
2. THE Priority_Badge SHALL apply the same color style for a given priority value on every page where it appears.
3. THE Status_Badge component SHALL accept a status value and return a deterministic color class: "Active"/"IN_PROGRESS" → blue, "In Review" → amber, "Planned"/"TODO" → gray, "Completed"/"DONE" → green, "On Hold" → red.
4. THE Priority_Badge component SHALL accept a priority value and return a deterministic color class: "HIGH" → red, "MEDIUM" → amber/yellow, "LOW" → green.

---

### Requirement 17: Responsive Layout

**User Story:** As a manager, I want the manager pages to be usable on different screen sizes, so that I can access the application from various devices.

#### Acceptance Criteria

1. THE Manager_Dashboard SHALL display the Stat_Cards in a 2-column grid on screens narrower than 1024px and a 4-column grid on screens 1024px and wider.
2. THE Sidebar SHALL collapse into a hamburger menu on screens narrower than 768px.
3. WHILE the Sidebar is collapsed on mobile, THE Sidebar SHALL display a full-screen overlay when the hamburger menu is opened.
4. THE Manager_Projects_Page, Manager_Tasks_Page, and Manager_Team_Page tables SHALL be horizontally scrollable on screens narrower than 900px.
5. THE Top_Bar breadcrumb SHALL be hidden on screens narrower than 768px to preserve space for the search input and user profile.
