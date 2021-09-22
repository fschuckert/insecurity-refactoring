<?php

require_once("./init.php");

$value = $_GET['id'];
$sql = "SELECT val FROM Tests WHERE id=$value";
$results = $GLOBALS['dbi']->query($sql);

foreach ($results as $row) 
{
    echo $row['val'];
}


?>