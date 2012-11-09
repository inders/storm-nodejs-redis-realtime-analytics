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
var channel = ["twitter", "email", "feed"];
var makeMessage = function (inmsg) {
    updateCounts(inmsg);
    idCount += 1;
    var chooselbl =  labels[Math.floor(Math.random() * labels.length)];
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

var makeTick = function(inmsg) {
    updateCounts(inmsg);
    var outmsg =  {
        channel: inmsg.channel,
        label: inmsg.sentiment,
        id: inmsg.id,
        title: inmsg.title,
        lik: inmsg.link
    };
    return outmsg;
};

var updateCounts = function (message) {
    var counts = channelCounts[message.channel];
    if (!counts) {
        counts = new Object();
        counts[POS] = 0;
        counts[NEG] = 0;
        counts[NEUT] = 0;
        channelCounts[message.channel] = counts;
    }

    counts[message.sentiment] += 1;
};

/** Socket.io **/
io.sockets.on('connection', function(socket) {
    console.log(' <<<<<< User connected');

    socket.on('disconnect', function() {
        console.log(' >>>>>>>> User disconnected');
    });
});

/** Web endpoints and Express setup **/
//app.param('channel', /^\w+$/);

app.use("/static", express.static(__dirname + '/static'));

app.get('/', function (req, res) {
    res.sendfile(__dirname + '/index.html');
});

app.get('/ticks/:channel', function (req, res) {
    // send ticks for a channel
    console.log('Getting ticks for ' + req.parans.channel);
});

app.get('/counts/:channel', function (req, res) {
    // get sentiment counts for a channel
    console.log('Getting counts for ' + req.parans.channel);
    res.send(JSON.stringify(channelCounts[req.params.channel]));
});


/** Redis **/
/**
var redisClient = redis.createClient();
redisClient.subscribe("tags");

redisClient.on('message', function (channel, message) {
    // Broadcast message to all clients
    io.sockets.emit('tick', makeTick(message));
    console.log('New tick from STORM: ' + channel);
});
**/

// Dummy tick sender
setInterval(function() {
        console.log("Sending tick");
        io.sockets.emit('tick', makeMessage('dummy'));
    }, 500);


