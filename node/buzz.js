/** Imports **/
var express = require('express')
 ,   app = express()  
 , server = require('http').createServer(app)
 , io = require('socket.io').listen(server)
// , redis = require('redis')

;

server.listen(8080);

/* Convert a message sent by redis to the format expected by browser */
// TODO : Remove random code with actual redis code
var idCount = 0;
var labels = ["pos", "neg"];
var channel = ["twitter", "email", "feed"];
var makeMessage = function (inmsg) {
    idCount += 1;
    var chooselbl =  labels[Math.floor(Math.random() * 2)];
    var choosechannel = channel[Math.floor(Math.random() * channel.length)];

    var outmsg = {
        channel: choosechannel,
        label: chooselbl,
        id: idCount,
        title: 'Message ' + idCount,
        link: 'http://www.google.com'
    };
    return outmsg;
};

app.use("/static", express.static(__dirname + '/static'));

app.get('/', function (req, res) {
    res.sendfile(__dirname + '/index.html');
});

io.sockets.on('connection', function(socket) {
    console.log(' <<<<<< User connected');

    io.sockets.emit('news', makeMessage('dummy'));
    socket.on('newevent', function (data) {
        console.log(data);
    });

    socket.on('disconnect', function() {
        console.log(' >>>>>>>> User disconnected');
    });
});


/*
var redisClient = redis.createClient();
redisClient.subscribe("tags");

redisClient.on('message', function (channel, message) {
    // Broadcast message to all clients
    io.sockets.emit('tick', makeMessage());
    console.log('New tick from STORM: ' + channel);
});

*/


// Dummy tick sender
setInterval(function() {
        console.log("Sending tick");
        io.sockets.emit('tick', makeMessage('dummy'));
    }, 2000);


