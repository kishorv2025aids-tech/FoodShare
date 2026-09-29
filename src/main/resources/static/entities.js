const entityConfig = {
    donors: {
        endpoint: "/api/donors",
        headers: ["ID", "Donor", "Email", "Phone"],
        render: row => [idCell(row.id), mainCell(row.name), textCell(row.email), textCell(row.phone)]
    },
    ngos: {
        endpoint: "/api/ngos",
        headers: ["ID", "NGO partner", "Email", "Phone"],
        render: row => [idCell(row.id), mainCell(row.name), textCell(row.email), textCell(row.phone)]
    },
    "food-listings": {
        endpoint: "/api/food-listings",
        headers: ["ID", "Food listing", "Donor", "Quantity", "Safe until", "Status"],
        render: row => [idCell(row.id), mainCell(row.foodType), textCell(row.donor?.name), textCell(row.quantity), textCell(formatDate(row.safeToEatUntil)), statusCell(row.status)]
    },
    claims: {
        endpoint: "/api/claims",
        headers: ["ID", "Food listing", "NGO partner", "Claimed at", "Status"],
        render: row => [idCell(row.id), mainCell(row.foodListing?.foodType, `Listing #${row.foodListing?.id ?? ""}`), textCell(row.ngo?.name), textCell(formatDate(row.claimedAt)), statusCell(row.status)]
    }
};

let allRecords = {};
let activeEntity = "donors";

function safe(value) {
    return String(value ?? "—").replace(/[&<>"']/g, character => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[character]);
}

function idCell(value) { return `<span class="record-id">#${safe(value)}</span>`; }
function mainCell(value, detail = "") { return `<span>${safe(value)}</span>${detail ? `<span class="record-secondary">${safe(detail)}</span>` : ""}`; }
function textCell(value) { return safe(value); }

function formatDate(value) {
    if (!value) return "—";
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? "—" : new Intl.DateTimeFormat(undefined, { dateStyle: "medium", timeStyle: "short" }).format(date);
}

function statusCell(value) {
    const status = ["AVAILABLE", "CLAIMED", "COLLECTED", "EXPIRED", "ACTIVE", "COMPLETED"].includes(value) ? value : "UNKNOWN";
    const style = status === "ACTIVE" ? "claimed" : status === "COMPLETED" ? "collected" : status.toLowerCase();
    return `<span class="status-pill ${style}"><i class="status-dot ${style}-dot"></i>${safe(status)}</span>`;
}

function renderActiveEntity() {
    const config = entityConfig[activeEntity];
    const term = document.querySelector("#record-search").value.trim().toLowerCase();
    const records = (allRecords[activeEntity] || []).filter(record => JSON.stringify(record).toLowerCase().includes(term));
    document.querySelector("#entity-head").innerHTML = `<tr>${config.headers.map(header => `<th scope="col">${header}</th>`).join("")}</tr>`;
    document.querySelector("#records-summary").textContent = `${records.length} ${records.length === 1 ? "record" : "records"} shown`;
    document.querySelector("#entity-rows").innerHTML = records.length
        ? records.map(record => `<tr>${config.render(record).map(value => `<td>${value}</td>`).join("")}</tr>`).join("")
        : `<tr><td class="empty-records" colspan="${config.headers.length}">${term ? "No records match your search." : "No records found for this entity."}</td></tr>`;
}

async function fetchJson(path) {
    const response = await fetch(path, { headers: { Accept: "application/json" } });
    if (!response.ok) throw new Error(`Could not load records (${response.status})`);
    return response.json();
}

async function loadEntities() {
    const refresh = document.querySelector("#records-refresh");
    const errorToast = document.querySelector("#error-toast");
    refresh.disabled = true;
    errorToast.classList.remove("visible");
    document.querySelector("#records-summary").textContent = "Loading project records";
    try {
        const csrf = await fetchJson("/api/auth/csrf");
        const csrfInput = document.createElement("input");
        csrfInput.type = "hidden";
        csrfInput.name = "_csrf";
        csrfInput.value = csrf.token;
        document.querySelector("#logout-form").append(csrfInput);
        const currentUser = await fetchJson("/api/auth/me");
        document.querySelector("#account-label").textContent = `${currentUser.role} / ${currentUser.email}`;
        await Promise.all(Object.entries(entityConfig).map(async ([key, config]) => {
            allRecords[key] = await fetchJson(config.endpoint);
            document.querySelector(`#${key}-tab-count`).textContent = allRecords[key].length;
        }));
        renderActiveEntity();
        document.querySelector("#records-updated").textContent = `Updated ${new Intl.DateTimeFormat(undefined, { hour: "numeric", minute: "2-digit" }).format(new Date())}`;
    } catch (error) {
        errorToast.textContent = "Could not load project records. Sign in again or refresh the page.";
        errorToast.classList.add("visible");
        document.querySelector("#records-summary").textContent = "Records unavailable";
    } finally {
        refresh.disabled = false;
    }
}

document.querySelectorAll(".entity-tab").forEach(tab => tab.addEventListener("click", () => {
    document.querySelectorAll(".entity-tab").forEach(item => {
        item.classList.toggle("active", item === tab);
        item.setAttribute("aria-selected", String(item === tab));
    });
    activeEntity = tab.dataset.entity;
    renderActiveEntity();
}));
document.querySelector("#record-search").addEventListener("input", renderActiveEntity);
document.querySelector("#records-refresh").addEventListener("click", loadEntities);
document.querySelector("#footer-year").textContent = new Date().getFullYear();
loadEntities();