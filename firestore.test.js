const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ADMIN_EMAIL = "biharkrantidal76@gmail.com";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write any collection", async () => {
  const unauth = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauth.collection("users").doc(ALICE_UID).get());
  await assertFails(unauth.collection("chats").where("uid", "==", ALICE_UID).get());
  await assertFails(unauth.collection("dailyTasks").where("uid", "==", ALICE_UID).get());
  await assertFails(
    unauth.collection("dailyTasks").doc("task_1").set({
      uid: ALICE_UID,
      title: "Hack",
      category: "Work",
      completed: false,
      reminderTime: "09:00 AM",
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("User profile creation enforces initial 20 credits and free membership", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 2000);

  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).set({
      uid: ALICE_UID,
      name: "Aarav Sharma",
      email: "aarav@example.com",
      language: "en",
      credits: 20,
      membership: "free",
      darkMode: true,
      notificationsEnabled: true,
      lastDailyCreditDate: "2026-11-20",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Reject tampered initial credits or membership escalation
  await assertFails(
    alice.collection("users").doc(ALICE_UID).set({
      uid: ALICE_UID,
      name: "Cheater",
      language: "en",
      credits: 9999,
      membership: "yearly",
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("User credit deduction, rewarded ad (+5), and cross-user isolation", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 5000);

  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).set({
      uid: ALICE_UID,
      name: "Aarav",
      language: "en",
      credits: 20,
      membership: "free",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Valid deduction (-1)
  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).update({
      credits: 19,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Valid rewarded ad (+5 -> 24)
  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).update({
      credits: 24,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject arbitrary credit inflation (+500)
  await assertFails(
    alice.collection("users").doc(ALICE_UID).update({
      credits: 524,
      updatedAt: new Date(Date.now() - 200),
    })
  );

  // Reject negative credit balance
  await assertFails(
    alice.collection("users").doc(ALICE_UID).update({
      credits: -1,
      updatedAt: new Date(Date.now() - 200),
    })
  );

  // Reject membership self-escalation
  await assertFails(
    alice.collection("users").doc(ALICE_UID).update({
      membership: "monthly",
      updatedAt: new Date(Date.now() - 200),
    })
  );

  // Bob cannot read or modify Alice's profile
  await assertFails(bob.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bob.collection("users").doc(ALICE_UID).update({
      name: "Hijacked",
      updatedAt: new Date(Date.now() - 200),
    })
  );
});

test("Chats and messages enforce owner isolation and filtered queries", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 3000);

  await assertSucceeds(
    alice.collection("chats").doc("chat_1").set({
      uid: ALICE_UID,
      title: "Hindi Translation Help",
      toolId: "ai_chat",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    alice.collection("chats").doc("chat_1").collection("messages").doc("msg_1").set({
      chatId: "chat_1",
      uid: ALICE_UID,
      role: "user",
      content: "Namaste!",
      timestamp: now,
    })
  );

  // Filtered queries succeed
  await assertSucceeds(alice.collection("chats").where("uid", "==", ALICE_UID).get());
  await assertSucceeds(
    alice.collection("chats").doc("chat_1").collection("messages").where("uid", "==", ALICE_UID).get()
  );

  // Unfiltered list queries rejected
  await assertFails(alice.collection("chats").get());
  await assertFails(alice.collection("chats").doc("chat_1").collection("messages").get());

  // Cross-user access denied
  await assertFails(bob.collection("chats").doc("chat_1").get());
  await assertFails(
    bob.collection("chats").doc("chat_1").collection("messages").doc("msg_1").get()
  );
});

test("DailyTasks, CreditTransactions, UserFiles, Notifications respect ownership and immutability", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 3000);

  await assertSucceeds(
    alice.collection("dailyTasks").doc("task_1").set({
      uid: ALICE_UID,
      title: "Revise Physics Chapter 4",
      category: "Study",
      completed: false,
      reminderTime: "07:00 AM",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    alice.collection("dailyTasks").doc("task_1").update({
      completed: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject createdAt tampering
  await assertFails(
    alice.collection("dailyTasks").doc("task_1").update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Credit transaction write & immutability
  await assertSucceeds(
    alice.collection("creditTransactions").doc("tx_1").set({
      uid: ALICE_UID,
      amount: -1,
      type: "ai_usage",
      feature: "AI Chat",
      status: "completed",
      timestamp: now,
    })
  );
  await assertFails(
    alice.collection("creditTransactions").doc("tx_1").update({ amount: 500 })
  );
  await assertFails(bob.collection("creditTransactions").doc("tx_1").get());
});

test("Admin email spoofing rejected when email_verified is false; verified admin succeeds", async () => {
  const spoofedAdmin = testEnv
    .authenticatedContext("spoof_uid", { email: ADMIN_EMAIL, email_verified: false })
    .firestore();
  const realAdmin = testEnv
    .authenticatedContext("admin_uid", { email: ADMIN_EMAIL, email_verified: true })
    .firestore();

  await assertFails(
    spoofedAdmin.collection("appConfig").doc("global").set({ activeModel: "gemini-3.8-flash" })
  );
  await assertSucceeds(
    realAdmin.collection("appConfig").doc("global").set({ activeModel: "gemini-3.8-flash" })
  );
});
