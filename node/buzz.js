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
var ChannelCounts = new Object();

var POS = "pos";
var NEG = "neg";
var NEUT = "neutral";

var labels = [POS, NEG, NEUT];
var channels = ["GoogleAlerts"];

var ES_HOST = 'localhost';
var ES_PORT = 9200;
var ES_INDEX = 'ticks';

/** Socket.io **/
io.sockets.on('connection', function(socket) {
    console.log(' <<<<<< User connected');
    // Send latest counts
    for (var i = 0; i < channels.length; i++) {
        var channel = channels[i];
        if (ChannelCounts[channel]) {
            socket.emit('counts', ChannelCounts[channel]);
        }
    }
    socket.on('disconnect', function() {
        console.log(' >>>>>>>> User disconnected');
    });
    
    socket.on('search', function(data) {
        console.log('NEW SEARCH REQUEST ' + JSON.stringify(data));
        var options = {
          hostname: ES_HOST,
          port: ES_PORT,
          path: ES_INDEX + '/_search?q=sentiment:' + data.sentiment + '&size=1000',
          method: 'GET'
        };
        
        console.log('Sending search req to ' + options.path);
        
        var req = http.request(options, function(res) {
            // Send the query result back to the client
            var chunks = new Array();
            res.on('data', function(chunk) {
                chunks.push(chunk);
            });
            
            res.on('end', function () {
                // Send the response to browser
                socket.emit('search_results', {
                    'channel': data.channel,
                    'label': data.sentiment,
                    'result_body': chunks.join("")
                });
            });
        });
        req.end();
        console.log('search request sent to ES:' + data.channel + ', ' + data.label);
    });
});

/** Web endpoints and Express setup **/
app.use(express.logger());
app.use("/static", express.static(__dirname + '/static'));

app.get('/', function (req, res) {
    res.sendfile(__dirname + '/index.html');
});

/** Redis **/
var redisClient = redis.createClient();
for (var i = 0; i < channels.length; i++) {
    redisClient.subscribe(channels[i]);
}

var saveInES = function(message) {
    message = JSON.parse(message);
    var options = {
      hostname: ES_HOST,
      port: ES_PORT,
      path: ES_INDEX + '/' + message.channel + '/' + message.id,
      method: 'PUT'
    };

    var req = http.request(options, function(res) {
    });

    req.write(JSON.stringify(message) + '\n');
    req.end();
    console.log('written to ES:' + message.id);
};

var updateCounts = function(inmsgStr) {
    var inmsg = JSON.parse(inmsgStr);
    var channel = inmsg.channel;
    var label = inmsg.sentiment;

    // Update counts for the channel
    var counts = ChannelCounts[channel];        
    if (!counts) {
      counts = new Object();
      counts['channel'] = channel;
      counts[POS] = 0;
      counts[NEG] = 0;
      counts[NEUT] = 0;
      ChannelCounts[channel] = counts;
    }
    var precount = counts[label];
    if (precount) {
        counts[label] = precount + 1;
    } else {
        counts[label] = 1;
    }
    console.log('Sending count:' + JSON.stringify(ChannelCounts[channel]));
    io.sockets.emit('counts', ChannelCounts[channel]);
    io.sockets.emit('tick', inmsg);
    return {'channel': channel, 'link': inmsg.link, 'sentiment': sentiment, 'title': inmsg.title};
};
  
redisClient.on('message', function (channel, message) {
    console.log('New event on ' + channel); 
    saveInES(message);
    updateCounts(message);
    console.log('Sent message and counts to clients');
});
