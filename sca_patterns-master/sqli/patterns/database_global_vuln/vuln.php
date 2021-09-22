<?php

require_once("./init.php");

global $db;

$id = $_GET['id'];

$sql = "SELECT val FROM Tests WHERE id=$id";

$results = $db->query($sql);

foreach ($results as $row) 
{
    echo $row['val'];
}


?>