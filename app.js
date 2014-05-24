var flatiron  = require('flatiron'),
    fs        = require('fs'),
    path      = require('path'),
    async     = require('async'),
    app       = flatiron.app,
    util      = require('util'),
    _         = require('underscore'),
    mu        = require('mu2'),
    moment    = require('moment');

// app: config
app.config.argv();
app.config.file({ file: path.join(__dirname, 'config', 'config.json') });
// app: networks config
app.config.file('networks', { file: path.join(__dirname, 'config', 'networks.json') });

// app: `http`, plugins
app.use(flatiron.plugins.http);
// "use" the `static` plugin, configure it to serve all available files in `./static` under http root (`/`).
// example: `./client/css/style.css` -> `http://app.url/css/style.css`
app.use(flatiron.plugins.static, {
  dir: path.join(__dirname, 'client'),
  path: "static"
});   


// app: internal modules
app.use(require("./lib/errors"));
app.use(require("./lib/register"));
app.use(require("./lib/model"), app.config.get('networks'));

// 
// # EXTERNAL API
// 
// Functions calling the internal API and their HTTP routes 
// (would have to be decoupled when supporting more interfaces).
// 
// Since routes except /home and /time need a `network` parameter, it is not further mentioned.


// # ROUTER
// 
// ## HOMEPAGE
app.router.get('/', function () {
  var http = this,
      network_id = app.config.get('homepage:network'),
      data = {};

  // TODO: list networks
  
  app.register.getAll(network_id, null, function(err, res) {
 
    data.network = network_id;
    data.name = app.config.get('name');
    
    if (res) {
      data.knoten = res.result.knoten;      
      data.knoten.sort(function(a,b){
        return b.last_seen - a.last_seen;
      });
      if (data.knoten && Array.isArray(data.knoten)) {
        data.knoten.forEach(function(parameter){
          parameter.last_seen = moment(parameter.last_seen).format('L LT');
          parameter.created_at = moment(parameter.created_at).format('L LT');
        });
      }
    } 
       
    app.renderWebsite(http, data);
    
 
  });  
    
});

app.renderWebsite = function (http, data) {
   
  var template  = './client/index.mustache';
  
  var stream = mu.compileAndRender(template, data);    
  util.pump(stream, http.res);

};

// ## LIST: GET /knoten
var listAll = function (network, property) {
  
  var http = this,
      givenprops = [],
      properties = [];
  
  // ### `/$NETWORK/list/numbers`
  // 
  // if a property was given as **url resource**
  if (!property) {
    // add it to the list of given properties.
    givenprops.push(property);    
  }
  
  // ### support `/$NETWORK/list?prop=numbers&prop=mac`
  // 
  // for each of those *query parameters**, 
  ["properties", "property", "props", "prop"].forEach(function(parameter) {
    // also add it to the list if we got a **value** for it
    givenprops = givenprops.concat(http.req.query[parameter]);
  });
  
  // ### support `/$NETWORK/list?number=yes&mac=1`
  // 
  // for each of those *query parameters**, 
  ["number", "mac", "knoten"].forEach(function(prop) {
    
    // FIXME: wtf? not needed?
    // if (givenprops.indexOf(parameter) !== -1 || givenprops.indexOf(parameter + 's') !== -1) {
      // properties.push(prop);
    // }
    
    // if they are in the query (or their plural), 
    if (http.req.query[prop] || http.req.query[prop + 's']) {
      
      // and if the value is neither emty nor the string 'false', 
      if (http.req.query[prop] !== "false" || http.req.query[prop + 's'] !== "false") {
        
        // add the corresponding property to the list.
        properties.push(prop);        
      }
    }
  });
  
  // ### call register
  // 
  // call internal API, with the list properties we want (if we got some, otherwise it's empty)
  // 
  app.register.getAll(network, properties, function(err, res) {
    
    // and send the result of it as pretty JSON (or an error if there was one).
    http.res.end(JSON.stringify((err || res), null, 2));
  });
  
};

// ### set up the routes calling `listAll()`
// 
// - `GET /$NETWORK/knoten`
app.router.get('/:network/knoten', listAll);
// - `GET /GET/$NETWORK/knoten`
app.router.get('/GET/:network/knoten', listAll);
// - `GET /$NETWORK/list?$PROPERTY=1`
app.router.get('/:network/list', listAll);
// - `GET /GET/$NETWORK/list?$PROPERTY=1`
app.router.get('/GET/:network/list', listAll);
// - `GET /$NETWORK/lists?$PROPERTY=1`
app.router.get('/:network/lists', listAll);
// - `GET /GET/$NETWORK/lists?$PROPERTY=1`
app.router.get('/GET/:network/lists', listAll);
// - `GET /$NETWORK/list/$PROPERTY`
app.router.get('/:network/list/:property', listAll);
// - `GET /GET/$NETWORK/list/$PROPERTY`
app.router.get('/GET/:network/list/:property', listAll);
// - `GET /$NETWORK/lists/$PROPERTY`
app.router.get('/:network/lists/:property', listAll);
// - `GET /GET/$NETWORK/lists/$PROPERTY`
app.router.get('/GET/:network/lists/:property', listAll);

