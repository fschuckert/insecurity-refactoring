<?php

require_once("./init.php");

global $addslashes;

$id = $addslashes($_GET['id']);

$sql = "SELECT val FROM Tests WHERE id=$id";
$db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));
$results = $db->query($sql);

foreach ($results as $row) 
{
    echo $row['val'];
}


?>