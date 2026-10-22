(() => {
  'use strict';

  const $ = (selector, root = document) => root.querySelector(selector);
  const $$ = (selector, root = document) => [...root.querySelectorAll(selector)];
  const app = { user: null, page: 'dashboard', projectId: null, project: null, tab: 'overview', projects: [], employees: [], search: '', statusFilter: 'ALL', activityPage: 0, activityHasNext: false };
  const dialog = $('#app-dialog');
  const dialogContent = $('#dialog-content');
  const content = $('#page-content');

  const iconPaths = {
    grid: '<rect x="3" y="3" width="7" height="7" rx="1.3"/><rect x="14" y="3" width="7" height="7" rx="1.3"/><rect x="3" y="14" width="7" height="7" rx="1.3"/><rect x="14" y="14" width="7" height="7" rx="1.3"/>',
    briefcase: '<rect x="3" y="7" width="18" height="14" rx="2"/><path d="M8 7V5a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2M3 12h18M10 12v2h4v-2"/>',
    users: '<path d="M16 21v-2a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4v2M9.5 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8ZM20 8v6M23 11h-6"/>',
    shield: '<path d="M12 22s8-4 8-11V5l-8-3-8 3v6c0 7 8 11 8 11Z"/><path d="m9 12 2 2 4-4"/>',
    logout: '<path d="M10 17l5-5-5-5M15 12H3"/><path d="M12 3h6a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-6"/>',
    bell: '<path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4"/>',
    menu: '<path d="M4 6h16M4 12h16M4 18h16"/>',
    chevron: '<path d="m6 9 6 6 6-6"/>',
    search: '<circle cx="10.8" cy="10.8" r="6.8"/><path d="m16 16 5 5"/>',
    plus: '<path d="M12 5v14M5 12h14"/>',
    arrow: '<path d="M5 12h14m-7-7 7 7-7 7"/>',
    chart: '<path d="M3 3v18h18M8 15l4-4 4 3 5-7"/>',
    wallet: '<rect x="3" y="5" width="18" height="15" rx="2"/><path d="M3 9h18M16 15h2"/><path d="M6 5V3h12v2"/>',
    people: '<path d="M16 21v-2a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4v2M9.5 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8ZM20 8v6M23 11h-6"/>',
    clock: '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
    calendar: '<rect x="3" y="5" width="18" height="16" rx="2"/><path d="M16 3v4M8 3v4M3 10h18"/>',
    edit: '<path d="m15 5 4 4M4 20l4-.8L19 8a2.1 2.1 0 0 0-3-3L5 16l-1 4Z"/>',
    trash: '<path d="M3 6h18M8 6V4h8v2m3 0-1 15H6L5 6m4 4v7m6-7v7"/>',
    close: '<path d="m18 6-12 12M6 6l12 12"/>',
    check: '<path d="m5 12 4 4L19 6"/>',
    task: '<rect x="4" y="3" width="16" height="18" rx="2"/><path d="M8 8h8M8 12h8M8 16h5"/><path d="m6 8 1 1 2-2"/>',
    receipt: '<path d="M4 3h16v18l-4-2-4 2-4-2-4 2V3Z"/><path d="M8 8h8M8 12h8M8 16h4"/>',
    dots: '<circle cx="5" cy="12" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/>',
    leaf: '<path d="M20 4c-7 0-14 3-14 10a6 6 0 0 0 6 6c7 0 10-7 8-16Z"/><path d="M4 21c3-6 7-9 12-12"/>',
    target: '<circle cx="12" cy="12" r="9"/><circle cx="12" cy="12" r="5"/><circle cx="12" cy="12" r="1"/>',
    user: '<circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/>',
    spark: '<path d="m12 3 1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8L12 3ZM19 16l1 2.5 2.5 1L20 20.5 19 23l-1-2.5-2.5-1 2.5-1L19 16Z"/>',
    minus: '<path d="M5 12h14"/>'
  };
  function icon(name, cls = '') { return `<svg class="${cls}" viewBox="0 0 24 24" aria-hidden="true">${iconPaths[name] || iconPaths.spark}</svg>`; }
  $$('[data-icon]').forEach(el => { el.innerHTML = icon(el.dataset.icon); });

  const esc = value => String(value ?? '').replace(/[&<>"']/g, ch => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[ch]);
  const initials = name => String(name || 'Employee').trim().split(/\s+/).slice(0, 2).map(part => part[0] || '').join('').toUpperCase();
  const money = value => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(Number(value || 0));
  const moneyExact = value => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 2 }).format(Number(value || 0));
  const date = value => {
    if (!value) return '—';
    const d = new Date(`${String(value).slice(0, 10)}T00:00:00`);
    return Number.isNaN(d.getTime()) ? esc(value) : new Intl.DateTimeFormat('en-IN', { day: 'numeric', month: 'short', year: 'numeric' }).format(d);
  };
  const relative = value => {
    if (!value) return '';
    const d = new Date(value);
    if (Number.isNaN(d.getTime())) return '';
    const days = Math.floor((Date.now() - d.getTime()) / 86400000);
    if (days <= 0) return 'Today';
    if (days === 1) return 'Yesterday';
    if (days < 7) return `${days} days ago`;
    return new Intl.DateTimeFormat('en-IN', { day: 'numeric', month: 'short' }).format(d);
  };
  const statusLabel = value => ({ ACTIVE: 'Active', PLANNING: 'Planning', ON_HOLD: 'On hold', COMPLETED: 'Completed', CANCELLED: 'Cancelled' }[value] || value || 'Unknown');
  const taskStatusLabel = value => ({ TO_DO: 'To do', IN_PROGRESS: 'In progress', COMPLETED: 'Completed', BLOCKED: 'Blocked' }[value] || value || 'Unknown');
  const titleCase = value => String(value || '').toLowerCase().replaceAll('_', ' ').replace(/\b\w/g, letter => letter.toUpperCase());
  const isAdmin = () => app.user?.role === 'ADMIN';
  function cookie(name) { return document.cookie.split('; ').find(row => row.startsWith(`${name}=`))?.split('=').slice(1).join('=') || ''; }
  async function csrf() {
    if (cookie('XSRF-TOKEN')) return;
    await fetch('/api/auth/csrf', { credentials: 'same-origin' });
  }
  async function api(url, options = {}) {
    const method = (options.method || 'GET').toUpperCase();
    const headers = new Headers(options.headers || {});
    if (options.body && !(options.body instanceof FormData)) headers.set('Content-Type', 'application/json');
    if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
      await csrf();
      const token = decodeURIComponent(cookie('XSRF-TOKEN'));
      if (token) headers.set('X-XSRF-TOKEN', token);
    }
    const response = await fetch(url, { ...options, method, headers, credentials: 'same-origin' });
    if (response.status === 401) {
      if (app.user) showLogin();
      const data = await response.json().catch(() => ({}));
      throw new Error(data.message || 'Please sign in to continue');
    }
    if (!response.ok) {
      const data = await response.json().catch(() => ({}));
      const detail = data.details && Object.values(data.details).filter(Boolean).join(' ');
      throw new Error(detail || data.message || `Request failed (${response.status})`);
    }
    if (response.status === 204) return null;
    return response.json();
  }
  function toast(message, error = false) {
    const node = document.createElement('div'); node.className = `toast${error ? ' error' : ''}`; node.textContent = message;
    $('#toast-region').append(node); setTimeout(() => node.remove(), 3600);
  }
  function emptyState(title, message, action = '') {
    return `<div class="empty-state"><div class="empty-state-icon">${icon('leaf')}</div><strong>${esc(title)}</strong><p>${esc(message)}</p>${action}</div>`;
  }
  function setProfile(employee) {
    app.user = employee;
    const name = employee.name || employee.username;
    $('#sidebar-name').textContent = name; $('#sidebar-role').textContent = employee.role;
    $('#profile-name').textContent = name; $('#profile-role').textContent = titleCase(employee.jobTitle || employee.role);
    $('#profile-avatar').textContent = initials(name); $('#sidebar-avatar').textContent = initials(name);
    $$('[data-admin-only]').forEach(el => { el.hidden = !isAdmin(); });
  }
  function showLogin() {
    app.user = null; $('#app-shell').hidden = true; $('#login-screen').hidden = false;
    $('#login-username').focus();
  }
  async function startApp() {
    try { await csrf(); const employee = await api('/api/auth/me'); setProfile(employee); $('#login-screen').hidden = true; $('#app-shell').hidden = false; await openHashRoute(); }
    catch { showLogin(); }
  }

  $('#login-form').addEventListener('submit', async event => {
    event.preventDefault(); const form = new FormData(event.currentTarget); const error = $('#login-error');
    error.hidden = true; const button = $('button[type="submit"]', event.currentTarget); button.disabled = true;
    try {
      await csrf(); const body = new URLSearchParams({ username: form.get('username'), password: form.get('password') });
      const response = await fetch('/api/auth/login', { method: 'POST', credentials: 'same-origin', headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'X-XSRF-TOKEN': decodeURIComponent(cookie('XSRF-TOKEN')) }, body });
      const data = await response.json().catch(() => ({})); if (!response.ok) throw new Error(data.message || 'Sign in failed');
      const employee = await api('/api/auth/me'); setProfile(employee); $('#login-screen').hidden = true; $('#app-shell').hidden = false; event.currentTarget.reset(); await openHashRoute();
    } catch (err) { error.textContent = err.message; error.hidden = false; }
    finally { button.disabled = false; }
  });

  $('#logout-button').addEventListener('click', async () => {
    try { await api('/api/auth/logout', { method: 'POST' }); } catch { /* the session may already have expired */ }
    showLogin();
  });
  $('#mobile-menu').addEventListener('click', () => { $('#sidebar').classList.add('open'); $('#sidebar-backdrop').classList.add('open'); });
  $('#sidebar-backdrop').addEventListener('click', closeMobileMenu);
  function closeMobileMenu() { $('#sidebar').classList.remove('open'); $('#sidebar-backdrop').classList.remove('open'); }

  async function openHashRoute() {
    const parts = location.hash.replace(/^#\/?/, '').split('/').filter(Boolean);
    if (parts[0] === 'project' && parts[1] && /^\d+$/.test(parts[1])) return showProject(Number(parts[1]));
    if (parts[0] === 'projects') return showProjects();
    if (parts[0] === 'employees') return showEmployees();
    if (parts[0] === 'activity') return showActivity();
    return showDashboard();
  }
  window.addEventListener('hashchange', openHashRoute);
  $$('.nav-link').forEach(button => button.addEventListener('click', () => { closeMobileMenu(); location.hash = button.dataset.page === 'dashboard' ? '#dashboard' : `#${button.dataset.page}`; }));
  function setPage(page, title) {
    app.page = page; $('#page-title').textContent = title;
    $$('.nav-link').forEach(button => button.classList.toggle('active', button.dataset.page === page));
    $('#notification-popover').hidden = true;
  }
  async function showDashboard() {
    setPage('dashboard', 'Overview'); app.project = null;
    content.innerHTML = `<div class="loading-panel skeleton"></div>`;
    try { const data = await api('/api/dashboard'); app.projects = data.projects || []; $('#project-nav-count').textContent = data.stats.totalProjects || ''; renderDashboard(data); }
    catch (err) { content.innerHTML = emptyState('Dashboard could not load', err.message, '<button class="button button-secondary button-small" data-action="retry-dashboard">Try again</button>'); }
  }
  function renderDashboard(data) {
    const s = data.stats;
    const statCards = [
      ['grid', 'Total projects', s.totalProjects, 'Across the workspace', false],
      ['target', 'Active projects', s.activeProjects, 'Currently in progress', false],
      ['wallet', 'Total project budget', money(s.totalBudget), 'Approved project budgets', true],
      ['receipt', 'Total expenditure', money(s.totalExpenditure), 'Recorded expense entries', true],
      ['chart', 'Remaining budget', money(s.remainingBudget), 'Budget less recorded spend', true],
      ['people', 'Active team members', s.activeTeamMembers, 'Across active assignments', false]
    ];
    const cards = statCards.map(([ic, label, value, foot, isMoney]) => `<article class="stat-card"><div class="stat-top"><span class="stat-label">${label}</span><span class="stat-icon">${icon(ic)}</span></div><div class="stat-value${isMoney ? ' money' : ''}">${esc(value)}</div><div class="stat-foot">${foot}</div></article>`).join('');
    const projects = data.projects || [];
    const projectMini = projects.slice(0, 4).map(p => `<div class="project-mini" data-action="view-project" data-id="${p.id}"><div class="project-initial">${esc((p.code || 'P').slice(0, 2))}</div><div class="project-mini-main"><strong>${esc(p.name)}</strong><small>${esc(p.code)} · ${esc(statusLabel(p.status))}</small></div><div class="mini-progress"><div class="mini-progress-top"><span>Progress</span><strong>${p.progress}%</strong></div><div class="progress-track"><div class="progress-fill" style="width:${Math.max(0, Math.min(100, p.progress))}%"></div></div></div></div>`).join('');
    const activities = data.recentActivities || [];
    const activityHtml = activities.length ? activities.map(a => `<div class="activity-row"><div class="activity-icon">${icon(activityIcon(a.action))}</div><div class="activity-copy"><p><strong>${esc(a.actor?.name || 'Workspace')}</strong> ${esc(activityVerb(a.action))} ${esc(a.description.replace(/^(Created|Updated|Added|Recorded|Deleted|Initialized)\s*/i, ''))}</p><small>${esc(a.projectName || '')} · ${esc(relative(a.occurredAt))}</small></div></div>`).join('') : emptyState('No activity yet', 'Project changes and updates will appear here.');
    const upcoming = data.upcomingTasks || [];
    const deadlines = upcoming.length ? upcoming.slice(0, 5).map(t => {
      const dateValue = new Date(`${t.dueDate}T00:00:00`); const day = dateValue.toLocaleDateString('en-IN', { day: '2-digit' }); const mon = dateValue.toLocaleDateString('en-IN', { month: 'short' });
      return `<div class="deadline-row"><div class="deadline-date"><strong>${day}</strong><span>${mon}</span></div><div class="deadline-copy"><strong>${esc(t.title)}</strong><small>${esc(t.projectName)}${t.assignee ? ` · ${esc(t.assignee.name)}` : ''}</small></div><i class="priority-mark ${String(t.priority).toLowerCase()}"></i></div>`;
    }).join('') : emptyState('All caught up', 'There are no upcoming deadlines in the next two weeks.');
    content.innerHTML = `
      <div class="page-heading-row"><div><p class="eyebrow">${escapeGreeting()}, ${esc((app.user?.name || '').split(' ')[0] || 'there')}</p><h2>Your workspace at a glance</h2><p>Keep projects, budgets and next steps in view.</p></div>${isAdmin() ? '<button class="button button-primary" data-action="add-project">' + icon('plus') + ' New project</button>' : ''}</div>
      <section class="stats-grid">${cards}</section>
      <section class="dashboard-grid">
        <article class="panel"><div class="panel-heading"><div><h3>Project expenditure</h3><p>Approved budget compared with recorded spend</p></div><span class="panel-period">All projects</span></div><div class="chart-area"><div class="chart-y-labels"><span>${money(chartMax(projects))}</span><span>${money(chartMax(projects) / 2)}</span><span>₹0</span></div><div class="chart-gridlines"><span></span><span></span><span></span></div><div class="bar-chart">${barChart(projects)}</div></div><div class="chart-legend"><span><i class="legend-dot"></i>Approved budget</span><span><i class="legend-dot spent"></i>Expenditure</span></div></article>
        <article class="panel"><div class="panel-heading"><div><h3>Project status</h3><p>Current distribution across all projects</p></div></div>${statusChart(projects)}</article>
      </section>
      <section class="bottom-grid">
        <article class="panel"><div class="panel-heading"><div><h3>Projects</h3><p>Recent updates from your portfolio</p></div><button class="text-button" data-action="go-projects">View all ${icon('arrow')}</button></div><div class="project-mini-list">${projectMini || emptyState('No projects yet', 'Create your first project to start tracking work.', isAdmin() ? '<button class="button button-primary button-small" data-action="add-project">Create project</button>' : '')}</div></article>
        <article class="panel"><div class="panel-heading"><div><h3>Recent activity</h3><p>Latest project changes</p></div><button class="text-button" data-action="show-activity">See all</button></div><div class="activity-list">${activityHtml}</div></article>
      </section>
      <section class="panel" style="margin-top:16px"><div class="panel-heading"><div><h3>Upcoming deadlines</h3><p>Due in the next 14 days</p></div><span class="panel-period">${s.upcomingTasks} tasks</span></div><div class="deadline-list">${deadlines}</div></section>`;
  }
  function escapeGreeting() { const hour = new Date().getHours(); return hour < 12 ? 'GOOD MORNING' : hour < 18 ? 'GOOD AFTERNOON' : 'GOOD EVENING'; }
  function chartMax(projects) { return Math.max(100, ...projects.map(p => Number(p.approvedBudget || 0))); }
  function barChart(projects) {
    if (!projects.length) return `<div class="bar-column"><span class="bar-label">No projects</span></div>`;
    const max = chartMax(projects);
    return projects.slice(0, 7).map(p => `<div class="bar-column" title="${esc(p.name)}"><div class="bar-pair"><div class="bar" style="height:${Math.max(2, Number(p.approvedBudget || 0) / max * 100)}%"></div><div class="bar spent" style="height:${Math.max(Number(p.expenditure || 0) ? 2 : 0, Number(p.expenditure || 0) / max * 100)}%"></div></div><span class="bar-label">${esc(p.code)}</span></div>`).join('');
  }
  function statusChart(projects) {
    if (!projects.length) return emptyState('No status data', 'Project status distribution appears when projects are added.');
    const statuses = ['ACTIVE', 'PLANNING', 'ON_HOLD', 'COMPLETED', 'CANCELLED']; const counts = Object.fromEntries(statuses.map(s => [s, projects.filter(p => p.status === s).length]));
    const colors = { ACTIVE: '#39866b', PLANNING: '#c9e2b4', ON_HOLD: '#e9bb69', COMPLETED: '#8bb3a0', CANCELLED: '#d8dfdb' };
    let cursor = 0; const stops = statuses.filter(s => counts[s]).map(s => { const from = cursor; cursor += counts[s] / projects.length * 100; return `${colors[s]} ${from}% ${cursor}%`; });
    const legend = statuses.filter(s => counts[s]).map(s => `<div class="status-legend-row"><i style="background:${colors[s]}"></i>${esc(statusLabel(s))}<strong style="margin-left:auto;color:#526158">${counts[s]}</strong></div>`).join('');
    return `<div class="status-chart-wrap"><div class="donut" style="background:conic-gradient(${stops.join(',')})"><div class="donut-center"><strong>${projects.length}</strong><span>PROJECTS</span></div></div><div class="status-legend">${legend}</div></div>`;
  }
  function activityIcon(action = '') { return action.includes('EXPENSE') ? 'receipt' : action.includes('TASK') ? 'task' : action.includes('MEMBER') ? 'users' : 'briefcase'; }
  function activityVerb(action = '') { return ({ PROJECT_CREATED: 'created a project', PROJECT_UPDATED: 'updated a project', PROJECT_DELETED: 'removed a project', TASK_CREATED: 'added a task', TASK_UPDATED: 'updated a task', TASK_DELETED: 'removed a task', EXPENSE_RECORDED: 'recorded an expense', EXPENSE_UPDATED: 'updated an expense', EXPENSE_DELETED: 'removed an expense', MEMBER_UPDATED: 'updated the project team' })[action] || 'updated the workspace'; }

  async function showProjects() {
    setPage('projects', 'Projects'); app.project = null; location.hash = '#projects';
    content.innerHTML = '<div class="loading-panel skeleton"></div>';
    try { app.projects = await api('/api/projects'); $('#project-nav-count').textContent = app.projects.length || ''; renderProjects(); }
    catch (err) { content.innerHTML = emptyState('Projects could not load', err.message); }
  }
  function renderProjects() {
    const query = app.search.toLowerCase().trim();
    const items = app.projects.filter(p => (app.statusFilter === 'ALL' || p.status === app.statusFilter) && (!query || `${p.name} ${p.code}`.toLowerCase().includes(query)));
    const cards = items.map(p => `<article class="project-card"><div class="project-card-top"><span class="project-code">${esc(p.code)}</span><span class="status-pill ${String(p.status).toLowerCase()}">${esc(statusLabel(p.status))}</span></div><h3>${esc(p.name)}</h3><p>${esc(p.description)}</p>${p.demoData ? '<span class="demo-chip">FICTIONAL DEMO VALUES</span>' : ''}<div class="project-card-progress"><div class="mini-progress"><div class="mini-progress-top"><span>Completion</span><strong>${p.progress}%</strong></div><div class="progress-track"><div class="progress-fill" style="width:${Math.max(0, Math.min(100, p.progress))}%"></div></div></div></div><div class="project-card-metrics"><div class="project-card-metric"><small>Approved budget</small><strong>${money(p.approvedBudget)}</strong></div><div class="project-card-metric"><small>Expenditure</small><strong>${money(p.expenditure)}</strong></div><div class="project-card-metric"><small>Remaining</small><strong>${money(p.remainingBudget)}</strong></div></div><div class="project-card-bottom"><span class="manager-chip"><span class="mini-avatar">${esc(initials(p.manager?.name))}</span>${esc(p.manager?.name || 'No manager')}</span><button class="text-button" data-action="view-project" data-id="${p.id}">View details ${icon('arrow')}</button></div></article>`).join('');
    content.innerHTML = `<div class="page-heading-row"><div><p class="eyebrow">PORTFOLIO</p><h2>Projects</h2><p>Search, review and manage project budgets and progress.</p></div>${isAdmin() ? `<button class="button button-primary" data-action="add-project">${icon('plus')} New project</button>` : ''}</div><div class="toolbar"><label class="search-box">${icon('search')}<input id="project-search" placeholder="Search by project name or code" value="${esc(app.search)}" aria-label="Search projects"></label><select id="project-status-filter" class="filter-select" aria-label="Filter projects by status"><option value="ALL">All statuses</option>${['ACTIVE', 'PLANNING', 'ON_HOLD', 'COMPLETED', 'CANCELLED'].map(s => `<option value="${s}" ${app.statusFilter === s ? 'selected' : ''}>${statusLabel(s)}</option>`).join('')}</select><span class="subtle-copy">${items.length} of ${app.projects.length} projects</span></div><div class="project-grid">${cards || emptyState('No projects match', 'Try a different search or status filter.')}</div>`;
    $('#project-search').addEventListener('input', e => { app.search = e.target.value; const cursor = e.target.selectionStart; renderProjects(); $('#project-search').focus(); $('#project-search').setSelectionRange(cursor, cursor); });
    $('#project-status-filter').addEventListener('change', e => { app.statusFilter = e.target.value; renderProjects(); });
  }

  async function showProject(id, requestedTab = 'overview') {
    setPage('projects', 'Project details'); app.projectId = id; app.tab = requestedTab; location.hash = `#project/${id}`;
    content.innerHTML = '<div class="loading-panel skeleton"></div>';
    try { app.project = await api(`/api/projects/${id}`); renderProjectDetail(); }
    catch (err) { content.innerHTML = emptyState('Project could not load', err.message, '<button class="button button-secondary button-small" data-action="go-projects">Back to projects</button>'); }
  }
  function renderProjectDetail() {
    const d = app.project; const p = d.project; const members = d.members || []; const active = members.filter(m => m.active).length;
    const tabNames = [['overview', 'Overview'], ['team', 'Team', members.length], ['tasks', 'Tasks', d.tasks.length], ['expenses', 'Expenses', d.expenses.length]];
    let tabContent = '';
    if (app.tab === 'overview') tabContent = renderProjectOverview(d);
    if (app.tab === 'team') tabContent = renderTeamTab(d);
    if (app.tab === 'tasks') tabContent = renderTasksTab(d);
    if (app.tab === 'expenses') tabContent = renderExpensesTab(d);
    content.innerHTML = `<div class="page-heading-row detail-heading-row"><div class="detail-title"><button class="icon-button back-button" data-action="go-projects" aria-label="Back to projects">${icon('arrow')}</button><div><div class="project-code">${esc(p.code)} · ${esc(date(p.startDate))} – ${esc(date(p.expectedCompletionDate))}</div><h2>${esc(p.name)}</h2></div></div><div class="detail-actions">${isAdmin() ? `<button class="button button-secondary button-small" data-action="edit-project">${icon('edit')} Edit</button><button class="button button-secondary button-small" data-action="delete-project">${icon('trash')} Delete</button>` : ''}</div></div>
      ${p.demoData ? '<div class="demo-chip" style="margin:0 0 10px">FICTIONAL DEMO VALUES — replace with verified company records</div>' : ''}
      <section class="detail-hero"><div class="detail-hero-content"><div><span class="detail-code">${esc(p.code)} <span class="status-pill light-pill ${String(p.status).toLowerCase()}">${esc(statusLabel(p.status))}</span></span><h2>${esc(p.name)}</h2><p>${esc(p.description)}</p></div><div class="detail-hero-side"><strong>${p.progress}%</strong><small>Overall completion · ${active} active team members</small><div class="progress-track" style="margin-top:10px;background:#ffffff30"><div class="progress-fill" style="width:${Math.max(0, Math.min(100, p.progress))}%;background:#c9e2b4"></div></div></div></div></section>
      <section class="detail-stats"><article class="detail-stat"><small>Approved budget</small><strong>${money(p.approvedBudget)}</strong><div class="detail-stat-foot">Approved project allocation</div></article><article class="detail-stat"><small>Total expenditure</small><strong>${money(p.expenditure)}</strong><div class="detail-stat-foot">${Number(p.budgetUtilization).toFixed(1)}% budget utilized</div></article><article class="detail-stat"><small>Remaining budget</small><strong>${money(p.remainingBudget)}</strong><div class="detail-stat-foot">Budget less recorded expenditure</div></article><article class="detail-stat"><small>Project manager</small><strong style="font-size:12px">${esc(p.manager?.name || '—')}</strong><div class="detail-stat-foot">${esc(p.manager?.jobTitle || 'Project manager')}</div></article></section>
      <nav class="tab-bar" aria-label="Project sections">${tabNames.map(([key, title, count]) => `<button class="tab-button ${app.tab === key ? 'active' : ''}" data-action="project-tab" data-tab="${key}">${title}${count !== undefined ? `<span class="tab-count">${count}</span>` : ''}</button>`).join('')}</nav><div id="project-tab-content">${tabContent}</div>`;
  }
  function renderProjectOverview(d) {
    const p = d.project; const breakdown = d.expenseBreakdown || []; const maximum = Math.max(1, ...breakdown.map(x => Number(x.amount)));
    const recent = d.expenses.slice(0, 5).map(e => `<tr><td><strong>${esc(e.title)}</strong><br><span class="subtle-copy">${esc(e.category)}</span></td><td>${esc(date(e.expenseDate))}</td><td>${moneyExact(e.amount)}</td><td>${esc(e.recordedBy?.name || '—')}</td></tr>`).join('');
    return `<div class="detail-content-grid"><article class="panel detail-panel"><div class="panel-heading"><div><h3>Project overview</h3><p>Key dates and project ownership</p></div></div><div class="detail-kv-grid"><div class="detail-kv"><small>Project manager</small><strong>${esc(p.manager?.name || '—')}</strong></div><div class="detail-kv"><small>Project status</small><strong>${esc(statusLabel(p.status))}</strong></div><div class="detail-kv"><small>Start date</small><strong>${esc(date(p.startDate))}</strong></div><div class="detail-kv"><small>Target completion</small><strong>${esc(date(p.expectedCompletionDate))}</strong></div><div class="detail-kv"><small>Active members</small><strong>${p.activeMembers} of ${p.assignedMembers} assigned</strong></div><div class="detail-kv"><small>Approved budget</small><strong>${moneyExact(p.approvedBudget)}</strong></div></div></article><article class="panel detail-panel"><div class="panel-heading"><div><h3>Expenditure by category</h3><p>Recorded project expense totals</p></div></div>${breakdown.length ? `<div class="breakdown-list">${breakdown.map(item => `<div class="breakdown-row"><span>${esc(item.category)}</span><div class="breakdown-bar"><span style="width:${Math.max(2, Number(item.amount) / maximum * 100)}%"></span></div><strong>${money(item.amount)}</strong></div>`).join('')}</div>` : emptyState('No expenses recorded', 'Expense totals by category will appear here.')}</article></div><article class="panel" style="margin-top:15px"><div class="panel-heading"><div><h3>Recent expenses</h3><p>Latest financial entries for this project</p></div><button class="text-button" data-action="project-tab" data-tab="expenses">View all ${icon('arrow')}</button></div>${recent ? `<div class="expense-table-wrap"><table class="data-table"><thead><tr><th>Expense</th><th>Date</th><th>Amount</th><th>Recorded by</th></tr></thead><tbody>${recent}</tbody></table></div>` : emptyState('No expenses recorded', 'Recorded expenses will appear here.')}</article>`;
  }
  function renderTeamTab(d) {
    const rows = d.members.map(m => `<tr><td><div class="table-assignee"><span class="mini-avatar">${esc(initials(m.employee?.name))}</span><strong>${esc(m.employee?.name || '—')}</strong></div><span class="subtle-copy">${esc(m.employee?.employeeCode || '')}</span></td><td>${esc(m.projectRole)}</td><td>${esc(m.employee?.jobTitle || '—')}</td><td>${esc(date(m.assignedDate))}</td><td><span class="status-pill ${m.active ? '' : 'cancelled'}">${m.active ? 'Active' : 'Inactive'}</span></td><td>${isAdmin() && m.active ? `<div class="table-actions"><button class="icon-action" data-action="remove-member" data-id="${m.employee.id}" aria-label="Remove member">${icon('minus')}</button></div>` : ''}</td></tr>`).join('');
    return `<article class="panel"><div class="panel-heading"><div><h3>Team members</h3><p>${d.project.activeMembers} active members · ${d.project.assignedMembers} assigned in total</p></div>${isAdmin() ? `<button class="button button-primary button-small" data-action="add-member">${icon('plus')} Assign member</button>` : ''}</div>${rows ? `<div class="table-wrap"><table class="data-table"><thead><tr><th>Employee</th><th>Project role</th><th>Job title</th><th>Assigned date</th><th>Status</th><th></th></tr></thead><tbody>${rows}</tbody></table></div>` : emptyState('No team members assigned', 'Assign employees to coordinate work on this project.')}</article>`;
  }
  function renderTasksTab(d) {
    const rows = d.tasks.map(t => `<tr><td><strong>${esc(t.title)}</strong><br><span class="subtle-copy">${esc(t.description)}</span></td><td>${t.assignee ? `<span class="table-assignee"><span class="mini-avatar">${esc(initials(t.assignee.name))}</span>${esc(t.assignee.name)}</span>` : 'Unassigned'}</td><td><span class="priority-mark ${String(t.priority).toLowerCase()}" style="display:inline-block;margin-right:6px"></span>${esc(titleCase(t.priority))}</td><td>${esc(date(t.dueDate))}</td><td><span class="status-pill ${String(t.status).toLowerCase()}">${esc(taskStatusLabel(t.status))}</span></td><td>${isAdmin() ? `<div class="table-actions"><button class="icon-action" data-action="edit-task" data-id="${t.id}" aria-label="Edit task">${icon('edit')}</button><button class="icon-action" data-action="delete-task" data-id="${t.id}" aria-label="Delete task">${icon('trash')}</button></div>` : ''}</td></tr>`).join('');
    return `<article class="panel"><div class="panel-heading"><div><h3>Project tasks</h3><p>Track owners, priority and due dates</p></div>${isAdmin() ? `<button class="button button-primary button-small" data-action="add-task">${icon('plus')} Add task</button>` : ''}</div>${rows ? `<div class="table-wrap"><table class="data-table"><thead><tr><th>Task</th><th>Assigned employee</th><th>Priority</th><th>Due date</th><th>Status</th><th></th></tr></thead><tbody>${rows}</tbody></table></div>` : emptyState('No tasks yet', 'Add tasks to track milestones and ownership.')}</article>`;
  }
  function renderExpensesTab(d) {
    const rows = d.expenses.map(e => `<tr><td><strong>${esc(e.title)}</strong><br><span class="subtle-copy">${esc(e.description)}</span></td><td>${esc(e.category)}</td><td>${esc(date(e.expenseDate))}</td><td><strong>${moneyExact(e.amount)}</strong></td><td>${esc(e.recordedBy?.name || '—')}</td><td>${isAdmin() ? `<div class="table-actions"><button class="icon-action" data-action="edit-expense" data-id="${e.id}" aria-label="Edit expense">${icon('edit')}</button><button class="icon-action" data-action="delete-expense" data-id="${e.id}" aria-label="Delete expense">${icon('trash')}</button></div>` : ''}</td></tr>`).join('');
    return `<article class="panel"><div class="panel-heading"><div><h3>Expense records</h3><p>All values are recalculated from saved expense entries</p></div>${isAdmin() ? `<button class="button button-primary button-small" data-action="add-expense">${icon('plus')} Record expense</button>` : ''}</div>${rows ? `<div class="table-wrap"><table class="data-table"><thead><tr><th>Expense</th><th>Category</th><th>Date</th><th>Amount</th><th>Recorded by</th><th></th></tr></thead><tbody>${rows}</tbody></table></div>` : emptyState('No expenses recorded', 'Record a project expense to update its financial overview.')}</article>`;
  }

  async function showEmployees() {
    setPage('employees', 'Employees'); app.project = null; location.hash = '#employees';
    content.innerHTML = '<div class="loading-panel skeleton"></div>';
    try { app.employees = await api('/api/employees'); renderEmployees(); }
    catch (err) { content.innerHTML = emptyState('Employees could not load', err.message); }
  }
  function renderEmployees() {
    const cards = app.employees.map(employee => `<article class="employee-card"><div class="employee-card-top"><div class="employee-avatar">${esc(initials(employee.name))}</div><div><h3>${esc(employee.name)}</h3><p class="emp-role">${esc(employee.jobTitle)} · ${esc(employee.role)}</p></div></div><div class="employee-card-meta"><span>Employee ID · ${esc(employee.employeeCode)}</span><span>Username · ${esc(employee.username)}</span><span>${esc(employee.email)}</span></div><div class="employee-card-footer"><span class="employee-state ${employee.active ? '' : 'inactive'}">${employee.active ? '● Active account' : '○ Deactivated'}</span><div class="table-actions"><button class="icon-action" data-action="edit-employee" data-id="${employee.id}" aria-label="Edit employee">${icon('edit')}</button>${employee.active ? `<button class="icon-action" data-action="deactivate-employee" data-id="${employee.id}" aria-label="Deactivate employee">${icon('minus')}</button>` : ''}</div></div></article>`).join('');
    content.innerHTML = `<div class="page-heading-row"><div><p class="eyebrow">PEOPLE</p><h2>Employees</h2><p>Manage employee accounts and workspace access.</p></div><button class="button button-primary" data-action="add-employee">${icon('plus')} Add employee</button></div><div class="toolbar"><label class="search-box">${icon('search')}<input id="employee-search" placeholder="Search employee, username or ID" aria-label="Search employees"></label><span class="subtle-copy">${app.employees.filter(e => e.active).length} active · ${app.employees.length} total</span></div><div id="employee-grid" class="employee-grid">${cards || emptyState('No employees yet', 'Add employee accounts to assign project work.')}</div>`;
    $('#employee-search').addEventListener('input', event => {
      const q = event.target.value.toLowerCase();
      const filtered = app.employees.filter(e => `${e.name} ${e.username} ${e.employeeCode} ${e.email}`.toLowerCase().includes(q));
      $('#employee-grid').innerHTML = filtered.length ? filtered.map(employee => `<article class="employee-card"><div class="employee-card-top"><div class="employee-avatar">${esc(initials(employee.name))}</div><div><h3>${esc(employee.name)}</h3><p class="emp-role">${esc(employee.jobTitle)} · ${esc(employee.role)}</p></div></div><div class="employee-card-meta"><span>Employee ID · ${esc(employee.employeeCode)}</span><span>Username · ${esc(employee.username)}</span><span>${esc(employee.email)}</span></div><div class="employee-card-footer"><span class="employee-state ${employee.active ? '' : 'inactive'}">${employee.active ? '● Active account' : '○ Deactivated'}</span><div class="table-actions"><button class="icon-action" data-action="edit-employee" data-id="${employee.id}" aria-label="Edit employee">${icon('edit')}</button>${employee.active ? `<button class="icon-action" data-action="deactivate-employee" data-id="${employee.id}" aria-label="Deactivate employee">${icon('minus')}</button>` : ''}</div></div></article>`).join('') : emptyState('No employees match', 'Try another employee name, username or ID.');
    });
  }

  function openDialog(title, subtitle, body, { wide = false, formId = '', submitLabel = 'Save changes' } = {}) {
    dialog.classList.toggle('wide', wide);
    dialogContent.innerHTML = `<div class="modal-heading"><div><h2 id="dialog-title">${esc(title)}</h2>${subtitle ? `<p>${esc(subtitle)}</p>` : ''}</div><button class="modal-close" type="button" data-action="close-dialog" aria-label="Close">${icon('close')}</button></div>${formId ? `<form id="${formId}" novalidate>${body}<div id="modal-error" class="form-error" role="alert" hidden></div><div class="modal-footer"><button class="button button-secondary" type="button" data-action="close-dialog">Cancel</button><button class="button button-primary" type="submit">${esc(submitLabel)}</button></div></form>` : body}`;
    dialog.showModal();
  }
  function projectFormFields(project = null, employees = []) {
    const p = project?.project || project || {};
    const employeeOptions = employees.filter(e => e.active).map(e => `<option value="${e.id}" ${Number(p.manager?.id) === e.id ? 'selected' : ''}>${esc(e.name)} · ${esc(e.employeeCode)}</option>`).join('');
    const today = new Date().toISOString().slice(0, 10);
    return `<div class="form-grid"><div class="form-field"><label for="f-name">Project name *</label><input id="f-name" name="name" maxlength="150" required value="${esc(p.name || '')}" placeholder="e.g. Regional distribution planning"></div><div class="form-field"><label for="f-code">Project code *</label><input id="f-code" name="code" maxlength="32" required value="${esc(p.code || '')}" placeholder="e.g. OPS-004"></div><div class="form-field full"><label for="f-description">Description *</label><textarea id="f-description" name="description" maxlength="1200" required placeholder="Project summary and scope">${esc(p.description || '')}</textarea><span class="form-help">Use verified project information. Mark fictional sample data clearly.</span></div><div class="form-field"><label for="f-manager">Project manager *</label><select id="f-manager" name="managerId" required><option value="">Choose an employee</option>${employeeOptions}</select></div><div class="form-field"><label for="f-budget">Approved budget (INR) *</label><input id="f-budget" name="approvedBudget" type="number" min="0" step="0.01" required value="${esc(p.approvedBudget ?? '')}" placeholder="0.00"></div><div class="form-field"><label for="f-start">Start date *</label><input id="f-start" name="startDate" type="date" required value="${esc(p.startDate || today)}"></div><div class="form-field"><label for="f-end">Expected completion *</label><input id="f-end" name="expectedCompletionDate" type="date" required value="${esc(p.expectedCompletionDate || '')}"></div><div class="form-field"><label for="f-status">Status *</label><select id="f-status" name="status">${['PLANNING', 'ACTIVE', 'ON_HOLD', 'COMPLETED', 'CANCELLED'].map(s => `<option value="${s}" ${p.status === s ? 'selected' : ''}>${statusLabel(s)}</option>`).join('')}</select></div><div class="form-field"><label class="form-checkbox"><input name="demoData" type="checkbox" ${p.demoData ? 'checked' : ''}> Mark as demo data</label><span class="form-help">Use this for fictional or unverified sample values.</span></div></div>`;
  }
  async function openProjectForm(existing = null) {
    try {
      const employees = await api('/api/employees');
      if (!employees.some(e => e.active)) throw new Error('Create an active employee account before adding a project.');
      const isEdit = Boolean(existing); const title = isEdit ? 'Edit project' : 'Create a project';
      openDialog(title, isEdit ? 'Update the project details and approved budget.' : 'Add a project to the internal portfolio.', projectFormFields(existing, employees), { wide: true, formId: 'project-form', submitLabel: isEdit ? 'Save project' : 'Create project' });
      $('#project-form').dataset.id = existing?.project?.id || existing?.id || '';
    } catch (err) { toast(err.message, true); }
  }
  function taskFormFields(task = null) {
    const p = app.project.project; const today = new Date().toISOString().slice(0, 10);
    const options = app.project.members.filter(m => m.active && m.employee.active).map(m => `<option value="${m.employee.id}" ${Number(task?.assignee?.id) === m.employee.id ? 'selected' : ''}>${esc(m.employee.name)} · ${esc(m.projectRole)}</option>`).join('');
    return `<div class="form-grid"><div class="form-field full"><label for="f-title">Task title *</label><input id="f-title" name="title" required maxlength="150" value="${esc(task?.title || '')}" placeholder="Task or milestone"></div><div class="form-field full"><label for="f-task-description">Description *</label><textarea id="f-task-description" name="description" required maxlength="1200">${esc(task?.description || '')}</textarea></div><div class="form-field"><label for="f-assignee">Assigned employee</label><select id="f-assignee" name="assigneeId"><option value="">Unassigned</option>${options}</select></div><div class="form-field"><label for="f-priority">Priority</label><select id="f-priority" name="priority">${['LOW', 'MEDIUM', 'HIGH', 'URGENT'].map(s => `<option value="${s}" ${task?.priority === s ? 'selected' : ''}>${titleCase(s)}</option>`).join('')}</select></div><div class="form-field"><label for="f-task-start">Start date *</label><input id="f-task-start" name="startDate" type="date" required value="${esc(task?.startDate || today)}"></div><div class="form-field"><label for="f-task-due">Due date *</label><input id="f-task-due" name="dueDate" type="date" required value="${esc(task?.dueDate || p.expectedCompletionDate)}"></div><div class="form-field"><label for="f-task-status">Status</label><select id="f-task-status" name="status">${['TO_DO', 'IN_PROGRESS', 'COMPLETED', 'BLOCKED'].map(s => `<option value="${s}" ${task?.status === s ? 'selected' : ''}>${taskStatusLabel(s)}</option>`).join('')}</select></div></div>`;
  }
  function expenseFormFields(expense = null) {
    const today = new Date().toISOString().slice(0, 10);
    return `<div class="form-grid"><div class="form-field full"><label for="f-exp-title">Expense title *</label><input id="f-exp-title" name="title" required maxlength="150" value="${esc(expense?.title || '')}" placeholder="What was purchased or paid for?"></div><div class="form-field"><label for="f-category">Category *</label><input id="f-category" name="category" list="expense-categories" required maxlength="80" value="${esc(expense?.category || '')}" placeholder="e.g. Travel"><datalist id="expense-categories"><option>Travel</option><option>Operations</option><option>Training</option><option>Equipment</option><option>Supplies</option><option>Personnel</option></datalist></div><div class="form-field"><label for="f-amount">Amount (INR) *</label><input id="f-amount" name="amount" type="number" min="0.01" step="0.01" required value="${esc(expense?.amount ?? '')}" placeholder="0.00"></div><div class="form-field"><label for="f-exp-date">Expense date *</label><input id="f-exp-date" name="expenseDate" type="date" required value="${esc(expense?.expenseDate || today)}"></div><div class="form-field full"><label for="f-exp-description">Description *</label><textarea id="f-exp-description" name="description" maxlength="1200" required placeholder="Add context for this financial record">${esc(expense?.description || '')}</textarea></div></div>`;
  }
  function employeeFormFields(employee = null) {
    return `<div class="form-grid"><div class="form-field"><label for="f-emp-code">Employee ID *</label><input id="f-emp-code" name="employeeCode" required maxlength="32" value="${esc(employee?.employeeCode || '')}" placeholder="EMP-004"></div><div class="form-field"><label for="f-emp-name">Full name *</label><input id="f-emp-name" name="name" required maxlength="120" value="${esc(employee?.name || '')}"></div><div class="form-field"><label for="f-username">Username *</label><input id="f-username" name="username" required maxlength="80" autocomplete="off" value="${esc(employee?.username || '')}"></div><div class="form-field"><label for="f-email">Work email *</label><input id="f-email" name="email" type="email" required maxlength="190" value="${esc(employee?.email || '')}"></div><div class="form-field"><label for="f-job-title">Job title *</label><input id="f-job-title" name="jobTitle" required maxlength="100" value="${esc(employee?.jobTitle || '')}"></div><div class="form-field"><label for="f-role">Access role *</label><select id="f-role" name="role"><option value="EMPLOYEE" ${employee?.role !== 'ADMIN' ? 'selected' : ''}>Employee</option><option value="ADMIN" ${employee?.role === 'ADMIN' ? 'selected' : ''}>Administrator</option></select></div><div class="form-field full"><label for="f-password">${employee ? 'Set a new password (optional)' : 'Initial password *'}</label><input id="f-password" name="password" type="password" ${employee ? '' : 'required'} minlength="12" maxlength="100" autocomplete="new-password" placeholder="At least 12 characters"><span class="form-help">Passwords are securely hashed and cannot be viewed later. Leave blank when no password change is needed.</span></div><div class="form-field full"><label class="form-checkbox"><input name="active" type="checkbox" ${employee?.active !== false ? 'checked' : ''}> Account active</label></div></div>`;
  }

  function serialize(form) { return Object.fromEntries(new FormData(form).entries()); }
  document.addEventListener('submit', async event => {
    const form = event.target;
    if (!['project-form', 'task-form', 'expense-form', 'member-form', 'employee-form'].includes(form.id)) return;
    event.preventDefault();
    if (!form.checkValidity()) { form.reportValidity(); return; }
    const submit = $('button[type="submit"]', form); submit.disabled = true; $('#modal-error').hidden = true;
    const raw = serialize(form); let url = ''; let method = form.dataset.id ? 'PUT' : 'POST'; let payload = {};
    try {
      if (form.id === 'project-form') {
        payload = { name: raw.name, code: raw.code, description: raw.description, managerId: Number(raw.managerId), approvedBudget: Number(raw.approvedBudget), startDate: raw.startDate, expectedCompletionDate: raw.expectedCompletionDate, status: raw.status, demoData: form.demoData.checked };
        url = form.dataset.id ? `/api/projects/${form.dataset.id}` : '/api/projects';
      } else if (form.id === 'task-form') {
        payload = { title: raw.title, description: raw.description, assigneeId: raw.assigneeId ? Number(raw.assigneeId) : null, priority: raw.priority, startDate: raw.startDate, dueDate: raw.dueDate, status: raw.status };
        url = form.dataset.id ? `/api/projects/${app.projectId}/tasks/${form.dataset.id}` : `/api/projects/${app.projectId}/tasks`;
      } else if (form.id === 'expense-form') {
        payload = { title: raw.title, category: raw.category, amount: Number(raw.amount), expenseDate: raw.expenseDate, description: raw.description };
        url = form.dataset.id ? `/api/projects/${app.projectId}/expenses/${form.dataset.id}` : `/api/projects/${app.projectId}/expenses`;
      } else if (form.id === 'member-form') {
        payload = { employeeId: Number(raw.employeeId), projectRole: raw.projectRole, assignedDate: raw.assignedDate, active: true };
        url = `/api/projects/${app.projectId}/members`;
      } else if (form.id === 'employee-form') {
        payload = { employeeCode: raw.employeeCode, name: raw.name, username: raw.username, email: raw.email, password: raw.password || null, jobTitle: raw.jobTitle, role: raw.role, active: form.active.checked };
        url = form.dataset.id ? `/api/employees/${form.dataset.id}` : '/api/employees';
      }
      await api(url, { method, body: JSON.stringify(payload) });
      dialog.close(); toast(form.id === 'employee-form' ? 'Employee account saved.' : 'Changes saved.');
      if (form.id === 'employee-form') await showEmployees();
      else if (form.id === 'project-form') { if (app.page === 'dashboard') await showDashboard(); else if (form.dataset.id) await showProject(Number(form.dataset.id)); else await showProjects(); }
      else await showProject(app.projectId, form.id === 'task-form' ? 'tasks' : form.id === 'expense-form' ? 'expenses' : form.id === 'member-form' ? 'team' : 'overview');
    } catch (err) { const box = $('#modal-error'); box.textContent = err.message; box.hidden = false; }
    finally { submit.disabled = false; }
  });

  async function handleAction(button) {
    const action = button.dataset.action; const id = Number(button.dataset.id);
    if (action === 'close-dialog') { dialog.close(); return; }
    if (action === 'retry-dashboard') { await showDashboard(); return; }
    if (action === 'go-projects') { closeMobileMenu(); location.hash = '#projects'; await showProjects(); return; }
    if (action === 'view-project') { await showProject(id); return; }
    if (action === 'add-project') { await openProjectForm(); return; }
    if (action === 'edit-project') { await openProjectForm(app.project); return; }
    if (action === 'delete-project') {
      if (!confirm(`Delete “${app.project.project.name}”? Projects with expense history cannot be deleted.`)) return;
      try { await api(`/api/projects/${app.projectId}`, { method: 'DELETE' }); toast('Project deleted.'); location.hash = '#projects'; await showProjects(); }
      catch (err) { toast(err.message, true); }
      return;
    }
    if (action === 'project-tab') { app.tab = button.dataset.tab; renderProjectDetail(); return; }
    if (action === 'add-task' || action === 'edit-task') {
      const task = action === 'edit-task' ? app.project.tasks.find(x => x.id === id) : null;
      openDialog(task ? 'Edit task' : 'Add a task', 'Track a project milestone and its owner.', taskFormFields(task), { wide: true, formId: 'task-form', submitLabel: task ? 'Save task' : 'Add task' });
      $('#task-form').dataset.id = task?.id || ''; return;
    }
    if (action === 'delete-task') {
      const task = app.project.tasks.find(x => x.id === id); if (!confirm(`Delete task “${task?.title || ''}”?`)) return;
      try { await api(`/api/projects/${app.projectId}/tasks/${id}`, { method: 'DELETE' }); toast('Task deleted.'); await showProject(app.projectId, 'tasks'); }
      catch (err) { toast(err.message, true); } return;
    }
    if (action === 'add-expense' || action === 'edit-expense') {
      const expense = action === 'edit-expense' ? app.project.expenses.find(x => x.id === id) : null;
      openDialog(expense ? 'Edit expense' : 'Record an expense', 'Project financial totals update from saved entries.', expenseFormFields(expense), { wide: true, formId: 'expense-form', submitLabel: expense ? 'Save expense' : 'Record expense' });
      $('#expense-form').dataset.id = expense?.id || ''; return;
    }
    if (action === 'delete-expense') {
      const expense = app.project.expenses.find(x => x.id === id); if (!confirm(`Delete expense “${expense?.title || ''}”? Financial totals will be recalculated.`)) return;
      try { await api(`/api/projects/${app.projectId}/expenses/${id}`, { method: 'DELETE' }); toast('Expense deleted.'); await showProject(app.projectId, 'expenses'); }
      catch (err) { toast(err.message, true); } return;
    }
    if (action === 'add-member') { await openMemberForm(); return; }
    if (action === 'remove-member') {
      const member = app.project.members.find(m => m.employee.id === id); if (!confirm(`Remove ${member?.employee.name || 'this employee'} from the active project team?`)) return;
      try { await api(`/api/projects/${app.projectId}/members/${id}`, { method: 'DELETE' }); toast('Project member updated.'); await showProject(app.projectId, 'team'); }
      catch (err) { toast(err.message, true); } return;
    }
    if (action === 'add-employee' || action === 'edit-employee') {
      const employee = action === 'edit-employee' ? app.employees.find(x => x.id === id) : null;
      openDialog(employee ? 'Edit employee' : 'Add employee', 'Account credentials are securely hashed before storage.', employeeFormFields(employee), { wide: true, formId: 'employee-form', submitLabel: employee ? 'Save employee' : 'Create account' });
      $('#employee-form').dataset.id = employee?.id || ''; return;
    }
    if (action === 'deactivate-employee') {
      const employee = app.employees.find(x => x.id === id); if (!confirm(`Deactivate ${employee?.name || 'this account'}? They will no longer be able to sign in.`)) return;
      try { await api(`/api/employees/${id}`, { method: 'DELETE' }); toast('Employee account deactivated.'); await showEmployees(); }
      catch (err) { toast(err.message, true); } return;
    }
    if (action === 'show-activity') { location.hash = '#activity'; await showActivity(); return; }
    if (action === 'load-more-activity') { await loadMoreActivity(); return; }
  }
  document.addEventListener('click', async event => {
    const button = event.target.closest('[data-action]');
    if (button) { event.preventDefault(); await handleAction(button); }
  });

  async function openMemberForm() {
    try {
      const employees = await api('/api/employees');
      const assigned = new Set(app.project.members.map(m => m.employee.id));
      const options = employees.filter(e => e.active && !assigned.has(e.id)).map(e => `<option value="${e.id}">${esc(e.name)} · ${esc(e.employeeCode)}</option>`).join('');
      if (!options) { toast('All active employees are already assigned to this project.', true); return; }
      const today = new Date().toISOString().slice(0, 10);
      const body = `<div class="form-grid"><div class="form-field full"><label for="f-member">Employee *</label><select id="f-member" name="employeeId" required><option value="">Choose an employee</option>${options}</select></div><div class="form-field"><label for="f-member-role">Project role *</label><input id="f-member-role" name="projectRole" required maxlength="100" placeholder="e.g. Field coordinator"></div><div class="form-field"><label for="f-member-date">Assigned date *</label><input id="f-member-date" name="assignedDate" type="date" value="${today}" required></div></div>`;
      openDialog('Assign a team member', 'Active employees can be assigned to this project.', body, { formId: 'member-form', submitLabel: 'Assign member' });
    } catch (err) { toast(err.message, true); }
  }

  function activityRows(records) {
    return records.map(a => `<div class="activity-row"><div class="activity-icon">${icon(activityIcon(a.action))}</div><div class="activity-copy"><p><strong>${esc(a.actor?.name || 'Workspace')}</strong> ${esc(activityVerb(a.action))}. ${esc(a.description)}</p><small>${esc(a.projectName || 'Workspace')} · ${esc(date(a.occurredAt))} ${esc(a.occurredAt?.slice(11, 16) || '')}</small></div></div>`).join('');
  }
  async function showActivity() {
    setPage('activity', 'Activity history'); app.activityPage = 0; app.activityHasNext = false; content.innerHTML = '<div class="loading-panel skeleton"></div>';
    try {
      const result = await api('/api/activity?page=0&size=20'); app.activityHasNext = result.hasNext;
      content.innerHTML = `<div class="page-heading-row"><div><p class="eyebrow">WORKSPACE HISTORY</p><h2>Activity history</h2><p>Recent project, team, task and expenditure changes.</p></div></div><article class="panel"><div id="activity-list" class="activity-list">${activityRows(result.content) || emptyState('No activity yet', 'Project changes will appear here.')}</div>${result.hasNext ? '<div style="text-align:center;padding-top:10px"><button class="button button-secondary button-small" data-action="load-more-activity">Load more activity</button></div>' : ''}</article>`;
    } catch (err) { content.innerHTML = emptyState('Activity history could not load', err.message); }
  }
  async function loadMoreActivity() {
    const button = $('[data-action="load-more-activity"]'); if (!button || !app.activityHasNext) return;
    button.disabled = true;
    try {
      const result = await api(`/api/activity?page=${app.activityPage + 1}&size=20`); app.activityPage = result.page; app.activityHasNext = result.hasNext;
      $('#activity-list').insertAdjacentHTML('beforeend', activityRows(result.content));
      if (!result.hasNext) button.remove(); else button.disabled = false;
    } catch (err) { button.disabled = false; toast(err.message, true); }
  }
  $('#notification-button').addEventListener('click', async () => {
    const popover = $('#notification-popover'); popover.hidden = !popover.hidden;
    if (popover.hidden) return;
    popover.innerHTML = '<h3>Recent activity</h3><div class="skeleton" style="height:80px"></div>';
    try {
      const result = await api('/api/activity?page=0&size=4'); const records = result.content || [];
      popover.innerHTML = `<h3>Recent activity</h3>${records.length ? records.map(a => `<div class="activity-row"><div class="activity-icon">${icon(activityIcon(a.action))}</div><div class="activity-copy"><p><strong>${esc(a.actor?.name || 'Workspace')}</strong> ${esc(activityVerb(a.action))}.</p><small>${esc(a.projectName || 'Workspace')} · ${esc(relative(a.occurredAt))}</small></div></div>`).join('') : '<p class="subtle-copy">No recent activity.</p>'}<button class="text-button" data-action="show-activity">View activity history ${icon('arrow')}</button>`;
      $('#notification-dot').hidden = records.length === 0;
    } catch (err) { popover.innerHTML = `<h3>Recent activity</h3><p class="subtle-copy">${esc(err.message)}</p>`; }
  });
  document.addEventListener('click', event => {
    if (!event.target.closest('#notification-popover') && !event.target.closest('#notification-button')) $('#notification-popover').hidden = true;
  });

  startApp();
})();
