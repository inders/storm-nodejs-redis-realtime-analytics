/** Imports **/
var express = require('express')
 ,   app = express()  
 , server = require('http').createServer(app)
 , io = require('socket.io').listen(server)
 , redis = require('redis')

;

server.listen(8080);

// Globals
var channelCounts = new Array();

/* Convert a message sent by redis to the format expected by browser */
// TODO : Remove random code with actual redis code
var idCount = 0;

var POS = "pos";
var NEG = "neg";
var NEUT = "neutral";

var labels = [POS, NEG, NEUT];
var channels = ["GoogleAlerts"];

var makeTick = function(inmsgStr) {
    var inmsg = JSON.parse(inmsgStr);
    /*
    var channelStr = inmsg.channel;
    if (channelStr.match(/^positive\_/)) {
        inmsg.sentiment = POS;
    } else if (channelStr.match(/^negative\_/)) {
        inmsg.sentiment = NEG;
    } else {
        inmsg.sentiment = NEUT;
    }
    */

    var outmsg =  {
        channel: inmsg.channel,
        label: inmsg.sentiment,
        id: inmsg.id,
        title: inmsg.title,
        link: inmsg.link
    };
    return outmsg;
};

/** Socket.io **/
io.sockets.on('connection', function(socket) {
    console.log(' <<<<<< User connected');

    socket.on('disconnect', function() {
        console.log(' >>>>>>>> User disconnected');
    });
});

/** Web endpoints and Express setup **/

app.use("/static", express.static(__dirname + '/static'));

app.get('/', function (req, res) {
    res.sendfile(__dirname + '/index.html');
});


/** Redis **/
var redisClient = redis.createClient();
for (var i = 0; i < channels.length; i++) {
    redisClient.subscribe(channels[i]);
}

redisClient.on('message', function (channel, message) {
    // Broadcast message to all clients
    io.sockets.emit('tick', makeTick(message));
    console.log('New tick from STORM: ' + channel);
});


