const { getUsers, addUser, generateSalt, hashPassword, createSession } = require('../_db');

module.exports = (req, res) => {
  res.setHeader('Content-Type', 'application/json');
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }

  if (req.method !== 'POST') {
    return res.status(405).json({ status: 'error', message: 'Method not allowed' });
  }

  const body = typeof req.body === 'string' ? JSON.parse(req.body || '{}') : (req.body || {});
  const { username, password, fullName, email, role } = body;

  if (!username || !password || !username.trim() || !password.trim()) {
    return res.status(400).json({ status: 'error', message: 'Username and password are required' });
  }

  const users = getUsers();
  const existing = users.find(u => (u.username || '').toLowerCase() === username.trim().toLowerCase());
  if (existing) {
    return res.status(409).json({ status: 'error', message: 'Username already exists' });
  }

  const salt = generateSalt();
  const passwordHash = hashPassword(password, salt);

  let maxId = 1000;
  users.forEach(u => {
    const num = parseInt((u.id || '').replace(/\D/g, ''), 10);
    if (!isNaN(num) && num > maxId) maxId = num;
  });

  const newUser = {
    id: `USR-${maxId + 1}`,
    username: username.trim(),
    passwordHash,
    salt,
    fullName: fullName ? fullName.trim() : username.trim(),
    email: email ? email.trim() : '',
    role: (role && ['ADMIN', 'TECH', 'STAFF'].includes(role.toUpperCase())) ? role.toUpperCase() : 'STAFF',
    createdAt: new Date().toISOString(),
    lastLoginAt: new Date().toISOString()
  };

  addUser(newUser);

  const safeUser = {
    id: newUser.id,
    username: newUser.username,
    fullName: newUser.fullName,
    email: newUser.email,
    role: newUser.role,
    createdAt: newUser.createdAt,
    lastLoginAt: newUser.lastLoginAt
  };

  const token = createSession(newUser.username, safeUser);

  return res.status(201).json({
    status: 'success',
    message: 'User registered successfully',
    token,
    user: safeUser
  });
};
