var express = require('express')
var path = require('path')
var compression = require('compression')

var app = express()
// app.use(compression())
app.use(express.static(__dirname))

function accessControlHeaders(res) {
  res.header('Access-Control-Allow-Origin', '*')
  res.header('Access-Control-Allow-Credentials', true)
  // try: 'POST, GET, PUT, DELETE, OPTIONS'
  res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')
  // try: 'X-Requested-With, X-HTTP-Method-Override, Content-Type, Accept'
  res.header('Access-Control-Allow-Headers', 'Content-Type, Accept, *')
}

function commonHeaders(req, res) {
  console.log('accessing ' + req.url)
  accessControlHeaders(res)
  res.header('Content-Type', 'application/json')
}

app.get('/api/oauth/url', function (req, res) {
  commonHeaders(req, res)
  var zanataUrl = req.query.z
  res.send(JSON.stringify({
    error: false,
    data: zanataUrl + '/authorize/?redirect_uri=http%3A%2F%2Flocalhost%3A8000%2Fsync%2Fauth%2F&client_id=zanata_sync'
  }))
})

app.get('/api/work/by/:username', function (req, res) {
  commonHeaders(req, res)
  var username = req.params.username
  var result = [
    {
      "id": 1,
      "name": "test work",
      "description": "soemthing",
      "syncToRepoJob": {
        "jobKey": "1.REPO_SYNC",
        "workId": 1,
        "name": "test work",
        "description": "soemthing",
        "type": "REPO_SYNC",
        "lastJobStatus": {
          "status": "NONE"
        }
      },
      "syncToTransServerJob": {
        "jobKey": "1.SERVER_SYNC",
        "workId": 1,
        "name": "test work",
        "description": "soemthing",
        "type": "SERVER_SYNC",
        "lastJobStatus": {
          "status": "NONE"
        }
      }
    }
  ]
  res.send(JSON.stringify(result))
})

app.options('*', function (req, res) {
  accessControlHeaders(res)
  res.send()
})


function renderPage(appHtml) {
  return `
    <!doctype html public="storage">
    <html>
    <meta charset=utf-8/>
    <title>My First React Router App</title>
    <div id=app>${appHtml}</div>
    <script src="/bundle.js"></script>
   `
}

var port = process.env.port || 3000

app.listen(port, function() {
  console.info('fake server running express on http://localhost:' + port)
})
