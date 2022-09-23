var express = require('express');
var router = express.Router();
const {getCurrentPrice} = require('../impala/queries');

router.get('/:currency', async function(req, res, next) {
  const { timeDiff } = req.query;
  const { currency } = req.params;

  const data = await getCurrentPrice(currency, timeDiff);
  res.json(data);
});

module.exports = router;
