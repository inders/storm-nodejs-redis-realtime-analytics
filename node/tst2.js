var io = require('socket.io').listen(80);

io.sockets.on('connection', function (socket) {
	socket.on('message', function () { });
	socket.on('disconnect', function () { });
    });