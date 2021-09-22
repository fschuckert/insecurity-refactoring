<?php

require_once("./init.php");
$db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));


putenv("REMOTE_ADDR=".$_GET['id']);

$sql = "SELECT val FROM Tests WHERE id=".getenv('REMOTE_ADDR');

$results = $db->query($sql);

foreach ($results as $row) 
{
    echo $row['val'];
}


?>