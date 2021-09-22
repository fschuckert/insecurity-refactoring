<?php

require_once("./init.php");
require_once("./countries_api_object.php");

$api = new Countries_Api_Object();

$api->init_request();


$results = $api->triggerVuln();

foreach ($results as $row) 
{
    echo $row['val'];
}


?>