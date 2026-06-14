const tokenKey = "miniS3Token";

function token() {
    return localStorage.getItem(tokenKey);
}

function requireAuth() {
    if (!token()) {
        window.location.href = "/login.html";
    }
}

function showAlert(message, type = "danger") {
    const alert = document.getElementById("alert");
    if (!alert) {
        return;
    }
    alert.className = `alert alert-${type}`;
    alert.textContent = message;
}

async function api(path, options = {}) {
    const headers = options.headers || {};
    if (token()) {
        headers.Authorization = `Bearer ${token()}`;
    }
    if (options.body && !(options.body instanceof FormData)) {
        headers["Content-Type"] = "application/json";
    }

    const response = await fetch(path, { ...options, headers });
    if (!response.ok) {
        let message = `Request failed with status ${response.status}`;
        try {
            const error = await response.json();
            message = error.message || message;
        } catch {
            // keep default message
        }
        throw new Error(message);
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
}

async function downloadFile(path, fileName) {
    const response = await fetch(path, {
        headers: {
            Authorization: `Bearer ${token()}`
        }
    });

    if (!response.ok) {
        throw new Error(`Download failed with status ${response.status}`);
    }

    const blob = await response.blob();
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
}

function setupLogout() {
    const logoutButton = document.getElementById("logoutButton");
    if (logoutButton) {
        logoutButton.addEventListener("click", () => {
            localStorage.removeItem(tokenKey);
            window.location.href = "/login.html";
        });
    }
}

function initLoginPage() {
    const form = document.getElementById("loginForm");
    if (!form) {
        return;
    }

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        try {
            const response = await api("/api/auth/login", {
                method: "POST",
                body: JSON.stringify({
                    email: document.getElementById("email").value,
                    password: document.getElementById("password").value
                })
            });
            localStorage.setItem(tokenKey, response.token);
            window.location.href = "/dashboard.html";
        } catch (error) {
            showAlert(error.message);
        }
    });
}

async function loadBuckets() {
    const table = document.getElementById("bucketTable");
    if (!table) {
        return;
    }

    const buckets = await api("/api/buckets");
    table.innerHTML = buckets.map((bucket) => `
        <tr>
            <td>${bucket.bucketName}</td>
            <td><span class="badge text-bg-${bucket.visibility === "PUBLIC" ? "success" : "secondary"}">${bucket.visibility}</span></td>
            <td>${new Date(bucket.createdAt).toLocaleString()}</td>
            <td class="text-end">
                <a class="btn btn-sm btn-outline-primary" href="/bucket.html?id=${bucket.id}">Open</a>
            </td>
        </tr>
    `).join("");
}

function initDashboardPage() {
    const form = document.getElementById("bucketForm");
    const refresh = document.getElementById("refreshBuckets");
    if (!form) {
        return;
    }
    requireAuth();
    setupLogout();
    loadBuckets().catch((error) => showAlert(error.message));

    refresh.addEventListener("click", () => loadBuckets().catch((error) => showAlert(error.message)));
    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        try {
            await api("/api/buckets", {
                method: "POST",
                body: JSON.stringify({
                    bucketName: document.getElementById("bucketName").value,
                    visibility: document.getElementById("visibility").value
                })
            });
            form.reset();
            await loadBuckets();
            showAlert("Bucket created", "success");
        } catch (error) {
            showAlert(error.message);
        }
    });
}

function bucketId() {
    return new URLSearchParams(window.location.search).get("id");
}

async function loadBucket() {
    const id = bucketId();
    const bucket = await api(`/api/buckets/${id}`);
    document.getElementById("bucketTitle").textContent = bucket.bucketName;
    document.getElementById("bucketMeta").textContent = `${bucket.visibility} · created ${new Date(bucket.createdAt).toLocaleString()}`;
}

async function loadFiles() {
    const id = bucketId();
    const search = document.getElementById("searchInput").value.trim();
    const path = search ? `/api/buckets/${id}/files?search=${encodeURIComponent(search)}` : `/api/buckets/${id}/files`;
    const files = await api(path);
    const table = document.getElementById("fileTable");
    table.innerHTML = files.map((file) => `
        <tr>
            <td>${file.fileName}</td>
            <td>${file.fileSize} bytes</td>
            <td>${file.contentType || "unknown"}</td>
            <td class="text-end">
                <button class="btn btn-sm btn-outline-primary" data-download-id="${file.id}" data-download-name="${file.fileName}">Download</button>
            </td>
        </tr>
    `).join("");

    table.querySelectorAll("[data-download-id]").forEach((button) => {
        button.addEventListener("click", async () => {
            try {
                await downloadFile(
                    `/api/buckets/${id}/files/${button.dataset.downloadId}/download`,
                    button.dataset.downloadName
                );
            } catch (error) {
                showAlert(error.message);
            }
        });
    });
}

function initBucketPage() {
    const form = document.getElementById("uploadForm");
    if (!form) {
        return;
    }
    requireAuth();
    setupLogout();
    loadBucket().catch((error) => showAlert(error.message));
    loadFiles().catch((error) => showAlert(error.message));

    document.getElementById("searchInput").addEventListener("input", () => {
        loadFiles().catch((error) => showAlert(error.message));
    });

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const fileInput = document.getElementById("fileInput");
        const formData = new FormData();
        formData.append("file", fileInput.files[0]);

        try {
            await api(`/api/buckets/${bucketId()}/files`, {
                method: "POST",
                body: formData
            });
            form.reset();
            await loadFiles();
            showAlert("File uploaded", "success");
        } catch (error) {
            showAlert(error.message);
        }
    });
}

initLoginPage();
initDashboardPage();
initBucketPage();
