<?php
require_once("./singleton.php");
require_once("./init.php");
$db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));

$id = $_GET['id'];


$modelClass = Singleton::strong_create('ModelClass', $id);

$sql = $modelClass->getQueryStr();

$results = $db->query($sql);

foreach ($results as $row) 
{
    echo $row['val'];
}


?>