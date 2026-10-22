# Employee User Guide

## Sign in

1. Open the internal workspace URL provided by your administrator.
2. Enter your employee username and password.
3. Select **Sign in**. If your credentials do not work, contact an administrator; there is no shared default account.
4. Use the profile area in the top-right corner or the sign-out control at the bottom of the sidebar to end your session.

Administrators can initialize the first account in a development setup using the environment-based bootstrap instructions in the README. They can create and provision employee accounts from the Employees page.

## Dashboard navigation

The Overview page is the first screen after sign-in. Its cards show project totals, active projects, approved budgets, recorded expenses, remaining budget, and active team members. Values come from saved project and expense records.

The charts summarize project expenditure and current statuses. The lower sections show recent projects, workspace activity, and deadlines due within the next two weeks. Select a project card or name to open its details. Use **Projects** in the sidebar to search by name/code or filter by status.

## Create and view projects

Administrators can use **New project** from Overview or Projects. Enter a project name, unique code, description, active manager, approved budget, start date, expected completion date, and status. The end date must be on or after the start date. Mark fictional or unverified sample information as demo data.

Open a project to see its overview and financial values. The project tabs show team members, tasks, and expenses. Select **Edit** to update project information. Projects with recorded expenses cannot be deleted because financial history is retained; set the project status to Cancelled instead.

The initial AJAH project uses a neutral description. Its sample dates, budget, progress, members, tasks, and expense records are fictional and are not statements about the actual initiative. The other two starter projects are labeled fictional demos.

## Managing team members

Administrators can open a project's **Team** tab and select **Assign member**. Choose an active employee, enter their project role and assigned date, and save. The page reports active team members and total assignments separately. Removing a member deactivates that assignment and preserves the membership record.

## Managing tasks

On a project's **Tasks** tab, administrators can add, edit, or delete tasks. A task can have an active project member as its assignee. Set a title, description, priority, start date, due date, and status: To do, In progress, Completed, or Blocked. The due date must be on or after the start date. Project completion is calculated from completed tasks.

## Recording expenses

On a project's **Expenses** tab, administrators can record a title, category, positive amount, expense date, and description. The application records the signed-in employee as the author. Edit or delete an expense to correct an entry; the total expenditure, remaining budget, utilization, and category breakdown are recalculated from the saved records.

## Managing employees

Administrators can open **Employees** in the sidebar to create or update accounts. Each account has a unique employee code, username, and email. New accounts require an initial password of at least 12 characters. Passwords are hashed before storage and cannot be viewed later. An administrator can set a new password when editing an account.

Deactivate an employee to prevent future sign-in while keeping project and expense history intact. Keep at least one active administrator account.

## Activity history

Select the notification bell to see recent changes, then open the activity history for a longer recent list. Project, team, task, and expense updates include the acting employee and time.

## Troubleshooting

- **I cannot sign in:** check the username and password or ask an administrator to create/reactivate your account or set a new password.
- **A page says I do not have permission:** employee accounts can view project information; administrators perform changes.
- **A project cannot be deleted:** it has expense history. Change its status instead.
- **I cannot assign a task to someone:** the assignee must be an active member of the project. Add them on the Team tab first.
- **A project does not appear:** clear the project search or status filter.
- **Financial values look stale:** reload the page. Totals are calculated from saved expenses; unsaved form values are not included.
- **The page reports a CSRF/session error:** reload and sign in again. The session may have expired.
- **Demo values appear:** those values are fictional setup data. Ask an authorized company administrator to verify and replace them before operational use.
