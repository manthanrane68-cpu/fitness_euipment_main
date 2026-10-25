const { getRecords, getUsers } = require('./_db');

module.exports = (req, res) => {
  res.setHeader('Content-Type', 'application/json');
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, HEAD, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }

  const records = getRecords();
  const users = getUsers();

  return res.status(200).json({
    status: 'UP',
    service: 'Fitness Equipment Maintenance Portal',
    version: '1.0.0-mvp',
    runtime: 'Vercel Serverless Platform (Node.js)',
    totalRecords: records.length,
    registeredUsers: users.length,
    timestamp: new Date().toISOString()
  });
};
