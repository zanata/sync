var express = require('express')
var path = require('path')
var compression = require('compression')

var app = express()
// app.use(compression())
app.use(express.static(__dirname))

function accessControlHeaders(res) {
  res.header('Access-Control-Allow-Origin', 'http://localhost:8000')
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

app.post('/api/work', function (req, res) {
  commonHeaders(req, res)
  res.send()
})

app.post('/api/oauth/logout', function (req, res) {
  commonHeaders(req, res)
  res.send()
})

app.get('/api/work/mine', function (req, res) {
  commonHeaders(req, res)
  var result = [{
    "id": 1,
    "name": "test ets",
    "description": "asd",
    "syncToRepoJob": {
      "jobKey": "1.REPO_SYNC",
      "workId": 1,
      "name": "test ets",
      "description": "asd",
      "type": "REPO_SYNC",
      "lastJobStatus": {
        "workId": 1,
        "id": "1466480052893",
        "status": "ERROR",
        "jobType": "REPO_SYNC",
        "startTime": "2016-06-24 02:15:46.046+0000",
        "endTime": "2016-06-24 02:18:46.046+0000",
        "nextStartTime": null
      }
    },
    "syncToTransServerJob": {
      "jobKey": "1.SERVER_SYNC",
      "workId": 1,
      "name": "test ets",
      "description": "asd",
      "type": "SERVER_SYNC",
      "lastJobStatus": {
        "workId": 1,
        "id": "1466480052894",
        "status": "ERROR",
        "jobType": "SERVER_SYNC",
        "startTime": "2016-06-24 02:15:46.046+0000",
        "endTime": "2016-06-24 03:15:46.046+0000",
        "nextStartTime": null
      }
    }
  }]
  res.send(JSON.stringify(result))
})

app.post('/api/job/start', function (req, res) {
  commonHeaders(req, res)
  res.send(JSON.stringify({
    workId: req.query.id,
    jobType: req.query.type
  }))
})

app.get('/api/job/last/status', function (req, res) {
  commonHeaders(req, res)
  var status = {
    "workId": parseInt(req.query.id),
    "id": "1466480052894",
    "status": "COMPLETED",
    "jobType": req.query.type,
    "startTime": "2016-06-24 02:15:46.046+0000",
    "endTime": "2016-06-24 02:15:56.046+0000",
    "nextStartTime": null
  };
  res.send(JSON.stringify(status))
})

app.get('/api/work/:id', function (req, res) {
  commonHeaders(req, res)
  var id = req.query.id
  var result = {
    "id": 1,
    "name": "asdf asdf asdf ",
    "description": "",
    "syncToZanataCron": "MANUAL",
    "syncToRepoCron": "MANUAL",
    "syncToZanataOption": "SOURCE",
    "srcRepoPluginName": "git",
    "srcRepoPluginConfig": {
      "username": "admin",
      "secret": "admin",
      "url": "asdf"
    },
    "syncToServerEnabled": true,
    "syncToRepoEnabled": true,
    "createdDate": "2016-06-24 02:15:46.046+0000",
    "jobRunHistory": [{
      "workId": 1,
      "id": "1467089217998",
      "status": "ERROR",
      "jobType": "REPO_SYNC",
      "startTime": "2016-06-28 04:49:03.003+0000",
      "endTime": "2016-06-28 04:59:11.011+0000",
      "nextStartTime": "2016-06-28 04:49:11.011+0000"
    }, {
      "workId": 1,
      "id": "1467089217997",
      "status": "ERROR",
      "jobType": "REPO_SYNC",
      "startTime": "2016-06-28 04:47:56.056+0000",
      "endTime": "2016-06-28 04:57:56.056+0000",
      "nextStartTime": "2016-06-28 04:47:56.056+0000"
    }, {
      "workId": 1,
      "id": "1467087334339",
      "status": "ERROR",
      "jobType": "REPO_SYNC",
      "startTime": "2016-06-28 04:16:41.041+0000",
      "endTime": "2016-06-28 05:16:42.042+0000",
      "nextStartTime": "2016-06-28 04:16:41.041+0000"
    }, {
      "workId": 1,
      "id": "1467087334338",
      "status": "ERROR",
      "jobType": "REPO_SYNC",
      "startTime": "2016-06-28 04:16:03.003+0000",
      "endTime": "2016-06-28 04:26:13.003+0000",
      "nextStartTime": "2016-06-28 04:16:03.003+0000"
    }]
  }
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
