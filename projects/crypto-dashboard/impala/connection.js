const { createClient } = require('node-impala');

const client = createClient();

client.connect({
    host: '35.202.92.142',
    port: 21000,
    resultType: 'json-array'
});

module.exports = client;