<?php

require_once("./init.php");
$db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));

$id = $_GET['id'];

$sql = sprintf("SELECT val FROM Tests WHERE id=%s", $id);

$results = $db->query($sql);

foreach ($results as $row) 
{
    echo $row['val'];
}


?>