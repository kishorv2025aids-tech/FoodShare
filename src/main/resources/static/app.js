const elements = {
    availableCount: document.querySelector("#available-count"),
    claimedCount: document.querySelector("#claimed-count"),
    collectedCount: document.querySelector("#collected-count"),
    donorCount: document.querySelector("#donor-count"),
    ngoCount: document.querySelector("#ngo-count"),
    divertedTotal: document.querySelector("#diverted-total"),
    listingCount: document.querySelector("#listing-count"),
    listingRows: document.querySelector("#listing-rows"),
    updatedLabel: document.querySelector("#updated-label"),
    todayLabel: document.querySelector("#today-label"),
    monthLabel: document.querySelector("#month-label"),
    footerYear: document.querySelector("#footer-year"),
    refreshButton: document.querySelector("#refresh-button"),
    errorToast: document.querySelector("#error-toast")
};

const statusLabels = {
    AVAILABLE: "Available",
    CLAIMED: "Claimed",
    COLLECTED: "Collected",
    EXPIRED: "Expired"
};

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, character => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[character]);
}

function formatDate(value) {
    if (!value) return "Not set";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return "Not set";
    return new Intl.DateTimeFormat(undefined, { month: "short", day: "numeric", hour: "numeric", minute: "2-digit" }).format(date);
}

function setStatusCounts(listings) {
    const counts = { AVAILABLE: 0, CLAIMED: 0, COLLECTED: 0, EXPIRED: 0 };
    listings.forEach(listing => {
        if (Object.hasOwn(counts, listing.status)) counts[listing.status] += 1;
    });

    elements.availableCount.textContent = counts.AVAILABLE;
    elements.claimedCount.textContent = counts.CLAIMED;
    elements.collectedCount.textContent = counts.COLLECTED;
    document.querySelector("#mix-available").textContent = counts.AVAILABLE;
    document.querySelector("#mix-claimed").textContent = counts.CLAIMED;
    document.querySelector("#mix-collected").textContent = counts.COLLECTED;
    document.querySelector("#mix-expired").textContent = counts.EXPIRED;

    const total = listings.length || 1;
    document.querySelector(".segment-available").style.width = `${counts.AVAILABLE / total * 100}%`;
    document.querySelector(".segment-claimed").style.width = `${counts.CLAIMED / total * 100}%`;
    document.querySelector(".segment-collected").style.width = `${counts.COLLECTED / total * 100}%`;
    document.querySelector(".segment-expired").style.width = `${counts.EXPIRED / total * 100}%`;
}

function renderListings(listings) {
    const newestFirst = [...listings].sort((first, second) => new Date(second.createdAt) - new Date(first.createdAt));
    elements.listingCount.textContent = `${listings.length} ${listings.length === 1 ? "record" : "records"}`;

    if (newestFirst.length === 0) {
        elements.listingRows.innerHTML = '<tr><td colspan="4" class="table-message">No food listings yet.</td></tr>';
        return;
    }

    elements.listingRows.innerHTML = newestFirst.slice(0, 8).map(listing => {
        const status = statusLabels[listing.status] ? listing.status : "EXPIRED";
        const donorName = listing.donor?.name || "Unknown donor";
        return `<tr>
            <td><div class="food-cell"><span class="food-name">${escapeHtml(listing.foodType)}</span><span class="food-donor">${escapeHtml(donorName)}</span></div></td>
            <td class="quantity-cell">${escapeHtml(listing.quantity)}</td>
            <td class="safe-cell">${escapeHtml(formatDate(listing.safeToEatUntil))}</td>
            <td><span class="status-pill ${status.toLowerCase()}"><i class="status-dot ${status.toLowerCase()}-dot"></i>${statusLabels[status]}</span></td>
        </tr>`;
    }).join("");
}

async function getJson(path) {
    const response = await fetch(path, { headers: { Accept: "application/json" } });
    if (!response.ok) throw new Error(`Request failed (${response.status})`);
    return response.json();
}

async function refreshDashboard() {
    elements.refreshButton.disabled = true;
    elements.refreshButton.classList.add("is-loading");
    elements.updatedLabel.textContent = "Updating from FoodShare API";
    elements.errorToast.classList.remove("visible");

    const now = new Date();
    const monthQuery = `year=${now.getFullYear()}&month=${now.getMonth() + 1}`;
    elements.todayLabel.textContent = new Intl.DateTimeFormat(undefined, { weekday: "short", month: "short", day: "numeric" }).format(now);
    elements.monthLabel.textContent = `Collected food, ${new Intl.DateTimeFormat(undefined, { month: "long", year: "numeric" }).format(now)}`;
    elements.footerYear.textContent = String(now.getFullYear());

    try {
        const [available, donors, ngos, diverted] = await Promise.all([
            getJson("/api/food-listings/available"),
            getJson("/api/donors"),
            getJson("/api/ngos"),
            getJson(`/api/food-listings/diverted?${monthQuery}`)
        ]);
        const listings = await getJson("/api/food-listings");

        elements.availableCount.textContent = available.length;
        elements.donorCount.textContent = donors.length;
        elements.ngoCount.textContent = ngos.length;
        elements.divertedTotal.textContent = Number(diverted || 0).toLocaleString();
        setStatusCounts(listings);
        renderListings(listings);
        elements.updatedLabel.textContent = `Updated ${new Intl.DateTimeFormat(undefined, { hour: "numeric", minute: "2-digit" }).format(new Date())}`;
    } catch (error) {
        elements.updatedLabel.textContent = "Could not reach the FoodShare API";
        elements.errorToast.textContent = "Dashboard data could not be loaded. Check that FoodShare is running, then refresh.";
        elements.errorToast.classList.add("visible");
    } finally {
        elements.refreshButton.disabled = false;
        elements.refreshButton.classList.remove("is-loading");
    }
}

elements.refreshButton.addEventListener("click", refreshDashboard);
refreshDashboard();