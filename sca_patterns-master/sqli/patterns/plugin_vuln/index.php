<?php

require_once("./Router.php");
require_once("./Kohana.php");

require_once("./init.php");

// $kohana = new Kohana();

Router::find_uri();
Router::setup();


Kohana::instance();



// Event::add('system.routing', array('router', 'find_uri'));
// Event::add('system.routing', array('router', 'setup'));


// Event::add('system.execute', array('kohana', 'instance'));

// Event::run('system.routing');
// Event::run('system.execute');

?>