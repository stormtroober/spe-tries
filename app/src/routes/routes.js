const express = require('express');
const router = express.Router();

const controller = require('../controllers/controller');

// API Routes
router.post('/submit', controller.align_sequences);
router.get('/show', controller.get_all_align_sequences);

module.exports = router;