// 
// ## Get a `Knoten`
// 
var getKnoten = function (network, number) {
  var http = this;
  
  // call the internal API, get a knoten by number
  app.register.get(network, number, function(err, res) {
    
    // send the error or result as pretty JSON.
    http.res.end(JSON.stringify((err || res), null, 2));
  });
};

// ### set up the routes calling `getKnoten()`
// 
// - `GET /$NETWORK/knoten/$NUMBER`
app.router.get('/:network/knoten/:number', getKnoten);
// - `GET /GET/$NETWORK/knoten/$NUMBER`
app.router.get('/GET/:network/knoten/:number', getKnoten);

// 
// ## AUTOREGISTER: POST a `Knoten`
// 
// - needs mac and pass
// - return a result with a `Knoten`
// - new number is the smallest available, where 
//   available means no db entry for this number
// 
var postKnoten = function (network) {
  var http = this, 
  
  // read the request query parameters
  mac = http.req.query.mac || null,
  pass = http.req.query.pass || null;
  
  // TODO: read JSON from request body
  
  // call the internal API, 
  app.register.create(network, mac, pass, function(err, res) {
    
    // send the error or result as pretty JSON.
    http.res.end(JSON.stringify((err || res), null, 2));
  });
  
};

// ### set up the routes calling `postKnoten()`
// 
// - `POST /$NETWORK/knoten/$NUMBER?mac=$MAC&pass=$PASS`
app.router.post('/:network/knoten', postKnoten);
// - `GET /POST/$NETWORK/knoten/$NUMBER?mac=$MAC&pass=$PASS`
app.router.get('/POST/:network/knoten', postKnoten);

// 
// ## HEARTBEAT: PUT a `Knoten`
// 
// - needs mac and pass
// - allow 'costum registration'
// - allows to capture a 'reserved' number
// - logic: if given number has no pass, set it to given pass
// 
var putKnoten = function (network, number) {
  var http = this,
  
  // read the request query parameters
  mac = http.req.query.mac || null,
  pass = http.req.query.pass || null;
  number = number || null;
  
  // TODO: read JSON from request body
  
  // call the internal API, 
  app.register.update(network, number, mac, pass, function(err, res) {
    
    // send the error or result as pretty JSON.
    http.res.end(JSON.stringify((err || res), null, 2));
  });
  
};

// ### set up the routes calling `putKnoten()`
// 
// - `PUT /$NETWORK/knoten/$NUMBER?mac=$MAC&pass=$PASS`
app.router.put('/:network/knoten/:number', putKnoten);
// - `GET /PUT/$NETWORK/knoten/$NUMBER?mac=$MAC&pass=$PASS`
app.router.get('/PUT/:network/knoten/:number', putKnoten);

// 
// ## TIMESTAMP
// 
// - `GET /time`
var getTime = function () {
  
  // send the current timestamp as pretty JSON.
  this.res.end(JSON.stringify({ 'now': new Date().getTime() }, null, 2));
};

app.router.get('/time', getTime);
app.router.get('/GET/time', getTime);


// 
// # STARTUP
// 
// - start app and http server on configured port
// 
// # Static files
app.router.get("/static/:file", function (file) {
  var http = this,
      target;
  
  // serve module js from node_modules
  if (file === "plates.js") {
    target = path.join(__dirname, 'node_modules', 'plates', 'lib', 'plates.js');
  } else {
    target = path.join(__dirname, 'public', file);
  }
  
  // read and send the target file
  fs.readFile(target, function (err, data) {
    if (err) {
      http.res.writeHead(404);
      return http.res.end('Error 404');
    }
    http.res.writeHead(200);
    http.res.end(data);
  });
});

// start http server on configured port
app.start(app.config.get('port'), function () {
  app.log.info("Server started!", 
    { "port": app.config.get('port') }
  );
});

// Socket.io
// 
var io = require('socket.io').listen(app.server);
io.set('log level', 1); // reduce logging

