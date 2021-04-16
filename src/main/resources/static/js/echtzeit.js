// Enable Bootstrap tooltips
$(function () {
    $('#console').tooltip({
        selector: "span[data-toggle=tooltip]"
    });

    // overide links
    $('body a[data-overide-link=true]').on('click', modalLink);


    // $("body").tooltip();
    // $('body').on('.tooltip.data-api');
    console.log('tooltips!');
});

function modalLink(event) {
    event.preventDefault();
    console.log(event);
}

function renderLine(data, callback) {

    if (data.id) {
        data.number = data.id.replace(/knoten\/network\/\w+\/(\d+)/, '$1');
        data.network = data.network || "error";
        data.knoten = '/GET/' + data.network + '/knoten/' + data.number;
    }

    var logentry = '<p class="logentry"><kbd class="timestamp label label-info" data-toggle="tooltip" title=""></kbd><kbd> <b class="event"></b>: </kbd><kbd class="message"></kbd><kbd><a class="knoten" href="#"></a></kbd></p>',
        map = new Plates.Map(),
        output;

    map.class('event').to('event');

    map.class('message').to('message');
    map.class('timestamp').to('timestamp');
    map.class('knoten').to('knoten');
    map.where('class').is('knoten')
        .use('knoten').as('href');

    callback(Plates.bind(logentry, data, map));

};

var socket = io.connect(document.location.href);

// when we receive a 'console' event,
socket.on('console', function (data) {

    console.log(data);

    // we run a jquery fn
    $(function () {

        // render the data to HTML
        renderLine(data, function (line) {

            // log it and append it into the #console
            console.log(line);
            $('#console').prepend(line);

        });

    });

});