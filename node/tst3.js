/***************************  Require modules  ********************************/
var     sys = require('sys'), 
    http = require('http'),
    io = require('socket.io');

/*************************  Start socket server  ******************************/
server = http.createServer(function(request, response) {
	response.writeHead(200, {'Content-Type': 'text/html'});
	response.end('<h1>Welcome to the socket.io server<h1>');
    });

server.listen(port);

var socket = io.listen(server);
socket.on('connection', function(client) {
        // This will only fire when a client has connected
        // Do some stuff when connected to a client
        
        client.send(/* Your twitter data */); // You don't have to put this here.
        // You can also use socket.broadcast() to send to everyone.
                                
        client.on('message', function(data) {
                // Do some stuff when you recieve a message
	    });
        
        client.on('disconnect', function(client) {
                sys.puts('Client disconnected');
	    });
        
    });