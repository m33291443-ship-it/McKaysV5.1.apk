const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const databaseId = "ai-studio-android-mckays-d15db111-e34e-4abb-aa72-cd106ebe4831";
let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: "gen-lang-client-0828226204",
    firestore: {
      rules: fs.readFileSync("firestore.rules", "utf8"),
      host: "127.0.0.1",
      port: 8085
    }
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

test("Unauthenticated user cannot read inventory", async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore({ databaseId });
  await assertFails(unauthedDb.collection("inventory").doc("item1").get());
});

test("Authenticated user can read and create valid inventory", async () => {
  const authedDb = testEnv.authenticatedContext("user123").firestore({ databaseId });
  const itemDoc = authedDb.collection("inventory").doc("item1");
  await assertSucceeds(itemDoc.set({
    branch: "Pusat",
    name: "VC Scandal",
    stock_ml: 150,
    stock_botol: 50
  }));
  await assertSucceeds(itemDoc.get());
});

test("Authenticated user cannot create invalid inventory (negative stock)", async () => {
  const authedDb = testEnv.authenticatedContext("user123").firestore({ databaseId });
  const itemDoc = authedDb.collection("inventory").doc("item2");
  await assertFails(itemDoc.set({
    branch: "Pusat",
    name: "VC Scandal",
    stock_ml: -10,
    stock_botol: 50
  }));
});

test("Authenticated user can manage stock orders", async () => {
  const authedDb = testEnv.authenticatedContext("user123").firestore({ databaseId });
  const orderDoc = authedDb.collection("stock_orders").doc("ORD-01");
  await assertSucceeds(orderDoc.set({
    branch: "Bandung",
    item: "VC Scandal",
    ml: 100,
    bottleSize: "30ml",
    botol: 20,
    status: "Menunggu Persetujuan"
  }));
});

test("Authenticated user can record transactions", async () => {
  const authedDb = testEnv.authenticatedContext("user123").firestore({ databaseId });
  const trxDoc = authedDb.collection("transactions").doc("TRX-1001");
  await assertSucceeds(trxDoc.set({
    branch: "Bandung",
    type: "Kombinasi (Bibit + Botol)",
    item: "VC Scandal (30ml) + Botol (1pcs)",
    detail: "Kombinasi | 30ml | 1pcs",
    price: 150000,
    member: "Siti Rahma",
    pointsEarned: 15,
    time: "10:30",
    date: "2026-11-09"
  }));
});