io.sockets.on('connection', function(socket) {
  
  socket.emit('console', {
    "event": 'HELLO',
    "message": 'socket.io connected!',
    "knoten": null,
    "timestamp": (new Date().toJSON())
  });

  app.resources.Knoten.on('update', function(doc) {
    socket.emit('console', {
      "event": "HEARTBEAT",
      "message": null,
      "id": doc.id,
      "network": doc.network_id,
      "timestamp": (new Date().toJSON())
    });
  });  

  app.resources.Knoten.on('save', function(doc) {
    
    socket.emit('console', {
      "event": "REGISTER",
      "message": null,
      "id": doc.id,
      "network": doc.network_id,
      "timestamp": (new Date().toJSON())
    });
  });  

});


//
// ## On-Demand
//
// - log to file
app.logfile = app.config.get('logfile');
if (app.logfile && typeof app.logfile !== "boolean") {

  app.log = require('winston');
  app.log.cli();
  
  app.log.add(app.log.transports.File, {
    filename: app.config.get('logfile'),
    level: 'debug',
    colorize: false,
    timestampe: true,
    json: false
  });
  
}


// 
// ## Check Database, Check and Setup Networks
//
(function bootstrapDB () {

  // - make tmp arrays
  var networks = app.config.get('networks'),
      configuredNetworks = [],
      databasedNetworks = [];
      
  var reserveKnoten = function (network, callback) {
    app.log.info('reserving knoten: ', network.id);
    
    var list = (_.where(networks, {"name": network.id}))[0].reserved;
    
    if (list) {
      
      // console.log("list: ", list);
      
      // reserve each knoten in **list**
      async.eachSeries(list, function (nr, callback) {
        
        // get the network
        Network.get(network.id, function(err, fNetwork) {

          // - we get the result back from the db
          if (err) {
            callback(err);        
          }
      
          else {
                  
            // console.log(nr);
      
            var knoten = { "id": nr }
              
            network.createKnoten(knoten, function (err, result) {
      
              // we get the result back from the db
  
              if (err) {
                
                // an error while creating means server error or conflict…

                if (err.status === 409) {
                  // we are just warning if a number already is in db
                  // app.log.silly("Failed to reserve exisiting knoten #" + nr + "!");
                  return callback(null);  
                }
                
                // everything else is a real error!
                app.log.error("Error reserving knoten #" + nr + "!", err);
                callback(err);
                
              } else {
                
                // success!
                app.log.debug("Reserved: ", knoten);
                callback(null);
                
              }
                
            });
        
          }
      
        });
        
      }, function done(err) {
        callback(null || err);
      });
            
    } else {
      // no **list**? just callback
      callback();
    }
    
  };
  
  // bootstrap() sets up network in db if it does not exist
  var bootstrap = function (network, callback) {
        
    // add network to the tmp array
    configuredNetworks.push(network.name);
  
    // check if it already exists in db.
    app.resources.Network.get(network.name, function(err, fNetwork) {
    
      // If we got an error db error, we exit!
      if (err && err.status > 500) {
        throw new Error("DB error! Cannot run! " + err);
      }
    
      // If the network is not found, we create it.
      else if (err && err.status === 404) {
      
        app.resources.Network.create({
          id: network.name,
          public: network.public,
          url: network.url,
          minimum: network.minimum,
          maximum: network.maximum,
          lease_days: network.lease_days,
          lease_seconds: network.lease_seconds
        }, function(err, cNetwork){
  
          if (err) {
          
            var msg = "could not create network" + network.name;
            
            app.log.error(msg, err);
            callback(new Error(msg));
            
          } else {
          
            app.log.debug("created network!", cNetwork);
            
            // reservation in the created network
            reserveKnoten(cNetwork, callback);
          }
        
        });
      
      }
      
      // if we found the network and the id is correct
      else if (!err && fNetwork.id === network.name) {
        
        // just reserve in the found network
        reserveKnoten(fNetwork, callback);

      } else {
        // something is very wrong
        callback(new Error("Network" + fNetwork + " is not " +  network.name + "!"))
      }
    
    });
  
  };  
  
  // Run async setup for each of the networks 
  // and run the self check after all of them completed.
  async.each(networks, bootstrap, function(err) {
        
    if (err) {
      app.log.error("DB Bootstrap failed!", err);
    }
    
    Network.all(function(err, networksInDB) {
    
      // and for each network
      if (networksInDB) {        
        networksInDB.forEach(function(n) {
      
          // add it to the tmp list.
          databasedNetworks.push(n.id);
          
        });
      }

      // debug: log configured and actual networks
      app.log.debug(" networks configured:", configuredNetworks.sort().toString());
      app.log.debug("networks in database:", databasedNetworks.sort().toString());
    
      // exit if they are not the same!
      if ( configuredNetworks.sort().toString() !== databasedNetworks.sort().toString() ) {
        app.log.error("Self-check: Networks are broken! :( \n Maybe check the database?");
      } else {
        app.log.info("self-check:", {'networks_ok': true})
      }
  
    });
    
  });
  
})();
