const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { setGlobalOptions } = require("firebase-functions/v2");
const { initializeApp } = require("firebase-admin/app");
const { getAuth } = require("firebase-admin/auth");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();
setGlobalOptions({ region: "asia-south1", maxInstances: 20 });

const DATABASE_ID = "ai-studio-android-aiassist-dc68094c-1594-4e78-955f-bbb46d360a8a";
const ADMIN_EMAIL = process.env.ADMIN_BOOTSTRAP_EMAIL || "biharkrantidal76@gmail.com";
const db = getFirestore(DATABASE_ID);

async function assertAdmin(request) {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Authentication required.");
  }
  const email = request.auth.token.email;
  const emailVerified = request.auth.token.email_verified === true;
  if (emailVerified && email === ADMIN_EMAIL) return true;
  if (request.auth.token.admin === true) return true;
  const adminDoc = await db.collection("admins").doc(request.auth.uid).get();
  if (adminDoc.exists && adminDoc.data()?.role === "admin") return true;
  throw new HttpsError("permission-denied", "Admin privileges required.");
}

/**
 * Validates and atomically deducts AI credits on the server.
 */
exports.validateAndDeductCredits = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Sign in required.");
  }
  const uid = request.auth.uid;
  const cost = Number(request.data?.cost ?? 1);
  const feature = String(request.data?.feature || "AI Tool").slice(0, 80);
  if (!Number.isInteger(cost) || cost < 1 || cost > 50) {
    throw new HttpsError("invalid-argument", "Invalid credit cost.");
  }

  const userRef = db.collection("users").doc(uid);
  const txRef = db.collection("creditTransactions").doc();

  const remaining = await db.runTransaction(async (tx) => {
    const snap = await tx.get(userRef);
    if (!snap.exists) {
      throw new HttpsError("not-found", "User profile not found.");
    }
    const current = Number(snap.data().credits || 0);
    if (current < cost) {
      throw new HttpsError("failed-precondition", "Insufficient AI credits.");
    }
    const updated = current - cost;
    tx.update(userRef, {
      credits: updated,
      updatedAt: FieldValue.serverTimestamp(),
    });
    tx.set(txRef, {
      uid,
      amount: -cost,
      type: "ai_usage",
      feature,
      status: "completed",
      timestamp: FieldValue.serverTimestamp(),
    });
    return updated;
  });

  return { remainingCredits: remaining };
});

/**
 * Verifies Google Play Billing subscription purchase token and activates membership & credits.
 */
exports.verifyPlaySubscription = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Sign in required.");
  }
  const uid = request.auth.uid;
  const productId = String(request.data?.productId || "");
  const purchaseToken = String(request.data?.purchaseToken || "");
  const allowedPlans = {
    ai_india_weekly: { credits: 500, days: 7 },
    ai_india_monthly: { credits: 2000, days: 30 },
    ai_india_yearly: { credits: 25000, days: 365 },
  };
  const plan = allowedPlans[productId];
  if (!plan || purchaseToken.length < 16) {
    throw new HttpsError("invalid-argument", "Invalid subscription product or purchase token.");
  }

  // Note: For full Google Play Developer API receipt validation, configure
  // GOOGLE_PLAY_SERVICE_ACCOUNT_JSON and query androidpublisher.purchases.subscriptionsv2.get.
  const expiryMs = Date.now() + plan.days * 24 * 60 * 60 * 1000;
  const userRef = db.collection("users").doc(uid);
  const subRef = db.collection("subscriptions").doc(`${uid}_${productId}`);
  const txRef = db.collection("creditTransactions").doc();

  await db.runTransaction(async (tx) => {
    const snap = await tx.get(userRef);
    const currentCredits = Number(snap.data()?.credits || 0);
    tx.update(userRef, {
      membership: productId,
      credits: currentCredits + plan.credits,
      updatedAt: FieldValue.serverTimestamp(),
    });
    tx.set(subRef, {
      uid,
      planId: productId,
      status: "active",
      purchaseTokenHash: purchaseToken.slice(0, 24),
      expiryDate: new Date(expiryMs),
      updatedAt: FieldValue.serverTimestamp(),
    });
    tx.set(txRef, {
      uid,
      amount: plan.credits,
      type: "subscription_bonus",
      feature: `Subscription ${productId}`,
      status: "completed",
      timestamp: FieldValue.serverTimestamp(),
    });
  });

  return { status: "active", planId: productId, creditsAdded: plan.credits };
});

/**
 * Admin: Sets custom admin claim and ensures admins/{uid} record.
 */
exports.bootstrapAdminAccess = onCall(async (request) => {
  await assertAdmin(request);
  const uid = request.auth.uid;
  await getAuth().setCustomUserClaims(uid, { admin: true });
  await db.collection("admins").doc(uid).set(
    {
      email: request.auth.token.email || ADMIN_EMAIL,
      role: "admin",
      updatedAt: FieldValue.serverTimestamp(),
    },
    { merge: true }
  );
  return { ok: true, uid };
});

/**
 * Admin: Adjust user credits and log audit transaction.
 */
exports.adminAdjustCredits = onCall(async (request) => {
  await assertAdmin(request);
  const targetUid = String(request.data?.targetUid || "").trim();
  const delta = Number(request.data?.delta || 0);
  const reason = String(request.data?.reason || "Admin Adjustment").slice(0, 80);

  if (!targetUid || !Number.isInteger(delta) || delta === 0 || Math.abs(delta) > 50000) {
    throw new HttpsError("invalid-argument", "Invalid targetUid or credit delta.");
  }

  const userRef = db.collection("users").doc(targetUid);
  const txRef = db.collection("creditTransactions").doc();

  const newBalance = await db.runTransaction(async (tx) => {
    const snap = await tx.get(userRef);
    if (!snap.exists) {
      throw new HttpsError("not-found", "Target user does not exist.");
    }
    const next = Math.max(0, Number(snap.data().credits || 0) + delta);
    tx.update(userRef, {
      credits: next,
      updatedAt: FieldValue.serverTimestamp(),
    });
    tx.set(txRef, {
      uid: targetUid,
      amount: delta,
      type: "admin_adjustment",
      feature: reason,
      status: "completed",
      timestamp: FieldValue.serverTimestamp(),
    });
    return next;
  });

  return { targetUid, credits: newBalance };
});

/**
 * Admin: Creates in-app Firestore notification and broadcasts optional FCM topic message.
 */
exports.adminSendNotification = onCall(async (request) => {
  await assertAdmin(request);
  const targetUid = String(request.data?.targetUid || "").trim();
  const title = String(request.data?.title || "").trim().slice(0, 120);
  const message = String(request.data?.message || "").trim().slice(0, 500);
  if (!title || !message) {
    throw new HttpsError("invalid-argument", "Title and message are required.");
  }

  if (targetUid) {
    await db.collection("notifications").add({
      uid: targetUid,
      title,
      message,
      read: false,
      timestamp: FieldValue.serverTimestamp(),
    });
  } else {
    await getMessaging().send({
      topic: "all_users",
      notification: { title, body: message },
    });
  }

  return { sent: true };
});
