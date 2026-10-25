const { getRecords, setRecords } = require('./_db');

module.exports = (req, res) => {
  res.setHeader('Content-Type', 'application/json');
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }

  const records = getRecords();
  const id = req.query.id;

  // Single Record by ID: /api/records/:id
  if (id) {
    const idx = records.findIndex(r => r.id === id);

    if (req.method === 'GET') {
      if (idx === -1) return res.status(404).json({ error: 'Record not found' });
      return res.status(200).json(records[idx]);
    }

    if (req.method === 'PUT' || req.method === 'POST') {
      if (idx === -1) return res.status(404).json({ error: 'Record not found' });
      const body = typeof req.body === 'string' ? JSON.parse(req.body || '{}') : (req.body || {});

      const updated = {
        ...records[idx],
        ...body,
        updatedAt: new Date().toISOString()
      };
      records[idx] = updated;
      setRecords(records);
      return res.status(200).json(updated);
    }

    if (req.method === 'DELETE') {
      if (idx === -1) return res.status(404).json({ error: 'Record not found' });
      const removed = records.splice(idx, 1)[0];
      setRecords(records);
      return res.status(200).json({ status: 'deleted', id: removed.id });
    }
  }

  // Collection: /api/records
  if (req.method === 'GET') {
    let result = [...records];
    const { status, zone, category, search } = req.query;

    if (status) {
      result = result.filter(r => (r.status || '').toLowerCase() === status.toLowerCase());
    }
    if (zone) {
      result = result.filter(r => (r.zone || '').toLowerCase() === zone.toLowerCase());
    }
    if (category) {
      result = result.filter(r => (r.category || '').toLowerCase() === category.toLowerCase());
    }
    if (search) {
      const q = search.toLowerCase();
      result = result.filter(r =>
        (r.equipmentName || '').toLowerCase().includes(q) ||
        (r.equipmentId || '').toLowerCase().includes(q) ||
        (r.issueDescription || '').toLowerCase().includes(q) ||
        (r.id || '').toLowerCase().includes(q)
      );
    }
    return res.status(200).json(result);
  }

  if (req.method === 'POST') {
    const body = typeof req.body === 'string' ? JSON.parse(req.body || '{}') : (req.body || {});

    // Generate next TKT id
    let maxId = 1000;
    records.forEach(r => {
      const num = parseInt((r.id || '').replace(/\D/g, ''), 10);
      if (!isNaN(num) && num > maxId) maxId = num;
    });

    const newTicket = {
      id: `TKT-${maxId + 1}`,
      equipmentId: body.equipmentId || 'EQ-01',
      equipmentName: body.equipmentName || 'Equipment',
      category: body.category || 'Cardio',
      zone: body.zone || 'Zone A - Cardio Deck',
      issueDescription: body.issueDescription || '',
      priority: body.priority || 'Medium',
      status: 'Reported',
      reportedBy: body.reportedBy || 'Staff',
      assignedTechnician: body.assignedTechnician || 'Unassigned',
      resolutionNotes: body.resolutionNotes || '',
      estimatedCost: parseFloat(body.estimatedCost) || 0.0,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };

    records.unshift(newTicket);
    setRecords(records);
    return res.status(201).json(newTicket);
  }

  return res.status(405).json({ error: 'Method not allowed' });
};
