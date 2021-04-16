// # REGISTRATOR client (jquery)

// This code is *very* verbose, because it tries to teach
// the reader about how the `registrator` works as well.

// > Structure loosely based on [underscore](http://underscorejs.org/docs/underscore.html)

(function () {

    var root = this,

        //  ## FFReg

        // First, config object (JSON):
        config = {
            "BaseURL": "https://reg.weimarnetz.de/",
            "network": "ffweimar"
        },

        // Everything is encapsulated into the `FFReg` object, which will be attached to `global` later.
        FFReg = {
            "VERSION": "0.0.1"
        };

    // Check for and set up JQuery
    if (!window.$) {
        console.log("FFReg: no JQuery!");
        return;
    } else {
        var JQuery = window.$;
    }


    // ## Functions
    //
    // ## Setup
    //
    // Usage: `FFReg.setup({ "BaseURL": "http://reg.weimarnetz.de/", "network": "testnet" });`
    FFReg.setup = function (userconfig) {
        if (userconfig) {
            Object.keys(userconfig).forEach(function (key) {
                config[key] = userconfig[key];
            });
            return "ok";
        } else {
            return config;
        }
    }


    //
    // ### Check
    //
    // A function to check a **number** against the *Registrator*.
    FFReg.check = function (number, callback) {

        // If all is well, get the **number**'s status from *Registrator* with `JQuery.ajax()`.
        var URL = config.BaseURL + config.network + "/knoten/" + number;

        jQuery.ajax(URL, {

            // When the request completes,
            complete: function complete(jqXHR, textStatus) {

                answer = {};
                // and "analyze" it:
                //
                // - If the was a `HTTP Error 404`, the number is *FREE*
                if (jqXHR.status == 404) {
                    answer.free = true;
                    answer.result = "FREE!";

                    // - If the was a `HTTP Status 200`, the number is *TAKEN*
                } else if (jqXHR.status == 200) {
                    // get the received data
                    var answer = {
                        "textStatus": textStatus,
                        "data": JSON.parse(jqXHR.responseText),
                    }
                    answer.free = false;
                    answer.result = "TAKEN! Number " + number + " registered" + (answer.data.result.mac ? (" to MAC " + answer.data.result.mac) : ("")) + "!";
                    // - Otherwise, it is an *error*
                } else {
                    answer.free = null;
                    answer.result = "WTF: Return code was" + jqXHR.status;
                }

                // We log the data and result to the console,
                console.log(JSON.stringify(answer));

                // and call back with the same answer.
                if (typeof callback === 'function') {
                    callback(answer);
                }
            }
        });

    };

    // ### Register
    //
    // A function to get a **fresh number** from the *Registrator* (aka registration). Needs a MAC and a secret(!), otherwise we won't know who the registration is forâ€¦
    FFReg.register = function (mac, secret, callback) {

        // Register MAC and Secret with *Registrator* using `JQuery.ajax()`.
        var URL = config.BaseURL + "POST/" + config.network + "/knoten";

        jQuery.ajax(URL, {

            // data (appended as query string by jquery)
            data: {"mac": mac, "pass": secret},

            // When the request completes,
            complete: function complete(jqXHR, textStatus) {

                // get the received data
                var answer = {
                    "textStatus": textStatus,
                    "data": JSON.parse(jqXHR.responseText),
                }
                answer.result = JSON.stringify(answer.data);

                // We log the data and result to the console,
                console.log(JSON.stringify(answer));

                // and call back with the same answer.
                if (typeof callback === 'function') {
                    callback(answer);
                }
            }
        });

    };

    // Lastly, we semi-safely attach **FFReg** to the `global` object.
    if (!root.FFReg) {
        root.FFReg = FFReg;
    }

// The above is run once on page load. That is all.
})();