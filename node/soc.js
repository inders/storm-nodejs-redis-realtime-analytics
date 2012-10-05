var app = require('http').createServer(handler)
    , io = require('socket.io').listen(app)
    , fs = require('fs')
    , redis = require('redis');
    
app.listen(5431);

var sub = redis.createClient();
sub.subscribe('tags');


sub.on('message', function(channel, message){
	//	console.log(" recvfrom channel : %s, the message : %s", channel, message);
    });

function handler (req, res) {
    fs.readFile(__dirname + '/indexsoc.html',
		function (err, data) {
		    if (err) {
			res.writeHead(500);
			return res.end('Error loading index.html');
		    }

		    res.writeHead(200);
		    res.end(data);
		});
}

io.sockets.on('connection', function (socket) {
  sub.on('message', function(pattern, key){
	  console.log("client channel recieve from channel : %s, the message : %s", pattern, key);
          socket.emit(pattern, key);
      });
    });