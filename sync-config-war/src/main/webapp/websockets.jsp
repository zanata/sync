<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <title>WebSocket: Say Hello</title>
    <script type="text/javascript">
        var websocket = null;

        function connect() {
            var wsURI = 'ws://' + window.location.host +
                    '/sync/websocket/jobStatus';
            websocket = new WebSocket(wsURI);

            websocket.onopen = function () {
                displayStatus('Open');
                document.getElementById('sayHello').disabled = false;
                displayMessage(
                        'Connection is now open. Type a name and click Say Hello to send a message.');
            };
            websocket.onmessage = function (event) {
                // log the event
                displayMessage('The response was received! ' + event.data,
                        'success');
            };
            websocket.onerror = function (event) {
                // log the event
                displayMessage('Error! ' + event.data, 'error');
            };
            websocket.onclose = function () {
                displayStatus('Closed');
                displayMessage(
                        'The connection was closed or timed out. Please click the Open Connection button to reconnect.');
                document.getElementById('sayHello').disabled = true;
            };
        }

        function disconnect() {
            if (websocket !== null) {
                websocket.close();
                websocket = null;
            }
            message.setAttribute("class", "message");
            message.value = 'WebSocket closed.';
            // log the event
        }

        function sendMessage() {
            if (websocket !== null) {
                var content = document.getElementById('name').value;
                websocket.send(content);
            } else {
                displayMessage(
                        'WebSocket connection is not established. Please click the Open Connection button.',
                        'error');
            }
        }

        function displayMessage(data, style) {
            var message = document.getElementById('hellomessage');
            message.setAttribute("class", style);
            message.value = data;
        }

        function displayStatus(status) {
            var currentStatus = document.getElementById('currentstatus');
            currentStatus.value = status;
        }

    </script>
</head>
<body>

<div>
    <h1>Welcome to JBoss!</h1>
    <div>This is a simple example of a WebSocket implementation.</div>
    <div id="connect-container">
        <div>
            <fieldset>
                <legend>Connect or disconnect using WebSocket :</legend>
                <input type="button" id="connect" onclick="connect();"
                        value="Open Connection"/>
                <input type="button" id="disconnect" onclick="disconnect();"
                        value="Close Connection"/>
            </fieldset>
        </div>
        <div>
            <fieldset>
                <legend>Type your name below. then click the `Say Hello` button
                    :
                </legend>
                <input id="name" type="text" size="40" style="width: 40%"/>
                <input type="button" id="sayHello" onclick="sendMessage();"
                        value="Say Hello" disabled="disabled"/>
            </fieldset>
        </div>
        <div>Current WebSocket Connection Status:
            <output id="currentstatus" class="message">Closed</output>
        </div>
        <div>
            <output id="hellomessage"/>
        </div>
    </div>
</div>
</body>
</html>
