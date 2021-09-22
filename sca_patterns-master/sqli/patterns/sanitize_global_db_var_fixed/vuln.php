<?php

require_once("./init.php");
require_once("./sanitize.php");
require_once("./sink.php");

global $db;

$id = Sanitize::escape($_GET['id']);

$results = $db->selectValue("Tests", "val", "id=$id");

foreach ($results as $row) 
{
    echo $row['val'];
}


?>