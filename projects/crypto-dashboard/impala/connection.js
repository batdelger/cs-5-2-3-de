const { createClient } = require('node-impala');

const client = createClient();

client.connect({
    host: '127.0.0.1',
    port: 21000,
    resultType: 'json-array'
});

module.exports = client;