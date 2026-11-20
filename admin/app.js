import { initializeApp } from "https://www.gstatic.com/firebasejs/11.4.0/firebase-app.js";
import { getAuth, GoogleAuthProvider, onAuthStateChanged, signInWithPopup, signOut } from "https://www.gstatic.com/firebasejs/11.4.0/firebase-auth.js";
import {
  addDoc,
  collection,
  doc,
  getDoc,
  getDocs,
  getFirestore,
  limit,
  orderBy,
  query,
  serverTimestamp,
  setDoc,
  updateDoc,
} from "https://www.gstatic.com/firebasejs/11.4.0/firebase-firestore.js";

// Register a Web App inside Firebase Project gen-lang-client-0466207256 and paste its apiKey & appId below.
const firebaseConfig = {
  apiKey: "REPLACE_WITH_FIREBASE_WEB_API_KEY",
  authDomain: "gen-lang-client-0466207256.firebaseapp.com",
  projectId: "gen-lang-client-0466207256",
  storageBucket: "gen-lang-client-0466207256.firebasestorage.app",
  messagingSenderId: "366466329697",
  appId: "REPLACE_WITH_FIREBASE_WEB_APP_ID",
};

const DATABASE_ID = "ai-studio-android-aiassist-dc68094c-1594-4e78-955f-bbb46d360a8a";
const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const db = getFirestore(app, DATABASE_ID);
const provider = new GoogleAuthProvider();

const loginBtn = document.getElementById("loginBtn");
const logoutBtn = document.getElementById("logoutBtn");
const loginNotice = document.getElementById("loginNotice");
const dashboard = document.getElementById("dashboard");

loginBtn.addEventListener("click", async () => {
  try {
    await signInWithPopup(auth, provider);
  } catch (err) {
    alert(`Login failed: ${err.message}`);
  }
});

logoutBtn.addEventListener("click", () => signOut(auth));
document.getElementById("refreshBtn").addEventListener("click", () => loadDashboardData());

onAuthStateChanged(auth, async (user) => {
  if (!user) {
    loginBtn.classList.remove("hidden");
    logoutBtn.classList.add("hidden");
    loginNotice.classList.remove("hidden");
    dashboard.classList.add("hidden");
    return;
  }
  loginBtn.classList.add("hidden");
  logoutBtn.classList.remove("hidden");
  loginNotice.classList.add("hidden");
  dashboard.classList.remove("hidden");
  await loadDashboardData();
});

async function loadDashboardData() {
  try {
    const configSnap = await getDoc(doc(db, "appConfig", "global"));
    if (configSnap.exists()) {
      const cfg = configSnap.data();
      document.getElementById("activeModel").value = cfg.activeModel || "gemini-3.8-flash";
      document.getElementById("chatCost").value = cfg.chatCreditCost ?? 1;
      document.getElementById("visionCost").value = cfg.visionCreditCost ?? 2;
      document.getElementById("docCost").value = cfg.documentCreditCost ?? 2;
      document.getElementById("maintenanceMode").checked = Boolean(cfg.maintenanceMode);
    }

    const usersSnap = await getDocs(query(collection(db, "users"), limit(100)));
    const txSnap = await getDocs(query(collection(db, "creditTransactions"), orderBy("timestamp", "desc"), limit(300)));
    const subsSnap = await getDocs(query(collection(db, "subscriptions"), limit(100)));

    const nowMs = Date.now();
    let dau = 0;
    let mau = 0;
    const tbody = document.getElementById("usersTableBody");
    tbody.innerHTML = "";

    usersSnap.forEach((docSnap) => {
      const u = docSnap.data();
      const updatedMs = u.updatedAt?.toMillis ? u.updatedAt.toMillis() : 0;
      if (nowMs - updatedMs <= 86400000) dau += 1;
      if (nowMs - updatedMs <= 2592000000) mau += 1;

      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td><strong>${escapeHtml(u.name || "User")}</strong><br/><small>${docSnap.id}</small></td>
        <td>${escapeHtml(u.email || u.phone || "-")}</td>
        <td>${escapeHtml(u.membership || "free")}</td>
        <td>${u.credits ?? 0}</td>
        <td>
          <button class="btn-outline" data-add="${docSnap.id}" data-curr="${u.credits ?? 0}">+20 Credits</button>
        </td>
      `;
      tbody.appendChild(tr);
    });

    tbody.querySelectorAll("button[data-add]").forEach((btn) => {
      btn.addEventListener("click", async () => {
        const uid = btn.getAttribute("data-add");
        const curr = Number(btn.getAttribute("data-curr") || 0);
        await updateDoc(doc(db, "users", uid), {
          credits: curr + 20,
          updatedAt: serverTimestamp(),
        });
        await addDoc(collection(db, "creditTransactions"), {
          uid,
          amount: 20,
          type: "admin_adjustment",
          feature: "Admin Grant +20",
          status: "completed",
          timestamp: serverTimestamp(),
        });
        await loadDashboardData();
      });
    });

    let aiRequests = 0;
    let creditsUsed = 0;
    let adRewards = 0;
    txSnap.forEach((tDoc) => {
      const t = tDoc.data();
      if (t.type === "ai_usage") {
        aiRequests += 1;
        creditsUsed += Math.abs(Number(t.amount || 0));
      } else if (t.type === "ad_reward") {
        adRewards += 1;
      }
    });

    let activeSubs = 0;
    subsSnap.forEach((s) => {
      if (s.data().status === "active") activeSubs += 1;
    });

    document.getElementById("statUsers").textContent = String(usersSnap.size);
    document.getElementById("statDau").textContent = String(dau);
    document.getElementById("statMau").textContent = String(mau);
    document.getElementById("statRequests").textContent = String(aiRequests);
    document.getElementById("statCreditsUsed").textContent = String(creditsUsed);
    document.getElementById("statAdRewards").textContent = String(adRewards);
    document.getElementById("statSubs").textContent = String(activeSubs);
  } catch (err) {
    alert(`Admin load error (confirm you are signed in as verified admin): ${err.message}`);
  }
}

document.getElementById("configForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  await setDoc(
    doc(db, "appConfig", "global"),
    {
      activeModel: document.getElementById("activeModel").value,
      chatCreditCost: Number(document.getElementById("chatCost").value || 1),
      visionCreditCost: Number(document.getElementById("visionCost").value || 2),
      documentCreditCost: Number(document.getElementById("docCost").value || 2),
      maintenanceMode: document.getElementById("maintenanceMode").checked,
      updatedAt: serverTimestamp(),
    },
    { merge: true }
  );
  alert("Global AI configuration saved.");
});

document.getElementById("notifyForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  await addDoc(collection(db, "notifications"), {
    uid: document.getElementById("notifyUid").value.trim(),
    title: document.getElementById("notifyTitle").value.trim(),
    message: document.getElementById("notifyMessage").value.trim(),
    read: false,
    timestamp: serverTimestamp(),
  });
  alert("Notification sent.");
  e.target.reset();
});

function escapeHtml(str) {
  return String(str).replace(/[&<>"']/g, (c) => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#39;",
  }[c]));
}
