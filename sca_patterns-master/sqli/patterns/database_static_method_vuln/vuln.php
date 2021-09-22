<?php

require_once("./init.php");
require_once("./database.php");

Database::connect();

$id = $_GET['id'];

$sql = sprintf("SELECT val FROM Tests WHERE val=%s", $id);

$results = Database::getDatabase()->query($sql);

foreach ($results as $row) 
{
    echo $row['val'];
}


?>