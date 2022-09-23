const client = require('./connection');

function getCurrentPrice(currency, timeDiff) {
  const query = `select t.currency, 
  FIRST_VALUE(t.price)
  OVER (ORDER BY t.ts RANGE BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) as first_price,
  LAST_VALUE(t.price)
  OVER (ORDER BY t.ts RANGE BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) as current_price,
  t.ts
  from hbasecryptoprice t
  where t.currency='${currency}' and 
  unix_timestamp(t.ts)
  between unix_timestamp((select max(ts) from hbasecryptoprice)) - 60 * ${timeDiff} 
    and unix_timestamp((select max(ts) from hbasecryptoprice))
  order by t.ts desc      
  limit 1;`;
  console.log('--------------', query);

  return client.query(query);
};

module.exports = {getCurrentPrice};