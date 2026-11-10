import type { PoiOfflineData } from "./api";

export interface PoiOfflineSnapshot {
  serverDataVersion: number;
  poiList: PoiOfflineData[];
}

const DATABASE_NAME = "linguaflow-offline";
const STORE_NAME = "poi-snapshots";
const SNAPSHOT_KEY = "latest";

function openPoiDatabase(): Promise<IDBDatabase> {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DATABASE_NAME, 1);
    request.onupgradeneeded = () => {
      request.result.createObjectStore(STORE_NAME);
    };
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
  });
}

export async function readPoiSnapshot(): Promise<PoiOfflineSnapshot | null> {
  const database = await openPoiDatabase();
  return new Promise((resolve, reject) => {
    const transaction = database.transaction(STORE_NAME, "readonly");
    const request = transaction.objectStore(STORE_NAME).get(SNAPSHOT_KEY);
    request.onsuccess = () => resolve((request.result as PoiOfflineSnapshot | undefined) ?? null);
    request.onerror = () => reject(request.error);
    transaction.oncomplete = () => database.close();
    transaction.onerror = () => database.close();
  });
}

export async function savePoiSnapshot(snapshot: PoiOfflineSnapshot): Promise<void> {
  const database = await openPoiDatabase();
  return new Promise((resolve, reject) => {
    const transaction = database.transaction(STORE_NAME, "readwrite");
    transaction.objectStore(STORE_NAME).put(snapshot, SNAPSHOT_KEY);
    transaction.oncomplete = () => {
      database.close();
      resolve();
    };
    transaction.onerror = () => {
      database.close();
      reject(transaction.error);
    };
    transaction.onabort = () => {
      database.close();
      reject(transaction.error);
    };
  });
}
