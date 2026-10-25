const { getUsers, hashPassword, createSession } = require('../_db');

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
  const { username, password } = body;

  if (!username || !password) {
    return res.status(400).json({ status: 'error', message: 'Username and password are required' });
  }

  const users = getUsers();
  const user = users.find(u => (u.username || '').toLowerCase() === username.trim().toLowerCase());

  if (!user) {
    return res.status(401).json({ status: 'error', message: 'Invalid username or password' });
  }

  const computedHash = hashPassword(password, user.salt);
  if (computedHash !== user.passwordHash) {
    return res.status(401).json({ status: 'error', message: 'Invalid username or password' });
  }

  user.lastLoginAt = new Date().toISOString();
  const safeUser = {
    id: user.id,
    username: user.username,
    fullName: user.fullName,
    email: user.email,
    role: user.role,
    createdAt: user.createdAt,
    lastLoginAt: user.lastLoginAt
  };

  const token = createSession(user.username, safeUser);

  return res.status(200).json({
    status: 'success',
    message: 'Login successful',
    token,
    user: safeUser
  });
};
