// Firebase Web Modular Configuration for AquaSustain
// Connected to Firebase project: aqua-sustain

import { initializeApp, getApps, getApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getFirestore } from "firebase/firestore";

const firebaseConfig = {
  apiKey: process.env.VITE_FIREBASE_API_KEY || "AIzaSyBFIQ0TuJMO32GZMYBFTQJqnNQXN2Y1Ess",
  authDomain: "aqua-sustain.firebaseapp.com",
  projectId: "aqua-sustain",
  storageBucket: "aqua-sustain.firebasestorage.app",
  messagingSenderId: "168404782972",
  appId: "1:168404782972:web:49ec1fb5c7b7c62ae5def9"
};

export const app = !getApps().length ? initializeApp(firebaseConfig) : getApp();
export const auth = getAuth(app);
export const db = getFirestore(app);

export default { app, auth, db };
