/** Imports **/
var express = require('express')
 ,   app = express() 
 , http = require('http')
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

var ES_URI = 'http://localhost:9200/ticks/'

var makeTick = function(inmsgStr) {
    var inmsg = JSON.parse(inmsgStr);
    console.log('>>>> sentiment ' + inmsg.sentiment);

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

/*
var cl2 = redis.createClient();
var negid = 1;
redisClient.on('subscribe', function(channel, count) {
    console.log('SUBSCRIBED ' + channel);
    if ('GoogleAlerts' === channel) { 
        console.log('>>> Setting timer')
        setInterval(function () {
            negid += 1;
            cl2.publish('GoogleAlerts', JSON.stringify({
                channel: 'GoogleAlerts',
                sentiment: NEG ,
                id: '' + negid,
                title: 'Glasdoor Employee Review',
                link: 'http://www.glassdoor.com/Reviews/Employee-Review-InMobi-RVW2045626.htm'
            }));
            console.log('###### Sent negative tick');
        }, 5000);
    }
});
*/

var saveInES = function(message) {
    var options = {
      hostname: 'localhost',
      port: 9200,
      path: '/ticks/' + message.channel + '/' + message.id,
      method: 'PUT'
    };

    var req = http.request(options, function(res) {
        console.log('STATUS: ' + res.statusCode);
        console.log('HEADERS: ' + JSON.stringify(res.headers));
    });

    req.write(JSON.stringify(message) + '\n');
    req.end();
    console.log('$#$#$#$#$#$#$#$#$ SAVED IN ES');
};
    
redisClient.on('message', function (channel, message) {
    // Broadcast message to all clients
    var tick = makeTick(message);
    saveInES(tick);
    io.sockets.emit('tick', tick);
});



