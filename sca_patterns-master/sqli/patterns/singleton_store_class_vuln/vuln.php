<?php

require_once('init.php');
require_once('source.php');

$id= url_param('id');

$db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));

$sql = "SELECT val FROM Tests WHERE id=$id";

$results = $db->query($sql);

foreach ($results as $row) 
{
    echo $row['val'];
}

// print_r($db->errorInfo());

?>