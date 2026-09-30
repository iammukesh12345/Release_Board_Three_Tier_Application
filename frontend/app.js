const API = '/api/tasks';           // nginx proxies /api to the backend container
const list = document.getElementById('list');
const empty = document.getElementById('empty');
const count = document.getElementById('count');
const errorBox = document.getElementById('error');

function showError(msg) { errorBox.textContent = msg; errorBox.hidden = !msg; }

async function call(url, options) {
  try {
    const res = await fetch(url, options);
    if (!res.ok) throw new Error('Request failed with status ' + res.status);
    showError('');
    return res.status === 204 ? null : res.json();
  } catch (e) {
    showError('Could not reach the server. Check that the backend is running, then try again.');
    throw e;
  }
}

function render(tasks) {
  list.innerHTML = '';
  empty.hidden = tasks.length > 0;
  const done = tasks.filter(t => t.done).length;
  count.textContent = tasks.length ? done + ' of ' + tasks.length + ' done' : '';

  tasks.forEach(t => {
    const li = document.createElement('li');
    if (t.done) li.className = 'done';

    const box = document.createElement('input');
    box.type = 'checkbox'; box.checked = t.done;
    box.setAttribute('aria-label', 'Mark "' + t.title + '" as done');
    box.onchange = async () => { await call(API + '/' + t.id + '/toggle', { method: 'PUT' }); load(); };

    const label = document.createElement('span');
    label.textContent = t.title;           // textContent avoids XSS

    const del = document.createElement('button');
    del.className = 'remove'; del.textContent = 'Delete';
    del.setAttribute('aria-label', 'Delete "' + t.title + '"');
    del.onclick = async () => { await call(API + '/' + t.id, { method: 'DELETE' }); load(); };

    li.append(box, label, del);
    list.append(li);
  });
}

async function load() { render(await call(API)); }

document.getElementById('add-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const input = document.getElementById('title');
  await call(API, { method: 'POST', headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ title: input.value.trim() }) });
  input.value = '';
  load();
});

load().catch(() => {});
