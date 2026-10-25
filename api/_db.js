const crypto = require('crypto');

let initialRecords = [];
let initialUsers = [];

try {
  initialRecords = require('../data/maintenance_records.json');
} catch (e) {
  initialRecords = [];
}

try {
  initialUsers = require('../data/users.json');
} catch (e) {
  initialUsers = [];
}

// Global in-memory state across warm serverless invocations
let records = Array.isArray(initialRecords) ? [...initialRecords] : [];
let users = Array.isArray(initialUsers) ? [...initialUsers] : [];
const sessions = new Map();

function getRecords() {
  return records;
}

function setRecords(newRecords) {
  records = newRecords;
}

function getUsers() {
  return users;
}

function addUser(user) {
  users.push(user);
}

function hashPassword(password, salt) {
  return crypto.createHash('sha256').update(salt, 'utf8').update(password, 'utf8').digest('hex');
}

function generateSalt() {
  return crypto.randomBytes(16).toString('hex');
}

function createSession(username, user) {
  const token = crypto.randomUUID();
  sessions.set(token, {
    token,
    username: username.toLowerCase(),
    user,
    expiresAt: Date.now() + (24 * 60 * 60 * 1000)
  });
  return token;
}

function getSession(token) {
  if (!token) return null;
  const s = sessions.get(token);
  if (!s) return null;
  if (Date.now() > s.expiresAt) {
    sessions.delete(token);
    return null;
  }
  return s;
}

function deleteSession(token) {
  if (token) sessions.delete(token);
}

module.exports = {
  getRecords,
  setRecords,
  getUsers,
  addUser,
  hashPassword,
  generateSalt,
  createSession,
  getSession,
  deleteSession
};
